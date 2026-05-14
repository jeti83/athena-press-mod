package pro.jeti.athenapress.integration;

public record IntegrationPluginMetadata(
        String pluginId,
        String displayName,
        String version,
        String targetServerVersion,
        String entrypointClassName,
        String status
) {
    public static IntegrationPluginMetadata current() {
        return new IntegrationPluginMetadata(
                "athena_press",
                "AthenaPress",
                "0.1.0-SNAPSHOT",
                "2026.03.26-89796e57b",
                "pro.jeti.athenapress.integration.AthenaPressIntegrationPlugin",
                "api-neutral-skeleton"
        );
    }
}