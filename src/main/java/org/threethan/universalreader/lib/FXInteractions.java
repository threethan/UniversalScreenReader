package org.threethan.universalreader.lib;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;

/**
 * Helper function(s) to add additional interactions to fx components
 * @author Ethan Medeiros
 */
public abstract class FXInteractions {

    /**
     * Allows the scroll wheel to control a combo-box
     * @param comboBox Combo-box for which to set up this interaction
     */
    public static <T> void makeScrollable(ComboBox<T> comboBox) {
        final double THRESH = 10;
        final double[] y = new double[1];
        comboBox.setOnScroll(e -> {
            int index = comboBox.getSelectionModel().getSelectedIndex();
            y[0] += e.getDeltaY();
            if (y[0] > THRESH) index++;
            else if (y[0] < -THRESH) index--;
            else return;
            y[0] = 0;
            if (index > comboBox.getItems().size() || index < 0) return; // Bounded
            comboBox.getSelectionModel().select(index);
        });
    }

    /**
     * Centers a particular node in a scroll pane by scrolling
     * @param scrollPane ScrollPane to scroll
     * @param node Node to center
     * @author Håvard Geithus
     */
    // Credit: Stack user Håvard Geithus from https://stackoverflow.com/questions/15840513/
    public static void centerNodeInScrollPane(ScrollPane scrollPane, Node node) {
        double h = scrollPane.getContent().getBoundsInLocal().getHeight();
        double y = (node.getBoundsInParent().getMaxY() +
                node.getBoundsInParent().getMinY()) / 2.0;
        double v = scrollPane.getViewportBounds().getHeight();
        scrollPane.setVvalue(scrollPane.getVmax() * ((y - 0.5 * v) / (h - v)));
    }
}
