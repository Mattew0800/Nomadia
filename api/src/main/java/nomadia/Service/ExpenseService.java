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

    @Transactional
    public ExpenseResponseDTO createExpense(CreateExpenseDTO dto, Long creatorId) {

        Activity activity = null;
        Trip trip;

        if (dto.getActivityId() != null) {
            activity = activityRepository.findById(dto.getActivityId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Actividad no encontrada"));
            trip = tripService.getTripAndValidateMember(activity.getTrip().getId(), creatorId);
        } else if (dto.getTripId() != null) {
            trip = tripService.getTripAndValidateMember(dto.getTripId(), creatorId);
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El gasto debe estar asociado a una actividad o a un viaje"
            );
        }

        Expense expense = new Expense();
        expense.setName(dto.getName());
        expense.setNote(dto.getNote());
        expense.setTotalAmount(dto.getTotalAmount());
        expense.setActivity(activity);
        expense.setTrip(trip);


        Map<Long, BigDecimal> paidMap = new HashMap<>();
        Map<Long, BigDecimal> owedMap = new HashMap<>();
        Set<Long> involvedUsers = new HashSet<>();

        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalOwed = BigDecimal.ZERO;

        for (PayerDTO payer : dto.getPayers()) {
            User user = userService.findById(payer.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Usuario no encontrado"));

            if (!trip.getUsers().contains(user)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "El pagador no pertenece al viaje");
            }

            paidMap.put(user.getId(), payer.getAmountPaid());
            involvedUsers.add(user.getId());
            totalPaid = totalPaid.add(payer.getAmountPaid());
        }

        if (totalPaid.compareTo(dto.getTotalAmount()) != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La suma de los pagos no coincide con el total");
        }

        if (dto.isCustomSplit()) {
            if (dto.getSplits() == null || dto.getSplits().isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La división personalizada requiere splits");
            }

            for (SplitDTO split : dto.getSplits()) {
                User user = userService.findById(split.getUserId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Usuario no encontrado"));

                if (!trip.getUsers().contains(user)) {
                    throw new ResponseStatusException(
                            HttpStatus.FORBIDDEN, "Participante fuera del viaje");
                }

                owedMap.put(user.getId(), split.getAmountOwed());
                involvedUsers.add(user.getId());
                totalOwed = totalOwed.add(split.getAmountOwed());
            }

            if (totalOwed.compareTo(dto.getTotalAmount()) != 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La suma de los consumos no coincide con el total");
            }

        } else {
            BigDecimal equalShare = dto.getTotalAmount()
                    .divide(BigDecimal.valueOf(involvedUsers.size()), 2, RoundingMode.HALF_UP);
            for (Long userId : involvedUsers) {
                owedMap.put(userId, equalShare);
                totalOwed = totalOwed.add(equalShare);
            }
        }

        List<Participant> participants = new ArrayList<>();

        for (Long userId : involvedUsers) {
            User user = userService.findById(userId).get();

            Participant participant = new Participant();
            participant.setExpense(expense);
            participant.setUser(user);
            participant.setAmountPaid(paidMap.getOrDefault(userId, BigDecimal.ZERO));
            participant.setAmountOwned(owedMap.getOrDefault(userId, BigDecimal.ZERO));

            participants.add(participant);
        }

        expense.setParticipants(participants);

        return ExpenseResponseDTO.fromEntity(expenseRepository.save(expense));
    }


    private void validateIntegrity(BigDecimal totalPaid, BigDecimal totalOwned, BigDecimal totalAmount) {
        if (totalPaid.compareTo(totalAmount) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La suma de los pagos no coincide con el total");
        }
        if (totalOwned.compareTo(totalAmount) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La suma de los pagos no coincide con el total");
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalTripCost(TripIdRequestDTO dto, Long userId) {
        tripService.getTripAndValidateMember(dto.getTripId(), userId);
        return activityRepository.getTotalActivityCostByTrip(dto.getTripId())
                .add(expenseRepository.getTotalExpensesByTrip(dto.getTripId()));
    }

    @Transactional
    public ExpenseResponseDTO updateExpense(UpdateExpenseDTO dto, Long userId) {

        Expense expense = expenseRepository.findById(dto.getExpenseId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Gasto no encontrado"
                ));

        Trip trip = expense.getTrip();
        if (!trip.getUsers().stream().anyMatch(u -> u.getId().equals(userId))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No pertenecés al viaje"
            );
        }

        expense.setName(dto.getName());
        expense.setNote(dto.getNote());
        expense.setTotalAmount(dto.getTotalAmount());

        expense.getParticipants().clear();

        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalOwned = BigDecimal.ZERO;

        Set<Long> uniqueUsers = new HashSet<>();
        for (ExpenseParticipantDTO p : dto.getParticipants()) {

            User user = userService.findById(p.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Usuario no encontrado"
                    ));

            if (!trip.getUsers().contains(user)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El usuario no pertenece al viaje"
                );
            }
            if (!uniqueUsers.add(p.getUserId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Un usuario no puede figurar más de una vez"
                );
            }

            Participant participant = new Participant();
            participant.setExpense(expense);
            participant.setUser(user);
            participant.setAmountPaid(p.getAmountPaid());
            participant.setAmountOwned(p.getAmountOwned());

            totalPaid = totalPaid.add(p.getAmountPaid());
            totalOwned = totalOwned.add(p.getAmountOwned());

            expense.getParticipants().add(participant);
        }

        validateIntegrity(totalPaid, totalOwned, dto.getTotalAmount());

        return ExpenseResponseDTO.fromEntity(expense);
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