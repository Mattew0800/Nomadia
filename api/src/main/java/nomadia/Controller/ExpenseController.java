package nomadia.Controller;

import jakarta.validation.Valid;
import nomadia.Config.UserDetailsImpl;
import nomadia.DTO.UserBalance.CreateExpenseDTO;
import nomadia.Service.ActivityService;
import nomadia.Service.ExpenseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/nomadia/expense")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ActivityService activityService;

    public ExpenseController(ExpenseService expenseService, ActivityService activityService) {
        this.expenseService = expenseService;
        this.activityService = activityService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createExpense(@Valid @RequestBody CreateExpenseDTO dto, @AuthenticationPrincipal UserDetailsImpl me) {
        expenseService.createExpense(dto, me.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
