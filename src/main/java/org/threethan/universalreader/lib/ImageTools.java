package org.threethan.universalreader.lib;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Helper class for quickly comparing images
 * @author Ethan Medeiros
 * */
public abstract class ImageTools {
    /**
     * Converts an image to a buffered image (or casts it if it is already)
     * @param image Java awt image
     * @return Same image, different type
     */
    public static BufferedImage toBuffered(Image image) {
        if (image instanceof BufferedImage bufferedImage) return bufferedImage;
        BufferedImage newImage = new BufferedImage(
                image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return newImage;
    }

    /**
     * Quickly checks if two awt BufferedImages are identical. May produce false positives, but not false negatives!<br>
     * Tested to take ~0.04ms with 128 samples on Ryzen 5 3600
     * @param a First image
     * @param b Second image
     * @return True if images are probably identical
     */
    public static boolean isIdenticalFast(BufferedImage a, BufferedImage b) {
        if (a == null || b == null) return false;
        final int w = a.getWidth();
        final int h = a.getHeight();
        // If dimensions don't match, images are necessarily different
        //noinspection DuplicatedCode
        if (w != b.getWidth() || h != b.getHeight()) return false;
        // Sample some arbitrary points on the image and check if they're identical
        final int N_SAMPLES = 128; // Number of points to sample
        final float ym = 6.9F; // # should be relatively aperiodic for best result
        final float xm = 5.1F; // # should be relatively aperiodic for best result
        final float dx = (w * xm / (N_SAMPLES+1));
        final float dy = (h * ym / (N_SAMPLES+1));
        // Sample N_SAMPLES points in looping diagonals across the image,
        //  which should provide a semi-random but distributed set of samples
        //   with which to compare the images
        for (int i = 0; i < N_SAMPLES; i++) {
            final int y = (int)(i * dy) % h;
            final int x = (int) ((((int)(i * dy / h)) + dx * i) % w);
            if (a.getRGB(x,y) != b.getRGB(x,y)) return false;
        }
        return true;
    }
}