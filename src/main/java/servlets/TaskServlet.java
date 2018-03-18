package servlets;

import auxiliaryclasses.ConstantsClass;
import server.controller.Controller;
import server.controller.XmlUtils;
import server.exceptions.ControllerActionException;
import server.model.Journal;
import server.model.Task;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(ConstantsClass.TASK_SERVLET_ADDRESS)
public class TaskServlet extends HttpServlet {
    private Controller controller = Controller.getInstance();
    private DataUpdateUtil updateUtil = DataUpdateUtil.getInstance();
    private XmlUtils xmlUtils = XmlUtils.getInstance();
    private PatternChecker patternChecker = PatternChecker.getInstance();

    private Task currentTask;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter(ConstantsClass.ACTION);
        switch (action) {
            case ConstantsClass.DO_CRUD_FROM_TASKS:
                doActionFromTasks(req, resp);
                break;
        }
    }

    private void doActionFromTasks(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String useraction = req.getParameter(ConstantsClass.USERACTION);
        String usernumber;
        Journal currentJournal = (Journal) req.getSession().getAttribute(ConstantsClass.CURRENT_JOURNAL);
        switch (useraction) {
            case ConstantsClass.ADD:
                req.getSession().setAttribute(ConstantsClass.IS_ADD, Boolean.TRUE);
                req.getRequestDispatcher(ConstantsClass.UPDATE_TASKS_ADDRESS).forward(req, resp);
                break;
            case ConstantsClass.BACK_TO_MAIN:
                req.getRequestDispatcher(ConstantsClass.JOURNAL_PAGE_ADDRESS).forward(req, resp);
                break;
            case ConstantsClass.UPDATE: // имена в сессии
                usernumber = req.getParameter(ConstantsClass.USERNUMBER);
                if (usernumber != null && !usernumber.equals("")) {
                    int num = Integer.parseInt(usernumber);
                    currentTask = controller.getTaskObject(currentJournal.getId(), num);
                    req.getSession().setAttribute(ConstantsClass.CURRENT_TASK, currentTask);
                    req.getSession().setAttribute(ConstantsClass.CURRENT_STATUS, currentTask.getStatus());
                    try {
                        xmlUtils.writeTask(currentTask, req.getServletContext().getRealPath(ConstantsClass.TASK_XML_FILE));
                    } catch (Exception e) {
                        resp.getWriter().print(ConstantsClass.ERROR_XML_WRITING);
                    }
                    boolean taskCorrect = false;
                    try {
                        taskCorrect = xmlUtils.compareWithXsd(
                                req.getServletContext().getRealPath(ConstantsClass.TASK_XML_FILE),
                                req.getServletContext().getRealPath(ConstantsClass.TASK_XSD_FILE));
                    } catch (Exception e) {
                        req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, e.getMessage());
                        req.getRequestDispatcher(ConstantsClass.TASKS_PAGE_ADDRESS).forward(req, resp);
                    }
                    if (taskCorrect) {
                        String t = xmlUtils.parseXmlToString(req.getServletContext().getRealPath(ConstantsClass.TASK_XML_FILE));
                        req.setAttribute(ConstantsClass.CURRENT_TASK, t);
                        req.getSession().setAttribute(ConstantsClass.CURRENT_JOURNAL_NAME, currentJournal.getName());
                        req.getSession().setAttribute(ConstantsClass.IS_ADD, Boolean.FALSE);
                        req.getRequestDispatcher(ConstantsClass.UPDATE_TASKS_ADDRESS).forward(req, resp);
                    } else {
                        resp.getWriter().print(ConstantsClass.ERROR_XSD_COMPARING);
                    }
                } else {
                    req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.ERROR_CHOOSE_TASK);
                    req.getRequestDispatcher(ConstantsClass.TASKS_PAGE_ADDRESS).forward(req, resp);
                }
                break;
            case ConstantsClass.DELETE:
                usernumber = req.getParameter(ConstantsClass.USERNUMBER);
                if (usernumber != null && !usernumber.equals("")) {
                    int num = Integer.parseInt(usernumber);
                    currentTask = controller.getTaskObject(currentJournal.getId(), num);
                    try {
                        controller.deleteTask(currentTask);
                        updateUtil.updateTasks(req, resp, currentJournal);
                    } catch (ControllerActionException e) {
                        req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, e.getMessage());
                        req.getRequestDispatcher(ConstantsClass.TASKS_PAGE_ADDRESS).forward(req, resp);
                    }
                } else {
                    req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.ERROR_CHOOSE_TASK);
                    req.getRequestDispatcher(ConstantsClass.TASKS_PAGE_ADDRESS).forward(req, resp);
                }
                break;
            case ConstantsClass.SORT:
                String sortColumn = req.getParameter(ConstantsClass.SORT_COLUMN); // name||description
                String sortCriteria = req.getParameter(ConstantsClass.SORT_CRITERIA); // asc||desc
                String filterLike = req.getParameter(ConstantsClass.FILTER_LIKE);
                String filterEquals = req.getParameter(ConstantsClass.FILTER_EQUALS);

                if (filterLike != null && !filterLike.equals("")) {
                    if (!patternChecker.isLikeFilterCorrect(filterLike)) {
                        req.setAttribute(ConstantsClass.FILTER_LIKE, filterLike);
                        req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.ERROR_FILTER_LIKE);
                        req.getRequestDispatcher(ConstantsClass.TASKS_PAGE_ADDRESS).forward(req, resp);
                    } else {
                        sortActionTasks(req, resp, sortColumn, sortCriteria, filterLike, filterEquals);
                    }
                } else if (filterEquals != null && !filterEquals.equals("")) {
                    if (!patternChecker.isEqualsFilterCorrect(filterEquals)) {
                        req.setAttribute(ConstantsClass.FILTER_EQUALS, filterEquals);
                        req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.ERROR_FILTER_EQUALS);
                        req.getRequestDispatcher(ConstantsClass.TASKS_PAGE_ADDRESS).forward(req, resp);
                    } else {
                        sortActionTasks(req, resp, sortColumn, sortCriteria, filterLike, filterEquals);
                    }
                } else {
                    sortActionTasks(req, resp, sortColumn, sortCriteria, null, null);
                }
                break;
            case ConstantsClass.SHOW_ALL :
                updateUtil.updateTasks(req, resp, currentJournal);
                break;
            case ConstantsClass.RELOAD :
                updateUtil.updateJournals(req, resp);
                break;
        }
    }

    private void sortActionTasks(HttpServletRequest req, HttpServletResponse resp, String sortColumn,
                                 String sortCriteria, String filterLike, String filterEquals)
            throws ServletException, IOException {
        Journal currentJournal = (Journal) req.getSession().getAttribute(ConstantsClass.CURRENT_JOURNAL);
        String sortedTasks;
        if (filterEquals == null && filterLike == null) {
            try {
                sortedTasks = controller.getSortedTasks(currentJournal.getId(), sortColumn, sortCriteria);
                if (sortedTasks == null) {
                    req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.ERROR_NO_DATA_FOR_THIS_CRITERION);
                    req.getRequestDispatcher(ConstantsClass.TASKS_PAGE_ADDRESS).forward(req, resp);
                } else {
                    updateUtil.updateSortedTasks(req, resp, sortedTasks);
                }
            } catch (ControllerActionException e) {
                req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, e.getMessage());
                req.getRequestDispatcher(ConstantsClass.TASKS_PAGE_ADDRESS).forward(req, resp);
            }
        } else if (filterEquals != null) {
            try {
                sortedTasks = controller.getFilteredTasksByEquals(currentJournal.getId(), sortColumn, filterEquals, sortCriteria);
                if (sortedTasks == null) {
                    req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.ERROR_NO_DATA_FOR_THIS_CRITERION);
                    req.getRequestDispatcher(ConstantsClass.TASKS_PAGE_ADDRESS).forward(req, resp);
                } else {
                    updateUtil.updateSortedTasks(req, resp, sortedTasks);
                }
            } catch (ControllerActionException e) {
                req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, e.getMessage());
                req.getRequestDispatcher(ConstantsClass.TASKS_PAGE_ADDRESS).forward(req, resp);
            }
        } else if (filterLike != null) {
            try {
                sortedTasks = controller.getFilteredTasksByPattern(currentJournal.getId(), sortColumn, filterLike, sortCriteria);
                if (sortedTasks == null) {
                    req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.ERROR_NO_DATA_FOR_THIS_CRITERION);
                    req.getRequestDispatcher(ConstantsClass.TASKS_PAGE_ADDRESS).forward(req, resp);
                } else {
                    updateUtil.updateSortedTasks(req, resp, sortedTasks);
                }
            } catch (ControllerActionException e) {
                req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, e.getMessage());
                req.getRequestDispatcher(ConstantsClass.TASKS_PAGE_ADDRESS).forward(req, resp);
            }
        }
        req.getRequestDispatcher(ConstantsClass.TASKS_PAGE_ADDRESS).forward(req, resp);
    }

}