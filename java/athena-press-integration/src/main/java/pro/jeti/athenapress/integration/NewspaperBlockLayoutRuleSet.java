package pro.jeti.athenapress.integration;

import java.util.EnumMap;
import java.util.Map;

public class NewspaperBlockLayoutRuleSet {

    private final Map<NewspaperVisualBlockType, NewspaperBlockLayoutRule> rules;
    private final NewspaperBlockLayoutRule fallbackRule;

    public NewspaperBlockLayoutRuleSet(
            Map<NewspaperVisualBlockType, NewspaperBlockLayoutRule> rules
    ) {
        this.rules = new EnumMap<>(NewspaperVisualBlockType.class);
        if (rules != null) {
            this.rules.putAll(rules);
        }
        this.fallbackRule = new NewspaperBlockLayoutRule(
                null,
                1,
                1,
                1,
                false
        );
    }

    public static NewspaperBlockLayoutRuleSet defaultRules() {
        Map<NewspaperVisualBlockType, NewspaperBlockLayoutRule> rules =
                new EnumMap<>(NewspaperVisualBlockType.class);

        rules.put(NewspaperVisualBlockType.HEADLINE, new NewspaperBlockLayoutRule(
                NewspaperVisualBlockType.HEADLINE,
                4,
                5,
                2,
                true
        ));
        rules.put(NewspaperVisualBlockType.SUBHEADLINE, new NewspaperBlockLayoutRule(
                NewspaperVisualBlockType.SUBHEADLINE,
                2,
                3,
                1,
                false
        ));
        rules.put(NewspaperVisualBlockType.BODY_TEXT, new NewspaperBlockLayoutRule(
                NewspaperVisualBlockType.BODY_TEXT,
                5,
                8,
                1,
                false
        ));
        rules.put(NewspaperVisualBlockType.IMAGE, new NewspaperBlockLayoutRule(
                NewspaperVisualBlockType.IMAGE,
                8,
                12,
                1,
                false
        ));
        rules.put(NewspaperVisualBlockType.CAPTION, new NewspaperBlockLayoutRule(
                NewspaperVisualBlockType.CAPTION,
                1,
                1,
                1,
                false
        ));
        rules.put(NewspaperVisualBlockType.QUOTE, new NewspaperBlockLayoutRule(
                NewspaperVisualBlockType.QUOTE,
                4,
                5,
                1,
                false
        ));
        rules.put(NewspaperVisualBlockType.NOTICE, new NewspaperBlockLayoutRule(
                NewspaperVisualBlockType.NOTICE,
                3,
                4,
                1,
                false
        ));
        rules.put(NewspaperVisualBlockType.ADVERTISEMENT, new NewspaperBlockLayoutRule(
                NewspaperVisualBlockType.ADVERTISEMENT,
                6,
                9,
                1,
                false
        ));
        rules.put(NewspaperVisualBlockType.DIVIDER, new NewspaperBlockLayoutRule(
                NewspaperVisualBlockType.DIVIDER,
                1,
                1,
                2,
                true
        ));

        return new NewspaperBlockLayoutRuleSet(rules);
    }

    public int rowSpanFor(
            NewspaperVisualBlock block,
            NewspaperLayoutTemplate template
    ) {
        if (block != null && block.layoutIntent() == NewspaperBlockLayoutIntent.COVER) {
            return coverRowSpanFor(block);
        }

        return ruleFor(block).rowSpanFor(block, template);
    }

    public int columnSpanFor(
            NewspaperVisualBlock block,
            NewspaperLayoutTemplate template
    ) {
        return ruleFor(block).columnSpanFor(block, template);
    }

    public int weightFor(
            NewspaperVisualBlock block,
            NewspaperLayoutTemplate template
    ) {
        return rowSpanFor(block, template) * columnSpanFor(block, template);
    }

    private NewspaperBlockLayoutRule ruleFor(NewspaperVisualBlock block) {
        if (block == null || block.type() == null) {
            return fallbackRule;
        }

        return rules.getOrDefault(block.type(), fallbackRule);
    }

    private int coverRowSpanFor(NewspaperVisualBlock block) {
        return switch (block.type()) {
            case HEADLINE -> 4;
            case SUBHEADLINE -> 2;
            case NOTICE -> 3;
            case IMAGE -> 10;
            case CAPTION, DIVIDER -> 1;
            default -> ruleFor(block).defaultRowSpan();
        };
    }
}
