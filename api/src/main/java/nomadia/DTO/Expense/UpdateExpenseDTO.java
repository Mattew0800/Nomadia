package nomadia.DTO.Expense;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateExpenseDTO {
    @NotNull
    private Long expenseId;

    @NotBlank
    private String name;

    private String note;

    @NotNull
    @Positive
    private BigDecimal totalAmount;

    @NotEmpty
    @Valid
    private List<ExpenseParticipantDTO> participants;
}
