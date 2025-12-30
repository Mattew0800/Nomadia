package nomadia.Validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class HalfStepValidator implements ConstraintValidator<HalfStep, Double> {

    @Override
    public boolean isValid(Double value, ConstraintValidatorContext context) {
        if (value == null) return true; // lo maneja otra validación si es obligatorio
        double remainder = value * 10 % 5; // verifica si el decimal es múltiplo de .5
        return remainder == 0;
    }
}
