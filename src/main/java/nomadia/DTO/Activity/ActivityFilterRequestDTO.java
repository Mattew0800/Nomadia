package nomadia.DTO.Activity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ActivityFilterRequestDTO {
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalTime fromTime;
    private LocalTime toTime;
    @NotBlank(message = "El id del viaje es necesario")
    private Long tripId;
}
