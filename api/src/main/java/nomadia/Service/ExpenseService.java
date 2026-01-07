package nomadia.Service;

import jakarta.transaction.Transactional;
import nomadia.DTO.UserBalance.CreateExpenseDTO;
import nomadia.DTO.UserBalance.ExpenseParticipantDTO;
import nomadia.DTO.UserBalance.ExpenseResponseDTO;
import nomadia.Model.Activity;
import nomadia.Model.Expense;
import nomadia.Model.Participant;
import nomadia.Model.User;
import nomadia.Repository.ActivityRepository;
import nomadia.Repository.ExpenseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ActivityRepository activityRepository;
    private final TripService tripService;
    private final UserService userService;

    public ExpenseService(ExpenseRepository expenseRepository, ActivityRepository activityRepository, TripService tripService, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.activityRepository = activityRepository;
        this.tripService = tripService;
        this.userService = userService;
    }

    @Transactional
    public ExpenseResponseDTO createExpense(CreateExpenseDTO dto, Long creatorId) {
        Activity activity = activityRepository.findById(dto.getActivityId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Actividad no encontrada"));
        Long tripId = activity.getTrip().getId();
        if (!tripService.isMember(tripId, creatorId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No pertenecés a este viaje");
        }
        Expense expense = new Expense();
        expense.setName(dto.getName());
        expense.setNote(dto.getNote());
        expense.setTotalAmount(dto.getTotalAmount());
        expense.setActivity(activity);

        List<Participant> participants = new ArrayList<>();
        BigDecimal totalPaid = BigDecimal.ZERO;
        for (ExpenseParticipantDTO pDto : dto.getParticipants()) {
            User user = userService.findById(pDto.getUserId())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
            if (!tripService.isMember(tripId, user.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Uno de los participantes no pertenece al viaje");
            }
            BigDecimal amountPaid = pDto.getAmountPaid() != null
                    ? pDto.getAmountPaid()
                    : BigDecimal.ZERO;
            if (amountPaid.compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El monto pagado no puede ser negativo");
            }
            totalPaid = totalPaid.add(amountPaid);
            Participant participant = new Participant();
            participant.setExpense(expense);
            participant.setUser(user);
            participant.setAmountPaid(amountPaid);
            participants.add(participant);
        }
        if (totalPaid.compareTo(dto.getTotalAmount()) != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La suma de los pagos no coincide con el total del gasto");
        }
        expense.setParticipants(participants);
        return ExpenseResponseDTO.fromEntity(expenseRepository.save(expense));
    }
}
