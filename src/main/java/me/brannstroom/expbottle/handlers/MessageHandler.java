package me.brannstroom.expbottle.handlers;

import me.brannstroom.expbottle.ExpBottle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MessageHandler {

    private static FileConfiguration config;
    private static String prefixString;
    private static Component prefixComponent;
    private static String commandName;

    static {
        loadMessages();
    }

    public static class MessageContext {
        private final CommandSender sender;
        private final String commandLabel;
        private final Map<String, String> placeholders;

        public MessageContext(CommandSender sender, String commandLabel) {
            this.sender = sender;
            this.commandLabel = commandLabel;
            this.placeholders = new HashMap<>();
            
            if (commandLabel != null) {
                placeholders.put("%command%", commandLabel);
            }
            
            if (sender instanceof Player) {
                placeholders.put("%player%", sender.getName());
            }
        }

        public MessageContext add(String placeholder, String value) {
            placeholders.put(placeholder, value);
            return this;
        }

        public CommandSender getSender() {
            return sender;
        }
        
        public String getCommandLabel() {
            return placeholders.get("%command%");
        }

        public Map<String, String> getPlaceholders() {
            return placeholders;
        }
    }

    private static String getRawMessage(String path) {
        return config.getString("messages." + path);
    }

    private static String getDefaultMessage(String path) {
        switch (path) {
            case "prefix":
                return "&b[ExpBottle] &r";
            case "general.no_permission":
                return "&cYou don't have permission.";
            case "general.players_only":
                return "&cPlayers only.";
            default:
                return "&cMissing message: messages." + path;
        }
    }

    public static String getMessage(String path) {
        String message = getRawMessage(path);
        return message != null ? message : getDefaultMessage(path);
    }
    
    public static void sendMessage(MessageContext context, String path) {
        String message = getMessage(path);
        message = replacePlaceholders(message, context);
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        context.getSender().sendMessage(prefixComponent.append(component));
    }

    public static void sendMessage(CommandSender sender, String path, String... replacements) {
        MessageContext context = new MessageContext(sender, null);
        
        if (replacements != null && replacements.length % 2 == 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                context.add(replacements[i], replacements[i + 1]);
            }
        }
        
        sendMessage(context, path);
    }
    
    public static void sendRawMessage(MessageContext context, String path) {
        String message = getMessage(path);
        message = replacePlaceholders(message, context);
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        context.getSender().sendMessage(component);
    }

    public static void sendRawMessage(CommandSender sender, String path, String... replacements) {
        MessageContext context = new MessageContext(sender, null);
        
        if (replacements != null && replacements.length % 2 == 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                context.add(replacements[i], replacements[i + 1]);
            }
        }
        
        sendRawMessage(context, path);
    }

    public static Component getFormattedMessage(String path, MessageContext context) {
        String message = getMessage(path);
        message = replacePlaceholders(message, context);
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }
    
    public static Component getFormattedMessage(String path, CommandSender sender, String... replacements) {
        MessageContext context = new MessageContext(sender, null);
        
        if (replacements != null && replacements.length % 2 == 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                context.add(replacements[i], replacements[i + 1]);
            }
        }
        
        return getFormattedMessage(path, context);
    }

    private static String replacePlaceholders(String message, MessageContext context) {
        if (message == null)
            return "";

        for (Map.Entry<String, String> entry : context.getPlaceholders().entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        
        if (message.contains("%command%")) {
            message = message.replace("%command%", commandName);
        }
        
        return message;
    }

    private static String replacePlaceholders(String message, CommandSender sender, String... replacements) {
        if (message == null)
            return "";

        if (sender instanceof Player) {
            message = message.replace("%player%", sender.getName());
        }

        if (replacements != null && replacements.length % 2 == 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                if (replacements[i] != null && replacements[i + 1] != null) {
                    message = message.replace(replacements[i], replacements[i + 1]);
                }
            }
        }
        
        if (message.contains("%command%")) {
            message = message.replace("%command%", commandName);
        }
        
        return message;
    }

    public static void loadMessages() {
        config = ExpBottle.instance.getConfig();
        prefixString = getMessage("prefix");
        prefixComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(prefixString);
        commandName = Objects.requireNonNull(config.getString("command.name", "exp"));
    }

    public static void sendRawText(MessageContext context, String text) {
        String processedText = replacePlaceholders(text, context);
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(processedText);
        context.getSender().sendMessage(component);
    }

    public static void sendRawText(CommandSender sender, String text, String... replacements) {
        MessageContext context = new MessageContext(sender, null);
        
        if (replacements != null && replacements.length % 2 == 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                context.add(replacements[i], replacements[i + 1]);
            }
        }
        
        sendRawText(context, text);
    }
}