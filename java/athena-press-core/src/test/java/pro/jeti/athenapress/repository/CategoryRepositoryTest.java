package pro.jeti.athenapress.repository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import pro.jeti.athenapress.model.Category;

class CategoryRepositoryTest {

    private final CategoryRepository repository = new CategoryRepository(
            Path.of("..", "..", "AthenaPress")
    );

    @Test
    void findAllReadsCategoriesFromTemplatesFolder() throws IOException {
        List<Category> categories = repository.findAll();

        assertEquals(6, categories.size());
        assertTrue(categories.stream().anyMatch(category -> "headline".equals(category.id())));
        assertTrue(categories.stream().anyMatch(category -> "server_news".equals(category.id())));
        assertTrue(categories.stream().anyMatch(category -> "build_projects".equals(category.id())));
        assertTrue(categories.stream().anyMatch(category -> "economy".equals(category.id())));
        assertTrue(categories.stream().anyMatch(category -> "classifieds".equals(category.id())));
        assertTrue(categories.stream().anyMatch(category -> "dating".equals(category.id())));
    }

    @Test
    void findByIdReturnsMatchingCategory() throws IOException {
        Category category = repository.findById("build_projects");

        assertNotNull(category);
        assertEquals("Bauprojekte", category.name());
        assertEquals("placeholders/no_image.png", category.defaultImage());
        assertTrue(category.enabled());
    }

    @Test
    void isEnabledCategoryReturnsTrueForEnabledCategory() throws IOException {
        assertTrue(repository.isEnabledCategory("dating"));
    }
}