package kasperstudios.khgui.core;

import kasperstudios.khgui.api.*;
import java.util.*;

public class CodeCompletionManager {

    private static final Map<String, String> commandParameters = new HashMap<>();
    private static final Map<String, String> builtInParameters = new HashMap<>();
    private static final Map<String, String> snippets = new HashMap<>();
    private static final Set<String> keywords = new HashSet<>(Arrays.asList(
            "if", "else", "while", "for", "function", "return", "break", "continue"));

    private static final Map<String, String> snippetDescriptions = new HashMap<>();

    private static final Map<String, String> environmentVariables = new HashMap<>();
    private static final Set<String> userVariables = new HashSet<>();

    // Cache likely static for now, or refreshed via refreshCompletions
    private static final Map<String, List<String>> objectMembers = new HashMap<>();
    private static final Set<String> objectDisplayNames = new HashSet<>();

    private static final List<ServerCommandMetadata> serverCommands = new ArrayList<>();

    static {
        // Core library initialization
    }

    public static List<ServerCommandMetadata> getServerCommandMetadata() {
        return serverCommands;
    }

    public static void setServerCommands(List<ServerCommandMetadata> commands) {
        serverCommands.clear();
        serverCommands.addAll(commands);
    }

    public static void refreshCompletions(IEditorContext context) {
        if (context == null)
            return;

        try {
            objectMembers.clear();
            objectDisplayNames.clear();
            environmentVariables.clear();

            // Refreshed Env Vars
            Map<String, String> envVars = context.getEnvironmentVariables();
            if (envVars != null) {
                for (Map.Entry<String, String> entry : envVars.entrySet()) {
                    String varName = "$" + entry.getKey();
                    environmentVariables.put(varName, entry.getValue());
                }
            }

            // Sync user vars
            List<String> userVars = context.getUserVariables();
            if (userVars != null) {
                userVariables.addAll(userVars);
            }

        } catch (Exception e) {
            System.err.println("Failed to initialize V2 completions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<String> getMemberCompletions(String objectName, String partialMember) {
        List<String> result = new ArrayList<>();
        List<String> members = objectMembers.get(objectName.toLowerCase());

        if (members != null) {
            for (String member : members) {
                if (partialMember.isEmpty() || member.toLowerCase().startsWith(partialMember.toLowerCase())) {
                    result.add(member);
                }
            }
        }
        return result;
    }

    public static boolean hasObjectSuggestions(String objectName) {
        return objectMembers.containsKey(objectName.toLowerCase());
    }

    public static void addUserVariable(String name) {
        userVariables.add(name);
    }

    public static void clearUserVariables() {
        userVariables.clear();
    }

    public static List<String> getArgumentCompletions(String commandName, String partialArg) {
        List<String> result = new ArrayList<>();
        // Now handled by ICompletionProvider in KasHub
        return result;
    }

    public static String getArgumentDescription(String command, String argument) {
        return "";
    }

    public static boolean hasArgumentSuggestions(String command) {
        return false; // Handled by ICompletionProvider now
    }

    public static String getCommandDescription(String command) {
        if (command.startsWith("$")) {
            String description = environmentVariables.get(command.toUpperCase());
            if (description != null) {
                return description;
            }
            if (userVariables.contains(command.substring(1))) {
                return "User variable";
            }
            return "";
        }

        for (ServerCommandMetadata meta : serverCommands) {
            if (meta.name.equalsIgnoreCase(command)) {
                return meta.description + "\nSyntax: " + meta.syntax;
            }
        }

        // Deprecated ICommand logic removed

        if (snippets.containsKey(command)) {
            return snippetDescriptions.getOrDefault(command, "");
        }

        return "";
    }

    public static String getCommandParameters(String command) {
        command = command.toLowerCase();

        for (ServerCommandMetadata meta : serverCommands) {
            if (meta.name.equalsIgnoreCase(command)) {
                return meta.syntax;
            }
        }

        // Deprecated ICommand logic removed

        if (builtInParameters.containsKey(command)) {
            return builtInParameters.get(command);
        }
        return commandParameters.getOrDefault(command, "");
    }

    public static List<String> getCompletions(String partialWord) {
        List<String> result = new ArrayList<>();

        for (String keyword : keywords) {
            if (keyword.toLowerCase().startsWith(partialWord.toLowerCase())) {
                result.add(keyword);
            }
        }

        if (partialWord.startsWith("$")) {
            String searchWord = partialWord.substring(1).toUpperCase();
            for (String var : environmentVariables.keySet()) {
                if (var.substring(1).startsWith(searchWord)) {
                    result.add(var);
                }
            }

            for (String var : userVariables) {
                String varWithPrefix = "$" + var;
                if (varWithPrefix.toLowerCase().startsWith(partialWord.toLowerCase())) {
                    result.add(varWithPrefix);
                }
            }
            return result;
        }

        for (ServerCommandMetadata meta : serverCommands) {
            if (meta.name.toLowerCase().startsWith(partialWord.toLowerCase())) {
                if (!result.contains(meta.name)) {
                    result.add(meta.name);
                }
            }
        }

        // Deprecated ICommand logic removed

        for (String snippet : snippets.keySet()) {
            if (snippet.toLowerCase().startsWith(partialWord.toLowerCase())) {
                if (!result.contains(snippet)) {
                    result.add(snippet);
                }
            }
        }

        for (String objName : objectDisplayNames) {
            if (objName.toLowerCase().startsWith(partialWord.toLowerCase())) {
                if (!result.contains(objName)) {
                    result.add(objName);
                }
            }
        }

        return result;
    }

    public static String getSnippet(String name) {
        return snippets.getOrDefault(name.toLowerCase(), "");
    }
}
