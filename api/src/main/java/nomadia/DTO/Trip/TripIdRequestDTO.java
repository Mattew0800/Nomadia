package nomadia.DTO.Trip;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripIdRequestDTO {
    @NotNull(message = "El id del viaje es necesario")
    private Long tripId;
}
