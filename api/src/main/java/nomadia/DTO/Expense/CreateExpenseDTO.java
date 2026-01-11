package nomadia.DTO.Expense;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class CreateExpenseDTO {

    private Long activityId;
    private Long tripId;

    @NotBlank
    private String name;

    private String note;

    @NotNull
    @Positive
    private BigDecimal totalAmount;


    @NotEmpty
    @Valid
    private List<PayerDTO> payers;


    @Valid
    private List<SplitDTO> splits;

    private boolean customSplit;
}


