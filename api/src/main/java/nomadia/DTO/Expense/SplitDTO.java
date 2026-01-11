package nomadia.DTO.Expense;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SplitDTO {

    @NotNull
    private Long userId;

    @Positive
    private BigDecimal amountOwed;
}
