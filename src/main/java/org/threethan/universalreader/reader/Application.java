package org.threethan.universalreader.reader;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.threethan.universalreader.helper.StyleHelper;
import org.threethan.universalreader.helper.WindowHelper;

import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


/**
 * The application class for the text viewer & playback window
 * @see Controller
 * @author Ethan Medeiros
 */
public class Application extends javafx.application.Application {
    /** The controller of the current overlay window, null if no window is an overlay */
    private static Controller overlayController = null;

    public static void main(String[] args) {

        new JFXPanel(); // this will prepare JavaFX toolkit and environment
        // This workaround can be 5-10 seconds faster than using Application.launch()
        Platform.runLater(() -> new Application().start(new Stage()));
    }

    public Controller controller;


        @Override
    public void start(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("/fxml/reader.fxml"));
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        controller = fxmlLoader.getController();

        stage.setScene(scene);
        new StyleHelper(stage).setStyles(scene);

        controller.setStage(stage);
    }

    @Override
    public void stop() throws Exception {
        controller.destroy();
        super.stop();
    }

    protected static double WIN_WIDTH = 500;
    protected static double WIN_HEIGHT = 250;
    protected static double WIN_MIN_HEIGHT = 46;
    protected static double WIN_MIN_WIDTH = 380;
    protected static double OVL_WIDTH = 195;
    protected static double OVL_HEIGHT = 47;

    /**
     * Sets various parameters on a stage which configure it as a normal program window
     * @param stage Stage of the window, must not have been previously made visible
     * @param settings OcrSettings instance for remembered window position & size
     */
    public static void setupOverlayWindow(Stage stage, Settings settings, Controller controller) {
        // Make sure there's only one overlay at a time
        if (overlayController != null && !overlayController.equals(controller) && overlayController.isOverlay())
            overlayController.toggleOverlay();
        overlayController = controller;

        stage.setTitle("Screen Reader Overlay");
        stage.getIcons().add(new Image(
                Objects.requireNonNull(Application.class.getResourceAsStream("/assets/icon.png"))));

        stage.setResizable(false);

        stage.setWidth(OVL_WIDTH);
        stage.setHeight(OVL_HEIGHT);

        Rectangle2D bounds = Screen.getPrimary().getBounds();
        final double def_x = bounds.getMinX();
        final double def_y = bounds.getMaxY() - OVL_HEIGHT;
        stage.setX((double) settings.getOrDefault(Settings.Pref.OVL_X, def_x));
        stage.setY((double) settings.getOrDefault(Settings.Pref.OVL_Y, def_y));

        WindowHelper.showTransparent(stage);
        WindowHelper.ensureOnScreen(stage);

        // Keep the overlay window above the taskbar
        Timer timer = new Timer();
        stage.iconifiedProperty().addListener((v1,v2,v3) -> stage.setIconified(false));
        stage.setAlwaysOnTop(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (Controller.getMostRecentController() != null &&
                            Controller.getMostRecentController().isOverlay())
                        stage.toFront(); // Prevents flickering, but messes with menus if there's another window
                    else {
                        stage.setAlwaysOnTop(false);
                        stage.setAlwaysOnTop(true);
                    }
                });
            }
        }, 0, 250);
        stage.setOnHidden(e -> timer.cancel()); // Stop the repeating timer when window is closed

        // Short flashing animation to help user locate the overlay
        new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(stage.getScene().fillProperty(), Color.TRANSPARENT)),
                new KeyFrame(Duration.millis(500 ), new KeyValue(stage.getScene().fillProperty(), Color.WHITE)),
                new KeyFrame(Duration.millis(1000), new KeyValue(stage.getScene().fillProperty(), Color.TRANSPARENT))
        ).play();
    }


    /**
     * Sets various parameters on a stage which configure it as a normal program window
     * @param stage Stage of the window, must not have been previously made visible
     * @param settings OcrSettings instance for remembered window position & size
     */
    public static void setupNormalWindow(Stage stage, Settings settings) {
        stage.setTitle("Universal Screen Reader");
        stage.getIcons().add(new Image(
                Objects.requireNonNull(Application.class.getResourceAsStream("/assets/icon.png"))));
        stage.setMinWidth(WIN_MIN_WIDTH);
        stage.setResizable(true);

        stage.setWidth ((double) settings.getOrDefault(Settings.Pref.WIN_W, WIN_WIDTH));
        stage.setHeight((double) settings.getOrDefault(Settings.Pref.WIN_H, WIN_HEIGHT));

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        final double def_x = (bounds.getMinX()+bounds.getMaxX())/2 - WIN_WIDTH /2;
        final double def_y = bounds.getMaxY() - WIN_HEIGHT - 50;
        stage.setX((double) settings.getOrDefault(Settings.Pref.WIN_X, def_x));
        stage.setY((double) settings.getOrDefault(Settings.Pref.WIN_Y, def_y));

        WindowHelper.showWithEffects(stage);
        WindowHelper.ensureOnScreen(stage);

        Platform.runLater(() -> {
            final double titleBarHeight = stage.getHeight() - stage.getScene().getHeight();
            stage.setMinHeight(titleBarHeight+WIN_MIN_HEIGHT);
        });

        stage.toFront();
    }

    /** Offset of new windows from the previous window */
    private static final double NEW_WINDOW_OFFSET = 30;

    /**
     * Opens a new window, at an offset from an existing stage
     */
    public static void newWindow(Stage fromStage) {
        final Stage newStage = new Stage();
        // Start new window
        new Application().start(newStage);
        // Position new window
        if (fromStage == null) return;
        newStage.setWidth (fromStage.getWidth ());
        newStage.setHeight(fromStage.getHeight());
        newStage.setX(fromStage.getX() + NEW_WINDOW_OFFSET);
        newStage.setY(fromStage.getY() + NEW_WINDOW_OFFSET);
        WindowHelper.ensureOnScreen(newStage);
    }
}
