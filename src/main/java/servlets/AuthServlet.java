package servlets;

import auxiliaryclasses.ConstantsClass;
import server.controller.PasswordEncoder;
import database.postgresql.PostgreSQLDAOFactory;
import server.controller.Controller;
import server.exceptions.ControllerActionException;
import server.model.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@WebServlet(ConstantsClass.AUTH_SERVLET_ADDRESS)
public class AuthServlet extends HttpServlet { //todo vlla АДСКИ перегруженный класс. Распилить, отрефакторить, вынести дублирующийся код в отдельные методы
    private PasswordEncoder encoder = PasswordEncoder.getInstance();
    private DataUpdateUtil updateUtil = DataUpdateUtil.getInstance();
    private PatternChecker patternChecker = PatternChecker.getInstance();
    private Controller controller = Controller.getInstance();

    private PostgreSQLDAOFactory dbFactory;
    private User currentUser;

    @Override
    public void init(ServletConfig config) throws ServletException {
        dbFactory = PostgreSQLDAOFactory.getInstance(config.getServletContext().getRealPath(ConstantsClass.SCRIPT_FILE));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter(ConstantsClass.ACTION);
        switch (action) {
            case ConstantsClass.DO_SIGN_IN:
                doSignIn(req, resp);
                break;
            case ConstantsClass.DO_SIGN_UP:
                doSignUp(req, resp);
                break;
        }
    }

    private void doSignIn(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String useraction = req.getParameter(ConstantsClass.USERACTION);
        String login;
        String password;
        String encryptedPassword;
        switch (useraction) {
            case ConstantsClass.DO_SIGN_IN:
                login = req.getParameter(ConstantsClass.LOGIN_PARAMETER);
                password = req.getParameter(ConstantsClass.PASSWORD_PARAMETER);
                if (!patternChecker.isCorrectLogin(login) || !patternChecker.isCorrectLogin(password) || login.length() > ConstantsClass.LOGIN_FIELD_LENGTH) {
                    req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.ERROR_AUTH);
                    req.setAttribute(ConstantsClass.LOGIN_PARAMETER, login);
                    req.getRequestDispatcher(ConstantsClass.SIGN_IN_ADDRESS).forward(req, resp);
                } else {
                    try {
                        encryptedPassword = encoder.encode(password);
                    } catch (NoSuchAlgorithmException e) {
                        req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.UNSUCCESSFUL_ACTION);
                        req.setAttribute(ConstantsClass.LOGIN_PARAMETER, login);
                        req.getRequestDispatcher(ConstantsClass.SIGN_IN_ADDRESS).forward(req, resp);
                        break;
                    }
                    currentUser = controller.signInUser(login, encryptedPassword);
                    if (currentUser != null) {
                        req.getSession().setAttribute(ConstantsClass.CURRENT_USER, currentUser);
                        updateUtil.updateJournals(req, resp);
                    } else {
                        req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.UNSUCCESSFUL_SIGN_IN);
                        req.setAttribute(ConstantsClass.LOGIN_PARAMETER, login);
                        req.getRequestDispatcher(ConstantsClass.SIGN_IN_ADDRESS).forward(req, resp);
                    }
                    break;
                }
            case ConstantsClass.DO_SIGN_UP:
                req.getRequestDispatcher(ConstantsClass.SIGN_UP_ADDRESS).forward(req, resp);
                break;
        }
    }

    private void doSignUp(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String login = req.getParameter(ConstantsClass.LOGIN_PARAMETER);
        String password = req.getParameter(ConstantsClass.PASSWORD_PARAMETER);
        String encryptedPassword = null;
        if (!patternChecker.isCorrectLogin(login) || !patternChecker.isCorrectLogin(password) || login.length() > ConstantsClass.LOGIN_FIELD_LENGTH) {
            req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.ERROR_AUTH);
            req.setAttribute(ConstantsClass.LOGIN_PARAMETER, login);
            req.getRequestDispatcher(ConstantsClass.SIGN_UP_ADDRESS).forward(req, resp);
        } else {
            try {
                encryptedPassword = encoder.encode(password);
            } catch (NoSuchAlgorithmException e) {
                req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.UNSUCCESSFUL_ACTION);
                req.setAttribute(ConstantsClass.LOGIN_PARAMETER, login);
                req.getRequestDispatcher(ConstantsClass.SIGN_UP_ADDRESS).forward(req, resp);
            }
            try {
                controller.addUser(login, encryptedPassword, ConstantsClass.USER_ROLE);
                currentUser = controller.signInUser(login, encryptedPassword);
                if (currentUser != null) {
                    req.getSession().setAttribute(ConstantsClass.CURRENT_USER, currentUser);
                    updateUtil.updateJournals(req, resp);
                } else {
                    req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, ConstantsClass.UNSUCCESSFUL_SIGN_UP);
                    req.setAttribute(ConstantsClass.LOGIN_PARAMETER, login);
                    req.getRequestDispatcher(ConstantsClass.SIGN_UP_ADDRESS).forward(req, resp);
                }
            } catch (ControllerActionException e) {
                req.setAttribute(ConstantsClass.MESSAGE_ATTRIBUTE, e.getMessage());
                req.setAttribute(ConstantsClass.LOGIN_PARAMETER, login);
                req.getRequestDispatcher(ConstantsClass.SIGN_UP_ADDRESS).forward(req, resp);
            }
        }
    }
}