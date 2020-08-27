import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;

public class Editor extends JFrame implements ActionListener {
    private final JTextArea textArea;
    private final LineNumberingTextArea lineNumbers;
    private final JMenuItem exitAction;
    private final JMenuItem cutAction;
    private final JMenuItem copyAction;
    private final JMenuItem pasteAction;
    private final JMenuItem selectAction;
    private final JMenuItem saveAction;
    private final JMenuItem openAction;
    private final JMenuItem compileAction;
    private String pad;

    private String currentFilePath = "";

    // File filters
    FileFilter duckFilter = new FileNameExtensionFilter("DuckyScript Files (*.duck)", "duck");
    FileFilter textFilter = new FileNameExtensionFilter("Text Files (*.txt)", "txt");
    FileFilter inoFilter = new FileNameExtensionFilter("Arduino Files (*.ino)", "ino");

    public Editor() {
        super("DigiDuck Editor");
        setSize(600, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container pane = getContentPane();
        pane.setLayout(new BorderLayout());

        pad = " ";

        // Make text area
        textArea = new JTextArea();
        textArea.setBorder(BorderFactory.createCompoundBorder(
                textArea.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");
        JMenu buildMenu = new JMenu("Build");
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Attach the line numbers to the scroll pane
        lineNumbers = new LineNumberingTextArea(textArea);
        scrollPane.setRowHeaderView(lineNumbers);
        lineNumbers.updateLineNumbers();

        exitAction = new JMenuItem("Exit");
        cutAction = new JMenuItem("Cut");
        copyAction = new JMenuItem("Copy");
        pasteAction = new JMenuItem("Paste");
        selectAction = new JMenuItem("Select All");
        saveAction = new JMenuItem("Save");
        openAction = new JMenuItem("Open");
        compileAction = new JMenuItem("Compile");
        JToolBar toolBar = new JToolBar();

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        setJMenuBar(menuBar);
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(buildMenu);

        fileMenu.add(saveAction);
        fileMenu.add(openAction);
        fileMenu.add(exitAction);

        editMenu.add(cutAction);
        editMenu.add(copyAction);
        editMenu.add(pasteAction);
        editMenu.add(selectAction);

        buildMenu.add(compileAction);

        saveAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        openAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        cutAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        copyAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        pasteAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        selectAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Failed to set look and feel");
            e.printStackTrace();
        }

        pane.add(scrollPane, BorderLayout.CENTER);
        pane.add(toolBar, BorderLayout.SOUTH);

        saveAction.addActionListener(this);
        openAction.addActionListener(this);
        exitAction.addActionListener(this);
        cutAction.addActionListener(this);
        copyAction.addActionListener(this);
        pasteAction.addActionListener(this);
        selectAction.addActionListener(this);
        compileAction.addActionListener(this);

        setVisible(true);

        // Events to update line numbers
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                lineNumbers.updateLineNumbers();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                lineNumbers.updateLineNumbers();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                lineNumbers.updateLineNumbers();
            }
        });
    }

    public String getText() {
        return this.textArea.getText();
    }

    private JFileChooser makeFileChooser() {
        // Get the FileSystemView to find the desktop
        FileSystemView filesys = FileSystemView.getFileSystemView();
        // Create an object of JFileChooser class
        JFileChooser fileChooser = new JFileChooser(filesys.getHomeDirectory().getAbsolutePath()) {
            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    int result = JOptionPane.showConfirmDialog(this, f.getName().concat(" already exists, would you like to replace it?"), "Confirm save as", JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        // Todo make no option increment filename e.g. "Test (1).txt"
                        case JOptionPane.NO_OPTION:
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }
        };

        // Filter file types to .duck and .txt
        fileChooser.setAcceptAllFileFilterUsed(false);

        return fileChooser;
    }

    private void save() {
        String filePath;
        if (this.currentFilePath.equals("")) {

            // Make a new file chooser
            JFileChooser fileChooser = this.makeFileChooser();
            fileChooser.addChoosableFileFilter(this.duckFilter);
            fileChooser.addChoosableFileFilter(this.textFilter);

            // Invoke the showsSaveDialog function to show the save dialog
            int choiceState = fileChooser.showSaveDialog(null);

            if (choiceState == JFileChooser.APPROVE_OPTION) {

                // Make sure the user specified a file extension
                // If not, add one automatically
                FileFilter chosenExt = fileChooser.getFileFilter();

                filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!(filePath.endsWith(".duck") || filePath.endsWith(".txt"))) {
                    if (chosenExt.equals(this.duckFilter)) {
                        filePath = filePath.concat(".duck");
                    } else if (chosenExt.equals(this.textFilter)) {
                        filePath = filePath.concat(".txt");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "The user cancelled the operation");
                return;
            }
        } else {
            filePath = this.currentFilePath;
        }
        File file = new File(filePath);

        try {
            if (file.createNewFile()) {
                System.out.println("File successfully created");
            } else {
                System.out.println("File already exists");
            }
            // Create a file writer
            FileWriter writer = new FileWriter(file, false);
            // Create buffered writer to write
            BufferedWriter buffWriter = new BufferedWriter(writer);
            // Write
            buffWriter.write(textArea.getText());
            buffWriter.flush();
            buffWriter.close();
            this.currentFilePath = filePath;
        } catch (FileNotFoundException exception) {
            // File wasn't found, we'll have to make one
            System.out.println("File wasn't found");
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void open() {
        JFileChooser fileChooser = this.makeFileChooser();
        fileChooser.addChoosableFileFilter(this.duckFilter);
        fileChooser.addChoosableFileFilter(this.textFilter);

        int choiceState = fileChooser.showOpenDialog(null);

        if (choiceState == JFileChooser.APPROVE_OPTION) {

            File chosenFile = fileChooser.getSelectedFile();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(chosenFile));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append(System.getProperty("line.separator"));
                }
                // Remove newline at end of file
                builder.deleteCharAt(builder.length() - 1);
                textArea.setText(builder.toString());
            } catch (IOException exception) {
                exception.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred");
            }

        } else {
            JOptionPane.showMessageDialog(this, "The user cancelled the operation");
        }
    }

    private void compile() {
        String compiledCode = new Compiler(this).compile();

        // Make a new file chooser
        JFileChooser fileChooser = this.makeFileChooser();
        fileChooser.addChoosableFileFilter(this.inoFilter);

        // Invoke the showsSaveDialog function to show the save dialog
        int choiceState = fileChooser.showSaveDialog(null);

        if (choiceState == JFileChooser.APPROVE_OPTION) {

            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith(".ino")) {
                filePath = filePath.concat(".ino");
            }
            File file = new File(filePath);

            try {
                if (file.createNewFile()) {
                    System.out.println("File successfully created");
                } else {
                    System.out.println("File already exists");
                }
                // Create a file writer
                FileWriter writer = new FileWriter(file, false);
                // Create buffered writer to write
                BufferedWriter buffWriter = new BufferedWriter(writer);
                // Write
                buffWriter.write(compiledCode);
                buffWriter.flush();
                buffWriter.close();
                this.currentFilePath = filePath;
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(this, exception.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "The user cancelled the operation");
        }
    }

    public void actionPerformed(ActionEvent e) {
        JMenuItem choice = (JMenuItem) e.getSource();
        if (choice == saveAction) {
            this.save();
        } else if (choice == openAction) {
            this.open();
        } else if (choice == exitAction)
            System.exit(0);
        else if (choice == cutAction) {
            pad = textArea.getSelectedText();
            textArea.replaceRange("", textArea.getSelectionStart(), textArea.getSelectionEnd());
        } else if (choice == copyAction)
            pad = textArea.getSelectedText();
        else if (choice == pasteAction)
            textArea.insert(pad, textArea.getCaretPosition());
        else if (choice == selectAction)
            textArea.selectAll();
        else if (e.getSource() == compileAction) {
            this.compile();
        }
    }

    public static void main(String[] args) {
        new Editor();
    }
}