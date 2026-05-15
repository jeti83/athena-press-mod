package pro.jeti.athenapress.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class NewspaperPageRoleTest {

    @Test
    void exposesReaderFacingDisplayNames() {
        assertEquals("Titelseite", NewspaperPageRole.FRONT_COVER.displayName());
        assertEquals("Innenseite", NewspaperPageRole.LEFT_INNER.displayName());
        assertEquals("Rückseite", NewspaperPageRole.BACK_COVER.displayName());
    }

    @Test
    void marksCoverRoles() {
        assertTrue(NewspaperPageRole.FRONT_COVER.isCover());
        assertTrue(NewspaperPageRole.BACK_COVER.isCover());
        assertFalse(NewspaperPageRole.RIGHT_INNER.isCover());
    }
}
