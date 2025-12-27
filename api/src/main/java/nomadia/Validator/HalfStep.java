package nomadia.Validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HalfStepValidator.class)
public @interface HalfStep {
    String message() default "La calificación debe tener pasos de 0.5";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
