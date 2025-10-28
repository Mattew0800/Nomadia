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

    @Size(min = 8, max = 25, message= "La contraseña debe tener entre 8 y 25 caracteres")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).+$",
            message = "La contraseña debe contener al menos un número, una mayúscula y una minúscula"
    )
    private String password;

    // No permitas que el propio usuario cambie el rol en /me/update
    private Role role; // lo vamos a ignorar en updateSelf
}
