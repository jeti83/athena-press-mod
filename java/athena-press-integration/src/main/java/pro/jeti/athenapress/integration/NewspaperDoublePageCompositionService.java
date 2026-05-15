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
        NewspaperPageLayout frontCover = pageLayouts.getFirst();
        spreads.add(new NewspaperDoublePageLayout(
                0,
                frontCover,
                null,
                roleFor(frontCover, true, pageLayouts.size()),
                NewspaperPageRole.SINGLE_PAGE,
                List.of()
        ));

        for (int index = 1; index < pageLayouts.size(); index += 2) {
            NewspaperPageLayout leftPage = pageLayouts.get(index);
            NewspaperPageLayout rightPage = index + 1 < pageLayouts.size()
                    ? pageLayouts.get(index + 1)
                    : null;
            spreads.add(new NewspaperDoublePageLayout(
                    spreads.size(),
                    leftPage,
                    rightPage,
                    roleFor(leftPage, true, pageLayouts.size()),
                    roleFor(rightPage, false, pageLayouts.size()),
                    List.of()
            ));
        }

        return withNavigation(spreads);
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

    private List<NewspaperDoublePageLayout> withNavigation(
            List<NewspaperDoublePageLayout> spreads
    ) {
        List<NewspaperDoublePageLayout> navigableSpreads = new ArrayList<>();

        for (NewspaperDoublePageLayout spread : spreads) {
            navigableSpreads.add(new NewspaperDoublePageLayout(
                    spread.spreadIndex(),
                    spread.leftPage(),
                    spread.rightPage(),
                    spread.leftRole(),
                    spread.rightRole(),
                    navigationFor(spread.spreadIndex(), spreads.size())
            ));
        }

        return navigableSpreads;
    }

    private List<NewspaperUiButton> navigationFor(
            int spreadIndex,
            int spreadCount
    ) {
        List<NewspaperUiButton> buttons = new ArrayList<>();

        if (spreadIndex > 0) {
            buttons.add(NewspaperUiButton.secondary(
                    "Zurückblättern",
                    NewspaperVisualUiCommands.previousSpread()
            ));
        }

        if (spreadIndex + 1 < spreadCount) {
            buttons.add(NewspaperUiButton.primary(
                    "Weiterblättern",
                    NewspaperVisualUiCommands.nextSpread()
            ));
        }

        buttons.add(NewspaperUiButton.danger(
                "Zeitung schließen",
                PlayerNewspaperUiCommand.closeIssue()
        ));

        return buttons;
    }
}
