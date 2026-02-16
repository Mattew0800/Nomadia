package nomadia.Service;


import nomadia.DTO.Activity.ActivityIdRequestDTO;
import nomadia.DTO.Expense.*;
import nomadia.DTO.Trip.TripIdRequestDTO;
import nomadia.DTO.UserBalance.DebtDTO;
import nomadia.DTO.UserBalance.UserBalanceDTO;
import nomadia.Model.*;
import nomadia.Repository.ActivityRepository;
import nomadia.Repository.ExpenseRepository;
import nomadia.Repository.ParticipantRepository;
import nomadia.Repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ActivityRepository activityRepository;
    private final TripService tripService;
    private final UserService userService;
    private final ParticipantRepository participantRepository;
    private final PaymentRepository paymentRepository;

    public ExpenseService(ExpenseRepository expenseRepository, ActivityRepository activityRepository, TripService tripService, UserService userService, ParticipantRepository participantRepository, PaymentRepository paymentRepository) {
        this.expenseRepository = expenseRepository;
        this.activityRepository = activityRepository;
        this.tripService = tripService;
        this.userService = userService;
        this.participantRepository = participantRepository;
        this.paymentRepository = paymentRepository;
    }

    private static class BalanceNode {
        Long userId;
        String email;
        BigDecimal amount;

        BalanceNode(Long userId, String email, BigDecimal amount) {
            this.userId = userId;
            this.email = email;
            this.amount = amount;
        }
    }

    private void rebuildParticipantsFromPayersAndSplits(Expense expense,Trip trip,BigDecimal totalAmount,List<PayerDTO> payers,boolean customSplit,List<SplitDTO> splits,boolean clearBefore) {

        if (payers == null || payers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Debe haber al menos un usuario que pague");
        }

        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"El monto total debe ser mayor a cero");
        }
        if (clearBefore) {
            expense.getParticipants().clear();
        } else if (expense.getParticipants() == null) {
            expense.setParticipants(new ArrayList<>());
        }

        Map<Long, BigDecimal> paidMap = new HashMap<>();
        Map<Long, BigDecimal> owedMap = new HashMap<>();
        Set<Long> involvedUsers = new HashSet<>();

        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalOwed = BigDecimal.ZERO;

        for (PayerDTO payer : payers) {
            User user = userService.findById(payer.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

            if (!trip.getUsers().contains(user)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario no pertenece al viaje");
            }

            if (payer.getAmountPaid() == null ||
                    payer.getAmountPaid().compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Monto pagado inválido");
            }

            if (paidMap.putIfAbsent(user.getId(), payer.getAmountPaid()) != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pagador duplicado");
            }
            involvedUsers.add(user.getId());
            totalPaid = totalPaid.add(payer.getAmountPaid());
        }
        if (paidMap.values().stream().allMatch(v -> v.compareTo(BigDecimal.ZERO) == 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Al menos un usuario debe pagar un monto mayor a cero");
        }
        if (totalPaid.compareTo(totalAmount) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La suma de los pagos no coincide con el total");
        }
        if (splits != null && !splits.isEmpty()) {
            for (SplitDTO split : splits) {
                User user = userService.findById(split.getUserId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Usuario no encontrado"));
                if (!trip.getUsers().contains(user)) {
                    throw new ResponseStatusException(
                            HttpStatus.FORBIDDEN, "Participante fuera del viaje");
                }
                if (split.getAmountOwed() == null ||
                        split.getAmountOwed().compareTo(BigDecimal.ZERO) < 0) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Monto del split inválido");
                }
                if (owedMap.putIfAbsent(user.getId(), split.getAmountOwed()) != null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Split duplicado para el mismo usuario");
                }
                involvedUsers.add(user.getId());
                totalOwed = totalOwed.add(split.getAmountOwed());
            }
            if (totalOwed.compareTo(totalAmount) != 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La suma del split no coincide con el total");
            }
        } else if (customSplit) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Split personalizado requerido");
        } else {
            BigDecimal base = totalAmount.divide(BigDecimal.valueOf(involvedUsers.size()),2,RoundingMode.DOWN);
            BigDecimal remainder = totalAmount.subtract(base.multiply(BigDecimal.valueOf(involvedUsers.size())));
            for (Long userId : involvedUsers){
                BigDecimal share = base;
                if (remainder.compareTo(BigDecimal.ZERO) > 0) {
                    share = share.add(new BigDecimal("0.01"));
                    remainder = remainder.subtract(new BigDecimal("0.01"));
                }
                owedMap.put(userId, share);
            }
        }
        for (Long userId : involvedUsers) {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
            Participant participant = new Participant();
            participant.setExpense(expense);
            participant.setUser(user);
            participant.setAmountPaid(paidMap.getOrDefault(userId, BigDecimal.ZERO));
            participant.setAmountOwned(owedMap.getOrDefault(userId, BigDecimal.ZERO));
            expense.getParticipants().add(participant);
        }
    }

    private Trip resolveTripAndValidateMember(CreateExpenseDTO dto, Long userId) {
        Trip trip;
        if (dto.getActivityId() != null) {
            Activity activity = activityRepository.findById(dto.getActivityId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Actividad no encontrada"));
            trip = activity.getTrip();
            if (trip.getUsers().stream().noneMatch(u -> u.getId().equals(userId))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No pertenecés al viaje");
            }
        } else {
            trip = tripService.getTripAndValidateMember(dto.getTripId(), userId);
        }
        return trip;
    }

    @Transactional
    public ExpenseResponseDTO createExpense(CreateExpenseDTO dto, Long userId) {
        Trip trip = resolveTripAndValidateMember(dto, userId);
        Expense expense = new Expense();
        if (dto.isCustomSplit() && (dto.getSplits() == null || dto.getSplits().isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe especificar el split personalizado");
        }
        expense.setName(dto.getName());
        expense.setNote(dto.getNote());
        expense.setTotalAmount(dto.getTotalAmount());
        expense.setTrip(trip);
        if (dto.getActivityId() != null) {
            Activity activity = activityRepository.findById(dto.getActivityId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Actividad no encontrada"));
            if (!activity.getTrip().getId().equals(trip.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La actividad no pertenece al viaje");
            }
            expense.setActivity(activity);
        }
        rebuildParticipantsFromPayersAndSplits(expense,trip,dto.getTotalAmount(),dto.getPayers(),dto.isCustomSplit(),dto.getSplits(),false);
        return ExpenseResponseDTO.fromEntity(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponseDTO updateExpense(ExpenseUpdateDTO dto, Long userId) {
        Expense expense = expenseRepository.findById(dto.getExpenseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gasto no encontrado"));
        Trip trip = expense.getTrip();
        if (trip.getUsers().stream().noneMatch(u -> u.getId().equals(userId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No pertenecés al viaje");
        }
        expense.setName(dto.getName());
        expense.setNote(dto.getNote());
        expense.setTotalAmount(dto.getTotalAmount());
        rebuildParticipantsFromPayersAndSplits(expense,trip,dto.getTotalAmount(),dto.getPayers(),dto.isCustomSplit(),dto.getSplits(),true);
        return ExpenseResponseDTO.fromEntity(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalTripCost(TripIdRequestDTO dto, Long userId) {
        tripService.getTripAndValidateMember(dto.getTripId(), userId);
        return activityRepository.getTotalActivityCostByTrip(dto.getTripId())
                .add(expenseRepository.getTotalByTrip(dto.getTripId()));
    }

    @Transactional
    public void deleteExpense(Long expenseId, Long userId) {
        Expense expense = expenseRepository.findByIdWithTrip(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gasto no encontrado"));
        tripService.getTripAndValidateMember(expense.getTrip().getId(), userId);
        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getExpensesByTrip(TripIdRequestDTO dto, Long userId) {
        Trip trip = tripService.getTripAndValidateMember(dto.getTripId(), userId);
        return expenseRepository.findByTripId(trip.getId()).stream()
                .map(ExpenseResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getExpensesByActivity(ActivityIdRequestDTO dto, Long userId) {
        Activity activity = activityRepository.findById(dto.getActivityId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Actividad no encontrada"));
        Trip trip = activity.getTrip();

        if (trip.getUsers().stream().noneMatch(u -> u.getId().equals(userId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No pertenecés al viaje");
        }
        return expenseRepository.findByActivityId(activity.getId()).stream()
                .map(ExpenseResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponseDTO getExpense(ExpenseIdRequestDTO dto, Long userId) {
        Expense expense = expenseRepository.findByIdWithTrip(dto.getExpenseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gasto no encontrado"));
        Trip trip = expense.getTrip();
        if (trip.getUsers().stream().noneMatch(u -> u.getId().equals(userId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No pertenecés al viaje");
        }
        return ExpenseResponseDTO.fromEntity(expense);
    }

    @Transactional(readOnly = true)
    public List<UserBalanceDTO> getTripBalance(TripIdRequestDTO dto, Long userId) {
        Trip trip = tripService.getTripAndValidateMember(dto.getTripId(), userId);
        Map<Long, UserBalanceDTO> balances = new HashMap<>();
        for (User user : trip.getUsers()) {
            balances.put(user.getId(),new UserBalanceDTO(user.getId(),user.getEmail(),BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO));
        }
        List<Participant> participants = participantRepository.findByTripId(trip.getId());
        for (Participant p : participants) {
            UserBalanceDTO b = balances.get(p.getUser().getId());
            b.setPaid(b.getPaid().add(p.getAmountPaid()));
            b.setOwed(b.getOwed().add(p.getAmountOwned()));
        }
        balances.values().forEach(b ->
                b.setBalance(b.getPaid().subtract(b.getOwed()))
        );
        applyPayments(trip.getId(),balances);
        return new ArrayList<>(balances.values());
    }

    public void applyPayments(Long tripId, Map<Long, UserBalanceDTO> balances) {
        List<Payment> payments = paymentRepository.findByTripId(tripId);
        for (Payment p : payments) {
            Long fromId = p.getFromUser().getId();
            Long toId = p.getToUser().getId();
            BigDecimal amount = p.getAmount();
            UserBalanceDTO fromBalance = balances.get(fromId);
            UserBalanceDTO toBalance = balances.get(toId);
            if (fromBalance != null) {
                fromBalance.setBalance(fromBalance.getBalance().add(amount));
            }
            if (toBalance != null) {
                toBalance.setBalance(toBalance.getBalance().subtract(amount));
            }
        }
    }
    public List<DebtDTO> calculateDebts(List<UserBalanceDTO> balances) {
        List<BalanceNode> creditors = balances.stream()
                .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) > 0)
                .map(b -> new BalanceNode(b.getUserId(),b.getEmail(),b.getBalance())).toList();
        List<BalanceNode> debtors = balances.stream()
                .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) < 0)
                .map(b -> new BalanceNode(
                        b.getUserId(),
                        b.getEmail(),
                        b.getBalance().abs()))
                .toList();
        List<DebtDTO> debts = new ArrayList<>();
        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            BalanceNode debtor = debtors.get(i);
            BalanceNode creditor = creditors.get(j);
            BigDecimal amount = debtor.amount.min(creditor.amount);
            debts.add(new DebtDTO(debtor.userId,debtor.email,creditor.userId,creditor.email,amount));
            debtor.amount = debtor.amount.subtract(amount);
            creditor.amount = creditor.amount.subtract(amount);
            if (debtor.amount.compareTo(BigDecimal.ZERO) == 0) i++;
            if (creditor.amount.compareTo(BigDecimal.ZERO) == 0) j++;
        }
        return debts;
    }
}