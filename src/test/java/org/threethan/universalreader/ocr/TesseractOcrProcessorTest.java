package org.threethan.universalreader.ocr;

import org.threethan.universalreader.TestImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class TesseractOcrProcessorTest {
    TesseractOcrProcessor ocrProcessor;
    // Credit to @Martin on StackOverflow for this solution https://stackoverflow.com/questions/631598/
    CountDownLatch latch;
    @BeforeEach
    void setUp() {
        ocrProcessor = new TesseractOcrProcessor();
        latch = new CountDownLatch(1);
    }

    @Test
    @DisplayName("Test Quick-Brown-Fox")
    void testImage1() throws InterruptedException {
        BufferedImage image = TestImage.get("quick_brown_fox.png");
        OcrProcessor.Request request = new OcrProcessor.Request(image, Locale.ENGLISH);
        AtomicReference<OcrProcessor.Result> result = new AtomicReference<>();
        ocrProcessor.submitRequest(request, result1 -> {
            result.set(result1);
            latch.countDown();
        });
        latch.await();
        assertNotNull(result.get());
        assertEquals(TestImage.QUICK_BROWN_FOX_TEXT, result.get().ocrText());
    }
    @Test
    @DisplayName("Test Sphinx-Of-Black")
    void testImage2() throws InterruptedException {
        BufferedImage image = TestImage.get("sphinx_of_black.png");
        OcrProcessor.Request request = new OcrProcessor.Request(image, Locale.ENGLISH);
        AtomicReference<OcrProcessor.Result> result = new AtomicReference<>();
        ocrProcessor.submitRequest(request, result1 -> {
            result.set(result1);
            latch.countDown();
        });
        latch.await();
        assertNotNull(result.get());
        assertEquals(TestImage.SPHINX_OF_BLACK_TEXT, result.get().ocrText());
    }

    @Test
    @DisplayName("Test Multiple Concurrent Images")
    void testMultiple() throws InterruptedException {
        latch = new CountDownLatch(2);

        BufferedImage image1 = TestImage.get(TestImage.QUICK_BROWN_FOX_FILE);
        OcrProcessor.Request request1 = new OcrProcessor.Request(image1, Locale.ENGLISH);
        AtomicReference<OcrProcessor.Result> result1 = new AtomicReference<>();
        ocrProcessor.submitRequest(request1, r -> {
            result1.set(r);
            latch.countDown();
        });

        BufferedImage image2 = TestImage.get(TestImage.SPHINX_OF_BLACK_FILE);
        OcrProcessor.Request request2 = new OcrProcessor.Request(image2, Locale.ENGLISH);
        AtomicReference<OcrProcessor.Result> result2 = new AtomicReference<>();
        ocrProcessor.submitRequest(request2, r -> {
            result2.set(r);
            latch.countDown();
        });

        latch.await();

        assertNotNull(result1.get());
        assertNotNull(result2.get());

        assertEquals(TestImage.QUICK_BROWN_FOX_TEXT, result1.get().ocrText());
        assertEquals(TestImage.SPHINX_OF_BLACK_TEXT, result2.get().ocrText());
    }
}