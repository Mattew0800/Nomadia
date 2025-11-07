package nomadia.DTO.Trip;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TripAddUserByEmailDTO extends TripIdRequestDTO{
    @NotNull(message = "El email no puede estar vacío")
    @Email(message = "Debe ser un email válido")
    private String email;

}
