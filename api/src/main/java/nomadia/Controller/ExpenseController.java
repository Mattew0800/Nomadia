package nomadia.Controller;

import jakarta.validation.Valid;
import nomadia.Config.UserDetailsImpl;
import nomadia.DTO.Expense.ExpenseIdRequestDTO;
import nomadia.DTO.Expense.UpdateExpenseDTO;
import nomadia.DTO.Trip.TripIdRequestDTO;
import nomadia.DTO.Expense.CreateExpenseDTO;
import nomadia.DTO.Expense.ExpenseResponseDTO;
import nomadia.Service.ExpenseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

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
    @PostMapping("/total-cost")
    @PreAuthorize("hasRole('USER')")
    public BigDecimal getTotalTripCost(@RequestBody TripIdRequestDTO tripId, @AuthenticationPrincipal UserDetailsImpl me){
        return expenseService.getTotalTripCost(tripId,me.getId());
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ExpenseResponseDTO> updateExpense(@Valid @RequestBody UpdateExpenseDTO dto, @AuthenticationPrincipal UserDetailsImpl me) {
        return ResponseEntity.ok(
                expenseService.updateExpense(dto, me.getId())
        );
    }


    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> delete(@RequestBody ExpenseIdRequestDTO dto,@AuthenticationPrincipal UserDetailsImpl me) {
        expenseService.deleteExpense(dto.getExpenseId(), me.getId());
        return ResponseEntity.noContent().build();
    }
}
