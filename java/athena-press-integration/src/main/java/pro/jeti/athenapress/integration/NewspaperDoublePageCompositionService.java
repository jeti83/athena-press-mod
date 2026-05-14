package pro.jeti.athenapress.integration;

import java.util.ArrayList;
import java.util.List;

public class NewspaperDoublePageCompositionService {

    private final NewspaperVisualRenderer renderer;
    private final NewspaperLayoutTemplate defaultTemplate;

    public NewspaperDoublePageCompositionService() {
        this(
                new NewspaperVisualRenderer(),
                NewspaperLayoutTemplate.classicDoublePage()
        );
    }

    public NewspaperDoublePageCompositionService(
            NewspaperVisualRenderer renderer,
            NewspaperLayoutTemplate defaultTemplate
    ) {
        this.renderer = renderer == null ? new NewspaperVisualRenderer() : renderer;
        this.defaultTemplate = defaultTemplate == null
                ? NewspaperLayoutTemplate.classicDoublePage()
                : defaultTemplate;
    }

    public List<NewspaperDoublePageLayout> compose(NewspaperVisualIssue issue) {
        return compose(issue, defaultTemplate);
    }

    public List<NewspaperDoublePageLayout> compose(
            NewspaperVisualIssue issue,
            NewspaperLayoutTemplate template
    ) {
        List<NewspaperPageLayout> pageLayouts = renderer.render(
                issue,
                template == null ? defaultTemplate : template
        );

        if (pageLayouts.isEmpty()) {
            return List.of();
        }

        List<NewspaperDoublePageLayout> spreads = new ArrayList<>();

        for (int index = 0; index < pageLayouts.size(); index += 2) {
            NewspaperPageLayout leftPage = pageLayouts.get(index);
            NewspaperPageLayout rightPage = index + 1 < pageLayouts.size()
                    ? pageLayouts.get(index + 1)
                    : null;
            spreads.add(new NewspaperDoublePageLayout(
                    index / 2,
                    leftPage,
                    rightPage,
                    roleFor(leftPage, true, pageLayouts.size()),
                    roleFor(rightPage, false, pageLayouts.size()),
                    navigationFor(index / 2, pageLayouts.size())
            ));
        }

        return spreads;
    }

    private NewspaperPageRole roleFor(
            NewspaperPageLayout page,
            boolean left,
            int pageCount
    ) {
        if (page == null) {
            return NewspaperPageRole.SINGLE_PAGE;
        }

        if (page.pageNumber() == 1) {
            return NewspaperPageRole.FRONT_COVER;
        }

        if (page.pageNumber() == pageCount && pageCount > 2) {
            return NewspaperPageRole.BACK_COVER;
        }

        return left ? NewspaperPageRole.LEFT_INNER : NewspaperPageRole.RIGHT_INNER;
    }

    private List<NewspaperUiButton> navigationFor(
            int spreadIndex,
            int pageCount
    ) {
        List<NewspaperUiButton> buttons = new ArrayList<>();

        if (spreadIndex > 0) {
            buttons.add(NewspaperUiButton.secondary(
                    "Zurückblättern",
                    PlayerNewspaperUiCommand.custom("visual_previous_spread", null)
            ));
        }

        if ((spreadIndex + 1) * 2 < pageCount) {
            buttons.add(NewspaperUiButton.primary(
                    "Weiterblättern",
                    PlayerNewspaperUiCommand.custom("visual_next_spread", null)
            ));
        }

        buttons.add(NewspaperUiButton.danger(
                "Zeitung schließen",
                PlayerNewspaperUiCommand.closeIssue()
        ));

        return buttons;
    }
}
