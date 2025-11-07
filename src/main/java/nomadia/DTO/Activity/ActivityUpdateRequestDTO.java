package nomadia.DTO.Activity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nomadia.Model.Activity;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
public class ActivityUpdateRequestDTO {

    @NotNull(message = "El ID de la actividad es obligatorio")
    private Long activityId;

    @NotNull(message = "El ID del trip es obligatorio")
    private Long tripId;

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
        if (this.name != null && !this.name.isBlank()) {
            activity.setName(this.name);
        }
        if (this.date != null) {
            activity.setDate(this.date);
        }
        if (this.description != null && !this.description.isBlank()) {
            activity.setDescription(this.description);
        }
        if (this.cost != null) {
            activity.setCost(this.cost);
        }
        if (this.startTime != null) {
            activity.setStartTime(this.startTime);
        }
        if (this.endTime != null) {
            activity.setEndTime(this.endTime);
        }
    }


    public boolean isTimeValid() {
        return startTime == null || endTime == null || startTime.isBefore(endTime);
    }
}