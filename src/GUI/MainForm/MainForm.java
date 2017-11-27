package GUI.MainForm;

import GUI.TaskWindow.TaskForm;
import GUI.TaskWindow.TaskWindow;
import controller.SerializeDeserialize;
import model.Journal;
import model.Task;
import model.TaskStatus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Date;

public class MainForm extends JFrame {
    private JFileChooser fileChooser;
    private SerializeDeserialize journalBackup;
    private Journal journal;
    private TablePanel tablePanel;
    private ButtonPanel buttonPanel;
    private TrayIcon tray;
    private ImageIcon icon = new ImageIcon("icon.png");
    private SystemTray systemTray = SystemTray.getSystemTray();

    public MainForm() {
        super("Task Scheduler");

        fileChooser = new JFileChooser();
        journalBackup = new SerializeDeserialize();
        this.journal = new Journal();
        //testTable();

        tablePanel = new TablePanel();
        tablePanel.setData(this.journal.getTasks());
        tablePanel.refresh();
        buttonPanel = new ButtonPanel();
        buttonPanel.setTable(tablePanel.getTable());

        tablePanel.setTableListener((Integer... rows) -> {
            buttonPanel.setListener((int action) -> {
                switch (action) {
                    case TaskActionListener.ADD_TASK:
                        new TaskForm().layoutForAdd();
                        break;
                    case TaskActionListener.EDIT_TASK:
                        new TaskForm().layoutForEdit();
                        break;
                    case TaskActionListener.DELETE_TASK:
                        for (int i = 0; i < rows.length; i++) {
                            this.journal.removeTask(i);
                            tablePanel.refresh();
                        }
                        //System.out.println(this.journal.getTasks());
                        break;
                }
            });
        });
        buttonPanel.setTableListener((Integer... rows) -> {
            buttonPanel.setListener((int action) -> {
                switch (action) {
                    case TaskActionListener.ADD_TASK:
                        new TaskWindow(this);
                        break;
                    case TaskActionListener.EDIT_TASK:
                        new TaskWindow(this);
                        break;
                    case TaskActionListener.DELETE_TASK:
                        for (int i = 0; i < rows.length; i++) {
                            this.journal.removeTask(rows[i]);
                            tablePanel.refresh();
                            tablePanel.setData(this.journal.getTasks());
                            buttonPanel.setTable(tablePanel.getTable());
                            for(int j = i+1; j < rows.length; j++) {
                                rows[j]--;
                            }
                        }
                        break;
                }
            });
        });

        setJMenuBar(createMenu());
        setLayout(new BorderLayout());
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        tray = new TrayIcon(icon.getImage());
        tray.addActionListener((ActionEvent e) -> {
            setVisible(true);
            setState(JFrame.NORMAL);
            removeFromTray();
        });
        tray.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        tray.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {

            }

            @Override
            public void mouseMoved(MouseEvent e) {
                tray.setToolTip("Double click to show");
            }
        });

        addWindowStateListener(new WindowStateListener() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                if (e.getNewState() == JFrame.ICONIFIED) {
                    setVisible(false);
                    addToTray();
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int action = JOptionPane.showConfirmDialog(
                        MainForm.this, "Do you really want to close the app?",
                        "Warning!",
                        JOptionPane.YES_NO_CANCEL_OPTION);

                if (action == JOptionPane.OK_OPTION) {
                    try {
                        journalBackup.writeJournal(journal);
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(MainForm.this, "Could not save journal to file ",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    //System.out.println("Closing");
                    System.exit(0);
                }
            }
        });

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(600, 400));
        setSize(600, 500);
        setIconImage(icon.getImage());
        setLocationRelativeTo(null);
        setResizable(true);
        setVisible(true);
    }

    private void removeFromTray() {
        systemTray.remove(tray);
    }

    private void addToTray() {
        try {
            systemTray.add(tray);
            //tray.displayMessage("Свернулся", "В трей", TrayIcon.MessageType.INFO);
        } catch (AWTException e) {
            e.printStackTrace();
        }

    }

    private JMenuBar createMenu() {
        JMenuBar menu = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        menu.add(fileMenu);

        JMenuItem exportJournal = new JMenuItem("Export journal...");
        JMenuItem importJournal = new JMenuItem("Import journal...");
        JMenuItem exit = new JMenuItem("Exit");

        fileMenu.add(exportJournal);
        fileMenu.add(importJournal);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        exit.addActionListener((ActionEvent e) -> {
            int action = JOptionPane.showConfirmDialog(
                    MainForm.this, "Do you really want to close the app?",
                    "Warning!",
                    JOptionPane.YES_NO_CANCEL_OPTION);

            if (action == JOptionPane.OK_OPTION) {
                try {
                    journalBackup.writeJournal(this.journal);
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(MainForm.this, "Could not save journal to file ",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                System.exit(0);
            }
        });

        exportJournal.addActionListener((ActionEvent e) -> {  //todo запись в файл при запуске и выходе из проги

            try {
                journalBackup.writeJournal(journal);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(MainForm.this, "Could not save journal to file ",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        importJournal.addActionListener((ActionEvent e) -> {
            try {
                this.journal = journalBackup.readJournal();
                tablePanel.setData(journal.getTasks());
                tablePanel.refresh();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(MainForm.this, "Could not load journal from file",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return menu;
    }

    private void testTable() {
        Task task1 = new Task("Test", TaskStatus.Planned, "Test",
                new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()));
        this.journal.addTask(task1);
        Task task2 = new Task("Test2", TaskStatus.Completed, "Test2",
                new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()));
        this.journal.addTask(task2);
        Task task3 = new Task("Test3", TaskStatus.Completed, "Test3",
                new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()));
        Task task4 = new Task("Test3", TaskStatus.Completed, "Test3",
                new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()));
        Task task5 = new Task("Test3", TaskStatus.Completed, "Test3",
                new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()));
        this.journal.addTask(task3);
        this.journal.addTask(task4);
        this.journal.addTask(task5);
    }

    /**
     * Sets journal to be represented at this <code>MainForm</code>
     * @param journal object with tasks for representation
     */
    public void setJournal(Journal journal) {
        if (journal != null) {
            this.journal = journal;
            tablePanel.setData(this.journal.getTasks());
            tablePanel.refresh();
        }
    }
}