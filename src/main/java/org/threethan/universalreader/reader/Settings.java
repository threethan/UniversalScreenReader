package org.threethan.universalreader.reader;

import org.threethan.universalreader.lib.FileMap;

import java.io.File;
import java.io.Serializable;

/**
 * Wrapper for FileMap which maps enumerated preferences to serializable objects;
 * stores all preferences for the application.<br>
 * Since it's only read from file one window creation, windows can have independent settings,
 * and new windows will inherit the most recently used settings
 *
 * @author Ethan Medeiros
 */
public class Settings extends FileMap<Settings.Pref, Serializable> {
    /**
     * Enumerated list of all possible preferences
     */
    public enum Pref {
        EDITABLE, // boolean
        VOICE_NAME, // string
        VOICE_SPEED, // VoiceSpeed
        WATCH_CLIPBOARD_IMAGE, // boolean
        WATCH_CLIPBOARD_TEXT, // boolean
        AUTO_SPEAK, // boolean
        ALWAYS_ON_TOP, // boolean
        USE_OVERLAY, // boolean
        IMAGE_BEHIND_TEXT, // boolean
        SPEAK_IN_PARTS, // boolean

        // Window settings, all doubles
        WIN_X,
        WIN_Y,
        WIN_W,
        WIN_H,
        OVL_X,
        OVL_Y,
    }

    /** Default file to store settings in */
    final static File file = new File(System.getProperty("user.home"), ".OCR-Reader-Settings");
    /** Constructs an OcrSettings instance using the default settings file (~/.OCR-Reader-Settings) */
    public Settings() {
        super(file);
    }
    /** Constructs an OcrSettings instance using a provided settings file */
    public Settings(File file) {
        super(file);
    }
}
