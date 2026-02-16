package nomadia.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nomadia.DTO.Login.RegisterRequestDTO;
import nomadia.Enum.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 10)
    private Role role;

    @Pattern(
            regexp = "^$|^[\\S\\s]{3,100}$",
            message = "El sobre mi debe tener entre 3 y 100 caracteres"
    )
    @Column(nullable = true)
    private String about;

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    @Size(max = 100)
    @Column(unique = true, nullable = false)
    private String email;

    @Pattern(
            regexp = "^$|^[\\S\\s]{3,10}$",
            message = "El apodo debe tener entre 3 y 10 caracteres"
    )

    @Column(nullable = true)
    private String nick;

    @Column(nullable = true)
    private LocalDate birth;

    @Column(nullable = true)
    private Integer age;

    @Lob
    @Column(name = "photo_url", columnDefinition = "LONGTEXT", nullable = true)
    private String photoUrl;

    @Column(nullable = true)
    private String phone;

    @ManyToMany
    @JoinTable(
            name = "user_trip",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "trip_id")
    )
    private Set<Trip> trips = new HashSet<>();

//    @OneToMany(mappedBy = "user")
//    private Set<Calification> califications = new HashSet<>();


    public static User fromRegisterDTO(RegisterRequestDTO dto, PasswordEncoder encoder) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setRole(Role.USER);
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return id != null && id.equals(((User) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
