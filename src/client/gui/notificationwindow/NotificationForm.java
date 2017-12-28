package client.gui.notificationwindow;

import client.commandprocessor.ClientCommandSender;
import client.gui.mainform.MainForm;
import client.exceptions.IllegalPropertyException;
import client.model.Task;
import client.model.TaskStatus;
import client.network.ClientNetworkFacade;
import client.properties.ParserProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class NotificationForm extends JFrame {
    private Task task;
    private ButtonPanel buttonPanel;
    private LabelPanel labelPanel;
    private ImageIcon icon;
    private MainForm mainForm = MainForm.getInstance();
    private ClientNetworkFacade clientFacade = ClientNetworkFacade.getInstance();
    private ClientCommandSender commandSender = ClientCommandSender.getInstance();

    public NotificationForm() {
        super("Alarm!");
        try {
            icon = new ImageIcon(ParserProperties.getInstance().getProperties("MAIN_FORM_ICON"));
        } catch (IllegalPropertyException e) {
            JOptionPane.showMessageDialog(null, "Illegal value of property",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        buttonPanel = new ButtonPanel();
        labelPanel = new LabelPanel();
        setLayout(new BorderLayout());
        add(labelPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(400, 300));
        Dimension prefSize = new Dimension(400, 300);
        setPreferredSize(prefSize);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(screenSize.width - prefSize.width, screenSize.height - prefSize.height - 70, prefSize.width, prefSize.height);
        setIconImage(icon.getImage());
        setResizable(false);
    }

    public void setTask(Task task) {
        this.task = task;
        labelPanel.setName(this.task.getName());
        labelPanel.setDescription(this.task.getDescription());
    }

    private class ButtonPanel extends JPanel {
        private JButton later;
        private JButton finish;
        private JButton cancel;

        public ButtonPanel() {
            later = new JButton("Remind later");
            finish = new JButton("Finish task");
            cancel = new JButton("Cancel task");

            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            setLayout(new FlowLayout());
            add(later);
            add(finish);
            add(cancel);

            Dimension dimension = getPreferredSize();
            dimension.height = 37;
            setPreferredSize(dimension);

            later.addActionListener((ActionEvent e) -> {
                if(checkAction())
                    SwingUtilities.invokeLater(() -> new LaterForm(task));
                dispose();
            });

            finish.addActionListener((ActionEvent e) -> {
                if(checkAction())
                    commandSender.sendFinishCommand(task, clientFacade.getNotificationOutputStream());
                dispose();
            });

            cancel.addActionListener((ActionEvent e) -> {
                if(checkAction())
                    commandSender.sendCancelCommand(task, clientFacade.getNotificationOutputStream());
                dispose();
            });
        }

        private boolean checkAction() {
            if(MainForm.getInstance().getJournal().getTask(task.getId()) == null) {
                JOptionPane.showMessageDialog(null, "No task to perform this action",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(MainForm.getInstance().getJournal().getTask(task.getId()).getStatus() == TaskStatus.Completed) {
                JOptionPane.showMessageDialog(null, "This task has been already completed by another user!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if(MainForm.getInstance().getJournal().getTask(task.getId()).getStatus() == TaskStatus.Cancelled) {
                JOptionPane.showMessageDialog(null, "This task has been already cancelled by another user!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            return true;
        }
    }
}
