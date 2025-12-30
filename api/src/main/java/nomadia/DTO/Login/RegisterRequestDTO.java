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
    @Size(min = 6,message = "La contrasenia debe tener 6 caracteres como minimo")
    private String password;
}

