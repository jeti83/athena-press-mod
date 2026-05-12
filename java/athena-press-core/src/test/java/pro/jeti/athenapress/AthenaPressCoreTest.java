package pro.jeti.athenapress;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class AthenaPressCoreTest {

    @Test
    void createsCoreWithRepositoriesAndServices() {
        Path athenaPressRoot = Path.of("AthenaPress");

        AthenaPressCore core = new AthenaPressCore(athenaPressRoot);

        assertEquals("AthenaPress Core", core.getName());
        assertEquals("0.1.0-SNAPSHOT", core.getVersion());
        assertEquals(athenaPressRoot, core.getAthenaPressRoot());

        assertNotNull(core.getArticleRepository());
        assertNotNull(core.getIssueRepository());
        assertNotNull(core.getSubscriberRepository());

        assertNotNull(core.getPressService());
        assertNotNull(core.getDeliveryService());
        assertNotNull(core.getValidationService());
        assertNotNull(core.getPreviewService());
    }
}