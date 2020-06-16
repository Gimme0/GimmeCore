package me.gimme.gimmecore.command;

import java.util.*;

class CommandCollection {

    private Map<String, Map<String, BaseCommand>> commandByNameByParent = new HashMap<>();
    private Map<String, List<BaseCommand>> commandsByParent = new HashMap<>();

    void add(BaseCommand command) {
        commandByNameByParent.computeIfAbsent(command.getParent(), k -> new HashMap<>());
        commandsByParent.computeIfAbsent(command.getParent(), k -> new ArrayList<>());

        commandByNameByParent.get(command.getParent()).put(command.getName(), command);
        for (String alias : command.getAliases()) {
            commandByNameByParent.get(command.getParent()).put(alias, command);
        }
        commandsByParent.get(command.getParent()).add(command);
    }

    BaseCommand get(String parent, String command) {
        return commandByNameByParent.get(parent).get(command);
    }

    boolean contains(String parent, String command) {
        return commandByNameByParent.containsKey(parent) && commandByNameByParent.get(parent).containsKey(command);
    }

    List<BaseCommand> getCommands(String parent) {
        if (!commandsByParent.containsKey(parent)) commandsByParent.put(parent, new ArrayList<>());
        return commandsByParent.get(parent);
    }

    Set<String> getCommandsAliases(String parent) {
        return commandByNameByParent.get(parent).keySet();
    }

}
