package org.threethan.universalreader.lib;

import javafx.application.Platform;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A thread which regularly polls the system clipboard for images and text.
 * Note: Despite what people on stack say, flavorListener does not work consistently for this!
 *
 * @author Ethan Medeiros
 * */
public class ClipboardMonitorThread extends Thread {
    /** How frequently to check the clipboard, in ms */
    private static final long POLLING_INTERVAL = 250;
    private final Clipboard systemClipboard;
    private String prevText = null;

    /** Runnable action on updates */
    private final List<Runnable> updateActions = new ArrayList<>();
    /** The previous copied image, for comparing changes against */
    private BufferedImage prevCopiedImage = null;

    /**
     * Creates a new thread which monitors the clipboards
     * @param updateActions Called on the UI thread either when the thread polls and finds a change
     *                      in image or text content
     */
    public ClipboardMonitorThread(Runnable... updateActions) {
        this.updateActions.addAll(Arrays.asList(updateActions));
        this.systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    /**
     * Gets the current mutable list of actions to be
     * Called on the UI thread either when the thread polls and finds a change in image or text content
     * @return List of Runnable actions
     */
    public List<Runnable> getUpdateActions() {
        return updateActions;
    }

    /** Whether the thread is active. Set to false when it may be stopped. */
    private boolean active = true;

    /** Runs the thread and starts monitoring the clipboard; continues until done() is called */
    @Override
    public void run() {
        while (active) {
            try {
                BufferedImage copiedImage =
                        ImageTools.toBuffered((Image) systemClipboard.getData(DataFlavor.imageFlavor));
                if (!ImageTools.isIdenticalFast(copiedImage, prevCopiedImage)) {
                    prevCopiedImage = copiedImage;
                    updateActions.forEach(Platform::runLater);
                    prevText = null;
                }
            } catch (UnsupportedFlavorException | IOException e) {
                String copiedText = null;
                try {
                    copiedText = (String) systemClipboard.getData(DataFlavor.stringFlavor);
                } catch (UnsupportedFlavorException | IOException ignored) {}

                if (!Objects.equals(copiedText, prevText)) {
                    updateActions.forEach(Platform::runLater);
                    prevText = copiedText;
                }
            } catch (IllegalStateException ignored) {
                // Happens if we poll the clipboard too fast
            }
            try {
                //noinspection BusyWait
                Thread.sleep(POLLING_INTERVAL);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /** Ends the thread; a safer alternative to thread.stop() */
    public void done() {
        active = false;
    }
}
