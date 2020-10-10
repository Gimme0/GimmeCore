package me.gimme.gimmecore.command;

import com.google.common.base.Strings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Handles the execution and tab completion of all commands.
 * <p>
 * All commands need to be registered first.
 */
public class CommandManager implements TabExecutor {

    public static final String WILDCARD_PLACEHOLDER = "%*%";

    private JavaPlugin plugin;

    private CommandCollection commands = new CommandCollection();
    private Map<String, PlaceholderCollection<?>> placeholders = new HashMap<>();

    public CommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String subCommand = "help";
        String[] subCommandArgs = new String[0];

        if (args.length > 0) {
            subCommand = args[0].toLowerCase();
            if (args.length > 1) {
                subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
            }
        }

        if (commands.contains(command.getName(), subCommand)) {
            commands.get(command.getName(), subCommand).handle(sender, subCommandArgs);
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> result = new ArrayList<>();

        if (args.length >= 1) {
            String subCommand = args[0];
            if (args.length == 1) {
                for (BaseCommand c : commands.getCommands(command.getName())) {
                    if (!c.isPermitted(sender)) continue;
                    if (c.getName().startsWith(subCommand)) result.add(c.getName());
                    for (String a : c.getAliases()) {
                        if (a.startsWith(subCommand)) result.add(a);
                    }
                }
            } else {
                if (commands.contains(command.getName(), subCommand)) {
                    BaseCommand c = commands.get(command.getName(), subCommand);
                    if (c.isPermitted(sender)) {
                        String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
                        Set<String> usedPlaceholders = new HashSet<>();
                        for (String argsAlternative : c.getArgsAlternatives()) {
                            String[] argsAlternativeArray = argsAlternative.split(" ");
                            if (subCommandArgs.length > argsAlternativeArray.length) continue;
                            int currentArgIndex = subCommandArgs.length - 1;
                            boolean matching = true;
                            for (int i = 0; i < currentArgIndex; i++) {
                                if (!subCommandArgs[i].equals(argsAlternativeArray[i]) &&
                                        !placeholders.containsKey(argsAlternativeArray[i]) &&
                                        !argsAlternativeArray[i].equals(WILDCARD_PLACEHOLDER)) {
                                    matching = false;
                                    break;
                                }
                            }
                            if (!matching) continue;

                            String currentAltArg = argsAlternativeArray[currentArgIndex];
                            if (currentAltArg.equals(WILDCARD_PLACEHOLDER)) continue;
                            if (placeholders.containsKey(currentAltArg)) {
                                if (!usedPlaceholders.contains(currentAltArg)) {
                                    usedPlaceholders.add(currentAltArg);
                                    for (String s : placeholders.get(currentAltArg).getList()) {
                                        if (s.toLowerCase().startsWith(subCommandArgs[currentArgIndex].toLowerCase())) {
                                            result.add(s);
                                        }
                                    }
                                }
                            } else {
                                if (currentAltArg.toLowerCase().startsWith(subCommandArgs[currentArgIndex].toLowerCase())) {
                                    result.add(currentAltArg);
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Registers a command to be handled by this command manager.
     *
     * @param command the command to be registered
     */
    public void register(@NotNull BaseCommand command) {
        PluginCommand pluginCommand = plugin.getCommand(command.getParent());
        if (pluginCommand == null) {
            plugin.getLogger().log(Level.WARNING,
                    "Could not register the command \"" + command.getParent() + " " + command.getName() +
                            "\". You need to add the command \"" + command.getParent() + "\" in your plugin.yml!");
            return;
        }

        pluginCommand.setExecutor(this);
        commands.add(command);
        registerPermission(command);
    }

    /**
     * Registers a basic help command for all commands under the specified parent.
     *
     * @param parent the parent command name
     */
    public void registerBasicHelpCommand(String parent) {
        register(new BaseHelpCommand(this, parent, null, true) {
        });
    }

    /**
     * Registers a placeholder string that represents a list of other arguments to be used during tab completion in its
     * place.
     * <p>
     * For example, if you register the placeholder "%team%" with a supplier of a collection of your custom Team objects
     * and the function Team::getName, then every instance of the term %team% in your commands' args alternatives is
     * replaced with all the names of the teams during tab completion.
     *
     * @param placeholder    the placeholder string for the collection
     * @param supplier       a supplier of the collection of data to get the strings from for the placeholder
     * @param stringFunction a function on the collection's objects that returns a string
     * @param <T>            the type of data to get the strings from
     * @throws IllegalArgumentException if the placeholder string is the same as {@link this#WILDCARD_PLACEHOLDER}
     */
    public <T> void registerPlaceholder(@NotNull String placeholder,
                                        @NotNull Supplier<Collection<? extends T>> supplier,
                                        @NotNull Function<? super T, ? extends String> stringFunction) {
        if (placeholder.equals(WILDCARD_PLACEHOLDER))
            throw new IllegalArgumentException("Placeholder string cannot be the same as the wildcard placeholder");
        placeholders.put(placeholder, new PlaceholderCollection<>(supplier, stringFunction));
    }

    /**
     * @return the list of registered commands
     */
    public List<BaseCommand> getCommandList(String parentCommand) {
        return commands.getCommands(parentCommand);
    }

    /**
     * Registers the specified command's permission and adds it as a child to the corresponding wildcard permission.
     * E.g. the permission "plugin.p.command" becomes a child of "plugin.p.*".
     *
     * @param command the command with the permission to register
     */
    private void registerPermission(BaseCommand command) {
        if (Strings.isNullOrEmpty(command.getPermission())) return;

        PluginManager pm = plugin.getServer().getPluginManager();
        Permission permission = pm.getPermission(command.getPermission());
        if (permission == null) {
            permission = new Permission(command.getPermission(),
                    "Use /" + command.getParent() + " " + command.getName());
            plugin.getServer().getPluginManager().addPermission(permission);
        }

        String[] permissionComponents = command.getPermission().split("\\.");
        StringBuilder parent = new StringBuilder();
        for (int i = 0; i < permissionComponents.length - 1; i++) {
            parent.append(permissionComponents[i]).append(".");
        }
        parent.append("*");
        permission.addParent(parent.toString(), true);
    }
}
