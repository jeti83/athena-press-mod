package pro.jeti.athenapress.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class IntegrationPluginMetadataTest {

    @Test
    void exposesCurrentIntegrationMetadata() {
        IntegrationPluginMetadata metadata = IntegrationPluginMetadata.current();

        assertEquals("athena_press", metadata.pluginId());
        assertEquals("AthenaPress", metadata.displayName());
        assertEquals("0.1.0-SNAPSHOT", metadata.version());
        assertEquals("2026.03.26-89796e57b", metadata.targetServerVersion());
        assertEquals(
                "pro.jeti.athenapress.integration.AthenaPressIntegrationPlugin",
                metadata.entrypointClassName()
        );
        assertEquals("api-neutral-skeleton", metadata.status());
    }
}