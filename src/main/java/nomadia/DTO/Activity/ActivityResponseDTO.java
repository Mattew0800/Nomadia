package nomadia.DTO.Activity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nomadia.Model.Activity;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
