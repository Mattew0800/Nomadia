package nomadia.DTO.Activity;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import nomadia.Model.Activity;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
public class ActivityUpdateRequestDTO {

    @NotNull(message = "El ID del viaje es obligatorio")
    private Long tripId;

    @NotNull(message = "El ID de la actividad es obligatorio")
    private Long activityId;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 120)
    private String name;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate date;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 2, max = 2000)
    private String description;

    @NotNull(message = "El costo es obligatorio")
    @PositiveOrZero(message = "El costo no puede ser negativo")
    private Double cost;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;

    public void applyToEntity(Activity activity) {
        activity.setName(this.name);
        activity.setDate(this.date);
        activity.setDescription(this.description);
        activity.setCost(this.cost);
        activity.setStartTime(this.startTime);
        activity.setEndTime(this.endTime);
    }

    public boolean isTimeValid() {
        return startTime == null || endTime == null || startTime.isBefore(endTime);
    }
}
