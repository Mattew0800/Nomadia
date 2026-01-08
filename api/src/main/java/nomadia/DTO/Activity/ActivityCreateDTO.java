package nomadia.DTO.Activity;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nomadia.Model.Activity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityCreateDTO {
    @NotNull
    private Long tripId;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 120, message = "El nombre debe tener entre 2 y 120 caracteres")
    private String name;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate date;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 2, max = 2000, message = "La descripción debe tener entre 2 y 2000 caracteres")
    private String description;

    @NotNull(message = "El costo es obligatorio")
    @PositiveOrZero(message = "El costo no puede ser negativo")
    private BigDecimal cost;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;

    public Activity toEntity() {
        Activity activity = new Activity();
        activity.setName(this.name);
        activity.setDate(this.date);
        activity.setDescription(this.description);
        activity.setCost(this.cost);
        activity.setStartTime(this.startTime);
        activity.setEndTime(this.endTime);
        return activity;
    }
}
