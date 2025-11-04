package nomadia.DTO.Activity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class ActivityUpdateRequestDTO {

    @NotNull(message = "El ID de la actividad es obligatorio")
    private Long activityId;

    @NotNull(message = "El ID del trip es obligatorio")
    private Long tripId;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 120)
    private String name;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate date;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 2, max = 2000)
    private String description;

    @NotNull(message = "El costo es obligatorio")
    @PositiveOrZero(message = "El costo no puede ser negativo")
    private Double cost;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;
}