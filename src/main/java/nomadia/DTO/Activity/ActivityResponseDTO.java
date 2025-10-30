package nomadia.DTO.Activity;

import lombok.Getter; import lombok.Setter;
import lombok.NoArgsConstructor; import lombok.AllArgsConstructor;
import nomadia.Model.Activity;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
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
