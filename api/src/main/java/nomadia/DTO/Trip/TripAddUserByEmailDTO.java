package nomadia.DTO.Trip;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TripAddUserByEmailDTO extends TripIdRequestDTO{
    @NotBlank(message = "El email no puede estar vacío")
    @Email (message = "El mail debe tener un formato válido")
    private String email;
}
