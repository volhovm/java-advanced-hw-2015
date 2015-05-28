package ru.ifmo.ctddev.volhov.progressbar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class implements the progress bar for copy operation. When started with 2 arguments ('from' directory and
 * target directory), it copies recursively all files from 'from' to target, ignoring symlinks and displaying
 * dashboard. Dashboard contains speed, time and progress info for global and local copy progresses.
 *
 * @author volhovm
 *         Created on 5/21/15
 */
public class UIFileCopy extends JPanel implements ActionListener, PropertyChangeListener {
    private static final String usage = "Usage: UIFileCopy from to";
    private CopyTask task;
    private JFrame frame;
    private JLabel title, localUptime, globalUptime, localEstimated, globalEstimated, average, current, fromLabel, toLabel;
    private JProgressBar progressBar, overallProgressBar;
    private JPanel panel, currentOperationStats, container, globalContainer, labelsHolder;
    private JButton cancelButton;

    /**
     * Main constructor for creating an instance of copier
     *
     * @param from path to file that should be copied from
     * @param to   path to target
     */
    public UIFileCopy(String from, String to) {
        Path fromP, toP;
        fromP = Paths.get(from);
        toP = Paths.get(to);
        task = new CopyTask(fromP, toP);
        frame = new JFrame("UIFileCopy");
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) {
                task.cancel(true);
                super.windowClosed(windowEvent);
            }
        });
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

        title = new JLabel("Copying from " + from + " to " + to);
        title.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.add(title, BorderLayout.PAGE_START);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(new Font(title.getFont().getFontName(), Font.PLAIN, 14));
//        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Containers
        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        container.setAlignmentX(Container.LEFT_ALIGNMENT);
        panel.add(container);

        // From/to labels
        Box b = Box.createHorizontalBox();
        labelsHolder = new JPanel();
        labelsHolder.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelsHolder.setLayout(new BoxLayout(labelsHolder, BoxLayout.PAGE_AXIS));
        b.add(labelsHolder);
        b.add(Box.createHorizontalGlue());
        container.add(b);
        fromLabel = new JLabel("From: ");
        labelsHolder.add(fromLabel);
        toLabel = new JLabel("To: ");
        labelsHolder.add(toLabel);

        // Global container (global info)
        globalContainer = new JPanel();
        globalContainer.setLayout(new BoxLayout(globalContainer, BoxLayout.PAGE_AXIS));
        globalContainer.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Overall progress"));
        container.add(globalContainer);


        overallProgressBar = new JProgressBar(0, 100);

        overallProgressBar.setStringPainted(true);
        globalContainer.add(overallProgressBar);

        JPanel globalTextStats = new JPanel();
        globalTextStats.setLayout(new GridLayout(2, 2, 5, 5));

        globalContainer.add(globalTextStats);

        globalUptime = new JLabel("Uptime: ");
        globalTextStats.add(globalUptime);

        globalEstimated = new JLabel("Estimated: ");
        globalTextStats.add(globalEstimated);

        average = new JLabel("Average: ");
        globalTextStats.add(average);

        current = new JLabel("Current: ");
        globalTextStats.add(current);

        currentOperationStats = new JPanel();
        currentOperationStats.setLayout(new BoxLayout(currentOperationStats, BoxLayout.PAGE_AXIS));
        currentOperationStats.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Current operation status"));
//        panel.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        container.add(currentOperationStats);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setBorder(new EmptyBorder(5, 5, 5, 5));
        progressBar.setStringPainted(true);
        currentOperationStats.add(progressBar);

        JPanel localTextStats = new JPanel();
        localTextStats.setLayout(new GridLayout(1, 2, 5, 5));
        currentOperationStats.add(localTextStats);

        localUptime = new JLabel("Uptime: ");
        localUptime.setAlignmentX(Component.LEFT_ALIGNMENT);
        localUptime.setHorizontalAlignment(SwingConstants.LEFT);
        localTextStats.add(localUptime);

        localEstimated = new JLabel("Estimated: ");
        localEstimated.setAlignmentX(Component.LEFT_ALIGNMENT);
        localTextStats.add(localEstimated);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand("cancel");
        cancelButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        panel.add(cancelButton, BorderLayout.PAGE_END);

        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        frame.setContentPane(panel);
    }

    /**
     * Method, that starts copying files and showing process.
     */
    void run() {
        frame.pack();
        frame.setVisible(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        task.addPropertyChangeListener(this);
        task.execute();
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2
                || Arrays.stream(args).anyMatch(a -> a == null)
                || Arrays.stream(args).anyMatch(a -> a.length() == 0)) {
            System.err.println(usage);
            return;
        }
        UIFileCopy copier = new UIFileCopy(args[0], args[1]);
        copier.run();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getActionCommand().equals("cancel")) {
            task.cancel(true);
            cancelButton.setEnabled(false);
            progressBar.setString("Canceled");
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if ("progress".equals(propertyChangeEvent.getPropertyName())) {
            int progress = (Integer) propertyChangeEvent.getNewValue();
            progressBar.setValue(progress);
        }
    }

    /**
     * This class is a {@link javax.swing.SwingWorker}, that simply copies files and updates info
     * in GUI elements in outer class.
     */
    class CopyTask extends SwingWorker<Void, Void> {
        private Path from, to;
        private boolean active = true;
        private int count;

        /**
         * Constructor for making instance out of paths
         *
         * @param from source path
         * @param to   destination path
         */
        public CopyTask(Path from, Path to) {
            this.from = from;
            this.to = to;
        }

        private int countFiles(File file) {
            if (!file.isDirectory()) { return 1; }
            if (file.listFiles().length == 0) { return 0; }
            File[] files = file.listFiles();
            int count = 0;
            for (File f : files)
                if (f.isDirectory()) { count += countFiles(f); } else { count++; }
            return count;
        }

        private long countSize(File file) {
            if (!file.isDirectory()) { return file.length(); }
            return Arrays.stream(file.listFiles()).mapToLong(this::countSize).sum();
        }

        @Override
        protected Void doInBackground() throws Exception {
            count = countFiles(from.toFile());
            long globalSize = countSize(from.toFile());
            overallProgressBar.setMaximum(count);
            Set<FileVisitOption> options = new HashSet<>();
//            options.add(FileVisitOption.FOLLOW_LINKS);
            Files.walkFileTree(from, options, Integer.MAX_VALUE, new CopyFileVisitor(from, to, globalSize));
            return null;
        }

        @Override
        protected void done() {
            super.done();
            task.active = false;
            progressBar.setString("Done");
            cancelButton.setEnabled(false);
        }

        /**
         * FileVisitor, that simply copies files from source to destination, updates info about
         * files (time from copy to copy, average speed, and so on), and interacts with some GUI
         * elements in other class, such as global progress bar for number of files copied.
         */
        class CopyFileVisitor implements FileVisitor<Path> {
            private Path to;
            private Path from;
            private int visited = 0;
            private double aver = -1;
            private long globalSize, globalInitSize, globalStartTime;

            public CopyFileVisitor(Path from, Path to, long globalSize) {
                this.to = to;
                this.from = from;
                this.globalSize = globalInitSize = globalSize;
                globalStartTime = System.currentTimeMillis();
            }

            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes)
                    throws IOException {
                Path targetdir = to.resolve(from.relativize(path));
                if (targetdir.toFile().isFile() && path.toFile().isDirectory()) {
                    JOptionPane.showMessageDialog(panel,
                            "Tried to copy directory to file, error: " + path + " to " + targetdir);
                    return FileVisitResult.SKIP_SUBTREE;
                }
                if (targetdir.toFile().isDirectory() && path.toFile().isFile()) {
                    JOptionPane.showMessageDialog(panel,
                            "Tried to copy file to directory, error: " + path + " to " + targetdir);
                    return FileVisitResult.SKIP_SUBTREE;
                }
                try {
                    Files.createDirectory(targetdir);
                } catch (FileAlreadyExistsException ignored) {}
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                Path targetfile = to.resolve(from.relativize(path));
                try (BufferedInputStream sin = new BufferedInputStream(new FileInputStream(path.toFile()));
                     BufferedOutputStream sout = new BufferedOutputStream(new FileOutputStream(targetfile.toFile()));
                ) {
                    fromLabel.setText("From: " + path);
                    toLabel.setText("To: " + targetfile);
                    title.setText(
                            "Copying " + (visited + 1) + " of " + count + ":");
                    byte[] buf = new byte[128];
                    int got = 0;
                    long started = System.currentTimeMillis();
                    long prev = System.currentTimeMillis();
                    double size = path.toFile().length();
                    double initsize = size;
                    long gotSum = 0;
                    while (active && !isCancelled()) {
                        got = sin.read(buf);
                        if (got < 1) {
                            setProgress(100);
                            overallProgressBar.setValue(++visited);
                            break;
                        }
                        sout.write(buf, 0, got);
                        gotSum += got;
                        long temp = System.currentTimeMillis();
                        globalSize -= got;
                        int progress = (int) (Math.floor((initsize - size + gotSum) / (initsize + 1) * 100));
                        setProgress(Math.min(100, progress));
                        if (aver == -1 || temp - prev > 400) {
                            size -= gotSum;
                            double speed = gotSum * 1000 / (temp - prev + 1);
                            aver = (globalInitSize - globalSize) * 1000 / (temp - globalStartTime);
//                            aver = aver == -1 ? speed : ((aver + speed) / 2);
                            localUptime.setText("Uptime:    " + String.valueOf((temp - started) / 1000) + "s.");
                            current.setText("Current:   " + String.valueOf((int) speed / 1024 / 1024) + " MiB/s");
                            average.setText("Average:   " + String.valueOf((int) aver / 1024 / 1024) + " MiB/s");
                            localEstimated.setText("Estimated: " + String.valueOf((int) (size / aver)) + "s.");
                            globalEstimated.setText("Estimated: " + String.valueOf((int) (globalSize / aver)) + "s.");
                            globalUptime.setText(
                                    "Uptime:    " + String.valueOf((temp - globalStartTime) / 1000) + "s.");
                            System.out.println(
                                    (temp - started) / 1000 + " " + (speed / 1024 / 1024) + " " +
                                            (aver / 1024 / 1024) + " " + (size / aver));
                            gotSum = 0;
                            prev = System.currentTimeMillis();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Fatal error: " + e.getLocalizedMessage());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
                return FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        }
    }
}
