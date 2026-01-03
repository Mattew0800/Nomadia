package nomadia.Service;

import jakarta.transaction.Transactional;
import nomadia.DTO.UserBalance.CreateExpenseDTO;
import nomadia.DTO.UserBalance.ExpenseParticipantDTO;
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

    public final ExpenseRepository expenseRepository;
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
    public void createExpense(CreateExpenseDTO dto, Long creatorId) {
        Activity activity = activityRepository.findById(dto.getActivityId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Actividad no encontrada"));
        Long tripId = activity.getTrip().getId();
        if (!tripService.isMember(tripId, creatorId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No perteneces a este viaje");
        }
        Expense expense = new Expense();
        expense.setName(dto.getName());
        expense.setNote(dto.getNote());
        expense.setTotalAmount(dto.getTotalAmount());
        expense.setActivity(activity);
        List<Participant> participants = new ArrayList<>();
        for (ExpenseParticipantDTO pDto : dto.getParticipants()) {
            User user = userService.findById(pDto.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Usuario no encontrado"));
            if (!tripService.isMember(tripId, user.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El participante no pertenece al viaje");
            }
            Participant participant = new Participant();
            participant.setExpense(expense);
            participant.setUser(user);
            participant.setAmountPaid(
                    pDto.getAmountPaid() != null
                            ? pDto.getAmountPaid()
                            : BigDecimal.ZERO);
            participants.add(participant);
        }
        expense.setParticipants(participants);
        expenseRepository.save(expense);
    }
}
