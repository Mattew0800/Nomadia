package nomadia.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "activity", indexes = @Index(name = "idx_activity_trip", columnList = "trip_id"))
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 120, message = "El nombre debe tener entre 2 y 120 caracteres")
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "date")
    private LocalDate date; // puede ser null si aún no se definió

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 2, max = 2000, message = "La descripción debe tener entre 2 y 2000 caracteres")
    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @NotNull(message = "El costo es obligatorio")
    @PositiveOrZero(message = "El costo no puede ser negativo")
    @Column(name = "cost", nullable = false)
    private Double cost;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;
}
