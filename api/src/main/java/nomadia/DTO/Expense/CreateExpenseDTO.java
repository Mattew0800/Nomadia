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

    @NotBlank(message = "El nombre no tiene que estar vacio")
    private String name;

    private String note;

    @NotNull(message = "El monto total no tiene que estar vacio")
    @Positive
    private BigDecimal totalAmount;

    @NotEmpty(message = "Los que pagan no tiene que estar vacio")
    @Valid
    private List<PayerDTO> payers;

    @Valid
    private List<SplitDTO> splits;

    private boolean customSplit;

    @AssertTrue(message = "Debe asociarse directamente a un viaje o a una actividad")
    public boolean isTripOrActivityPresent() {
        return tripId != null || activityId != null;
    }
}




