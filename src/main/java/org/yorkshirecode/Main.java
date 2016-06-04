package org.yorkshirecode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);



    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            LOG.error("Error setting L&F", ex);
        }

        Frame f = new Frame();
        f.setVisible(true);
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);

        //todo - save series to images

    }
}
