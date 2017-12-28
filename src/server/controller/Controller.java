package server.controller;

import server.commandproccessor.User;
import server.gui.mainform.MainForm;
import server.model.Journal;
import server.model.Task;
import server.model.TaskStatus;
import server.properties.ParserProperties;

import javax.swing.*;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Controller {
    private Journal journal;
    private Notifier notifier;
    private static Controller instance;
    private MainForm mainForm = MainForm.getInstance();
    private Map<String, String> userData; //todo vlla серьезное нарушение принципа одной ответственности. Почему у вас контроллер и журналом управляет, и пользователей авторизует? Высести авторизацию отдельно!
    private XMLSerializer serializer;
    private UserDataSerializer userDataSerializer;

    private Controller() {
        this.journal = new Journal();
        this.notifier = new Notifier();
        this.userDataSerializer = new UserDataSerializer();
        try {
            this.userData = userDataSerializer.readData(ParserProperties.getInstance()
                    .getProperties("USER_DATA"));//todo vlla вынести все константы в специаьный класс
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Could not load user data from file!",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } //todo vlla этот блок кода встречается в программе просто кучу раз, меняются только сообщения об ошибке. Не было бы лучше просто пробрасывать свои собственные
        // эксепшены сквозь весь код до какой-то верхней точки обработки, и в этом месте их обрабатывать (доставать error message и показывать окно пользователю?)
        this.serializer = new XMLSerializer();
        try {
            setJournal(serializer.readJournal(ParserProperties.getInstance().getProperties("XML_FILE")));//todo vlla вынести все константы в специаьный класс
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Could not load journal from file!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            setJournal(new Journal());
        }
    }

    /**
     * Gets the instance of server.exceptions.controller using private constructor.
     * @return If current <code>instance</code> is null, creates and returns new object,
     * otherwise returns current instance
     */

    public static Controller getInstance() {
        if (instance == null)
            instance = new Controller();
        return instance;
    }

    /**
     * Gets the <code>Journal</code> object of current server.exceptions.controller
     */

    public Journal getJournal() {
        return journal;
    }

    private boolean checkDate(Date date){
        return !date.before(Calendar.getInstance().getTime());
    }

    /**
     * Sets the <code>Journal</code> of current server.exceptions.controller. Checks dates of tasks from received journal:
     * if notification date is overdue, sets task status <code>Overdue</code>,
     * otherwise task is added without any changes
     * @param journal with tasks
     */

    public void setJournal(Journal journal) {
        this.journal = journal;
        for (Task task : journal.getTasks()) {
            if (checkDate(task.getNotificationDate()) && (task.getStatus() == TaskStatus.Planned || task.getStatus() == TaskStatus.Rescheduled)) {
                notifier.addNotification(task);
            } else {
                if (task.getStatus() == TaskStatus.Planned || task.getStatus() == TaskStatus.Rescheduled)
                    task.setStatus(TaskStatus.Overdue);
            }
        }
    }

    public void updateMainForm() { //todo vlla я не согласен с тем, что этот метод вообще должен быть в контроллере. Контроллер не должен управлять Формой, Форма должна управлять контроллером
        // (тем ботее у вас все равно вызов этого самого updateJournal дублируется почти везде)
        mainForm = MainForm.getInstance();
        if (mainForm != null)
            mainForm.updateJournal();
    }

    /**
     * Adds the received task to current journal, sets a notification for it and updates a table in <code>MainForm</code>
     * @param task to be added
     */

    public void addTask(Task task) {
        journal.addTask(task);
        notifier.addNotification(task);
        updateMainForm();
    }

    /**
     * Removes a task from current journal, cancels a notification for it and updates a table in <code>MainForm</code>
     * @param id of task to be removed
     */

    public void removeTask(int id){
        notifier.cancelNotification(id);
        journal.removeTask(id);
        updateMainForm();
    }

    /**
     * Cancels a notification for task in current journal, sets it a <code>Cancelled</code> status
     * and updates a <code>MainForm</code>
     * @param id of task for which a notification is being cancelled
     */

    public void cancelNotification(int id){
        notifier.cancelNotification(id);
        journal.getTask(id).setStatus(TaskStatus.Cancelled); //todo vlla а где собственно происходит, что смена стратуса - валидна? На сколько я помню, мы договорились, что статус таски должен меняться усключительно согласно графу переходов
        // а у вас в кучу мест кода просто вызывается setStatus, без какие либо проверок. Подключаем сюда несколько пользователей и гарантированно ловим ситуацию, когда статусы будут менять в обход графа переходов.
        // часть со статусами надо серьезно доделать: выделить сущность, когда будет ответственной только за контроль смены статусов тасок (это может быть контроллер, но я советую завести какой нибудь LifecycleManager)
        // в этом классе реализуем корректный перевод таски из одного статуса в дргуой: если переход разрешен согласно графу переходов - меняем статус, если запрещем - выдаем специальный эксепшен и корректно обрабатываем его выше.
        updateMainForm();
    }

    /**
     * Cancels a notification for task in current journal, sets it a <code>Completed</code> status
     * and updates a <code>MainForm</code>
     * @param id of completed task
     */

    public void finishNotification(int id) {
        notifier.cancelNotification(id);
        journal.getTask(id).setStatus(TaskStatus.Completed);
        updateMainForm();
    }

    /**
     * Updates a notification for the task in the current <code>Journal</code>
     * @param id of task to be updated
     * @see Notifier#editNotification(Task)
     */

    public void updateNotification(int id){
        Task task = journal.getTask(id);
        task.setStatus(TaskStatus.Rescheduled);
        notifier.editNotification(task);
        updateMainForm();
    }

    /**
     * Edits the received task
     * @param task to be edited
     * @see Notifier#editNotification(Task)
     */

    public void editTask(Task task){
        notifier.editNotification(task);
        updateMainForm();
    }

    /**
     * Checks if user with current login exists in user's map and its password equals password from parameter
     */

    public boolean isUserDataCorrect(User user) {
        if (user == null) return false;
        return userData.containsKey(user.getLogin()) &&
                userData.get(user.getLogin()).equals(user.getPassword());
    }

    public boolean isSuchLoginExists(String login) {
        return userData.containsKey(login);
    }

    public void addUser(User user) {
        if (user != null) {
            userData.put(user.getLogin(), user.getPassword());
        }
    }

    public void writeUserData(String path) {
        try {
            userDataSerializer.writeData(this.userData, path);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Could not write user data to file!",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
