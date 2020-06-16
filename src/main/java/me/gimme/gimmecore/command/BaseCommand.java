package me.gimme.gimmecore.command;

import com.google.common.base.Strings;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base class for commands.
 */
public abstract class BaseCommand {

    /**
     * Command error type
     */
    public enum CommandError {
        ILLEGAL_CHARACTERS("Contains illegal characters"),
        INVALID_ARGUMENT("Invalid argument"),
        NO_PERMISSION("You do not have permission for this command"),
        NOT_A_COLOR("Not a color"),
        NOT_A_NUMBER("Not a number"),
        PLAYER_ONLY("Only players can do this"),
        TOO_FEW_ARGUMENTS("Not enough input"),
        TOO_MANY_ARGUMENTS("Too much input"),
        UNKNOWN("Something went wrong");

        private String defaultMessage;

        CommandError(@NotNull final String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        public void setMessage(@NotNull String message) {
            this.defaultMessage = message;
        }
    }

    protected static final String newLine = "\n";

    private static final ChatColor COLOR_COMMAND = ChatColor.AQUA;
    private static final ChatColor COLOR_COMMAND_NO_PERMISSION = ChatColor.DARK_RED;
    private static final ChatColor COLOR_ARGS_USAGE = ChatColor.DARK_AQUA;
    private static final ChatColor COLOR_DESCRIPTION = ChatColor.YELLOW;

    private String parent;
    private String name;
    protected List<String> aliases = new ArrayList<>();
    protected String argsUsage = "";
    protected List<String> argsAlternatives = new ArrayList<>();
    protected int minArgs = 0;
    protected int maxArgs = 0;
    protected boolean playerOnly = false;
    protected String description = "";
    protected String permission = null;

    protected BaseCommand(@NotNull String parent, @NotNull String name) {
        this.parent = parent;
        this.name = name;
    }

    /**
     * Handles a command and executes it if the arguments are valid.
     *
     * @param sender the sender of the command
     * @param args   the arguments used for this command
     */
    void handle(@NotNull final CommandSender sender, @NotNull final String[] args) {
        if (args.length > maxArgs) {
            String superfluousInput = String.join(" ", Arrays.copyOfRange(args, maxArgs, args.length));
            sender.sendMessage(errorMessageWithUsage(CommandError.TOO_MANY_ARGUMENTS, superfluousInput));
            return;
        } else if (args.length < minArgs) {
            sender.sendMessage(errorMessageWithUsage(CommandError.TOO_FEW_ARGUMENTS, null));
            return;
        } else if (playerOnly && !(sender instanceof Player)) {
            sender.sendMessage(errorMessage(CommandError.PLAYER_ONLY, null));
            return;
        } else if (!isPermitted(sender)) {
            sender.sendMessage(errorMessage(CommandError.NO_PERMISSION, null));
            return;
        }

        String confirmationMessage = execute(sender, args);
        if (confirmationMessage != null) {
            sender.sendMessage(confirmationMessage);
        }
    }

    /**
     * Executes the specific implementation of the command. Returns a confirmation message (error or success)
     * to be sent to the sender, or null if no message should be sent.
     *
     * @param sender the sender of the command
     * @param args   the arguments used for this command
     * @return the return message, or null if no message should be sent
     */
    @Nullable
    protected abstract String execute(@NotNull final CommandSender sender, @NotNull final String[] args);

    /**
     * Returns if the sender has permission for this command.
     * Command implementations can override this to provide additional more specific conditions.
     *
     * @param sender the sender of the command
     * @return if the sender has permission for this command
     */
    protected boolean isPermitted(@NotNull CommandSender sender) {
        if (Strings.isNullOrEmpty(permission) || sender.isOp()) return true;
        return sender.hasPermission(permission);
    }

    /**
     * Gets a command success message with a custom message.
     *
     * @param customMessage the custom message to get as a success message
     * @return the formatted success message
     */
    @NotNull
    protected String successMessage(@NotNull String customMessage) {
        return ChatColor.GREEN + customMessage;
    }

    /**
     * Gets a predefined command error message based on the type of error with the correct usage attached.
     *
     * @param error         the type of error
     * @param relevantInput the part of the input that contains the error, or null if no relevant input
     * @return the formatted error message
     */
    @NotNull
    protected String errorMessageWithUsage(@NotNull CommandError error, @Nullable String relevantInput) {
        String message = error.defaultMessage;
        if (!Strings.isNullOrEmpty(relevantInput)) {
            message += ": " + relevantInput;
        }

        return errorMessageWithUsage(message);
    }

    /**
     * Gets a command error message with a custom message and the correct usage attached.
     *
     * @param customMessage the custom message to get as an error message
     * @return the formatted error message
     */
    @NotNull
    protected String errorMessageWithUsage(@NotNull String customMessage) {
        return errorMessage(customMessage) + ChatColor.YELLOW + " Correct usage:" + newLine + getUsage();
    }

    /**
     * Gets a predefined command error message based on the type of error.
     *
     * @param error         the type of error
     * @param relevantInput the part of the input that contains the error, or null if no relevant input
     * @return the formatted error message
     */
    @NotNull
    protected String errorMessage(@NotNull CommandError error, @Nullable String relevantInput) {
        String message = error.defaultMessage;
        if (!Strings.isNullOrEmpty(relevantInput)) {
            message += ": " + relevantInput;
        }

        return errorMessage(message);
    }

    /**
     * Gets a command error message with a custom message.
     *
     * @param customMessage the custom message to get as an error message
     * @return the formatted error message
     */
    @NotNull
    protected String errorMessage(@NotNull String customMessage) {
        return ChatColor.RED + customMessage;
    }

    /**
     * @return the parent command name
     */
    @NotNull
    public String getParent() {
        return parent;
    }

    /**
     * @return the command name
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * @return the commands aliases
     */
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * @return the command usage
     */
    public String getUsage() {
        return getUsage(COLOR_COMMAND, COLOR_ARGS_USAGE);
    }

    /**
     * Returns the command usage with colors depending on the senders permissions.
     *
     * @param sender the sender of the command
     * @return the command usage
     */
    public String getUsage(CommandSender sender) {
        ChatColor commandColor = COLOR_COMMAND;
        ChatColor argsColor = COLOR_ARGS_USAGE;

        boolean permission = isPermitted(sender) && !(isPlayerOnly() && !(sender instanceof Player));

        if (!permission) commandColor = COLOR_COMMAND_NO_PERMISSION;
        return getUsage(commandColor, argsColor);
    }

    private String getUsage(ChatColor commandColor, ChatColor argsColor) {
        StringBuilder sb = new StringBuilder(commandColor + "/" + parent + " " + name);

        for (String alias : aliases) {
            sb.append(",").append(alias);
        }
        if (!argsUsage.isEmpty()) {
            sb.append(argsColor).append(" ").append(argsUsage);
        }

        return sb.toString();
    }

    /**
     * @return the arguments alternatives
     */
    public List<String> getArgsAlternatives() {
        return argsAlternatives;
    }

    /**
     * @return if the command is for players only
     */
    public boolean isPlayerOnly() {
        return playerOnly;
    }

    /**
     * @return the command description
     */
    public String getDescription() {
        return COLOR_DESCRIPTION + description;
    }

    /**
     * @return the command permission
     */
    @Nullable
    public String getPermission() {
        return permission;
    }

}
