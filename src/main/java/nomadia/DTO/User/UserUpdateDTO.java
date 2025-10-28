package nomadia.DTO.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import nomadia.Enum.Role;
import nomadia.Model.User;

@Data
public class UserUpdateDTO {

    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String name;

    @Email(message = "Email inválido")
    private String email;

    @Size(min = 6,message = "La contrasenia debe tener 6 caracteres como minimo")
    private String password;

    // No permitas que el propio usuario cambie el rol en /me/update
    private Role role; // lo vamos a ignorar en updateSelf
}
