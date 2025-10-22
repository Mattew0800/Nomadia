package nomadia.nomadia.User;


import jakarta.persistence.*;
import lombok.*;


@Entity @Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true)
    private String username;


    @Column(nullable = false, unique = true)
    private String email;


    @Column(nullable = false)
    private String password; // BCrypt


    @Enumerated(EnumType.STRING)
    private Role role;


    private boolean enabled = true;
}