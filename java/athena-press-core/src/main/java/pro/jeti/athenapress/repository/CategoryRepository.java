package pro.jeti.athenapress.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import pro.jeti.athenapress.model.Category;

public class CategoryRepository {

    private final Path categoriesFile;
    private final ObjectMapper objectMapper;

    public CategoryRepository(Path athenaPressRoot) {
        this.categoriesFile = athenaPressRoot
                .resolve("templates")
                .resolve("categories.json");
        this.objectMapper = new ObjectMapper();
    }

    public List<Category> findAll() throws IOException {
        if (!Files.exists(categoriesFile)) {
            return List.of();
        }

        CategoriesFile file = objectMapper.readValue(
                categoriesFile.toFile(),
                CategoriesFile.class
        );

        if (file.categories() == null) {
            return List.of();
        }

        return file.categories();
    }

    public List<Category> findEnabledCategories() throws IOException {
        return findAll().stream()
                .filter(Category::enabled)
                .toList();
    }

    public Category findById(String categoryId) throws IOException {
        for (Category category : findAll()) {
            if (categoryId.equals(category.id())) {
                return category;
            }
        }

        return null;
    }

    public boolean isEnabledCategory(String categoryId) throws IOException {
        Category category = findById(categoryId);

        return category != null && category.enabled();
    }

    private record CategoriesFile(
            List<Category> categories
    ) {
    }
}