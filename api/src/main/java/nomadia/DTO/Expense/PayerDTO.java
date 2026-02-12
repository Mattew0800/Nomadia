package nomadia.DTO.Expense;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayerDTO {

    @NotNull
    private Long userId;

    @NotNull
    @Positive
    private BigDecimal amountPaid;
}
