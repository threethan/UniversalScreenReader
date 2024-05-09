package org.threethan.universalreader.helper;

import com.pixelduke.window.*;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;


/**
 * Handles OS-specific window effects.
 * Should enable blur effects on Windows 10, 11 and macOS; enable fallback background otherwise.
 * @author Ethan Medeiros
 */
public class WindowHelper {

    public static void showWithEffects(Stage stage) {
        ThemeWindowManager themeWindowManager = null;
        try {
            themeWindowManager = ThemeWindowManagerFactory.create();
        } catch (Exception e) {
            System.out.
                    println("Failed to set themeWindowManager");
        }

        stage.initStyle(StageStyle.UNIFIED);
        stage.show();

        StyleHelper styleHelper = new StyleHelper(stage);

        if (themeWindowManager != null) {
            // Windows
            if (themeWindowManager instanceof Win11ThemeWindowManager win11ThemeWindowManager) {
                win11ThemeWindowManager.setWindowBackdrop(stage, Win11ThemeWindowManager.Backdrop.ACRYLIC);
            }
            else if (themeWindowManager instanceof Win10ThemeWindowManager win10ThemeWindowManager) {
                win10ThemeWindowManager.enableAcrylic(stage, 100, 0x990500);
            } else if (!(themeWindowManager instanceof MacThemeWindowManager)) {
                // Linux does not have any sort of universal translucency, so simply set the background normally
                styleHelper.setBackgrounds(stage.getScene());
                stage.getScene().setFill(null);
            }
            stage.getScene().setFill(Color.TRANSPARENT);


            ThemeWindowManager finalThemeWindowManager = themeWindowManager;
            styleHelper.setDarkLightActions(
                    () -> finalThemeWindowManager.setDarkModeForWindowFrame(stage, true),
                    () -> finalThemeWindowManager.setDarkModeForWindowFrame(stage, false)
            );

        }
    }
    public static void showTransparent(Stage stage) {
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();

        new StyleHelper(stage).setStyles(stage.getScene());
    }

    /**
     * Make it possible to drag the stage (window) by dragging the node
     * @param stage Stage to move
     * @param node Area to drag
     */
    public static void addDragArea(Stage stage, Node node) {
        new DragArea(stage, node);
    }

    private static class DragArea {
        private double startX, startY;
        double dragPointX, dragPointY;

        public DragArea(Stage stage, Node node) {
            // Custom drag behaviour
            node.setOnMousePressed(e -> {
                dragPointX = e.getScreenX();
                dragPointY = e.getScreenY();
                startX = stage.getX();
                startY = stage.getY();
            });
            node.setOnMouseDragged(e -> {
                stage.setX(startX + e.getScreenX()-dragPointX);
                stage.setY(startY + e.getScreenY()-dragPointY);
                // Keep transparent window from leaving the screen
                if (stage.getStyle().equals(StageStyle.TRANSPARENT)) ensureOnScreen(stage);
            });
        }
    }

    /**
     * Ensure a stage is fully on-screen, moving it if not
     * @param stage Stage to check & move
     */
    synchronized public static void ensureOnScreen(Stage stage) {
        List<Screen> screens = Screen.getScreensForRectangle(stage.getX(), stage.getY(),
                stage.getX()+stage.getWidth(), stage.getY()+stage.getHeight());
        Rectangle2D screenBounds = (screens.isEmpty() ? Screen.getPrimary() : screens.get(0)).getBounds();

        if (stage.getX() < screenBounds.getMinX()) stage.setX(screenBounds.getMinX());
        if (stage.getY() < screenBounds.getMinY()) stage.setY(screenBounds.getMinY());
        if (stage.getX() + stage.getWidth()  > screenBounds.getMaxX()) stage.setX(screenBounds.getMaxX() - stage.getWidth());
        if (stage.getY() + stage.getHeight() > screenBounds.getMaxY()) stage.setY(screenBounds.getMaxY() - stage.getHeight());
    }
}
