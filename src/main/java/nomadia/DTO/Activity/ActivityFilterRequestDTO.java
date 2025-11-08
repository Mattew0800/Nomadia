package nomadia.DTO.Activity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ActivityFilterRequestDTO {
    private LocalDate fromDate;  // opcional
    private LocalDate toDate;    // opcional
    private LocalTime fromTime;  // opcional (franja horaria)
    private LocalTime toTime;    // opcional
}
