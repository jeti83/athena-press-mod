package pro.jeti.athenapress.service;

public class ValidationReportService {

    private static final String SECTION_LINE = "----------------------------------------";

    public String createInlineValidationText(ValidationResult validationResult) {
        StringBuilder text = new StringBuilder();

        if (validationResult == null || validationResult.isValid()) {
            text.append("Validierung: OK - Keine Fehler gefunden.\n");
            return text.toString();
        }

        appendErrorSummary(text, "Validierung: FEHLER", validationResult);

        return text.toString();
    }

    public String createStandaloneValidationText(String title, ValidationResult validationResult) {
        StringBuilder text = new StringBuilder();

        text.append("\n");
        text.append(safeText(title)).append("\n");
        text.append(SECTION_LINE).append("\n");

        if (validationResult == null || validationResult.isValid()) {
            text.append("OK - Keine Fehler gefunden.\n");
            text.append("\n");
            return text.toString();
        }

        appendErrorSummary(text, "FEHLER", validationResult);
        text.append("\n");

        return text.toString();
    }

    private void appendErrorSummary(
            StringBuilder text,
            String prefix,
            ValidationResult validationResult
    ) {
        int errorCount = validationResult.errors().size();
        String problemText = errorCount == 1 ? "Problem" : "Probleme";

        text.append(prefix)
                .append(" - ")
                .append(errorCount)
                .append(" ")
                .append(problemText)
                .append(" gefunden.\n");

        for (String error : validationResult.errors()) {
            text.append("- ").append(error).append("\n");
        }
    }

    private String safeText(String value) {
        if (value == null || value.isBlank()) {
            return "(leer)";
        }

        return value;
    }
}