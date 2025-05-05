package me.brannstroom.expbottle.command;

import me.brannstroom.expbottle.ExpBottle;
import me.brannstroom.expbottle.handlers.InfoKeeper;
import me.brannstroom.expbottle.handlers.MessageHandler;
import me.brannstroom.expbottle.model.Experience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ExpCommand implements CommandExecutor, TabCompleter {

    private final ExpBottle plugin = ExpBottle.instance;
    private final NamespacedKey expKey = new NamespacedKey(plugin, "experience_points");

    private static final String PERM_BASE = "expbottle.use";
    private static final String PERM_GET = "expbottle.get";
    private static final String PERM_SEND = "expbottle.send";
    private static final String PERM_SPLIT = "expbottle.split";
    private static final String PERM_HELP = "expbottle.help";
    private static final String PERM_RELOAD = "expbottle.reload";

    private int getExpToNextLevel(int level) {
        if (level < 0) return 0;
        if (level <= 15) return 2 * level + 7;
        if (level <= 30) return 5 * level - 38;
        return 9 * level - 158;
    }

    private int getExpInLevel(Player player) {
        return (int) (player.getExp() * getExpToNextLevel(player.getLevel()));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MessageHandler.MessageContext ctx = new MessageHandler.MessageContext(sender, label);

        if (!(sender instanceof Player)) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission(PERM_RELOAD)) {
                    MessageHandler.sendMessage(ctx, "general.no_permission");
                    return true;
                }
                handleReload(sender, ctx);
            } else {
                MessageHandler.sendMessage(ctx, "general.console_usage_hint");
            }
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(PERM_BASE)) {
            MessageHandler.sendMessage(ctx, "general.no_permission");
            return true;
        }

        if (args.length == 0) {
            handleInfo(player, ctx);
            return true;
        }

        String subCmd = args[0].toLowerCase();

        switch (subCmd) {
            case "g":
            case "get":
                handleGetExp(player, args, ctx);
                break;
            case "send":
                handleSendExp(player, args, ctx);
                break;
            case "s":
            case "split":
                handleSplitExp(player, args, ctx);
                break;
            case "h":
            case "help":
                handleHelp(player, ctx);
                break;
            case "reload":
                if (!player.hasPermission(PERM_RELOAD)) {
                    MessageHandler.sendMessage(ctx, "general.no_permission");
                } else {
                    handleReload(player, ctx);
                }
                break;
            default:
                MessageHandler.sendMessage(ctx, "general.unknown_subcommand");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!(sender instanceof Player)) {
            if (args.length == 1) {
                StringUtil.copyPartialMatches(args[0], Collections.singletonList("reload"), completions);
            }
            return completions;
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            List<String> subCmds = new ArrayList<>();
            if (player.hasPermission(PERM_GET)) subCmds.add("get");
            if (player.hasPermission(PERM_SEND)) subCmds.add("send");
            if (player.hasPermission(PERM_SPLIT)) subCmds.add("split");
            if (player.hasPermission(PERM_HELP)) subCmds.add("help");
            if (player.hasPermission(PERM_RELOAD)) subCmds.add("reload");

            StringUtil.copyPartialMatches(args[0], subCmds, completions);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("send")) {
            if (player.hasPermission(PERM_SEND)) {
                List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> !name.equals(player.getName()))
                        .collect(Collectors.toList());
                StringUtil.copyPartialMatches(args[1], playerNames, completions);
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("g"))) {
            if (player.hasPermission(PERM_GET)) {
                StringUtil.copyPartialMatches(args[1], Collections.singletonList("all"), completions);
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("send")) {
            if (player.hasPermission(PERM_SEND)) {
                StringUtil.copyPartialMatches(args[2], Collections.singletonList("all"), completions);
            }
        }

        Collections.sort(completions);
        return completions;
    }

    private void handleInfo(Player player, MessageHandler.MessageContext ctx) {
        if (!player.hasPermission(PERM_BASE)) {
            MessageHandler.sendMessage(ctx, "general.no_permission");
            return;
        }

        int level = player.getLevel();
        int expToNext = getExpToNextLevel(level);
        int expInLevel = getExpInLevel(player);
        int totalExp = Experience.getExp(player);

        ctx.add("%level%", String.valueOf(level))
           .add("%exp%", String.valueOf(expInLevel))
           .add("%next_level_exp%", String.valueOf(expToNext))
           .add("%total_exp%", String.valueOf(totalExp));

        MessageHandler.sendRawMessage(ctx, "info.display");
    }

    private void handleGetExp(Player player, String[] args, MessageHandler.MessageContext ctx) {
        if (!player.hasPermission(PERM_GET)) {
            MessageHandler.sendMessage(ctx, "general.no_permission");
            return;
        }

        int currentExp = Experience.getExp(player);
        if (currentExp == 0) {
            MessageHandler.sendMessage(ctx, "get.error.no_exp");
            return;
        }

        int expToWithdraw;
        int levelEquiv = -1;
        boolean specificLevel = false;

        if (args.length > 1 && !args[1].equalsIgnoreCase("all")) {
            try {
                levelEquiv = Integer.parseInt(args[1]);
                specificLevel = true;
                if (levelEquiv <= 0) {
                    MessageHandler.sendMessage(ctx, "general.positive_number");
                    return;
                }
                expToWithdraw = Experience.getExpFromLevel(levelEquiv);

                if (expToWithdraw > currentExp) {
                    ctx.add("%amount_lvl%", String.valueOf(levelEquiv))
                       .add("%amount_xp%", String.valueOf(expToWithdraw))
                       .add("%total_exp%", String.valueOf(currentExp));
                    MessageHandler.sendMessage(ctx, "get.error.not_enough_lvl");
                    return;
                }
            } catch (NumberFormatException e) {
                ctx.add("%input%", args[1]);
                MessageHandler.sendMessage(ctx, "general.invalid_number");
                return;
            }
        } else {
            expToWithdraw = currentExp;
            levelEquiv = player.getLevel();
            specificLevel = false;
        }

        if (expToWithdraw <= 0) {
            MessageHandler.sendMessage(ctx, "general.positive_number");
            return;
        }

        if (expToWithdraw > currentExp) {
            ctx.add("%needed%", String.valueOf(expToWithdraw))
               .add("%current%", String.valueOf(currentExp));
            MessageHandler.sendMessage(ctx, "get.error.not_enough_exp");
            return;
        }

        if (InfoKeeper.getInventoryFullAction.equals("error") && !canReceiveItem(player)) {
            MessageHandler.sendMessage(ctx, "get.error.inventory_full");
            return;
        }

        Experience.changeExp(player, -expToWithdraw);

        if (specificLevel) {
            ctx.add("%amount_lvl%", String.valueOf(levelEquiv))
               .add("%amount_xp%", String.valueOf(expToWithdraw));
            MessageHandler.sendMessage(ctx, "get.success.levels");
        } else {
            ctx.add("%amount_xp%", String.valueOf(expToWithdraw));
            MessageHandler.sendMessage(ctx, "get.success.all");
        }

        createAndGiveBottle(player, expToWithdraw, levelEquiv, ctx);
    }

    private void createAndGiveBottle(Player player, int expInBottle, int levelEquiv) {
        createAndGiveBottle(player, expInBottle, levelEquiv, 
            new MessageHandler.MessageContext(player, null));
    }

    private void createAndGiveBottle(Player player, int expInBottle, int levelEquiv, 
            MessageHandler.MessageContext ctx) {
        ItemStack bottle = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = bottle.getItemMeta();
        if (meta == null) {
            plugin.getLogger().warning("Could not get ItemMeta for EXPERIENCE_BOTTLE!");
            return;
        }

        meta.displayName(
                Component.translatable("item.minecraft.experience_bottle").decoration(TextDecoration.ITALIC, false));

        List<Component> loreComps = new ArrayList<>();
        List<String> lore = InfoKeeper.bottleLore;
        int fullLevels = (int) Experience.getLevelFromExp(expInBottle);

        for (String line : lore) {
            line = line.replace("%amount_xp%", String.valueOf(expInBottle))
                    .replace("%amount_lvl%", String.valueOf(levelEquiv))
                    .replace("%full_levels%", String.valueOf(fullLevels));
            loreComps.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
        }

        meta.getPersistentDataContainer().set(expKey, PersistentDataType.INTEGER, expInBottle);
        meta.lore(loreComps);
        bottle.setItemMeta(meta);

        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), bottle);
            MessageHandler.sendMessage(ctx, "get.success.inventory_full_drop");
        } else {
            player.getInventory().addItem(bottle);
        }
    }

    private void handleSendExp(Player sender, String[] args, MessageHandler.MessageContext ctx) {
        if (!sender.hasPermission(PERM_SEND)) {
            MessageHandler.sendMessage(ctx, "general.no_permission");
            return;
        }

        if (args.length < 2) {
            MessageHandler.sendMessage(ctx, "send.usage");
            return;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null) {
            ctx.add("%target%", targetName);
            MessageHandler.sendMessage(ctx, "general.player_not_found");
            return;
        }

        if (target.equals(sender)) {
            MessageHandler.sendMessage(ctx, "send.error.cannot_self");
            return;
        }

        int senderExp = Experience.getExp(sender);
        if (senderExp == 0) {
            MessageHandler.sendMessage(ctx, "send.error.no_exp");
            return;
        }

        int expToSend;
        int levelsToSend = -1;
        boolean specificLevel = false;

        if (args.length > 2 && !args[2].equalsIgnoreCase("all")) {
            try {
                levelsToSend = Integer.parseInt(args[2]);
                specificLevel = true;
                if (levelsToSend <= 0) {
                    MessageHandler.sendMessage(ctx, "general.positive_number");
                    return;
                }
                expToSend = Experience.getExpFromLevel(levelsToSend);

                if (senderExp < expToSend) {
                    ctx.add("%amount_lvl%", String.valueOf(levelsToSend))
                       .add("%amount_xp%", String.valueOf(expToSend))
                       .add("%total_exp%", String.valueOf(senderExp));
                    MessageHandler.sendMessage(ctx, "send.error.not_enough_lvl");
                    return;
                }
            } catch (NumberFormatException e) {
                ctx.add("%input%", args[2]);
                MessageHandler.sendMessage(ctx, "general.invalid_number");
                return;
            }
        } else {
            expToSend = senderExp;
            levelsToSend = sender.getLevel();
            specificLevel = false;
        }

        if (expToSend <= 0) {
            MessageHandler.sendMessage(ctx, "general.positive_number");
            return;
        }

        if (expToSend > senderExp) {
            ctx.add("%needed%", String.valueOf(expToSend))
               .add("%current%", String.valueOf(senderExp));
            MessageHandler.sendMessage(ctx, "send.error.not_enough_exp");
            return;
        }

        Experience.changeExp(sender, -expToSend);
        Experience.changeExp(target, expToSend);

        MessageHandler.MessageContext targetCtx = new MessageHandler.MessageContext(target, ctx.getCommandLabel());

        if (specificLevel) {
            ctx.add("%amount_lvl%", String.valueOf(levelsToSend))
               .add("%amount_xp%", String.valueOf(expToSend))
               .add("%target%", target.getName());
            MessageHandler.sendMessage(ctx, "send.success.levels_sender");

            targetCtx.add("%amount_lvl%", String.valueOf(levelsToSend))
                    .add("%amount_xp%", String.valueOf(expToSend))
                    .add("%sender%", sender.getName());
            MessageHandler.sendMessage(targetCtx, "send.success.levels_target");
        } else {
            ctx.add("%amount_xp%", String.valueOf(expToSend))
               .add("%target%", target.getName());
            MessageHandler.sendMessage(ctx, "send.success.all_sender");

            targetCtx.add("%amount_xp%", String.valueOf(expToSend))
                    .add("%sender%", sender.getName());
            MessageHandler.sendMessage(targetCtx, "send.success.all_target");
        }

        playSoundForPlayer(target);
    }

    private void handleHelp(Player player, MessageHandler.MessageContext ctx) {
        if (!player.hasPermission(PERM_HELP)) {
            MessageHandler.sendMessage(ctx, "general.no_permission");
            return;
        }

        MessageHandler.sendRawMessage(ctx, "help.header");

        if (player.hasPermission(PERM_GET)) {
            sendHelpSection(player, "help.get", ctx);
        }

        if (player.hasPermission(PERM_SEND)) {
            sendHelpSection(player, "help.send", ctx);
        }

        if (player.hasPermission(PERM_SPLIT)) {
            sendHelpSection(player, "help.split", ctx);
        }

        sendHelpSection(player, "help.help", ctx);
        if (player.hasPermission(PERM_RELOAD)) {
            sendHelpSection(player, "help.reload", ctx);
        }
        MessageHandler.sendRawMessage(ctx, "help.footer");
    }

    private void sendHelpSection(Player player, String configKey, MessageHandler.MessageContext ctx) {
        List<String> helpLines = plugin.getConfig().getStringList("messages." + configKey);
        if (helpLines == null || helpLines.isEmpty()) {
            plugin.getLogger().warning("Missing or empty help section in config.yml: messages." + configKey);
            return;
        }
        
        String cmdLabel = ctx.getCommandLabel();
        
        for (String line : helpLines) {
            MessageHandler.MessageContext lineCtx = new MessageHandler.MessageContext(player, cmdLabel);
            MessageHandler.sendRawText(lineCtx, line);
        }
    }

    private void handleReload(CommandSender sender, MessageHandler.MessageContext ctx) {
        if (!sender.hasPermission(PERM_RELOAD)) {
            MessageHandler.sendMessage(ctx, "general.no_permission");
            return;
        }

        try {
            plugin.reloadPluginConfig();
            MessageHandler.sendMessage(ctx, "reload.success");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error reloading ExpBottle configuration", e);
            MessageHandler.sendMessage(ctx, "reload.error");
        }
    }

    private void playSoundForPlayer(Player player) {
        if (InfoKeeper.sendSoundEnabled) {
            try {
                Sound sound = Sound.valueOf(InfoKeeper.sendSoundName.toUpperCase());
                player.playSound(player.getLocation(), sound, InfoKeeper.sendSoundVolume, InfoKeeper.sendSoundPitch);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound name in config.yml: '" + InfoKeeper.sendSoundName
                        + "'. Please check the Bukkit Sound enum.");
            }
        }
    }

    private void handleSplitExp(Player player, String[] args, MessageHandler.MessageContext ctx) {
        if (!player.hasPermission(PERM_SPLIT)) {
            MessageHandler.sendMessage(ctx, "general.no_permission");
            return;
        }

        if (args.length < 2) {
            MessageHandler.sendMessage(ctx, "split.usage");
            return;
        }

        int levelsPerBottle;
        try {
            levelsPerBottle = Integer.parseInt(args[1]);
            if (levelsPerBottle <= 0) {
                MessageHandler.sendMessage(ctx, "general.positive_number");
                return;
            }
            if (levelsPerBottle < InfoKeeper.splitMinLevelsPerBottle) {
                ctx.add("%min_levels%", String.valueOf(InfoKeeper.splitMinLevelsPerBottle));
                MessageHandler.sendMessage(ctx, "split.error.min_levels_not_met");
                return;
            }
            if (InfoKeeper.splitLevelStep > 1 && levelsPerBottle % InfoKeeper.splitLevelStep != 0) {
                ctx.add("%level_step%", String.valueOf(InfoKeeper.splitLevelStep));
                MessageHandler.sendMessage(ctx, "split.error.step_not_met");
                return;
            }
        } catch (NumberFormatException e) {
            ctx.add("%input%", args[1]);
            MessageHandler.sendMessage(ctx, "general.invalid_number");
            return;
        }

        int currentExp = Experience.getExp(player);
        int expPerBottle = Experience.getExpFromLevel(levelsPerBottle);

        if (expPerBottle == 0) {
            plugin.getLogger().warning(
                    "Split failed: Calculated 0 exp for level " + levelsPerBottle + ". This shouldn't happen.");
            MessageHandler.sendMessage(ctx, "split.error.generic");
            return;
        }

        int bottlesToCreate = currentExp / expPerBottle;
        if (bottlesToCreate == 0) {
            ctx.add("%amount_lvl%", String.valueOf(levelsPerBottle))
               .add("%amount_xp%", String.valueOf(expPerBottle))
               .add("%total_exp%", String.valueOf(currentExp));
            MessageHandler.sendMessage(ctx, "split.error.not_enough_exp");
            return;
        }

        int totalExpNeeded = bottlesToCreate * expPerBottle;

        int freeSlots = getFreeInventorySlots(player);
        if (bottlesToCreate > freeSlots) {
            ctx.add("%count%", String.valueOf(bottlesToCreate))
               .add("%free_slots%", String.valueOf(freeSlots))
               .add("%slots_required%", String.valueOf(bottlesToCreate));
            MessageHandler.sendMessage(ctx, "split.error.inventory_full");
            return;
        }

        for (int i = 0; i < bottlesToCreate; i++) {
            createAndGiveBottle(player, expPerBottle, levelsPerBottle, ctx);
        }
        Experience.changeExp(player, -totalExpNeeded);
        int remainingExp = Experience.getExp(player);

        ctx.add("%count%", String.valueOf(bottlesToCreate))
           .add("%amount_lvl%", String.valueOf(levelsPerBottle));
        MessageHandler.sendMessage(ctx, "split.success.base");

        ctx.add("%remaining_xp%", String.valueOf(remainingExp));
        MessageHandler.sendMessage(ctx, "split.success.remaining");
    }

    private boolean canReceiveItem(Player player) {
        return player.getInventory().firstEmpty() != -1;
    }

    private int getFreeInventorySlots(Player player) {
        int emptySlots = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) {
                emptySlots++;
            }
        }
        return emptySlots;
    }
}