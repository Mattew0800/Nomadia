package nomadia.DTO.User;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.Data;
import nomadia.Enum.Role;
import nomadia.Model.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Date;

@Data
public class UserUpdateDTO {

    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String name;

    @Email(message = "Email inválido")
    private String email;

    @Size(min = 6, message = "La contraseña debe tener 6 caracteres como mínimo")
    private String password;

    @Pattern(
            regexp = "^$|^\\+?[0-9\\s-]{7,20}$",
            message = "El teléfono debe contener solo números, espacios, guiones y opcionalmente un '+' inicial"
    )
    private String phone;

    private String photoUrl;

    @Pattern(
            regexp = "^$|^[\\S\\s]{3,10}$",
            message = "El apodo debe tener entre 3 y 10 caracteres si se proporciona"
    )
    private String nick;

    @Pattern(
            regexp = "^$|^[\\S\\s]{3,10}$",
            message = "El sobre mi debe tener entre 3 y 10 caracteres si se proporciona"
    )
    @Column(nullable = true)
    private String about;

    @Past(message = "La fecha no puede ser futura")
    private LocalDate birth;

    @Size(min = 6, message = "La contraseña debe tener 6 caracteres como mínimo")
    private String oldPassword;

    @Size(min = 6, message = "La contraseña debe tener 6 caracteres como mínimo")
    private String newNewPassword;

    @Size(min = 6, message = "La contraseña debe tener 6 caracteres como mínimo")
    private String newPassword;

    @Min(value = 0, message = "La edad no puede ser negativa")
    @Max(value = 120, message = "La edad no puede superar los 120 años")
    private Integer age;

    private Role role;

    public void applyToEntity(User user, PasswordEncoder passwordEncoder, boolean allowRoleChange){
        if (this.name != null) user.setName(this.name.trim());
        if (this.email != null) user.setEmail(this.email.trim().toLowerCase());
        if (this.phone != null) user.setPhone(this.phone.trim());
        if (this.nick != null) user.setNick(this.nick.trim());
        if (this.about != null) user.setAbout(this.about.trim());
        if (this.photoUrl != null) user.setPhotoUrl(this.photoUrl.trim());
        if (this.birth != null) user.setBirth(this.birth);
        if (this.age != null) user.setAge(this.age);

        if (this.newNewPassword != null && !this.newNewPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(this.newNewPassword));
        }
        if (this.role != null && allowRoleChange) {
            user.setRole(this.role);
        }
    }

}
