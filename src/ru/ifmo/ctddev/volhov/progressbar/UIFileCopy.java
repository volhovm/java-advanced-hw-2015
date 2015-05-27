package ru.ifmo.ctddev.volhov.progressbar;

import oracle.jrockit.jfr.JFR;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * @author volhovm
 *         Created on 5/21/15
 */
public class UIFileCopy extends JPanel implements ActionListener, PropertyChangeListener {
    private CopyTask task;
    private JFrame frame;
    private JPanel panel;
    private JLabel title, uptime, estimated, average, current;
    private JProgressBar progressBar;
    private JPanel statPanel;
    private JButton cancelButton;

    public UIFileCopy(String from, String to) {
        Path fromP, toP;
        fromP = Paths.get(from);
        toP = Paths.get(to);
        task = new CopyTask(fromP, toP);
        frame = new JFrame("UIFileCopy");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//        System.setProperty("awt.useSystemAAFontSettings","on");
//        System.setProperty("swing.aatext", "true");
//        try {
//            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
//        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
//            e.printStackTrace();
//        }
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        title = new JLabel("Copying from in to out");
        panel.add(title, BorderLayout.PAGE_START);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new EmptyBorder(5, 5, 5, 5));
        title.setBorder(BorderFactory.createLineBorder(Color.BLUE));
//        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        statPanel = new JPanel();
        statPanel.setLayout(new BoxLayout(statPanel, BoxLayout.PAGE_AXIS));
        statPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.add(statPanel);
        panel.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        statPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        progressBar = new JProgressBar(0, 100);
        statPanel.add(progressBar);
        progressBar.setValue(34);
        progressBar.setBorder(new EmptyBorder(5, 5, 5, 5));
        progressBar.setStringPainted(true);

        uptime = new JLabel("Uptime: aoeuaoeu");
        statPanel.add(uptime);
        uptime.setAlignmentX(Component.LEFT_ALIGNMENT);
        uptime.setHorizontalAlignment(SwingConstants.LEFT);

        estimated = new JLabel("Estimated: aoeu");
        statPanel.add(estimated);
        estimated.setAlignmentX(Component.LEFT_ALIGNMENT);

        average = new JLabel("Average: aoeu");
        statPanel.add(average);

        current = new JLabel("Current: aoeu");
        statPanel.add(current);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand("cancel");
        cancelButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        panel.add(cancelButton, BorderLayout.PAGE_END);

        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        frame.setContentPane(panel);
    }

    void run() {
        frame.pack();
        frame.setVisible(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        task.addPropertyChangeListener(this);
        task.execute();
    }

    public static void main(String[] args) {
//        args = new String[]{"A", "B"};
        UIFileCopy copier = new UIFileCopy(args[0], args[1]);
        copier.run();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getActionCommand().equals("cancel")) {
            JOptionPane.showMessageDialog(frame, "Closing application.");
            task.cancel(true);
            System.exit(0);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if ("progress".equals(propertyChangeEvent.getPropertyName())) {
            int progress = (Integer) propertyChangeEvent.getNewValue();
            progressBar.setValue(progress);
        }
    }

    class CopyTask extends SwingWorker<Void, Void> {
        private Path from, to;

        public CopyTask(Path from, Path to) {
            this.from = from;
            this.to = to;
        }

        @Override
        protected Void doInBackground() throws Exception {
            System.out.println(new File(".").getAbsolutePath());
            try (BufferedInputStream sin = new BufferedInputStream(new FileInputStream(from.toFile()));
                 BufferedOutputStream sout = new BufferedOutputStream(new FileOutputStream(to.toFile()));
            ) {
                byte[] buf = new byte[50];
                int got = 0;
                long started = System.currentTimeMillis();
                long prev = System.currentTimeMillis();
                double size = from.toFile().length();
                double initsize = size;
                double aver = -1;
                while (got != -1) {
                    got = sin.read(buf);
                    size -= got;
                    sout.write(buf, 0, got);
                    Thread.sleep(300);
                    long temp = System.currentTimeMillis();
                    double speed = got * 1000 / (temp - prev);
                    int progress  = (int) (Math.floor((initsize - size) / initsize * 100));
                    setProgress(progress);
                    aver = aver == -1 ? speed : (aver + speed) / 2;
                    uptime.setText(   "Uptime:    " + String.valueOf((temp - started) / 1000) + "s.");
                    current.setText(  "Current:   " + String.valueOf((int) speed) + " b/s");
                    average.setText(  "Average:   " + String.valueOf((int) aver) + " b/s");
                    estimated.setText("Estimated: " + String.valueOf((int) (size / aver)) + "s.");
                    prev = System.currentTimeMillis();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            setProgress(10);
            while (getProgress() < 100) {
                Thread.sleep(300);
                setProgress(Math.min(100, getProgress() + 5));
            }
            return null;
        }

        @Override
        protected void done() {
            super.done();
            progressBar.setString("Done");
            cancelButton.setEnabled(false);
        }

        private class UIFileVisitor implements FileVisitor {
            @Override
            public FileVisitResult preVisitDirectory(Object o, BasicFileAttributes basicFileAttributes)
                    throws IOException {
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
}
