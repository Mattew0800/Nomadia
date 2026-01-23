package nomadia.DTO.Trip;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nomadia.DTO.Activity.ActivityResponseDTO;
import nomadia.Enum.State;
import nomadia.Enum.TripType;
import nomadia.Model.Trip;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripResponseDTO {

    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private State state;
    private TripType type;
    private long durationDays;
    private Long createdById;
    private String createdByName;
    private List<ActivityResponseDTO> activities;

    public static TripResponseDTO fromEntity(Trip trip) {
        if (trip == null) return null;

        return TripResponseDTO.builder()
                .id(trip.getId())
                .name(trip.getName())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .description(trip.getDescription())
                .state(trip.getState())
                .type(trip.getType())
                .durationDays(trip.getDurationDays())
                .createdById(trip.getCreatedBy() != null ? trip.getCreatedBy().getId() : null)
                .createdByName(trip.getCreatedBy() != null ? trip.getCreatedBy().getName() : null)
                .activities(trip.getActivities() != null
                        ? trip.getActivities().stream()
                        .map(a -> ActivityResponseDTO.builder()
                                .id(a.getId())
                                .name(a.getName())
                                .date(a.getDate())
                                .description(a.getDescription())
                                .cost(a.getCost())
                                .build())
                        .collect(Collectors.toList())
                        : null)                .build();
    }
}
