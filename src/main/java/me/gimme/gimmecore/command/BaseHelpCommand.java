package me.gimme.gimmecore.command;

import me.gimme.gimmecore.util.Pageifier;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows a list of all the sub commands for a specific parent command.
 */
public abstract class BaseHelpCommand extends BaseCommand {

    protected static final String PAGE_PLACEHOLDER = "%page%";
    private static final String ERROR_PAGE_NUMBER_OOB = "Page must be between 1 and %n%";

    private final List<BaseCommand> commandList;
    private final String header;
    private final int commandsPerPage;
    private final boolean hideUnpermittedCommands;
    private final boolean showAliases;

    protected BaseHelpCommand(@NotNull CommandManager commandManager, @NotNull String parent, @Nullable String header) {
        this(commandManager, parent, header, true);
    }

    protected BaseHelpCommand(@NotNull CommandManager commandManager, @NotNull String parent, @Nullable String header,
                              boolean showAliases) {
        this(commandManager, parent, header, showAliases, 9, true);
    }

    protected BaseHelpCommand(@NotNull CommandManager commandManager, @NotNull String parent, @Nullable String header,
                              boolean showAliases, int commandsPerPage, boolean hideUnpermittedCommands) {
        super(parent, "help");

        addAlias("?");
        setArgsUsage("[page=1]");
        addArgsAlternative("1");
        setMinArgs(0);
        setMaxArgs(1);
        setPlayerOnly(false);
        setDescription("Shows a list of all the commands");

        this.commandList = commandManager.getCommandList(parent);
        this.commandsPerPage = commandsPerPage;
        this.hideUnpermittedCommands = hideUnpermittedCommands;
        this.showAliases = showAliases;
        this.header = header;
    }

    @Override
    @NotNull
    protected String execute(@NotNull CommandSender sender, @NotNull String[] args) {
        String pageInput = args.length > 0 ? args[0] : "1";
        int page;

        // Validation
        try {
            page = Integer.parseInt(pageInput);
        } catch (NumberFormatException e) {
            return errorMessageWithUsage(CommandError.NOT_A_NUMBER, pageInput);
        }
        int perPage = (sender instanceof ConsoleCommandSender) ? -1 : commandsPerPage;

        List<BaseCommand> commandListSnapshot = new ArrayList<>(commandList);
        if (hideUnpermittedCommands && !(sender instanceof ConsoleCommandSender))
            commandListSnapshot.removeIf(command -> !command.isPermitted(sender));

        Pageifier.PageResult<BaseCommand> pageResult = Pageifier.getPage(commandListSnapshot, perPage, page);

        if (!(1 <= page && page <= pageResult.totalPages)) {
            return errorMessage(ERROR_PAGE_NUMBER_OOB.replaceAll("%n%", pageResult.totalPages + ""));
        }

        return getFormattedMessage(sender, pageResult.content, page, pageResult.totalPages);
    }

    @NotNull
    private String getFormattedMessage(@NotNull CommandSender messageReceiver, @NotNull List<BaseCommand> commands, int page, int totalPages) {
        String message = "";

        final String header = getListHeader(page, totalPages);
        final String content = getListContent(messageReceiver, commands);
        final String footer = getListFooter();

        if (messageReceiver instanceof ConsoleCommandSender) message += newLine;
        message += header;
        if (!header.isEmpty()) message += newLine;
        message += content;
        if (!footer.isEmpty()) message += newLine;
        message += footer;

        return message;
    }

    @NotNull
    protected String getListHeader(int page, int totalPages) {
        if (header == null) return "";
        return header.replaceAll(PAGE_PLACEHOLDER, page + "/" + totalPages);
    }

    @NotNull
    protected String getListContent(@NotNull CommandSender messageReceiver, @NotNull List<BaseCommand> commands) {
        StringBuilder sb = new StringBuilder();
        for (BaseCommand c : commands) {
            if (!sb.toString().isEmpty()) sb.append(newLine);
            sb.append(c.getUsage(messageReceiver, showAliases)).append(" ").append(c.getDescription());
        }
        return sb.toString();
    }

    @NotNull
    protected String getListFooter() {
        return "";
    }

}
