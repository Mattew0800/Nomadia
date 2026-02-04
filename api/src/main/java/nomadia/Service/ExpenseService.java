package nomadia.Service;


import nomadia.DTO.Expense.*;
import nomadia.DTO.Trip.TripIdRequestDTO;
import nomadia.Model.*;
import nomadia.Repository.ActivityRepository;
import nomadia.Repository.ExpenseRepository;
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

    public ExpenseService(ExpenseRepository expenseRepository, ActivityRepository activityRepository,
                          TripService tripService, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.activityRepository = activityRepository;
        this.tripService = tripService;
        this.userService = userService;
    }

    private void rebuildParticipantsFromPayersAndSplits(Expense expense,Trip trip,BigDecimal totalAmount,List<PayerDTO> payers,boolean customSplit,List<SplitDTO> splits) {

        if (payers == null || payers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe haber al menos un usuario que pague");
        }

        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El monto total debe ser mayor a cero");
        }

        expense.getParticipants().clear();

        Map<Long, BigDecimal> paidMap = new HashMap<>();
        Map<Long, BigDecimal> owedMap = new HashMap<>();
        Set<Long> involvedUsers = new HashSet<>();

        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalOwed = BigDecimal.ZERO;

        for (PayerDTO payer : payers) {
            User user = userService.findById(payer.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Usuario no encontrado"));

            if (!trip.getUsers().contains(user)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Pagador fuera del viaje");
            }

            if (payer.getAmountPaid() == null ||
                    payer.getAmountPaid().compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Monto pagado inválido");
            }

            if (paidMap.putIfAbsent(user.getId(), payer.getAmountPaid()) != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Pagador duplicado");
            }

            involvedUsers.add(user.getId());
            totalPaid = totalPaid.add(payer.getAmountPaid());
        }

        if (paidMap.values().stream().allMatch(v -> v.compareTo(BigDecimal.ZERO) == 0)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Al menos un usuario debe pagar un monto mayor a cero");
        }

        if (totalPaid.compareTo(totalAmount) != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La suma de los pagos no coincide con el total");
        }

        if (customSplit) {
            if (splits == null || splits.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Split personalizado requerido");
            }

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

            for (Long payerId : paidMap.keySet()) {
                if (!owedMap.containsKey(payerId)) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Todo pagador debe estar incluido en el split");
                }
            }

        } else {
            BigDecimal base = totalAmount.divide(
                    BigDecimal.valueOf(involvedUsers.size()),
                    2,
                    RoundingMode.DOWN);

            BigDecimal remainder = totalAmount.subtract(
                    base.multiply(BigDecimal.valueOf(involvedUsers.size())));

            for (Long userId : involvedUsers) {
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
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Usuario no encontrado"));

            Participant participant = new Participant();
            participant.setExpense(expense);
            participant.setUser(user);
            participant.setAmountPaid(paidMap.getOrDefault(userId, BigDecimal.ZERO));
            participant.setAmountOwned(owedMap.getOrDefault(userId, BigDecimal.ZERO));

            expense.getParticipants().add(participant);
        }
    }



    @Transactional
    public ExpenseResponseDTO updateExpense(UpdateExpenseDTO dto, Long userId) {

        Expense expense = expenseRepository.findById(dto.getExpenseId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Gasto no encontrado"));

        Trip trip = expense.getTrip();
        if (trip.getUsers().stream().noneMatch(u -> u.getId().equals(userId))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No pertenecés al viaje");
        }

        expense.setName(dto.getName());
        expense.setNote(dto.getNote());
        expense.setTotalAmount(dto.getTotalAmount());

        rebuildParticipantsFromPayersAndSplits(
                expense,
                trip,
                dto.getTotalAmount(),
                dto.getPayers(),
                dto.isCustomSplit(),
                dto.getSplits()
        );

        expenseRepository.save(expense);

        return ExpenseResponseDTO.fromEntity(expense);
    }


    @Transactional
    public ExpenseResponseDTO createExpense(CreateExpenseDTO dto, Long userId) {
        Trip trip = tripService.getTripAndValidateMember(dto.getTripId(), userId);
        Expense expense = new Expense();
        expense.setName(dto.getName());
        expense.setNote(dto.getNote());
        expense.setTotalAmount(dto.getTotalAmount());
        expense.setTrip(trip);
        rebuildParticipantsFromPayersAndSplits(expense,trip,dto.getTotalAmount(),dto.getPayers(),dto.isCustomSplit(),dto.getSplits());
        return ExpenseResponseDTO.fromEntity(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalTripCost(TripIdRequestDTO dto, Long userId) {
        tripService.getTripAndValidateMember(dto.getTripId(), userId);
        return activityRepository.getTotalActivityCostByTrip(dto.getTripId())
                .add(expenseRepository.getTotalExpensesByTrip(dto.getTripId()));
    }

    @Transactional
    public void deleteExpense(Long expenseId, Long userId) {
        Expense expense = expenseRepository.findByIdWithTrip(expenseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Gasto no encontrado"));
        tripService.getTripAndValidateMember(expense.getTrip().getId(), userId);
        expenseRepository.delete(expense);
    }

}