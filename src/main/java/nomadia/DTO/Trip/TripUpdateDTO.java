package nomadia.DTO.Trip;

import jakarta.validation.constraints.*;
import lombok.*;
import nomadia.Enum.State;
import nomadia.Enum.TripType;
import nomadia.Model.Activity;
import nomadia.Model.Trip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TripUpdateDTO {

    @Size(min = 2, max = 100)
    private String name;

    private Long tripId;

    @FutureOrPresent(message = "La fecha de inicio debe ser hoy o futura")
    private LocalDate startDate;

    private LocalDate endDate;

    @Size(min = 2, max = 100)
    private String description;

    private State state;
    private TripType type;

    @PositiveOrZero(message = "El presupuesto no puede ser negativo")
    private BigDecimal budget;

    private List<Activity> activities;

    public void applyToEntity(Trip trip) {
        if(tripId!=null) trip.setId(tripId);
        if (name != null) trip.setName(name);
        if (startDate != null) trip.setStartDate(startDate);
        if (endDate != null) trip.setEndDate(endDate);
        if (description != null) trip.setDescription(description);
        if (state != null) trip.setState(state);
        if (type != null) trip.setType(type);
        if (budget != null) trip.setBudget(budget);
        if (activities != null) {
            Map<Long, Activity> existing = trip.getActivities().stream()
                    .collect(Collectors.toMap(Activity::getId, a -> a));

            List<Activity> updatedList = new ArrayList<>();

            for (Activity newAct : activities) {
                if (newAct.getId() != null && existing.containsKey(newAct.getId())) {
                    Activity existingAct = existing.get(newAct.getId());
                    existingAct.setName(newAct.getName());
                    existingAct.setDescription(newAct.getDescription());
                    existingAct.setCost(newAct.getCost());
                    existingAct.setDate(newAct.getDate());
                    updatedList.add(existingAct);
                } else {
                    newAct.setTrip(trip);
                    updatedList.add(newAct);
                }
            }
            trip.getActivities().clear();
            trip.getActivities().addAll(updatedList);
        }
    }
}
