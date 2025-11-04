package nomadia.DTO.Activity;

import lombok.*;
import nomadia.Model.Activity;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityResponseDTO {
    private Long id;
    private String name;
    private LocalDate date;
    private String description;
    private Double cost;
    private Long tripId;

    public static ActivityResponseDTO fromEntity(Activity a) {
        ActivityResponseDTO dto = new ActivityResponseDTO();
        dto.setId(a.getId());
        dto.setName(a.getName());
        dto.setDate(a.getDate());
        dto.setDescription(a.getDescription());
        dto.setCost(a.getCost());
        dto.setTripId(a.getTrip().getId());
        return dto;
    }
}
