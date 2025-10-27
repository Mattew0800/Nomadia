package nomadia.DTO.Trip;

import jakarta.validation.constraints.*;
import lombok.*;
import nomadia.Enum.State;
import nomadia.Enum.TripType;
import nomadia.Model.Trip;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TripUpdateDTO {

    @Size(min = 2, max = 100)
    private String name;

    @FutureOrPresent(message = "La fecha de inicio debe ser hoy o futura")
    private LocalDate startDate;

    private LocalDate endDate;

    @Size(min = 2, max = 100)
    private String description;

    private State state;
    private TripType type;

    @PositiveOrZero(message = "El presupuesto no puede ser negativo")
    private BigDecimal budget;

    public void applyToEntity(Trip trip) {
        if (name != null) trip.setName(name);
        if (startDate != null) trip.setStartDate(startDate);
        if (endDate != null) trip.setEndDate(endDate);
        if (description != null) trip.setDescription(description);
        if (state != null) trip.setState(state);
        if (type != null) trip.setType(type);
        if (budget != null) trip.setBudget(budget);
    }
}
