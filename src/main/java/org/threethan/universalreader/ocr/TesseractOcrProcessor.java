package org.threethan.universalreader.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.function.Consumer;
/**
 * An implementation of OcrProcessor which uses Tesseract to extract text from images.
 * <p/>
 * It also automatically downloads a model for tesseract based on locale, with some error-checking.
 * @author Ethan Medeiros
 */
@SuppressWarnings("FieldCanBeLocal")
public class TesseractOcrProcessor implements OcrProcessor {
    private static final String DEFAULT_LANG = "eng";
    private static final Logger logger = LoggerFactory.getLogger(TesseractOcrProcessor.class);

    private final String DATA_PATH = "./TesseractData/";
    private final String DATA_EXT = ".traineddata";
    private final String DATA_URL = "https://github.com/tesseract-ocr/tessdata_best/raw/main/";

    @Override
    public void submitRequest(Request request, Consumer<Result> responseConsumer) {
        new Thread(() -> {
            try {
                responseConsumer.accept(new Result(
                        getTesseract(request.locale()).doOCR(request.image()).strip()
                ));
            } catch (Exception e) {
                logger.error("Tesseract had an unrecoverable error: {}", e.getMessage());
                responseConsumer.accept(new Result("Tesseract OCR had an error!"));
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Gets a Tesseract OCR instance, performing necessary setup & prep
     * @param locale Preferred locale for OCR
     * @return A new  Tesseract instance
     */
    private Tesseract getTesseract(Locale locale) throws  TesseractException {
        String language;
        try {
            language = locale.getISO3Language();
        } catch (MissingResourceException | NullPointerException e) {
            logger.warn("Missing resource for locale {}, will default to {}.", locale, DEFAULT_LANG);
            language = DEFAULT_LANG;
        }
        return getTesseract(language);
    }
    /**
     * Gets a Tesseract OCR instance, performing necessary setup & prep (such as download a model if needed)
     * @param language Preferred language for OCR
     * @return A new Tesseract instance
     */
    private Tesseract getTesseract(String language) throws TesseractException {
        // Download language
        String fromUrl = DATA_URL + language + DATA_EXT;
        String toPath = DATA_PATH + language + DATA_EXT;
        File dataDir = new File(DATA_PATH);
        //noinspection ResultOfMethodCallIgnored
        dataDir.mkdir();
        // Download the file with various fallbacks
        if (!downloadFileIfNotExists(fromUrl, toPath)) {
            if (language.equals(DEFAULT_LANG)) {
                logger.warn("Default language {} also failed...", DEFAULT_LANG);
                // Look for any valid file in the directory
                boolean foundFallback = false;
                for (File file : Objects.requireNonNull(dataDir.listFiles())) {
                    if (file.getName().endsWith(DATA_EXT)) {
                        logger.warn("...but found {} to use as a final fallback.", file.getName());
                        //noinspection ReassignedVariable
                        language = file.getName().replace(DATA_EXT, "");
                        foundFallback = true;
                        break;
                    }
                }
                if (!foundFallback) {
                    logger.warn("...and no usable files were found in {}, so OCR cannot be performed!", DATA_PATH);
                    throw new TesseractException("Completely failed to find a usable language data file");
                }
            } else {
                logger.warn("Downloading trained date for {} failed, trying {} as a fallback", language, DEFAULT_LANG);
                return getTesseract(DEFAULT_LANG);
            }
        }

        // Setup and return  Tesseract instance
        Tesseract tesseract = new  Tesseract();
        tesseract.setDatapath(DATA_PATH);
        tesseract.setLanguage(language);
        return  tesseract;
    }

    /**
     * Downloads a file if it doesn't exist
     * @param fromUrl Web URL to download from
     * @param toPath Relative file path to download to
     * @return True if file exists previously or after downloading, false if non-existent and download failed
     */
    private boolean downloadFileIfNotExists(String fromUrl, String toPath) {
        final File target = new File(toPath);
        if (target.exists()) return true;
        // Credit: https://www.baeldung.com/java-download-file
        try (BufferedInputStream in = new BufferedInputStream(URI.create(fromUrl).toURL().openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(toPath)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1)
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            return true;
        } catch (IOException e) {
            logger.error("Exception when downloading {}: {}", fromUrl, e.getMessage());
            return false;
        }
    }
}
