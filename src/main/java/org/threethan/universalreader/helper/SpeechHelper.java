package org.threethan.universalreader.helper;

import io.github.jonelo.jAdapterForNativeTTS.engines.SpeechEngine;
import io.github.jonelo.jAdapterForNativeTTS.engines.SpeechEngineNative;
import io.github.jonelo.jAdapterForNativeTTS.engines.Voice;
import io.github.jonelo.jAdapterForNativeTTS.engines.exceptions.SpeechEngineCreationException;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import org.threethan.universalreader.reader.Controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * Provides various helper functions to enable native text-to-speech functionality
 * using <a href="https://github.com/jonelo/jAdapterForNativeTTS">jAdapterForNativeTTS</a>.
 * @author Ethan Medeiros
 */
public abstract class SpeechHelper {
    /** Used to split lines & sentences for speak-by-parts. Must be a lookaround that doesn't directly match chars. */
    public static final String DELIM_REGEX = "((?<=([A-z|0-9\\t ,;:]{10}[!?.;|:]\\s)))|(?=\\n)";
    /** Matches characters that make a pause at the end of a line */
    private static final String LINE_END_PAUSE_REGEX = "\\n[.,;:\\n ]*|[.,;:\\n]{0,2}$";
    /** Matches characters that signify a slight pause that may be reduced */
    private static final String LINE_MID_PAUSE_REGEX = "[.,;:\\n] ";
    /** Matches special quotes characters which may cause issues on Windows */
    private static final String QUOTES_REGEX = "[‘’“”]";
    /** If there are more than this number of voices, variants will be hidden unless they match the current locale */
    public static final int SOFT_ENTRY_CAP = 200;
    /** Used to filter voices if there are > 100 of them (Such as on Ubuntu & similar) */
    private static String prevLang = "English (America)";
    /** True if there are too many entries to reasonably show at once */
    public static boolean collapseEntries = false;

    private static final List<Voice> FALLBACK = new ArrayList<>();
    private static Runnable finishPartsRunnable = null;

    private static final BooleanProperty speakingProperty = new SimpleBooleanProperty();
    public static ReadOnlyBooleanProperty speakingProperty() {
        return speakingProperty;
    }

    static {
        Voice FV = new Voice();
        FV.setName("No voices found!");
        FALLBACK.add(FV);
    }
    /** The speech engine object to use */
    private static SpeechEngine speechEngine;
    static {
        try {
            speechEngine = SpeechEngineNative.getInstance();
            speechEngine.setRate(0); //-100: very slow   +100: very fast
        } catch (SpeechEngineCreationException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            speechEngine = null;
        }
    }

    /**
     * Represents a speed of tts voice playback
     * @param speed Rate passed to the tts executable (-100 to 100, No particular units)
     * @param string A string explaining the speed to the user
     * @param noPause Whether we should try and skip/shorten pauses such as sentence breaks
     * @author Ethan Medeiros
     */
    public record VoiceSpeed(int speed, String string, boolean noPause) implements Serializable {
        public static final VoiceSpeed DEFAULT = new VoiceSpeed(15, "Normal", false);
        public static final List<VoiceSpeed> SPEEDS = List.of(
                new VoiceSpeed(-25, "Slow", false),
                new VoiceSpeed(0, "Relaxed", false),
                DEFAULT,
                new VoiceSpeed(35, "Fast", true),
                new VoiceSpeed(50, "Faster", true),
                new VoiceSpeed(90, "Fastest", true)
        );
        @Override
        public String toString() {
            return string();
        }
    }

    /**
     * Speak a string out loud using with a particular voice and speed, split into parts
     * @param string String to speak aloud
     * @param name Name of the voice to use
     * @param voiceSpeed VoiceSpeed object for the speed of the voice
     * @param onPart Run for each part, with the start and end indices of the fragment
     */
    public static synchronized void speak(String string, String name, VoiceSpeed voiceSpeed, BiConsumer<Integer, Integer> onPart) {
        if (checkUnsupported()) return;
        stop();

        speechEngine.setRate(voiceSpeed.speed());
        setVoice(name);

        finishPartsRunnable = null;

        String[] parts = toParts(string);

        final AtomicReference<Runnable> speakNextPart = new AtomicReference<>();
        finishPartsRunnable = () -> {
            speakNextPart.set(() -> {});
            speakingProperty.set(false);
        };

        AtomicInteger partIndex = new AtomicInteger(0);
        AtomicInteger charIndex = new AtomicInteger(0);

        speakNextPart.set(() -> {
            if (partIndex.get() >= parts.length) {
                finishPartsRunnable.run();
            } else {
                int prevCharIndex = charIndex.get();
                String part = parts[partIndex.get()];
                charIndex.getAndAdd(part.length());
                Platform.runLater(() -> onPart.accept(prevCharIndex, charIndex.get()));
                if (part.isBlank()) Platform.runLater(speakNextPart.get());
                else {
                    if (voiceSpeed.noPause()) part = part.replaceAll(LINE_MID_PAUSE_REGEX, ", ");
                    speakInternal(part.replaceAll(LINE_END_PAUSE_REGEX, ""), () -> speakNextPart.get().run());
                }
            }
            partIndex.getAndAdd(1);
        });

        speakNextPart.get().run();
    }

    /**
     * Speak a string out loud using with a particular voice and speed
     * @param string String to speak aloud
     * @param name Name of the voice to use
     * @param voiceSpeed VoiceSpeed object for the speed of the voice
     */
    public static void speak(String string, String name, VoiceSpeed voiceSpeed) {
        if (checkUnsupported()) return;
        stop();
        speechEngine.setRate(voiceSpeed.speed());
        setVoice(name);
        if (voiceSpeed.noPause()) string = string.replaceAll(LINE_MID_PAUSE_REGEX,", ");
        speakInternal(string, () -> speakingProperty.set(false));
    }

    /**
     * Speak a string out loud using the current voice
     * @param string String to speak aloud
     * @param then Run when speech is done
     */
    private static void speakInternal(String string, Runnable then) {
        try {
            speakingProperty.set(true);
            Process process = speechEngine.say(string.replaceAll("\\$", " dollars ")
                    .replaceAll(QUOTES_REGEX, "\""));
            if (then != null) process.onExit().thenRun(() -> Platform.runLater(then));
        } catch (IOException e) {
            alert("Failed to speak: " + e.getLocalizedMessage());
        }
    }

    /** Sets the voice to the given name. If that voice isn't currently supported,
     * the voice is defaulted to the first option.
     * @param name Name of the voice to set (null to force default)
     */
    public static void setVoice(String name) {
        if (checkUnsupported()) return;
        if (speechEngine.getAvailableVoices().stream().anyMatch(voice -> voice.getName().equals(name))) {
            speechEngine.setVoice(name);
            prevLang = name.split("\\+")[0];
        } else speechEngine.setVoice(speechEngine.getAvailableVoices().get(0).getName());
    }

    /** Check that the speechEngine is OK */
    private static boolean checkUnsupported() {
        if (speechEngine == null) {
            alert("""
                    Text-to-speech could not start!
                    
                    If you're on Linux, make sure speech-dispatcher & pulseaudio are installed and working.

                    This program will only work if you can run the spd-say command successfully.

                    (This error may occur on computers/VMs without audio output devices.)""");
            return true;
        } else return false;
    }

    /** Stop talking immediately */
    public static synchronized void stop() {
        if (checkUnsupported()) return;
        speechEngine.stopTalking();
        if (finishPartsRunnable != null) finishPartsRunnable.run();
        speakingProperty.set(false);
    }

    /** Warn the user, including with a popup */
    private static void alert(String string) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Text-to-Speech Error");
        alert.setHeaderText("Critical Error");
        alert.setContentText(string);

        Scene scene = alert.getDialogPane().getScene();
        StyleHelper styleHelper = new StyleHelper(scene.getWindow());
        styleHelper.setStyles(scene);
        styleHelper.setBackgrounds(scene);

        if (Controller.getMostRecentController() != null)
            alert.initOwner(Controller.getMostRecentController().getStage());
        alert.initModality(Modality.WINDOW_MODAL);
        alert.show();
    }

    /** Get the list of all available voices */
    private static List<Voice> voiceListInternal() {
        return speechEngine == null ? FALLBACK : speechEngine.getAvailableVoices();
    }
    /** Get the list of relevant available voices */
    public static List<Voice> voiceList() {
        List<Voice> voiceListFull = voiceListInternal();
        voiceListFull.sort(Comparator.comparing(Voice::getCulture));
        if (voiceListFull.size() > SOFT_ENTRY_CAP) {
            collapseEntries = true;
            // Special case for linux with crazy number of voices
            List<Voice> voiceList = new LinkedList<>();
            Set<String> voiceLocaleSet = new HashSet<>();
            SpeechHelper.voiceListInternal().forEach(voice -> {
                if (voice.getName().contains(prevLang) &&
                        !voice.getName().replace("1","").matches(".*\\d")) {
                    voiceList.add(voice);
                    voiceLocaleSet.add(voice.getCulture());
                }
            });
            SpeechHelper.voiceListInternal().forEach(voice -> {
                if (!voiceLocaleSet.contains(voice.getCulture())) {
                    voiceList.add(voice);
                    voiceLocaleSet.add(voice.getCulture());
                }
            });
            return voiceList;
        } else {
            collapseEntries = false;
            return voiceListFull;
        }
    }
    /**
     * Get the list names of available voices
     */
    public static List<String> voiceNameList() {
        List<String> voiceNameList = new LinkedList<>();
        SpeechHelper.voiceListInternal().forEach(voice -> voiceNameList.add(voice.getName()));
        return voiceNameList;
    }

    /**
     * Gets a locale object representing a voice name
     * @param voiceName Name of the voice
     * @return Locale representing the voice, or null if failed
     */
    public static Locale getLocale(String voiceName) {
        if (checkUnsupported()) return null;
        final Voice[] voice = new Voice[1];
        voiceList().stream().filter(v1 -> Objects.equals(v1.getName(), voiceName)).findFirst().ifPresentOrElse(
                v2 -> voice[0] = v2,
                () -> voice[0] = null
        );
        if (voice[0] == null || voice[0].getCulture() == null) voice[0] = speechEngine.getAvailableVoices().get(0);
        return Locale.forLanguageTag(voice[0].getCulture());
    }
    /**
     * Gets a voice object representing a voice name
     * @param voiceName Name of the voice
     * @return Full voice object, or default voice if it isn't available
     */
    public static Voice getVoiceByName(String voiceName) {
        if (checkUnsupported()) return null;
        final Voice[] voice = new Voice[1];
        voiceList().stream().filter(v1 -> Objects.equals(v1.getName(), voiceName)).findFirst().ifPresentOrElse(
                v2 -> voice[0] = v2,
                () -> voice[0] = null
        );
        if (voice[0] == null) return speechEngine.getAvailableVoices().get(0);
        return voice[0];
    }

    /**
     * Splits text into a list of parts
     * @param text Full text
     * @return Text parts to be spoken separately
     */
    public static String[] toParts(String text) {
        return text.split(DELIM_REGEX);
    }
}
