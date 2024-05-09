package org.threethan.universalreader.lib;

import org.threethan.universalreader.TestImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageCompareTest {

    @Test
    @DisplayName("Test images of different objects")
    void testImagesOfObjects() {
        final BufferedImage img1A = TestImage.get("baboon.png");
        final BufferedImage img1B = TestImage.get("baboon_copy.png");
        final BufferedImage img2  = TestImage.get("fruits.png");
        assertTrue(ImageTools.isIdenticalFast(img1A, img1B));
        assertFalse(ImageTools.isIdenticalFast(img1A, img2));
        assertFalse(ImageTools.isIdenticalFast(img1B, img2));
    }

    @Test
    @DisplayName("Test images of different text")
    void testImagesOfTexts() {
        final BufferedImage img1A = TestImage.get("quick_brown_fox.png");
        final BufferedImage img1B = TestImage.get("quick_brown_fox_copy.png");
        final BufferedImage img2  = TestImage.get("sphinx_of_black.png");
        assertTrue(ImageTools.isIdenticalFast(img1A, img1B));
        assertFalse(ImageTools.isIdenticalFast(img1A, img2));
        assertFalse(ImageTools.isIdenticalFast(img1B, img2));
    }

    @Test
    @DisplayName("Test differently sized blank images")
    void testImagesOfNothing() {
        final BufferedImage img1 = TestImage.get("white128.png");
        final BufferedImage img2 = TestImage.get("white256.png");
        assertFalse(ImageTools.isIdenticalFast(img1, img2));
    }

    @RepeatedTest(8)
    @DisplayName("Test performance of individual comparisons (Must be <5ms each)")
    void testPerformance() {
        final BufferedImage img1A = TestImage.get("quick_brown_fox.png");
        final BufferedImage img1B = TestImage.get("quick_brown_fox_copy.png");
        final long startTime = System.nanoTime();
        ImageTools.isIdenticalFast(img1A, img1B);
        final long endTime = System.nanoTime();
        final long MAX_NS = 5000000; // 5 ms
        assertTrue(endTime < startTime + MAX_NS);
    }

    @RepeatedTest(4)
    @DisplayName("Test performance of 100 ops (Must be < 10ms total)")
    void testPerformance100() {
        final BufferedImage img1A = TestImage.get("quick_brown_fox.png");
        final BufferedImage img1B = TestImage.get("quick_brown_fox_copy.png");
        final long startTime = System.nanoTime();

        for (int i = 0; i < 100; i++) ImageTools.isIdenticalFast(img1A, img1B);

        final long endTime = System.nanoTime();
        final long MAX_NS = 1000000*10; // 10 ms
        System.out.println("Completed 100 approximate image comparisons in "
                + ((endTime - startTime)/1000000) + "ms");
        assertTrue(endTime < startTime + MAX_NS);
    }


}