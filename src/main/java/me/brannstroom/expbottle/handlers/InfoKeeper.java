package me.brannstroom.expbottle.handlers;

import me.brannstroom.expbottle.ExpBottle;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class InfoKeeper {

    // #region Fields
    private static final ExpBottle plugin = ExpBottle.instance;

    public static List<String> bottleLore;

    public static boolean sendReceiveSoundEnabled;
    public static String sendReceiveSoundName;
    public static float sendReceiveSoundVolume;
    public static float sendReceiveSoundPitch;

    public static boolean getSoundEnabled;
    public static String getSoundName;
    public static float getSoundVolume;
    public static float getSoundPitch;

    public static boolean splitSoundEnabled;
    public static String splitSoundName;
    public static float splitSoundVolume;
    public static float splitSoundPitch;

    public static String getInventoryFullAction;

    public static int splitMinLevelsPerBottle;
    public static int splitLevelStep;
    public static int splitExpStep;
    // #endregion

    // #region Config Loading
    public static void loadConfigValues() {
        FileConfiguration config = plugin.getConfig();

        List<String> rawLore = config.getStringList("bottle_lore");
        bottleLore = rawLore.stream()
                .collect(Collectors.toList());

        sendReceiveSoundEnabled = config.getBoolean("sound_settings.send_receive.enabled", true);
        sendReceiveSoundName = config.getString("sound_settings.send_receive.name", "ENTITY_PLAYER_LEVELUP");
        sendReceiveSoundVolume = (float) config.getDouble("sound_settings.send_receive.volume", 1.0);
        sendReceiveSoundPitch = (float) config.getDouble("sound_settings.send_receive.pitch", 1.0);

        getSoundEnabled = config.getBoolean("sound_settings.get.enabled", true);
        getSoundName = config.getString("sound_settings.get.name", "ENTITY_ITEM_PICKUP");
        getSoundVolume = (float) config.getDouble("sound_settings.get.volume", 1.0);
        getSoundPitch = (float) config.getDouble("sound_settings.get.pitch", 1.0);

        splitSoundEnabled = config.getBoolean("sound_settings.split.enabled", true);
        splitSoundName = config.getString("sound_settings.split.name", "BLOCK_AMETHYST_BLOCK_CHIME");
        splitSoundVolume = (float) config.getDouble("sound_settings.split.volume", 1.0);
        splitSoundPitch = (float) config.getDouble("sound_settings.split.pitch", 1.0);

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

        splitExpStep = config.getInt("command.split.exp_step", 1);
        if (splitExpStep <= 0) {
            plugin.getLogger().warning("Invalid value for 'command.split.exp_step': '" + splitExpStep
                    + "'. Must be positive. Using default 1.");
            splitExpStep = 1;
        }

        ExpBottle.instance.getLogger().info("Loaded configuration values from config.yml");
    }
    // #endregion
}