package oldserverclasses.commandproccessor.commandhandlers;

import oldserverclasses.commandproccessor.Command;

/**
 * An object that handles processing of a command
 */

public interface CommandHandler {

    /**
     * Handles execution of an incoming command
     *
     * @param command that needs to be handled
     */

    void handle(Command command);
}