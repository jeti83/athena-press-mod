package pro.jeti.athenapress.service;

import java.util.List;

public record ValidationResult(List<String> errors) {

    public boolean isValid() {
        return errors == null || errors.isEmpty();
    }

    public static ValidationResult valid() {
        return new ValidationResult(List.of());
    }

    public static ValidationResult invalid(List<String> errors) {
        return new ValidationResult(List.copyOf(errors));
    }
}