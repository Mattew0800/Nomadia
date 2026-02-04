package nomadia.DTO.Expense;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExpenseParticipantDTO {
    @NotNull
    private Long userId;

    @PositiveOrZero
    private BigDecimal amountPaid;

    @PositiveOrZero
    private BigDecimal amountOwned;
}
