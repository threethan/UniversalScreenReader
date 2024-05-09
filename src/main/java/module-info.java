module org.threethan.universalreader {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.datatransfer;
    requires java.desktop;
    requires javafx.swing;
    requires jAdapterForNativeTTS;
    requires com.jthemedetector;
    requires com.pixelduke.fxthemes;
    requires tess4j;
    requires org.slf4j;

    opens org.threethan.universalreader.reader to javafx.fxml;
    exports org.threethan.universalreader.reader;
    exports org.threethan.universalreader;
    opens org.threethan.universalreader to javafx.fxml;
}