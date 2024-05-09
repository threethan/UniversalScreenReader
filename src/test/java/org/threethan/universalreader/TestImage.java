package org.threethan.universalreader;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.util.Objects;

/** Helper class to retrieve images used by multiple unit tests */
public abstract class TestImage {
    public static final String QUICK_BROWN_FOX_TEXT = "The quick brown fox\njumped over the lazy dog";
    public static final String SPHINX_OF_BLACK_TEXT = "Sphinx of black quartz,\naccept my vow";
    public static final String QUICK_BROWN_FOX_FILE = "quick_brown_fox.png";
    public static final String SPHINX_OF_BLACK_FILE = "sphinx_of_black.png";

    /**
     * Gets a test image from its file name
     * @param imageName File name of the image in test/resources/images
     **/
    public static BufferedImage get(String imageName) {
        Image fxImage = new Image(
                Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("images/" + imageName)));
        return SwingFXUtils.fromFXImage(fxImage, null);
    }
}
