package org.threethan.universalreader.reader;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import org.threethan.universalreader.lib.ClipboardMonitorThread;
import org.threethan.universalreader.helper.SpeechHelper;
import org.threethan.universalreader.ocr.OcrProcessor;
import org.threethan.universalreader.ocr.TesseractOcrProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * In instance of a text viewer which can:
 * monitor the clipboard,
 * submit to the processor,
 * handle speech settings,
 * and speak text
 *
 * @author Ethan Medeiros
 */
public class Reader {
    /** The current text */
    protected String currentText;

    /** OCR settings instance for this window.
     * New windows spawn with the last-used settings,
     * but open windows' settings may be set independently. */
    final Settings settings = new Settings();

    /** Handles clipboard images changes as needed */
    private static final ClipboardMonitorThread clipboardMonitorThread = new ClipboardMonitorThread();
    static { clipboardMonitorThread.start(); }
    private final Runnable onClipboardContentChanged = this::clipboardContentChanged;

    /** Used to auto-open clipboard images, determine when to fully stop */
    private static final List<Reader> OPEN_READERS = new ArrayList<>();

    /** The implementation and instance of ocrProcessor used by the viewer */
    private final OcrProcessor ocrProcessor = new TesseractOcrProcessor();

    /** Constructs a new Viewer, adds it to the list of open viewers, and binds it to the clipboard monitor */
    public Reader() {
        OPEN_READERS.add(this);
        clipboardMonitorThread.getUpdateActions().add(onClipboardContentChanged);
    }

    /** Stop the viewer & clean up, similar to C++ destructor but must be called manually */
    public void destroy() {
        OPEN_READERS.remove(this);
        clipboardMonitorThread.getUpdateActions().remove(onClipboardContentChanged);
        // If the last window is closing
        if (OPEN_READERS.isEmpty()) {
            SpeechHelper.stop();
            clipboardMonitorThread.done();
        }
    }

    /**
     * Sets the image currently opened by this controller, and submits it to the OCR Processor.
     * @param image awt image
     */
    public void submitImage(Image image) {
        // Submit image to server (or local processor)
        OcrProcessor.Request request = new OcrProcessor.Request(
                SwingFXUtils.fromFXImage(image, null),
                SpeechHelper.getLocale(getVoiceName())
        );
        ocrProcessor.submitRequest(request, this::processResponse);
    }

    /**
     * Process a response from the server, setting OCRed text and so on
     * @param response Response from the server
     */
    protected void processResponse(@SuppressWarnings("ClassEscapesDefinedScope") OcrProcessor.Result response) {
        currentText = response.ocrText();
        onTextChanged();
    }

    /**
     * (Must be) Called when text in the main area is changed,
     * updates button states
     */
    protected void onTextChanged() {
        if (currentText == null) currentText = "";
    }

    /** Called by the clipboard monitor whenever the copied image has changed (or ceased being an image) */
    protected void clipboardContentChanged() {
        // Auto-open clipboard image if setting is enabled
        // Opens only if this is the most recently opened window
        if (this.equals(OPEN_READERS.get(OPEN_READERS.size()-1))) {
            final Clipboard cb = Clipboard.getSystemClipboard();
            if (cb.hasImage() &&
                    (boolean) settings.getOrDefault(Settings.Pref.WATCH_CLIPBOARD_IMAGE, false))
                openClipboard();
            if (cb.hasString() &&
                    (boolean) settings.getOrDefault(Settings.Pref.WATCH_CLIPBOARD_TEXT, false))
                openClipboard();
        }
    }

    /**
     * Sets the current voice name
     * @param voiceName Name of the voice
     */
    void setVoiceName(String voiceName) {
        settings.put(Settings.Pref.VOICE_NAME, voiceName);
        speak(voiceName);
    }
    /** Gets the current voice name */
    protected String getVoiceName() {
        return (String) settings.getOrDefault(Settings.Pref.VOICE_NAME, null);
    }

    /**
     * Sets the current voice speed
     * @param voiceSpeed Speed of the voice (object)
     */
    void setVoiceSpeed(SpeechHelper.VoiceSpeed voiceSpeed) {
        settings.put(Settings.Pref.VOICE_SPEED, voiceSpeed);
        speak(voiceSpeed.toString());
    }
    /**
     * Gets the current voice speed
     */
    @SuppressWarnings("ClassEscapesDefinedScope")
    protected SpeechHelper.VoiceSpeed getVoiceSpeed() {
        return (SpeechHelper.VoiceSpeed)
                settings.getOrDefault(Settings.Pref.VOICE_SPEED, SpeechHelper.VoiceSpeed.DEFAULT);
    }

    /**
     * Speak a string with current settings for this window
     * @param text Text to speak
     */
    protected void speak(String text) {
        SpeechHelper.speak(text, getVoiceName(), getVoiceSpeed());
    }

    /** Opens the image or text currently in the clipboard */
    protected void openClipboard() {
        final Clipboard cb = Clipboard.getSystemClipboard();
        if (cb.hasImage()) submitImage(cb.getImage());
        else processResponse(new OcrProcessor.Result(cb.getString()));
    }
}
