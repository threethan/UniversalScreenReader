package org.threethan.universalreader.reader;

import io.github.jonelo.jAdapterForNativeTTS.engines.Voice;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.threethan.universalreader.helper.SpeechHelper;
import org.threethan.universalreader.helper.WindowHelper;
import org.threethan.universalreader.lib.FXInteractions;
import org.threethan.universalreader.lib.IOUtils;
import org.threethan.universalreader.ocr.OcrProcessor;

import java.util.ArrayList;
import java.util.Objects;

/**
 * The controller for the viewer window.
 * Extends upon the base viewer class to implement a GUI.
 * @see Reader
 * @see Application
 * @author Ethan Medeiros
 */
public class Controller extends Reader {

    @FXML private Node loadingIndicator;
    @FXML private Button startSpeakingButton;
    @FXML private Button stopSpeakingButton;
    @FXML private ComboBox<SpeechHelper.VoiceSpeed> speedSelectionBox;
    @FXML private Button settingsButton;
    @FXML Button openImageClipboardButton;
    @FXML private Button openTextClipboardButton;
    @FXML private HBox toolBar;
    @FXML private Separator selectionBoxSeparator;
    @FXML AnchorPane tutorial;

    @FXML private ComboBox<Voice> voiceSelectionBox;
    @FXML ImageView openedImageView;
    @FXML TextArea textArea;

    // Speak-in-parts highlighting
    @FXML private ScrollPane textHighlightArea;
    @FXML public TextFlow textHighlightFlow;
    @FXML private Text textHighlightEarlier;
    @FXML private Text textHighlightCurrent;
    @FXML private Text textHighlightLater;
    @FXML private Text textHighlightBefore;
    @FXML private Text textHighlightAfter;
    private static Controller mostRecentController;

    /** The text previously set via the OCR server, excluding any manual edits */
    private String mostRecentOcrText;
    private Stage stage;
    private final SettingsMenu settingsMenu = new SettingsMenu(this);

    /** Used to display currently read text line-by-line when in overlay mode */
    Tooltip overlayReadoutTooltip = new Tooltip();

    /** Called automatically by javaFX when FXML vars are ready */
    @FXML private void initialize() {
        // Set initial state of open clipboard button
        updateClipboardButtons();

        // Set initial state of spinners
        final SpeechHelper.VoiceSpeed voiceSpeed = (SpeechHelper.VoiceSpeed) settings.getOrDefault(Settings.Pref.VOICE_SPEED, SpeechHelper.VoiceSpeed.DEFAULT);
        speedSelectionBox.getSelectionModel().select(voiceSpeed);

        // Fancy language list
        Callback<ListView<Voice>, ListCell<Voice>> cellFactory = new Callback<>() {
            @Override
            public ListCell<Voice> call(ListView<Voice> l) {
                return new ListCell<>() {

                    @Override
                    protected void updateItem(Voice item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setContentDisplay(null);
                        } else {
                            Label name = new Label(item.getName());
                            Label culture = new Label(item.getCulture());
                            culture.setOpacity(0.5);
                            name.setMaxWidth(1000);
                            HBox.setHgrow(name, Priority.ALWAYS);
                            HBox hBox = new HBox(name, culture);
                            hBox.setSpacing(5);
                            hBox.setPadding(new Insets(7, 20, 6, 0));
                            hBox.maxWidth(1500);
                            setGraphic(hBox);
                        }
                    }
                };
            }
        };
        voiceSelectionBox.setButtonCell(cellFactory.call(null));
        voiceSelectionBox.setCellFactory(cellFactory);
        // Set list for voice selection
        voiceSelectionBox.setItems(FXCollections.observableList(SpeechHelper.voiceList()));
        final Voice selectedVoice =
                SpeechHelper.getVoiceByName((String) settings.getOrDefault(Settings.Pref.VOICE_NAME, ""));
        voiceSelectionBox.getSelectionModel().select(selectedVoice);

        // Set list for speed selection
        speedSelectionBox.setItems(FXCollections.observableList(new ArrayList<>(SpeechHelper.VoiceSpeed.SPEEDS)));


        // Set initial edit-ability of text
        textArea.setEditable((boolean) settings.getOrDefault(Settings.Pref.EDITABLE, false));
        tutorial.setVisible(!textArea.isEditable());

        // Set scroll-wheel interaction on combo-boxes
        FXInteractions.makeScrollable(voiceSelectionBox);
        FXInteractions.makeScrollable(speedSelectionBox);

        // Mutually exclusive button pairs
        startSpeakingButton.visibleProperty().bind(stopSpeakingButton.visibleProperty().not());
        stopSpeakingButton.visibleProperty().bind(SpeechHelper.speakingProperty());

        SpeechHelper.speakingProperty().addListener((val, from, to) -> {
            if (!to && textHighlightArea.isVisible()) {
                textArea.setScrollTop(textHighlightArea.getVvalue());
                textArea.setVisible(true);
            } else if (!to) overlayReadoutTooltip.hide();
        });

        openTextClipboardButton.visibleProperty().bind(openImageClipboardButton.visibleProperty().not());
        textHighlightArea.visibleProperty().bind(textArea.visibleProperty().not());

        // Small spacing fixup for macOS
        if (System.getProperty("os.name").equals("Mac OS X"))
            textHighlightFlow.setLineSpacing(1);

        Rectangle r = new Rectangle();
        r.widthProperty().bind(textArea.widthProperty());
        r.heightProperty().bind(textArea.heightProperty());
        textHighlightArea.setClip(r);
    }

    /** Called by the application class when the application is stopping/closing */
    @Override
    public void destroy() {
        // Calling this here stops these settings from being reverted due to multi-window edge cases
        // Calling this function also, intentionally, saves the settings from this window to the settings file
        updateDefaultWindowSettings();
        super.destroy();
    }

    /**
     * Passes the stage to the controller and sets certain required properties. Also shows the stage.
     * @param stage State of this viewer window
     */
    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setAlwaysOnTop((boolean) settings.getOrDefault(Settings.Pref.ALWAYS_ON_TOP, false));

        // Setup overlay mode
        final boolean overlay = (boolean) settings.getOrDefault(Settings.Pref.USE_OVERLAY, false);
        if (overlay) Application.setupOverlayWindow(stage, settings, this);
        else Application.setupNormalWindow(stage, settings);
        // Nodes which are hidden in the overlay mode
        Node[] windowOnlyNodes = new Node[]
                { speedSelectionBox, voiceSelectionBox, selectionBoxSeparator };
        for (Node node : windowOnlyNodes ) {
            node.setVisible(!overlay);
            node.setManaged(!overlay);
        }
        // Set visibility of opened image view
        openedImageView.setVisible(!overlay &&
                (boolean) settings.getOrDefault(Settings.Pref.IMAGE_BEHIND_TEXT, false));
        openedImageView.fitWidthProperty().bind(stage.widthProperty());

        // Allow dragging of the toolbar & its separators
        WindowHelper.addDragArea(stage, toolBar);
        for (Node child : toolBar.getChildren()) if (child instanceof Separator) WindowHelper.addDragArea(stage, child);

        setKeyboardShortcuts(stage);

        stage.focusedProperty().addListener((v1, v2, focused) -> { if (focused) mostRecentController = this; } );
        mostRecentController = this;

        // Set up the tooltip for overlay text display
        ChangeListener<Number> moveToolTip = (v1, v2, v3) -> {
            if (!isOverlay()) return;
            final double y = stage.getY()- overlayReadoutTooltip.getHeight()+8;
            overlayReadoutTooltip.setY(y);
            if (overlayReadoutTooltip.getY() > y) overlayReadoutTooltip.setY(stage.getY()+40);
            overlayReadoutTooltip.setX (stage.getX()+1);
        };
        stage.xProperty().addListener(moveToolTip);
        stage.yProperty().addListener(moveToolTip);
        overlayReadoutTooltip.heightProperty().addListener(moveToolTip);
    }

    /**
     * Gets the most recently used instance of this class.
     * Useful for retrieving "any" instance of this class from a static context.
     * @return The controller of the most recently focused {@link Application} window
     */
    public static Controller getMostRecentController() {
        return mostRecentController;
    }

    /**
     * Gets the stage (window) this controller is connected to
     * @return Stage of the window of this controller
     * @see Application
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Clears the image for when it's no longer in use (ie: opened text without associated image)
     */
    public void clearImage() {
        openedImageView.setImage(
                new Image(String.valueOf(Controller.class.getResource("/assets/default_preview.png"))));
    }

    /**
     * Sets the image used by this controller. Also starts client/server communication.
     * @param image awt image instance, should be a {@link java.awt.image.BufferedImage}
     */
    @Override
    public void submitImage(Image image) {
        tutorial.setVisible(false);
        if (image == null) return;
        openedImageView.setImage(image);
        // Clear text area & start loading
        onStopSpeakingPressed();
        loadingIndicator.setVisible(true);
        if (textArea != null) {
            textArea.clear();
            textArea.setEditable(false);
        }
        startSpeakingButton.setDisable(true);
        super.submitImage(image);
    }

    /**
     * Process a response from the server, setting OCRed text and so on
     * @param response Response from the server
     */
    @Override
    protected void processResponse(@SuppressWarnings("ClassEscapesDefinedScope") OcrProcessor.Result response) {
        super.processResponse(response);
        textArea.setText(response.ocrText());
        Platform.runLater(() -> {
            loadingIndicator.setVisible(false);
            this.mostRecentOcrText = textArea.getText();
            textArea.setEditable((boolean) settings.getOrDefault(Settings.Pref.EDITABLE, false));

            startSpeakingButton.setDisable(false);
            // Auto-speak, if enabled
            if ((boolean) settings.getOrDefault(Settings.Pref.AUTO_SPEAK, false)) speak(textArea.getText());
        });

    }

    /**
     * Called when text in the main area is typed in
     */
    @FXML
    private void onTextMaybeChanged() {
        if (!Objects.equals(textArea.getText(), currentText)) {
            currentText = textArea.getText();
            onTextChanged();
        }
    }
    /**
     * (Must be) Called when text in the main area is changed,
     * updates button states
     */
    @Override
    protected void onTextChanged() {
        super.onTextChanged();
        Platform.runLater(() -> {
            if (textArea.getText() == null) textArea.setText("");
            startSpeakingButton.setDisable(textArea.getText().isBlank());
            if (textArea.getText().isBlank() && !loadingIndicator.isVisible()) clearImage();
            if (openImageClipboardButton.isDisable()) {
                final Clipboard cb = Clipboard.getSystemClipboard();
                openImageClipboardButton.setDisable(Objects.equals(textArea.getText(), mostRecentOcrText) || !cb.hasImage());
            }
            if (openTextClipboardButton.isDisable()) {
                final Clipboard cb = Clipboard.getSystemClipboard();
                openTextClipboardButton .setDisable(Objects.equals(textArea.getText(), mostRecentOcrText) || cb.hasImage());
            }
        });
    }

    @Override
    protected void clipboardContentChanged() {
        super.clipboardContentChanged();
        updateClipboardButtons();
    }

    /** True if this window is an overlay */
    boolean isOverlay() {
        return  (boolean) settings.getOrDefault(Settings.Pref.USE_OVERLAY, false);
    }

    /** Toggles whether this window is an overlay or normal window */
    void toggleOverlay() {
        final boolean isOverlay = (boolean) settings.getOrDefault(Settings.Pref.USE_OVERLAY, false);
        updateDefaultWindowSettings();
        Scene scene = stage.getScene();
        stage.hide();
        stage = new Stage();
        stage.setScene(scene);
        settings.put(Settings.Pref.USE_OVERLAY, !isOverlay);
        setStage(stage);
    }

    /** Set settings relating to window size and position or overlay position */
    private void updateDefaultWindowSettings() {
        final boolean isOverlay = (boolean) settings.getOrDefault(Settings.Pref.USE_OVERLAY, false);
        if (isOverlay) {
            settings.put(Settings.Pref.OVL_X, stage.getX());
            settings.put(Settings.Pref.OVL_Y, stage.getY());
        } else {
            settings.put(Settings.Pref.WIN_X, stage.getX());
            settings.put(Settings.Pref.WIN_Y, stage.getY());
            settings.put(Settings.Pref.WIN_W, stage.getWidth());
            settings.put(Settings.Pref.WIN_H, stage.getHeight());
        }
    }

    /**
     * Sets the current voice name
     * @param voiceName Name of the voice
     */
    @Override
    void setVoiceName(String voiceName) {
        super.setVoiceName(voiceName);
        // Needed for linux where some voices are sometimes hidden
        if (SpeechHelper.collapseEntries) Platform.runLater(() -> {
            voiceSelectionBox.setItems(FXCollections.observableList(SpeechHelper.voiceList()));
            voiceSelectionBox.getSelectionModel().select(SpeechHelper.getVoiceByName(voiceName));
        });
    }

    /**
     * Sets the current voice speed
     * @param voiceSpeed Speed of the voice (object)
     */
    @Override
    void setVoiceSpeed(SpeechHelper.VoiceSpeed voiceSpeed) {
        super.setVoiceSpeed(voiceSpeed);
        speedSelectionBox.getSelectionModel().select(voiceSpeed);
    }

    /**
     * Speak a string with current settings for this window
     * @param text Text to speak
     */
    @Override
    protected void speak(String text) {
        if (text == null || text.isBlank()) return;
        startSpeakingButton.setDisable(false);
        if ((boolean) settings.getOrDefault(Settings.Pref.SPEAK_IN_PARTS, true))
            speakInParts(text);
        else speakAtOnce(text);
    }

    /** Speak a string "normally" */
    private void speakAtOnce(String text) {
        SpeechHelper.speak(text, getVoiceName(), getVoiceSpeed());
    }

    /** Speak a string in parts, and update the interface accordingly */
    private void speakInParts(String text) {
        final String fullText = textArea.getText();
        int offset;
        if (text.equals(textArea.getText())) offset = 0;
        else if (textArea.getSelectedText().equals(text))
            offset = textArea.getSelection().getStart();
        else {
            speakAtOnce(text);
            return;
        }

        textHighlightLater.setText(text);
        textHighlightBefore.setText(fullText.substring(0, offset));
        textHighlightAfter.setText(fullText.substring(offset+text.length()));

        SpeechHelper.speak(text, getVoiceName(), getVoiceSpeed(),
            (s, e) -> {
                if (isOverlay()) {
                    overlayReadoutTooltip.setText(text.substring(s,e).strip());
                    overlayReadoutTooltip.setMaxWidth(300);
                    overlayReadoutTooltip.setWrapText(true);
                    if (!overlayReadoutTooltip.isShowing()) overlayReadoutTooltip.show(stage);
                } else {
                    textArea.setVisible(false);
                    textHighlightEarlier.setText('\u200B' + text.substring(0, s));
                    textHighlightCurrent.setText(text.substring(s, e));
                    textHighlightLater.setText(text.substring(e));
                    FXInteractions.centerNodeInScrollPane(textHighlightArea, textHighlightCurrent);
                }
            });
    }

    /* BUTTONS */
    /** Starts speaking */
    @FXML
    public void onSpeakPressed() {
        String text = textArea.getSelectedText().isBlank() ? textArea.getText() : textArea.getSelectedText();
        speak(text);
    }
    /** Stops speech */
    @FXML
    public void onStopSpeakingPressed() {
        SpeechHelper.stop();
    }

    /** Sets the voice to that which is selected in voiceSelectionBox */
    @FXML
    public void onVoiceSelectionBoxPressed() {
        Voice voice = voiceSelectionBox.getSelectionModel().getSelectedItem();
        if (voice != null && !Objects.equals(getVoiceName(), voice.getName())) setVoiceName(voice.getName());
    }

    /** Sets the voice speed to that which is selected in speedSelectionBox */
    @FXML
    public void onSpeedSelectionBoxPressed() {
        SpeechHelper.VoiceSpeed voiceSpeed = speedSelectionBox.getSelectionModel().getSelectedItem();
        if (voiceSpeed != null && !Objects.equals(getVoiceSpeed(), voiceSpeed)) setVoiceSpeed(voiceSpeed);
    }

    /** Shows the settings menu */
    @FXML
    public void onSettingsButtonPressed() {
        settingsMenu.show(settingsButton);
    }

    /** Opens the image or text which is currently in the clipboard */
    public void openClipboard() {
        clearImage();
        tutorial.setVisible(false);
        super.openClipboard();
        openImageClipboardButton.setDisable(true);
        openTextClipboardButton.setDisable(true);
    }

    /** Opens a file picker from which an image file is loaded */ 
    public void openImageFile() {
        Image image = IOUtils.loadImage(stage);
        if (image != null) {
            submitImage(image);
            updateClipboardButtons();
        }
    }

    /** Switches the visibility of the clipboard image and text buttons based on the current clipboard content */
    private void updateClipboardButtons() {
        final Clipboard cb = Clipboard.getSystemClipboard();
        openImageClipboardButton.setDisable(false);
        openTextClipboardButton.setDisable(false);
        openImageClipboardButton.setVisible(cb.hasImage()); // Text button is set to be visible when this is not
    }
    
    /* KEYBOARD SHORTCUTS */
    /** Sets various keyboard shortcuts to the stage */
    private void setKeyboardShortcuts(Stage stage) {
        ObservableMap<KeyCombination, Runnable> a = stage.getScene().getAccelerators();
        a.put(KeyCombination.keyCombination("ctrl+n"), () -> {
            if (!(boolean) settings.getOrDefault(Settings.Pref.USE_OVERLAY, false))
                Application.newWindow(stage);
        });
        a.put(KeyCombination.keyCombination("ctrl+w")/* Common alternative to alt+f4()*/, () -> {
            if (!(boolean) settings.getOrDefault(Settings.Pref.USE_OVERLAY, false))
                stage.hide();
        });
        a.put(KeyCombination.keyCombination("ctrl+o"), this::openImageFile);
        a.put(KeyCombination.keyCombination("ctrl+shift+v"), this::openClipboard);
        a.put(KeyCombination.keyCombination("ctrl+alt+v"), this::openClipboard);
        a.put(KeyCombination.keyCombination("ctrl+shift+o"), this::toggleOverlay);
        a.put(KeyCombination.keyCombination("ctrl+alt+o"), this::toggleOverlay);
        // Play/pause
        Runnable playStop = () -> {
            if (startSpeakingButton.isVisible() && !startSpeakingButton.isDisable()) onSpeakPressed();
            else onStopSpeakingPressed();
        };
        a.put(KeyCombination.keyCombination("ctrl+enter"), playStop);
        a.put(KeyCombination.keyCombination("ctrl+p"), playStop);
        a.put(KeyCombination.keyCombination("ctrl+space"), playStop);
        a.put(KeyCombination.keyCombination("ctrl+s"), playStop);

        a.put(KeyCombination.keyCombination("ctrl+v"), () -> {
            if (!textArea.isEditable()) openClipboard();
        });
        // Play/stop on space IF not editable
        stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, (event) -> {
            if (event.getCode() == KeyCode.SPACE && !textArea.isEditable()) {
                playStop.run();
                event.consume();
            }
        });
    }
}
