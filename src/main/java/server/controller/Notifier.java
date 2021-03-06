package server.controller;

import server.model.Task;
import server.model.TaskStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

public class Notifier {

    private Map<Integer, Timer> timers;

    protected Notifier() {
        timers = new HashMap<>();
    }

    //выставление таймера для таски
    protected void addNotification(Task task) {
        if (checkStatus(task) && checkTime(task)) {
            NotificationTimer notificationTimer = new NotificationTimer(task);
            Timer timer = new Timer(true);
            timer.schedule(notificationTimer, task.getNotificationDate().getTime() - System.currentTimeMillis()
                    + 86400000);
            timers.put(task.getId(), timer);
        }
    }

    //отменяет таймер для таски
    protected void cancelNotification(int id) {
        if (timers.containsKey(id)) {
            Timer timer = timers.get(id);
            timer.cancel();
        }
    }

    //приходит уже измененный таск с перенесенным временем
    protected void editNotification(Task task) {
        if (timers.containsKey(task.getId())) {
            Timer timer_old = timers.get(task.getId());
            timer_old.cancel();
            NotificationTimer notificationTimer = new NotificationTimer(task);
            Timer timer = new Timer(true);
            timer.schedule(notificationTimer, task.getNotificationDate().getTime() - System.currentTimeMillis()
                    + 86400000);
            timers.put(task.getId(), timer);
        }
    }

    private boolean checkStatus(Task task) {
        return task.getStatus() != TaskStatus.Cancelled && task.getStatus() != TaskStatus.Completed
                && task.getStatus() != TaskStatus.Overdue;
    }

    private boolean checkTime(Task task) {
        if ((task.getNotificationDate().getTime() - System.currentTimeMillis()) >= 0)
            return true;
        return false;
    }
}
