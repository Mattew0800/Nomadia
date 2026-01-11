package nomadia.DTO.Expense;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class ExpenseIdRequestDTO {
    @NotNull
    private Long expenseId;
}
