package org.threethan.universalreader.helper;

import com.jthemedetecor.OsThemeDetector;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Window;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Provides a few application-specific helper functions to help with stylesheets and dark/light themes
 * @author Ethan Medeiros
 */
public class StyleHelper {
    private final List<Consumer<Boolean>> consumerList = new LinkedList<>();
    public StyleHelper(Window window) {
        final OsThemeDetector detector = OsThemeDetector.getDetector();

        Consumer<Boolean> listener = isDark -> consumerList.forEach(c -> c.accept(isDark));

        detector.registerListener(listener);
        window.setOnCloseRequest(e -> detector.removeListener(listener));
    }

    /**
     * Set actions to run when the theme is changed
     * @param setDark Action to run when switching to the dark theme,
     *                as well as immediately if the theme is dark when calling this method
     * @param setLight Action to run when switching to the light theme
     */
    public void setDarkLightActions(Runnable setDark, Runnable setLight) {
        consumerList.add(isDark -> Platform.runLater((isDark) ? setDark : setLight));
        Platform.runLater(() -> {
            // Dark detector must be constructed on the UI thread for macOS specifically
            final boolean isDark = OsThemeDetector.getDetector().isDark();
            Platform.runLater((isDark) ? setDark : setLight);
        });
    }
    public void setStyles(Scene scene) {
        scene.getStylesheets().add(Objects.requireNonNull(StyleHelper.class.getResource("/styles/default.css")).toExternalForm());
        final String darkSheet = Objects.requireNonNull(Objects.requireNonNull(StyleHelper.class.getResource("/styles/dark.css")).toExternalForm());
        setDarkLightActions(
                () -> scene.getStylesheets().add(darkSheet),
                () -> scene.getStylesheets().remove(darkSheet)
        );
    }
    public void setBackgrounds(Scene scene) {
        setDarkLightActions(
                () -> scene.setFill(Color.valueOf("#222")),
                () -> scene.setFill(Color.valueOf("#FFF"))
        );
    }
}
