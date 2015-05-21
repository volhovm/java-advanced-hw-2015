package ru.ifmo.ctddev.volhov.progressbar;

import oracle.jrockit.jfr.JFR;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author volhovm
 *         Created on 5/21/15
 */
public class UIFileCopy implements ActionListener {
    private Path from, to;
    private JFrame frame;
    private JPanel panel;
    private JLabel title;
    private JProgressBar progressBar;
    private JPanel statPanel;
    private JButton cancelButton;

    public UIFileCopy(String from, String to) {
        this.from = Paths.get(from);
        this.to = Paths.get(to);
        frame = new JFrame("UIFileCopy");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//        System.setProperty("awt.useSystemAAFontSettings","on");
//        System.setProperty("swing.aatext", "true");
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        title = new JLabel("Copying from in to out");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(5, 5, 5, 5));
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(34);
//        progressBar.setBorder(new EmptyBorder(5, 5, 5, 5));
        statPanel = new JPanel();
        statPanel.setLayout(new GridLayout(2, 2, 5, 5));
        statPanel.add(new JLabel("Lol"));
        statPanel.add(new JLabel("Lol1"));
        statPanel.add(new JLabel("Lol2"));
        statPanel.add(new JLabel("Lol3"));
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand("cancel");
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(title);
        panel.add(progressBar);
        panel.add(statPanel);
        panel.add(cancelButton);
//        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
//        panel.setAlignmentY(Component.CENTER_ALIGNMENT);
        frame.setContentPane(panel);
    }

    void run() {
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        args = new String[]{"A", "B"};
        UIFileCopy copier = new UIFileCopy(args[0], args[1]);
        copier.run();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getActionCommand().equals("cancel")) {
            JOptionPane.showMessageDialog(frame, null);
            // cancel
        }
    }

    static class UIFileVisitor implements FileVisitor {
        @Override
        public FileVisitResult preVisitDirectory(Object o, BasicFileAttributes basicFileAttributes) throws IOException {
            return null;
        }

        @Override
        public FileVisitResult visitFile(Object o, BasicFileAttributes basicFileAttributes) throws IOException {
            return null;
        }

        @Override
        public FileVisitResult visitFileFailed(Object o, IOException e) throws IOException {
            return null;
        }

        @Override
        public FileVisitResult postVisitDirectory(Object o, IOException e) throws IOException {
            return null;
        }
    }
}
