package nomadia.DTO.Trip;

import lombok.*;
import nomadia.DTO.Activity.ActivityResponseDTO;
import nomadia.Enum.State;
import nomadia.Enum.TripType;
import nomadia.Model.Trip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripListDTO {

    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private State state;
    private TripType type;
    private BigDecimal budget;
    private long durationDays;
    private List<ActivityResponseDTO> activities;

    public static TripListDTO fromEntity(Trip trip) {
        if (trip == null) return null;

        return TripListDTO.builder()
                .id(trip.getId())
                .name(trip.getName())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .state(trip.getState())
                .type(trip.getType())
                .budget(trip.getBudget())
                .durationDays(trip.getDurationDays())
                .activities(
                        trip.getActivities() != null
                                ? trip.getActivities().stream()
                                .map(ActivityResponseDTO::fromEntity)
                                .toList()
                                : null
                )  .build();
    }
}
