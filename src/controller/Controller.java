package controller;

import gui.mainform.MainForm;
import model.Journal;
import model.Task;
import model.TaskStatus;

import java.util.Calendar;
import java.util.Date;

public class Controller {

    private Journal journal;
    private Notifier notifier;
    private static Controller instance;
    private MainForm mainForm = MainForm.getInstance();

    private Controller() {
        this.journal = new Journal();
        this.notifier = new Notifier();
    }

    public static Controller getInstance() {
        if (instance == null)
            instance = new Controller();
        return instance;
    }

    public Journal getJournal() {
        return journal;
    }

    private boolean checkDate(Date date){
        return !date.before(Calendar.getInstance().getTime());
    }

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
        mainForm.updateJournal();
    }

    //создается таск вместе с оповещением
    public void addTask(Task task) {
        journal.addTask(task);
        notifier.addNotification(task);
        mainForm.updateJournal();
    }

    //удаляется таск и оповещение
    public void removeTask(int id){
        notifier.cancelNotification(id);
        journal.removeTask(id);
        mainForm.updateJournal();
    }

    //отменяется оповещение
    public void cancelNotification(int id){
        notifier.cancelNotification(id);
        journal.getTask(id).setStatus(TaskStatus.Cancelled);
        mainForm.updateJournal();
    }

    //завершает таску
    public void finishNotification(int id) {
        notifier.cancelNotification(id);
        journal.getTask(id).setStatus(TaskStatus.Completed);
        mainForm.updateJournal();
    }

    //изменяется оповещение (читать в классе Notifier описание по работе с методом)
    public void updateNotification(int id){
        Task task = journal.getTask(id);
        task.setStatus(TaskStatus.Rescheduled);
        notifier.editNotification(task);
        mainForm.updateJournal();
    }

    //изменение таски полностью
    public void editTask(Task task){
        notifier.editNotification(task);
        mainForm.updateJournal();
    }
}
