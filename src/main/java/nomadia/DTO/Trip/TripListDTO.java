package nomadia.DTO.Trip;

import lombok.*;
import nomadia.Enum.State;
import nomadia.Enum.TripType;
import nomadia.Model.Trip;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    public static TripListDTO fromEntity(Trip trip) { // a modificar
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
                .build();
    }
}
