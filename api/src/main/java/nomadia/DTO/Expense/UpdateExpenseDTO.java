package nomadia.DTO.Expense;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateExpenseDTO extends CreateExpenseDTO{

    @NotNull
    private Long expenseId;
}
