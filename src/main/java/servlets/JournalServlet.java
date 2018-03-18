package servlets;

import auxiliaryclasses.ConstantsClass;
import server.controller.Controller;
import server.controller.XmlUtils;
import server.exceptions.ControllerActionException;
import server.model.Journal;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(ConstantsClass.JOURNAL_SERVLET_ADDRESS)
public class JournalServlet extends HttpServlet {
    private Controller controller = Controller.getInstance();
    private DataUpdateUtil updateUtil = DataUpdateUtil.getInstance();
    private XmlUtils xmlUtils = XmlUtils.getInstance();
    private PatternChecker patternChecker = PatternChecker.getInstance();

    private Journal currentJournal;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter(ConstantsClass.ACTION);
        switch (action) {
            case ConstantsClass.DO_CRUD_FROM_JOURNAL:
                doActionFromMain(req, resp);
                break;
        }
    }

    private void doActionFromMain(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String useraction = req.getParameter(ConstantsClass.USERACTION);
        String usernumber;
        switch (useraction) {
            case ConstantsClass.ADD:
                req.getSession().setAttribute(ConstantsClass.IS_ADD, Boolean.TRUE);
                req.getRequestDispatcher(ConstantsClass.UPDATE_JOURNALS_ADDRESS).forward(req, resp);
                break;
            case ConstantsClass.UPDATE:
                usernumber = req.getParameter(ConstantsClass.USERNUMBER);
                if (usernumber != null && !usernumber.equals("")) {
                    int num = Integer.parseInt(usernumber);
                    String journal = null;
                    try {
                        journal = controller.getJournal(num);
                    } catch (ControllerActionException e) {
                        req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, e.getMessage());
                        req.getRequestDispatcher(ConstantsClass.JOURNAL_PAGE_ADDRESS).forward(req, resp);
                    }
                    boolean journalCorrect = false;
                    try {
                        journalCorrect = xmlUtils.compareWithXsd(journal,
                                req.getServletContext().getRealPath(ConstantsClass.JOURNAL_XSD_FILE));
                    } catch (Exception e) {
                        req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.ERROR_CHOOSE_JOURNAL);
                        req.getRequestDispatcher(ConstantsClass.JOURNAL_PAGE_ADDRESS).forward(req, resp);
                    }
                    if (journalCorrect) {
                        req.setAttribute(ConstantsClass.JOURNAL_PARAMETER, journal);
                        req.getSession().setAttribute(ConstantsClass.CURRENT_JOURNAL, controller.getJournalObject(num));
                        req.getSession().setAttribute(ConstantsClass.IS_ADD, Boolean.FALSE);
                        req.getRequestDispatcher(ConstantsClass.UPDATE_JOURNALS_ADDRESS).forward(req, resp);
                    }
                } else {
                    req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.ERROR_CHOOSE_JOURNAL);
                    req.getRequestDispatcher(ConstantsClass.JOURNAL_PAGE_ADDRESS).forward(req, resp);
                }
                break;
            case ConstantsClass.DELETE:
                usernumber = req.getParameter(ConstantsClass.USERNUMBER);
                if (usernumber != null && !usernumber.equals("")) {
                    int num = Integer.parseInt(usernumber);
                    try {
                        controller.deleteJournal(num);
                        updateUtil.updateJournals(req, resp);
                    } catch (ControllerActionException e) {
                        req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, e.getMessage());
                        req.getRequestDispatcher(ConstantsClass.JOURNAL_PAGE_ADDRESS).forward(req, resp);
                    }
                } else {
                    req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.ERROR_CHOOSE_JOURNAL);
                    req.getRequestDispatcher(ConstantsClass.JOURNAL_PAGE_ADDRESS).forward(req, resp);
                }
                break;
            case ConstantsClass.CHOOSE: // записали в сессию текущий журнал и имена
                usernumber = req.getParameter(ConstantsClass.USERNUMBER);
                if (usernumber != null) {
                    currentJournal = controller.getJournalObject(Integer.parseInt(usernumber));
                    req.getSession().setAttribute(ConstantsClass.CURRENT_JOURNAL, currentJournal);
                    try {
                        xmlUtils.writeNames(controller.getJournalNamesContainer(),
                                req.getServletContext().getRealPath(ConstantsClass.NAMES_XML_FILE));
                    } catch (Exception e) {
                        resp.getWriter().print(ConstantsClass.ERROR_XML_WRITING);
                    }
                    String n = xmlUtils.parseXmlToString(req.getServletContext().getRealPath(ConstantsClass.NAMES_XML_FILE));
                    req.getSession().setAttribute(ConstantsClass.JOURNAL_NAMES, n);
                    req.getSession().setAttribute(ConstantsClass.JOURNAL_PARAMETER, controller.getTasks(currentJournal.getId()));
                    req.getRequestDispatcher(ConstantsClass.TASKS_PAGE_ADDRESS).forward(req, resp);
                } else {
                    req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.ERROR_CHOOSE_JOURNAL);
                    req.getRequestDispatcher(ConstantsClass.JOURNAL_PAGE_ADDRESS).forward(req, resp);
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
                        req.getRequestDispatcher(ConstantsClass.JOURNAL_PAGE_ADDRESS).forward(req, resp);
                    } else {
                        sortActionJournals(req, resp, sortColumn, sortCriteria, filterLike, filterEquals);
                    }
                } else if (filterEquals != null && !filterEquals.equals("")) {
                    if (!patternChecker.isEqualsFilterCorrect(filterEquals)) {
                        req.setAttribute(ConstantsClass.FILTER_EQUALS, filterEquals);
                        req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.ERROR_FILTER_EQUALS);
                        req.getRequestDispatcher(ConstantsClass.JOURNAL_PAGE_ADDRESS).forward(req, resp);
                    } else {
                        sortActionJournals(req, resp, sortColumn, sortCriteria, filterLike, filterEquals);
                    }
                } else {
                    sortActionJournals(req, resp, sortColumn, sortCriteria, null, null);
                }
                break;
            case ConstantsClass.SHOW_ALL:
                updateUtil.updateJournals(req, resp);
                break;
            case ConstantsClass.RELOAD:
                updateUtil.updateJournals(req, resp);
                break;
        }
    }

    private void sortActionJournals(HttpServletRequest req, HttpServletResponse resp, String sortColumn,
                                    String sortCriteria, String filterLike, String filterEquals)
            throws ServletException, IOException {
        String sortedJournals;
        if (filterEquals == null && filterLike == null) {
            try {
                sortedJournals = controller.getSortedJournals(sortColumn, sortCriteria);
                if (sortedJournals == null) {
                    req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.ERROR_NO_DATA_FOR_THIS_CRITERION);
                    req.getRequestDispatcher(ConstantsClass.JOURNAL_PAGE_ADDRESS).forward(req, resp);
                } else {
                    updateUtil.updateSortedJournals(req, resp, sortedJournals);
                }
            } catch (ControllerActionException e) {
                req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, e.getMessage());
                req.getRequestDispatcher(ConstantsClass.JOURNAL_PAGE_ADDRESS).forward(req, resp);
            }
        } else if (filterEquals != null) {
            try {
                sortedJournals = controller.getFilteredJournalsByEquals(sortColumn, filterEquals, sortCriteria);
                if (sortedJournals == null) {
                    req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.ERROR_NO_DATA_FOR_THIS_CRITERION);
                    req.getRequestDispatcher(ConstantsClass.JOURNAL_PAGE_ADDRESS).forward(req, resp);
                } else {
                    updateUtil.updateSortedJournals(req, resp, sortedJournals);
                }
            } catch (ControllerActionException e) {
                req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, e.getMessage());
                req.getRequestDispatcher(ConstantsClass.JOURNAL_PAGE_ADDRESS).forward(req, resp);
            }
        } else if (filterLike != null) {
            try {
                sortedJournals = controller.getFilteredJournalsByPattern(sortColumn, filterLike, sortCriteria);
                if (sortedJournals == null) {
                    req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.ERROR_NO_DATA_FOR_THIS_CRITERION);
                    req.getRequestDispatcher(ConstantsClass.JOURNAL_PAGE_ADDRESS).forward(req, resp);
                } else {
                    updateUtil.updateSortedJournals(req, resp, sortedJournals);
                }
            } catch (ControllerActionException e) {
                req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, e.getMessage());
                req.getRequestDispatcher(ConstantsClass.JOURNAL_PAGE_ADDRESS).forward(req, resp);
            }
        }
        req.getRequestDispatcher(ConstantsClass.JOURNAL_PAGE_ADDRESS).forward(req, resp);
    }
}