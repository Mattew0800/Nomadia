package nomadia.DTO.Trip;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nomadia.DTO.Activity.ActivityCreateDTO;
import nomadia.Enum.State;
import nomadia.Enum.TripType;
import nomadia.Model.Activity;
import nomadia.Model.Trip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripCreateDTO {

    @NotBlank(message = "El nombre es necesario")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio debe ser hoy o futura")
    private LocalDate startDate;

    @NotNull(message = "La fecha de finalización es obligatoria")
    private LocalDate endDate;

    @Size(min = 2, max = 100, message = "La descripción debe tener entre 2 y 100 caracteres")
    private String description;

    private State state;

    @NotNull(message = "El tipo es obligatorio")
    private TripType type;

    @PositiveOrZero(message = "El presupuesto no puede ser negativo")
    private BigDecimal budget;

    private List<ActivityCreateDTO> activities;

    @AssertTrue(message = "La fecha de finalización debe ser posterior a la fecha de inicio")
    public boolean isEndAfterStart() {
        if (startDate == null || endDate == null) return true;
        return endDate.isAfter(startDate);
    }

    @AssertTrue(message = "La fecha de inicio no puede ser anterior a hoy")
    public boolean isStartTodayOrFuture() {
        if (startDate == null) return true;
        return !startDate.isBefore(LocalDate.now());
    }

    public Trip toEntity() {
        Trip trip = new Trip();
        trip.setName(this.name);
        trip.setStartDate(this.startDate);
        trip.setEndDate(this.endDate);
        trip.setDescription(this.description);
        trip.setState(this.state);
        trip.setType(this.type);
        trip.setBudget(this.budget);

        if (activities != null) {
            trip.setActivities(
                    activities.stream()
                            .map(dto -> {
                                Activity activity = new Activity();
                                activity.setName(dto.getName());
                                activity.setDate(dto.getDate());
                                activity.setDescription(dto.getDescription());
                                activity.setCost(dto.getCost());
                                activity.setStartTime(dto.getStartTime());
                                activity.setEndTime(dto.getEndTime());
                                activity.setTrip(trip);
                                return activity;
                            })
                            .collect(Collectors.toList())
            );
        }

        return trip;
    }
}
