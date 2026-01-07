package nomadia.Controller;

import jakarta.validation.Valid;
import nomadia.Config.UserDetailsImpl;
import nomadia.DTO.UserBalance.CreateExpenseDTO;
import nomadia.DTO.UserBalance.ExpenseResponseDTO;
import nomadia.Service.ExpenseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nomadia/expense")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ExpenseResponseDTO> createExpense(@Valid @RequestBody CreateExpenseDTO dto, @AuthenticationPrincipal UserDetailsImpl me) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.createExpense(dto, me.getId()));
    }
}
