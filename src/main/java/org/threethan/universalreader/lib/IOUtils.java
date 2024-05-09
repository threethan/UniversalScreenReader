package org.threethan.universalreader.lib;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

/**
 * A helper class which provides useful static functions for saving and loading
 * serialized objects to and from files, optionally using JavaFX.
 * @author Ethan Medeiros
 */
@SuppressWarnings("unused")
public abstract class IOUtils {
    /**
     * List of supported extensions for image files
     */
    private static final String[] IMAGE_EXTENSIONS = {"*.png","*.gif","*.jpg","*.jpeg"};
    /**
     * Saves a serializable object to a file & notifies the user
     * @param object Object to save
     * @param filename Name of the file (path assumed relative
     */
    public static void save(String filename, Serializable object) {
        save(new File(filename), object);
    }

    /**
     * Saves a serializable object to a file & notifies the user
     * @param object Object to save
     * @param file File to save to
     */
    public static void save(File file, Serializable object) {
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos)  ) {
            oos.writeObject(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads a serialized object from a file
     * @param filename Name of the file (path assumed relative)
     * @return Serializable object from file (Null if invalid)
     */
    public static <T extends Serializable> T load(String filename) {
        return load(new File(filename));
    }

    /**
     * Loads a serialized object from a file
     * @param file File to load
     * @return Serializable object from file (null if invalid)
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T load(File file) {
        if (file == null || !file.exists()) return null;
        try (   FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis) ) {
            return (T) ois.readObject();
        } catch (ClassCastException | ClassNotFoundException | IOException e) {
            System.out.println("Exception while loading object from file: "+ e);
            return null;
        }
    }

    /**
     * Adds extension filters to a file chooser based on a list of extension strings
     * @param fileChooser File chooser to manipulate
     * @param extensions Extension for the filter (don't include period, star for none)
     */
    public static void addExtensionFilters(FileChooser fileChooser, String... extensions) {
        for(String extension : extensions) {
            if (extension.equals("*")) {
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("File", "*"));
            } else {
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter(extension + " File", "*." + extension.toLowerCase()));
            }
        }
    }

    /**
     * Saves an object with a prompt to the user
     * @param stage JavaFX stage
     * @param object Serializable Object
     * @param defaultName Default name for the file
     * @param extensions Extension for the file (don't include period, star for none)
     */
    public static void saveInteractive(Stage stage, Serializable object, String defaultName, String... extensions) {
        final String defaultExt = extensions.length>0 ? extensions[0] : "";
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save "+defaultExt+" File");
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.setInitialFileName(defaultName);
        addExtensionFilters(fileChooser, extensions);
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return;
        if (extensions.length == 1 && !file.getPath().endsWith("."+defaultExt.toLowerCase()))
            file = new File(file.getPath() + "."+defaultExt.toLowerCase());
        IOUtils.save(file, object);
    }

    /**
     * Loads an object with a prompt to the user
     * @param stage JavaFX stage
     * @param extensions Extension for the file (don't include period, star for none)
     * @return Loaded serializable object (Null if invalid)
     */
    public static <T extends Serializable> T loadInteractive(Stage stage, String... extensions) {
        final String defaultExt = extensions.length>0 ? extensions[0] : "";
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open "+defaultExt+" File");
        fileChooser.setInitialDirectory(new File("."));
        addExtensionFilters(fileChooser, extensions);
        File file = fileChooser.showOpenDialog(stage);
        return load(file);
    }

    /**
     * Loads a javaFX image from a fileChooser
     * @param stage JavaFX stage
     * @return JavaFX image from chosen file (null if cancelled)
     */
    public static Image loadImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image from File");
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image File", IMAGE_EXTENSIONS)
        );
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) return null;
        return new Image(file.toURI().toString());
    }
}
