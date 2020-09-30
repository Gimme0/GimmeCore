package me.gimme.gimmecore.command;

/**
 * Thrown when a command is supplied invalid arguments.
 */
public class CommandUsageException extends RuntimeException {

    /**
     * Constructs an instance of <code>CommandUsageException</code> with the specified detail message.
     *
     * @param msg the detail message
     */
    public CommandUsageException(String msg) {
        super(msg);
    }
}
