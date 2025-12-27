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
import nomadia.Enum.Role;

import java.util.Date;
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
            regexp = "^$|^[\\S\\s]{3,10}$",
            message = "El sobre mi debe tener entre 3 y 10 caracteres si se proporciona"
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
            message = "El apodo debe tener entre 3 y 10 caracteres si se proporciona"
    )
    @Column(nullable = true)
    private String nick;

    @Column(nullable = true)
    private Date birth;

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

    @Column(nullable = true)
    private Set<Trip> trips = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Calification> califications = new HashSet<>();


    public User toEntity() {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setPhone(phone);
        user.setAge(age);
        user.setAbout(about);
        user.setPhotoUrl(photoUrl);
        user.setNick(nick);
        user.setBirth(birth);
        return user;
    }

}
