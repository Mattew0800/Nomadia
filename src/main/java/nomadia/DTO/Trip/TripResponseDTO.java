package nomadia.DTO.Trip;

import lombok.*;
import nomadia.Enum.State;
import nomadia.Enum.TripType;
import nomadia.Model.Trip;
import nomadia.Model.User;

import java.math.BigDecimal;
import java.time.LocalDate;


@Getter
@Setter
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
    private BigDecimal budget;
    private long durationDays;
    private Long createdById;
    private String createdByName;

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
                .budget(trip.getBudget())
                .durationDays(trip.getDurationDays())
                .createdById(trip.getCreatedBy() != null ? trip.getCreatedBy().getId() : null)
                .createdByName(trip.getCreatedBy() != null ? trip.getCreatedBy().getName() : null)
                .build();
    }
}
