package org.threethan.universalreader.reader;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.stage.Screen;
import org.threethan.universalreader.helper.SpeechHelper;
import org.threethan.universalreader.lib.MenuBuilder;

/**
 * Builds the settings menu for the main viewer application;
 * may vary depending on window mode and currently selected options
 *
 * @author Ethan Medeiros
 */
public class SettingsMenu {
    /** The viewerController which owns this menu */
    private final Controller controller;
    /** The currently open settings menu */
    private ContextMenu openSettingsMenu;

    /**
     * Constructs an instance of a viewer settings menu
     * @param controller The ViewerController this menu will be for
     */
    public SettingsMenu(Controller controller) {
        this.controller = controller;
    }

    /**
     * Builds the entire settings menu,
     * including contextual options based on the current state of the ViewerController
     * @return ContextMenu object of the settings menu, which then gets displayed by the ViewerController
     */
    public ContextMenu build() {
        MenuBuilder settingsMenuBuilder = new MenuBuilder("Settings");
        Settings settings = controller.settings;

        // Voice and speed selections (only show here in overlay mode)
        if (controller.isOverlay())
            settingsMenuBuilder
                    .addSelectionSetSubmenu("Voice",
                            SpeechHelper.voiceNameList(),
                            (String) settings.getOrDefault(Settings.Pref.VOICE_NAME, null),
                            controller::setVoiceName)
                    .addSelectionSetSubmenu("Speed",
                            SpeechHelper.VoiceSpeed.SPEEDS,
                            (SpeechHelper.VoiceSpeed) settings.getOrDefault(Settings.Pref.VOICE_SPEED,
                                                                            SpeechHelper.VoiceSpeed.DEFAULT),
                            controller::setVoiceSpeed).addSeparator();

        // Allow text editing option (only shows if window mode)
        if (!controller.isOverlay()) {
            settingsMenuBuilder.addToggle("Allow Text Editing",
                    (boolean) settings.getOrDefault(Settings.Pref.EDITABLE, false),
                    b -> {
                        controller.textArea.setEditable(b);
                        settings.put(Settings.Pref.EDITABLE, b);
                    });
        }

        // Window options (differs if overlay)
        if (!controller.isOverlay()) {
            settingsMenuBuilder.addSubmenu(new MenuBuilder("Window")
                    .addItem("New Window", e -> Application.newWindow(controller.getStage()))
                    .addSeparator()
                    .addItem("Switch to a Floating Overlay", e -> controller.toggleOverlay())
                    .addSeparator()
                    .addToggle("Keep on Top of Other Applications",
                            (boolean) settings.getOrDefault(Settings.Pref.ALWAYS_ON_TOP, false),
                            b -> {
                                controller.getStage().setAlwaysOnTop(b);
                                settings.put(Settings.Pref.ALWAYS_ON_TOP, b);
                            })
                    .addToggle("Show Blurred Image Behind Text",
                            (boolean) settings.getOrDefault(Settings.Pref.IMAGE_BEHIND_TEXT, false),
                            b -> {
                                settings.put(Settings.Pref.IMAGE_BEHIND_TEXT, b);
                                controller.openedImageView.setVisible(b);
                            })
            );
        } else {
            settingsMenuBuilder.addSubmenu(new MenuBuilder("Window")
                    .addItem("Switch Back to a Normal Window", e -> controller.toggleOverlay())
                    .addItem("Close the Floating Overlay", e -> controller.getStage().close())
                    .addSeparator());
        }

        // Automatically ___ section; which is always visible
        settingsMenuBuilder.addSubmenu(new MenuBuilder("Automatically")
                .addToggle("Open Image on Copy",
                        (boolean) settings.getOrDefault(Settings.Pref.WATCH_CLIPBOARD_IMAGE, false),
                        b -> settings.put(Settings.Pref.WATCH_CLIPBOARD_IMAGE, b))
                .addToggle("Open Text on Copy",
                        (boolean) settings.getOrDefault(Settings.Pref.WATCH_CLIPBOARD_TEXT, false),
                        b -> settings.put(Settings.Pref.WATCH_CLIPBOARD_TEXT, b))
                .addToggle("Speak Opened Text",
                        (boolean) settings.getOrDefault(Settings.Pref.AUTO_SPEAK, false),
                        b -> settings.put(Settings.Pref.AUTO_SPEAK, b))
        );

        return settingsMenuBuilder.buildContextMenu();

    }

    /**
     * Shows the settings menu, automatically positioned to be next to a javaFX view
     * @param settingsButton JavaFx node of the settings button to use for positioning
     */
    public void show(Node settingsButton) {
        if (openSettingsMenu != null) {
            // Hide the menu if it's showing
            openSettingsMenu.hide();
            openSettingsMenu = null;
            return;
        }
        ContextMenu settingsContextMenu = build();

        // Positioning
        Bounds boundsInScreen = settingsButton.localToScreen(settingsButton.getBoundsInLocal());
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        settingsContextMenu.show(settingsButton, boundsInScreen.getMinX(), boundsInScreen.getMaxY());
        final int SHADOW_LEN = 15;
        // Move menu up if it's hitting the bottom
        if (settingsContextMenu.getY() + settingsContextMenu.getHeight()-SHADOW_LEN > visualBounds.getMaxY())
            settingsContextMenu.setY(boundsInScreen.getMinY() - (settingsContextMenu.getHeight()-SHADOW_LEN) );
        // Move menu left if it's hitting the edge
        if (settingsContextMenu.getX() + settingsContextMenu.getWidth() > visualBounds.getMaxX())
            settingsContextMenu.setX(boundsInScreen.getMaxX() - (settingsContextMenu.getWidth() - SHADOW_LEN) );

        openSettingsMenu = settingsContextMenu;
    }
}
