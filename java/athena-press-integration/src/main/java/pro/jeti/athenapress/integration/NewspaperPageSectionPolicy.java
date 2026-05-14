package pro.jeti.athenapress.integration;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class NewspaperPageSectionPolicy {

    private final Map<NewspaperPageSectionType, NewspaperSectionRequirement> requirements;

    public NewspaperPageSectionPolicy(
            Map<NewspaperPageSectionType, NewspaperSectionRequirement> requirements
    ) {
        this.requirements = new EnumMap<>(NewspaperPageSectionType.class);
        if (requirements != null) {
            this.requirements.putAll(requirements);
        }
    }

    public static NewspaperPageSectionPolicy defaultPolicy() {
        Map<NewspaperPageSectionType, NewspaperSectionRequirement> requirements =
                new EnumMap<>(NewspaperPageSectionType.class);

        requirements.put(NewspaperPageSectionType.TITLE_PAGE, NewspaperSectionRequirement.REQUIRED);
        requirements.put(NewspaperPageSectionType.MAIN_ARTICLE, NewspaperSectionRequirement.WHEN_CONTENT_EXISTS);
        requirements.put(NewspaperPageSectionType.MIXED_ARTICLES, NewspaperSectionRequirement.WHEN_CONTENT_EXISTS);
        requirements.put(NewspaperPageSectionType.ADVERTISEMENTS, NewspaperSectionRequirement.WHEN_CONTENT_EXISTS);
        requirements.put(NewspaperPageSectionType.SHORT_NOTICES, NewspaperSectionRequirement.WHEN_CONTENT_EXISTS);
        requirements.put(NewspaperPageSectionType.MEMORIAL, NewspaperSectionRequirement.WHEN_CONTENT_EXISTS);
        requirements.put(NewspaperPageSectionType.BACK_PAGE, NewspaperSectionRequirement.WHEN_CONTENT_EXISTS);

        return new NewspaperPageSectionPolicy(requirements);
    }

    public NewspaperSectionRequirement requirementFor(NewspaperPageSectionType type) {
        if (type == null) {
            return NewspaperSectionRequirement.WHEN_CONTENT_EXISTS;
        }

        return requirements.getOrDefault(type, NewspaperSectionRequirement.WHEN_CONTENT_EXISTS);
    }

    public boolean shouldInclude(NewspaperPageSection section) {
        if (section == null) {
            return false;
        }

        return switch (requirementFor(section.type())) {
            case REQUIRED -> true;
            case WHEN_CONTENT_EXISTS -> section.hasContent();
            case DISABLED -> false;
        };
    }

    public List<NewspaperPageSection> filter(List<NewspaperPageSection> sections) {
        if (sections == null || sections.isEmpty()) {
            return List.of();
        }

        return sections.stream()
                .filter(this::shouldInclude)
                .toList();
    }
}
