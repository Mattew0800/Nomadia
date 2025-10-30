package nomadia.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import nomadia.Enum.State;
import nomadia.Enum.TripType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "trip")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es necesario")
    @Size(min = 2, max = 100, message = "El nombre debe ser entre 2 y 100 caracteres")
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "El viaje debe empezar hoy o en el futuro")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "La fecha de finalización es obligatoria")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Size(min = 2, max = 100, message = "La descripción debe tener entre 2 y 100 caracteres")
    @Column(name = "description",nullable = true)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = true)
    private State state;

    @NotNull(message = "El tipo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TripType type;

    @ManyToMany(mappedBy = "trips")
    private Set<User> users = new HashSet<>();

    @PositiveOrZero(message = "El presupuesto no puede ser negativo")
    @Column(name = "budget", precision = 12, scale = 2,nullable = true)
    private BigDecimal budget;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Transient
    public long getDurationDays() {
        if (startDate == null || endDate == null) return 0;
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    @AssertTrue(message = "La fecha de finalización debe ser al menos mañana y posterior a la fecha de inicio")
    public boolean isEndAfterStartAndAtLeastTomorrow() {
        if (startDate == null || endDate == null) return true;
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return endDate.isAfter(startDate) && (endDate.isAfter(tomorrow) || endDate.isEqual(tomorrow));
    }

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Activity> activities = new ArrayList<>();
}
