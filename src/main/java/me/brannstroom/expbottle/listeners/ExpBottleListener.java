package me.brannstroom.expbottle.listeners;

import java.util.logging.Level;
import me.brannstroom.expbottle.ExpBottle;
import me.brannstroom.expbottle.handlers.MessageHandler;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.BlockProjectileSource;

public class ExpBottleListener implements Listener {

    // #region Fields
    private final ExpBottle plugin = ExpBottle.instance;
    private final NamespacedKey expKey = new NamespacedKey(plugin, "experience_points");
    // #endregion

    // #region Event Handlers
    @EventHandler
    public void onExpBottle(ExpBottleEvent e) {
        ThrownExpBottle bottle = e.getEntity();
        ItemStack item = bottle.getItem();
        if (isCustom(item))
            e.setExperience(getExp(item));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getView().getTopInventory().getHolder() instanceof AnvilInventory) ||
                e.getSlotType() != InventoryType.SlotType.RESULT ||
                e.getCurrentItem() == null)
            return;

        if (isCustom(e.getView().getItem(0)) && e.getRawSlot() == 2) {
            e.setCancelled(true);
            if (e.getWhoClicked() instanceof Player)
                MessageHandler.sendMessage((Player) e.getWhoClicked(), "anvil.error.modify_attempt");
        }
    }

    @SuppressWarnings("deprecation") // modern solutions
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null || !isCustom(item))
            return;

        Player p = e.getPlayer();
        Action action = e.getAction();

        if (action == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = e.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getType().isInteractable())
                return;
        }

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            e.setCancelled(true);
            ItemStack bottleToThrow = item.clone();
            bottleToThrow.setAmount(1);
            if (item.getAmount() > 1)
                item.setAmount(item.getAmount() - 1);
            else
                p.getInventory().setItem(e.getHand(), null);
            p.updateInventory();
            try {
                ThrownExpBottle thrownBottle = p.launchProjectile(ThrownExpBottle.class);
                if (thrownBottle != null)
                    thrownBottle.setItem(bottleToThrow);
                else
                    plugin.getLogger().warning("Launched projectile was NULL!");
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "Error launching projectile!", ex);
            }
        }
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent e) {
        if (e.getItem() != null && isCustom(e.getItem()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        Projectile projectile = e.getEntity();
        if (!(projectile instanceof ThrownExpBottle))
            return;
        if (projectile.getShooter() instanceof BlockProjectileSource) {
            BlockProjectileSource src = (BlockProjectileSource) projectile.getShooter();
            if (src.getBlock().getType() == Material.DISPENSER) {
                ThrownExpBottle thrownBottle = (ThrownExpBottle) projectile;
                if (isCustom(thrownBottle.getItem()))
                    e.setCancelled(true);
            }
        }
    }
    // #endregion

    // #region Utility Methods
    private boolean isCustom(ItemStack item) {
        if (item == null || item.getType() != Material.EXPERIENCE_BOTTLE || !item.hasItemMeta())
            return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(expKey, PersistentDataType.INTEGER);
    }

    private int getExp(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return 0;
        PersistentDataContainer c = meta.getPersistentDataContainer();
        return c.has(expKey, PersistentDataType.INTEGER) ? c.getOrDefault(expKey, PersistentDataType.INTEGER, 0) : 0;
    }
    // #endregion
}