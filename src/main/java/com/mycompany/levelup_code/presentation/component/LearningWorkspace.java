package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.business.evaluator.MissionEvaluation;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.CodeOrderingChallenge;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.ProfessionalMission;
import com.mycompany.levelup_code.domain.model.User;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/** Keeps learning pages in one window while retaining the previous page's controls and scroll. */
final class LearningWorkspace extends BorderPane implements LearningNavigation {
    private record Entry(LearningPage page, String breadcrumb) { }
    private final Deque<Entry> pages = new ArrayDeque<>();
    private final User user;
    private final Runnable lessonRefresh;
    private final Runnable challengeRefresh;
    private final Runnable exit;
    private final Consumer<Boolean> focus;
    private final Consumer<com.mycompany.levelup_code.domain.model.PracticeSession.Outcome> outcome;
    private final boolean singleChallenge;
    private final Label breadcrumb = WorkspaceUi.label("", "body-text");
    private final ToggleButton concentration = new ToggleButton("Concentrarme · F8");

    LearningWorkspace(User user, Runnable lessonRefresh, Runnable challengeRefresh, Runnable exit, Consumer<Boolean> focus) {
        this(user, lessonRefresh, challengeRefresh, exit, focus, null, false);
    }

    LearningWorkspace(User user, Runnable lessonRefresh, Runnable challengeRefresh, Runnable exit, Consumer<Boolean> focus,
            Consumer<com.mycompany.levelup_code.domain.model.PracticeSession.Outcome> outcome) {
        this(user, lessonRefresh, challengeRefresh, exit, focus, outcome, true);
    }

    LearningWorkspace(User user, Runnable lessonRefresh, Runnable challengeRefresh, Runnable exit, Consumer<Boolean> focus,
            Consumer<com.mycompany.levelup_code.domain.model.PracticeSession.Outcome> outcome, boolean singleChallenge) {
        this.user = user;
        this.lessonRefresh = lessonRefresh;
        this.challengeRefresh = challengeRefresh;
        this.exit = exit;
        this.focus = focus;
        this.outcome = outcome;
        this.singleChallenge = singleChallenge;
        setMinSize(0, 0);
        var back = WorkspaceUi.button("Volver", "btn-secondary", null);
        back.setOnAction(event -> back());
        concentration.getStyleClass().add("btn-secondary");
        concentration.selectedProperty().addListener((observable, old, selected) -> {
            concentration.setText(selected ? "Salir de concentración · F8" : "Concentrarme · F8");
            focus.accept(selected);
        });
        FlowPane navigation = new FlowPane(10, 8, back, concentration, breadcrumb);
        navigation.getStyleClass().add("learning-navigation");
        setTop(navigation);
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleShortcut);
    }

    void handleShortcut(KeyEvent event) {
        if (event.getCode() == KeyCode.F8) {
            concentration.setSelected(!concentration.isSelected());
            event.consume();
        } else if (event.isAltDown() && event.getCode() == KeyCode.LEFT) {
            back();
            event.consume();
        }
    }

    @Override public void openLesson(Lesson lesson) {
        if (lesson == null || !canLeave()) return;
        present(new LessonFrame(user, lesson, lessonRefresh), path(lesson));
    }

    @Override public void openChallenge(Challenge challenge) {
        if (challenge == null) return;
        openChallenge(challenge, path(challenge.getLesson()) + " › " + challenge.getTitle(), singleChallenge);
    }

    private void openChallenge(Challenge challenge, String title, boolean onlyThisChallenge) {
        if (!canLeave()) return;
        LearningPage page = challenge instanceof CodeOrderingChallenge ordering
                ? new CodeOrderingFrame(user, ordering, challengeRefresh, false)
                : new ChallengeFrame(user, challenge, challengeRefresh, false, onlyThisChallenge);
        present(page, title);
    }

    void openMission(ProfessionalMission mission, Supplier<Optional<Challenge>> lookup) {
        if (!canLeave()) return;
        String title = "Misiones › " + mission.title();
        present(new MissionBriefPane(mission, lookup, (brief, challenge) -> {
            var editor = new ChallengeFrame(user, challenge, challengeRefresh, false, true);
            editor.enableMissionReview(summary -> brief.showReview(MissionEvaluation.from(mission, summary)));
            Runnable openEditor = () -> present(editor, title + " › Reto");
            brief.resumeWith(openEditor);
            openEditor.run();
        }), title);
    }

    @Override public void evaluated(com.mycompany.levelup_code.domain.model.PracticeSession.Outcome result) {
        if (outcome != null) outcome.accept(result);
    }

    private static String path(Lesson lesson) {
        if (lesson == null) return "Práctica";
        String module = lesson.getModule() == null ? "Curso" : lesson.getModule().getTitle();
        String language = lesson.getModule() == null || lesson.getModule().getLanguage() == null
                ? "Aprender" : lesson.getModule().getLanguage().getName();
        return language + " › " + module + " › " + lesson.getTitle();
    }

    @Override public void challengeChanged(Challenge challenge) {
        if (pages.isEmpty()) return;
        Entry current = pages.pop();
        String title = path(challenge.getLesson()) + " › " + challenge.getTitle();
        pages.push(new Entry(current.page(), title));
        breadcrumb.setText(title);
    }

    private void present(LearningPage page, String title) {
        if (page instanceof Stage window) {
            SessionWindowRegistry.untrack(window);
            window.getScene().setRoot(new Group());
        }
        page.content().setMinSize(0, 0);
        page.setNavigator(this);
        pages.push(new Entry(page, title));
        showCurrent();
    }

    private void showCurrent() {
        Entry entry = pages.peek();
        setCenter(entry.page().content());
        entry.page().content().setFocusTraversable(true);
        javafx.application.Platform.runLater(() -> {
            if (getCenter() == entry.page().content()) entry.page().content().requestFocus();
        });
        breadcrumb.setText(entry.breadcrumb());
    }

    boolean hasPages() { return !pages.isEmpty(); }
    boolean canLeave() { return pages.isEmpty() || pages.peek().page().canLeave(); }

    @Override public void back() {
        if (pages.isEmpty() || !canLeave()) return;
        setCenter(null);
        pages.pop().page().disposePage();
        if (pages.isEmpty()) {
            concentration.setSelected(false);
            exit.run();
        } else showCurrent();
    }

    boolean clear() {
        if (!canLeave()) return false;
        setCenter(null);
        while (!pages.isEmpty()) pages.pop().page().disposePage();
        concentration.setSelected(false);
        return true;
    }
}
