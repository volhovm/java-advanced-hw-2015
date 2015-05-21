package ru.ifmo.ctddev.volhov.progressbar;

import javax.swing.*;
import java.awt.*;

/**
 * @author volhovm
 *         Created on 5/20/15
 */
public class Test {
    public static void main(String[] args)
            throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        JFrame frame = new JFrame("MyFrame");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(new JButton("mda memi"));
        panel.add(new JButton("heh"));
        panel.add(new JButton("hehaeouaoeuaoeu"));
        frame.setContentPane(panel);
        JOptionPane.showConfirmDialog(frame, "Heh", null, 1);
        frame.pack();
        frame.setVisible(true);
    }
}
