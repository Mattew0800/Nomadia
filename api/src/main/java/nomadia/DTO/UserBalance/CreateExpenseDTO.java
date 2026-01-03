package nomadia.DTO.UserBalance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateExpenseDTO {

    @NotNull(message = "La actividad es obligatoria")
    private Long activityId;

    @NotBlank(message = "El nombre del gasto es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String name;

    @NotBlank(message = "La nota del gasto es obligatoria")
    @Size(max = 255, message = "La nota no puede superar los 255 caracteres")
    private String note;

    @NotNull(message = "El total del gasto es obligatorio")
    @Positive(message = "El total del gasto debe ser mayor a 0")
    private BigDecimal totalAmount;

    @NotEmpty(message = "El gasto debe tener al menos un participante")
    @Valid
    private List<ExpenseParticipantDTO> participants;
}


