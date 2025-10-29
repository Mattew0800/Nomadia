package nomadia.DTO.User;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import nomadia.Enum.Role;

import java.util.Date;

@Data
public class UserUpdateDTO {

    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String name;

    @Email(message = "Email inválido")
    private String email;

    @Size(min = 6, message = "La contraseña debe tener 6 caracteres como mínimo")
    private String password;

    // E.164 simple / flexible: permite +, espacios y guiones (7 a 20 dígitos aprox.)
    @Pattern(regexp = "^\\+?[0-9\\s-]{7,20}$",
            message = "El teléfono debe contener solo números, espacios, guiones y opcionalmente un '+' inicial")
    private String phone;

    // URL opcional (simple). Si querés algo más estricto, podés validarlo en el front.
    @Size(max = 255, message = "La URL de la foto no debe superar 255 caracteres")
    private String photoUrl;

    @Size(min = 3, max = 10, message = "El apodo debe tener entre 3 y 10 caracteres")
    private String nick;

    @Size(max = 500, message = "El 'about' no debe superar 500 caracteres")
    private String about;

    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private Date birth;

    private String oldPassword;
    private String newNewPassword;
    private String newPassword;

    private Integer age;

     private Role role;
}
