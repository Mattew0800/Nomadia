package nomadia.DTO.Activity;

import lombok.Getter; import lombok.Setter;
import lombok.NoArgsConstructor; import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ActivityCreateRequestDTO {
    private String name;
    private LocalDate date;
    private String description;
    private Double cost;
}
