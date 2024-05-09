package org.threethan.universalreader.reader;

import javafx.embed.swing.SwingFXUtils;
import org.threethan.universalreader.TestImage;
import org.threethan.universalreader.ocr.OcrProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReaderTest {
    TestReader viewer;
    CountDownLatch responseLatch;

    private class TestReader extends Reader {
        public boolean hasTextChanged = false;

        @Override
        protected void processResponse(OcrProcessor.Result response) {
            super.processResponse(response);
            responseLatch.countDown();
        }

        @Override
        protected void onTextChanged() {
            super.onTextChanged();
            hasTextChanged = true;
        }

        public String getText() {
            return currentText;
        }
    }

    @BeforeEach
    void setUp() {
        viewer = new TestReader();
        responseLatch = new CountDownLatch(1);
    }

    @AfterEach
    void tearDown() {
        viewer.destroy();
    }

    @Test
    @DisplayName("Test start and stop work")
    void startStop() {
    } // Empty, just calls setUp and tearDown


    @Test
    @DisplayName("Test submission correctly calls processResponse, changes text, and sets text")
    void testSubmission() throws InterruptedException {
        viewer.submitImage(
                SwingFXUtils.toFXImage(TestImage.get(TestImage.QUICK_BROWN_FOX_FILE), null)
        );
        responseLatch.await();
        assertTrue(viewer.hasTextChanged);
        assertEquals(TestImage.QUICK_BROWN_FOX_TEXT, viewer.getText());
    }
}