package nomadia.DTO.ActivityAndTrip;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import nomadia.DTO.Activity.ActivityUpdateRequestDTO;

@Data
public class ActivityUpdateWithTripDTO {

    @NotBlank(message = "El id del viaje es necesario")
    private Long tripId;

    @NotBlank (message = "El id de la actividad es necesario")
    private Long activityId;

    @NotBlank(message = "La actividad es necesario")
    private ActivityUpdateRequestDTO update;
}
