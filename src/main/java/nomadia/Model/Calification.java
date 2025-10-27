package nomadia.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nomadia.Validator.HalfStep;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "calification")
public class Calification {

    @Id
    @GeneratedValue
    private Long id;

    @DecimalMin(value = "1.0", inclusive = true, message = "La calificación mínima es 1.0")
    @DecimalMax(value = "5.0", inclusive = true, message = "La calificación máxima es 5.0")
    @HalfStep
    @Column(name = "rate", nullable = false)
    private Double rate;

    @Size(min = 3,max = 100,message = "La descripcion debe tener entre 3 y 100 caracteres ")
    @Column(name = "comment", nullable = true)
    private  String comment;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
