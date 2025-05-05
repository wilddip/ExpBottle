package me.brannstroom.expbottle.handlers;

import me.brannstroom.expbottle.ExpBottle;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class InfoKeeper {

    private static final ExpBottle plugin = ExpBottle.instance;

    public static List<String> bottleLore;

    public static boolean sendSoundEnabled;
    public static String sendSoundName;
    public static float sendSoundVolume;
    public static float sendSoundPitch;

    public static String getInventoryFullAction;

    public static int splitMinLevelsPerBottle;
    public static int splitLevelStep;

    public static void loadConfigValues() {
        FileConfiguration config = plugin.getConfig();

        List<String> rawLore = config.getStringList("bottle_lore");
        bottleLore = rawLore.stream()
                .collect(Collectors.toList());
                
        sendSoundEnabled = config.getBoolean("command.send.sound.enabled", true);
        sendSoundName = config.getString("command.send.sound.name", "ENTITY_EXPERIENCE_ORB_PICKUP");
        sendSoundVolume = (float) config.getDouble("command.send.sound.volume", 1.0);
        sendSoundPitch = (float) config.getDouble("command.send.sound.pitch", 1.0);

        getInventoryFullAction = config.getString("command.get.inventory_full_action", "drop").toLowerCase(Locale.ROOT);
        if (!getInventoryFullAction.equals("error") && !getInventoryFullAction.equals("drop")) {
            plugin.getLogger().warning("Invalid value for 'command.get.inventory_full_action': '"
                    + getInventoryFullAction + "'. Using default 'drop'.");
            getInventoryFullAction = "drop";
        }

        splitMinLevelsPerBottle = config.getInt("command.split.min_levels_per_bottle", 10);
        if (splitMinLevelsPerBottle <= 0) {
            plugin.getLogger().warning("Invalid value for 'command.split.min_levels_per_bottle': '"
                    + splitMinLevelsPerBottle + "'. Must be positive. Using default 10.");
            splitMinLevelsPerBottle = 10;
        }
        
        splitLevelStep = config.getInt("command.split.level_step", 1);
        if (splitLevelStep <= 0) {
            plugin.getLogger().warning("Invalid value for 'command.split.level_step': '" + splitLevelStep
                    + "'. Must be positive. Using default 1.");
            splitLevelStep = 1;
        }

        ExpBottle.instance.getLogger().info("Loaded configuration values from config.yml");
    }
}