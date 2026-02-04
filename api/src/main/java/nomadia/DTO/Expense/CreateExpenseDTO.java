package nomadia.DTO.Expense;

import jakarta.validation.constraints.*;

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

    @AssertTrue(message = "Debe asociarse a un viaje o a una actividad")
    public boolean isTripOrActivityPresent() {
        return tripId != null || activityId != null;
    }
}




