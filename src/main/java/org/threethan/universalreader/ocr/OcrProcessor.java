package org.threethan.universalreader.ocr;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * A generic interface which takes an images and returns text to a consumer.
 * One implementation will be used to communicate with the server,
 * while another will be used on the server to actually handle requests.
 * @author Ethan Medeiros
 * */
public interface OcrProcessor {
    /**
     * A response from the server
     * @param ocrText Text extracted from the image
     */
    record Result(String ocrText) implements Serializable {}

    /**
     * A request from the client
     * @param image Image to submit
     * @param locale Preferred locale
     */
    record Request(BufferedImage image, Locale locale) implements Serializable {}

    /**
     * Submits an image to the server
     * @param request Request containing the image to analyze
     * @param responseConsumer Response from server (contains OCRed text)
     */
    void submitRequest(Request request, Consumer<Result> responseConsumer);
}