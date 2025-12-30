package nomadia.DTO.Trip;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TripIdRequestDTO {
    @NotNull(message = "El id del viaje es necesario")
    private Long tripId;
}
