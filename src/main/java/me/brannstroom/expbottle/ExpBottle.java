package me.brannstroom.expbottle;

import me.brannstroom.expbottle.command.ExpCommand;
import me.brannstroom.expbottle.handlers.InfoKeeper;
import me.brannstroom.expbottle.handlers.MessageHandler;
import me.brannstroom.expbottle.listeners.ExpBottleListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import io.papermc.paper.plugin.configuration.PluginMeta;

public class ExpBottle extends JavaPlugin {

    public static ExpBottle instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        reloadPluginConfig();

        registerCommands();
        registerListeners();
        
        PluginMeta meta = this.getPluginMeta();
        getLogger().info("ExpBottle v" + meta.getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ExpBottle disabled!");
        instance = null;
    }

    private void registerCommands() {
        String commandName = getConfig().getString("command.name", "exp");
        PluginCommand command = getCommand(commandName);
        if (command != null) {
            ExpCommand expCommandExecutor = new ExpCommand();
            command.setExecutor(expCommandExecutor);
            command.setTabCompleter(expCommandExecutor);
        } else {
            getLogger().severe("Could not register command '/" + commandName + "'! Check plugin.yml and config.yml.");
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ExpBottleListener(), this);
    }

    public void reloadPluginConfig() {
        reloadConfig();
        InfoKeeper.loadConfigValues();
        MessageHandler.loadMessages();
        getLogger().info("Configuration reloaded.");
    }
}