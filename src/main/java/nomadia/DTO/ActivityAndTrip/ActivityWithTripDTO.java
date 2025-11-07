package nomadia.DTO.ActivityAndTrip;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import nomadia.DTO.Activity.ActivityCreateDTO;

@Data
public class ActivityWithTripDTO {

    @NotNull(message = "El id del viaje es necesario")
    private Long tripId;

    @NotNull(message = "La actividad es necesaria")
    private ActivityCreateDTO activity;
}
