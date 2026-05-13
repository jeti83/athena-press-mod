package pro.jeti.athenapress.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ValidationReportServiceTest {

    private final ValidationReportService validationReportService = new ValidationReportService();

    @Test
    void createInlineValidationTextShowsSuccessMessage() {
        String text = validationReportService.createInlineValidationText(
                ValidationResult.valid()
        );

        assertTrue(text.contains("Validierung: OK - Keine Fehler gefunden."));
    }

    @Test
    void createInlineValidationTextShowsSingleError() {
        String text = validationReportService.createInlineValidationText(
                ValidationResult.invalid(List.of("Ein Testfehler"))
        );

        assertTrue(text.contains("Validierung: FEHLER - 1 Problem gefunden."));
        assertTrue(text.contains("- Ein Testfehler"));
    }

    @Test
    void createInlineValidationTextShowsMultipleErrors() {
        String text = validationReportService.createInlineValidationText(
                ValidationResult.invalid(List.of("Fehler eins", "Fehler zwei"))
        );

        assertTrue(text.contains("Validierung: FEHLER - 2 Probleme gefunden."));
        assertTrue(text.contains("- Fehler eins"));
        assertTrue(text.contains("- Fehler zwei"));
    }

    @Test
    void createStandaloneValidationTextShowsTitleAndSuccessMessage() {
        String text = validationReportService.createStandaloneValidationText(
                "Validierung für issue_0002",
                ValidationResult.valid()
        );

        assertTrue(text.contains("Validierung für issue_0002"));
        assertTrue(text.contains("----------------------------------------"));
        assertTrue(text.contains("OK - Keine Fehler gefunden."));
    }

    @Test
    void createStandaloneValidationTextShowsTitleAndErrors() {
        String text = validationReportService.createStandaloneValidationText(
                "Validierung für issue_0002",
                ValidationResult.invalid(List.of("Testfehler"))
        );

        assertTrue(text.contains("Validierung für issue_0002"));
        assertTrue(text.contains("FEHLER - 1 Problem gefunden."));
        assertTrue(text.contains("- Testfehler"));
    }

    @Test
    void createStandaloneValidationTextHandlesNullTitle() {
        String text = validationReportService.createStandaloneValidationText(
                null,
                ValidationResult.valid()
        );

        assertTrue(text.contains("(leer)"));
        assertTrue(text.contains("OK - Keine Fehler gefunden."));
    }
}