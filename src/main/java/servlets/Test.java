package servlets;

import beans.XmlUtilsLocal;
import server.model.Journal;
import server.model.Task;
import server.model.TaskStatus;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.JAXBException;
import java.sql.Date;
import java.util.Properties;

public class Test {
    public static void main(String[] args) {
        Task task1 = new Task(0, "1", TaskStatus.Overdue, "desc1", new Date(new java.util.Date().getTime()), new Date(new java.util.Date().getTime()),
                new Date(new java.util.Date().getTime()), new Date(new java.util.Date().getTime()), 0);
        Task task2 = new Task(1, "2", TaskStatus.Overdue, "desc2", new Date(new java.util.Date().getTime()), new Date(new java.util.Date().getTime()),
                new Date(new java.util.Date().getTime()), new Date(new java.util.Date().getTime()), 1);
        Task task3 = new Task(2, "3", TaskStatus.Overdue, "desc3", new Date(new java.util.Date().getTime()), new Date(new java.util.Date().getTime()),
                new Date(new java.util.Date().getTime()), new Date(new java.util.Date().getTime()), 1);
        Journal journal1 = new Journal();
        journal1.addTask(task1);
        journal1.addTask(task2);
        journal1.addTask(task3);
        journal1.setName("j1");
        journal1.setId(0);

        try {
            java.util.Properties properties = new Properties();
            properties.put(Context.INITIAL_CONTEXT_FACTORY, "beans.XmlUtilsLocal");
            Context context = new InitialContext(properties);
            XmlUtilsLocal xmlUtils = (XmlUtilsLocal) context.lookup("java:module/XmlUtilsLocal");
            try {
                System.out.println(xmlUtils.marshalToString(Journal.class, journal1));
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
}
