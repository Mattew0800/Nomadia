package nomadia.DTO.Activity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityIdRequestDTO {
    @NotNull(message = "El ID de la actividad es obligatorio")
    private Long activityId;

    @NotNull(message = "El ID del viaje es obligatorio")
    private Long tripId;
}
