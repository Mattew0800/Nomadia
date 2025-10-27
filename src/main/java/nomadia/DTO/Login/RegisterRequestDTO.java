package nomadia.DTO.Login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 50)
    private String name;

    @NotBlank(message = "El email es requerido")
    @Email(message = "Email inválido")
    private String email;
    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, max = 25, message= "La contraseña debe tener entre 8 y 25 caracteres")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).+$",
            message = "La contraseña debe contener al menos un número, una mayúscula y una minúscula"
    )
    private String password;
}

