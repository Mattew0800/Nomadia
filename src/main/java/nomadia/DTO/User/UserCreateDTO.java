package nomadia.DTO.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import nomadia.Enum.Role;
import nomadia.Model.User;

@Data
public class UserCreateDTO {

    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 50)
    private String name;

    @NotBlank(message = "El email es requerido")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6,message = "La contrasenia debe tener 6 caracteres como minimo")
    private String password;

    private Role role;
    public User toEntity() {
        User user = new User();
        user.setName(this.name);
        user.setEmail(this.email);
        user.setPassword(this.password);
        user.setRole(this.role);
        return user;
    }
}
