package nomadia.DTO.Activity;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nomadia.Model.Activity;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityCreateDTO {

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
    private Double cost;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;

    @AssertTrue(message = "La hora de inicio debe ser anterior a la hora de fin")
    public boolean isStartBeforeEnd() {
        if (startTime == null || endTime == null) return true;
        return startTime.isBefore(endTime);
    }

    private LocalDate tripStartDate;

    private LocalDate tripEndDate;

    private Long tripId;

    @AssertTrue(message = "La fecha de la actividad debe estar dentro de las fechas del viaje")
    public boolean isDateWithinTrip() {
        if (date == null || tripStartDate == null || tripEndDate == null) return true;
        return !date.isBefore(tripStartDate) && !date.isAfter(tripEndDate);
    }
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
