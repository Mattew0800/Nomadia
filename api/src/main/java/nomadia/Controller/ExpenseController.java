package nomadia.Controller;

import jakarta.validation.Valid;
import nomadia.Config.UserDetailsImpl;
import nomadia.DTO.Activity.ActivityIdRequestDTO;
import nomadia.DTO.Expense.*;
import nomadia.DTO.Trip.TripIdRequestDTO;
import nomadia.DTO.UserBalance.UserBalanceDTO;
import nomadia.Service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

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
        return ResponseEntity.ok(expenseService.createExpense(dto, me.getId()));
    }
    @PostMapping("/total-cost")
    @PreAuthorize("hasRole('USER')")
    public BigDecimal getTotalTripCost(@RequestBody TripIdRequestDTO tripId, @AuthenticationPrincipal UserDetailsImpl me){
        return expenseService.getTotalTripCost(tripId,me.getId());
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ExpenseResponseDTO> updateExpense(@Valid @RequestBody ExpenseUpdateDTO dto, @AuthenticationPrincipal UserDetailsImpl me) {
        return ResponseEntity.ok(expenseService.updateExpense(dto, me.getId()));
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> delete(@Valid @RequestBody ExpenseIdRequestDTO dto,@AuthenticationPrincipal UserDetailsImpl me) {
        expenseService.deleteExpense(dto.getExpenseId(), me.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/by-trip")
    @PreAuthorize("hasRole('USER')")
    public List<ExpenseResponseDTO> getExpensesByTrip(@Valid @RequestBody TripIdRequestDTO dto, @AuthenticationPrincipal UserDetailsImpl me ) {
        return expenseService.getExpensesByTrip(dto, me.getId());
    }

    @PostMapping("/by-activity")
    @PreAuthorize("hasRole('USER')")
    public List<ExpenseResponseDTO> getExpensesByActivity(@Valid @RequestBody ActivityIdRequestDTO dto,@AuthenticationPrincipal UserDetailsImpl me ) {
        return expenseService.getExpensesByActivity(dto, me.getId());
    }

    @PostMapping("/get")
    @PreAuthorize("hasRole('USER')")
    public ExpenseResponseDTO getExpense(@Valid @RequestBody ExpenseIdRequestDTO dto,@AuthenticationPrincipal UserDetailsImpl me) {
        return expenseService.getExpense(dto, me.getId());
    }

    @PostMapping("/balance")
    @PreAuthorize("hasRole('USER')")
    public List<UserBalanceDTO> getTripBalance(@Valid @RequestBody TripIdRequestDTO dto, @AuthenticationPrincipal UserDetailsImpl me) {
        return expenseService.getTripBalance(dto, me.getId());
    }

}
