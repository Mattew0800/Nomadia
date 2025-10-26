package nomadia.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trip")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El destino es necesario")
    @Size(min = 2, max = 100, message = "El destino debe ser entre 2 y 100 caracteres")
    @Column(name = "destination",nullable = false)
    private String destination;

    @FutureOrPresent(message = "El viaje debe empezar entre hoy o en el futuro")
    @Column(name = "start_date",nullable = false)
    private LocalDate startDate;

    @AssertTrue(message = "La fecha de finalizacion debe ser minimo mañana")
    @Column(name = "end_date",nullable = false)
    private LocalDate endDate;

    @ManyToMany(mappedBy = "trips")
    private Set<User> users = new HashSet<>();

}
