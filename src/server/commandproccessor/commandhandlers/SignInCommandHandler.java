package server.commandproccessor.commandhandlers;

import server.commandproccessor.Command;
import server.commandproccessor.ServerCommandSender;
import server.commandproccessor.User;
import server.controller.Controller;
import server.controller.UserAuthorizer;
import server.network.ServerNetworkFacade;

import java.io.DataOutputStream;

public class SignInCommandHandler implements CommandHandler {
    @Override
    public synchronized void handle(Command command) {
        ServerCommandSender commandSender = ServerCommandSender.getInstance();
        UserAuthorizer authorizer = UserAuthorizer.getInstance();
        Controller controller = Controller.getInstance();
        if (authorizer.isUserDataCorrect(((User) command.getObject()))) {
            commandSender.sendSuccessfulAuthCommand(controller.getJournal(),
                    ServerNetworkFacade.getInstance().getDataOutputStream(((User) command.getObject()).getPort()));
        }
        else
            commandSender.sendUnsuccessfulAuthCommand(ServerNetworkFacade.getInstance().
                    getDataOutputStream(((User) command.getObject()).getPort()));
    }
}
