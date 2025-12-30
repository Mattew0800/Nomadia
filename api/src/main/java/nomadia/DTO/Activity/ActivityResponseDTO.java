package nomadia.DTO.Activity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nomadia.Model.Activity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityResponseDTO {
    private Long id;
    private String name;
    private LocalDate date;
    private String description;
    private BigDecimal cost;
    private Long tripId;
    private LocalTime startTime;
    private LocalTime endTime;

    public static ActivityResponseDTO fromEntity(Activity a) {
        ActivityResponseDTO dto = new ActivityResponseDTO();
        dto.setId(a.getId());
        dto.setName(a.getName());
        dto.setDate(a.getDate());
        dto.setDescription(a.getDescription());
        dto.setCost(a.getCost());
        dto.setTripId(a.getTrip().getId());
        dto.setStartTime(a.getStartTime());
        dto.setEndTime(a.getEndTime());
        return dto;
    }
}
