package me.brannstroom.expbottle.listeners;

import java.util.logging.Level;
import me.brannstroom.expbottle.ExpBottle;
import me.brannstroom.expbottle.handlers.MessageHandler;
import org.bukkit.Material;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.inventory.AnvilInventory;

public class ExpBottleListener implements Listener {

    private final ExpBottle plugin = ExpBottle.instance;
    private final NamespacedKey experienceKey = new NamespacedKey(plugin, "experience_points");

    @EventHandler
    public void onExpBottle(ExpBottleEvent event) {
        ThrownExpBottle expBottle = event.getEntity();
        ItemStack item = expBottle.getItem();
        if (isCustomExpBottle(item)) {
            event.setExperience(getExperienceFromBottle(item));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof AnvilInventory))
            return;
        if (event.getSlotType() != InventoryType.SlotType.RESULT)
            return;
        if (event.getCurrentItem() == null)
            return;

        ItemStack itemInSlot0 = event.getView().getItem(0);
        boolean inputIsCustom = isCustomExpBottle(itemInSlot0);

        if (inputIsCustom && event.getRawSlot() == 2) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player) {
                MessageHandler.sendMessage((Player) event.getWhoClicked(), "anvil.error.modify_attempt");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !isCustomExpBottle(item))
            return;

        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);

            ItemStack bottleToThrow = item.clone();
            bottleToThrow.setAmount(1);

            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItem(event.getHand(), null);
            }
            player.updateInventory();

            try {
                ThrownExpBottle thrownBottle = player.launchProjectile(ThrownExpBottle.class);
                if (thrownBottle != null) {
                    thrownBottle.setItem(bottleToThrow);
                } else {
                    plugin.getLogger().warning("Launched projectile was NULL!");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error launching projectile!", e);
            }
        }
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event) {
        if (event.getItem() != null && isCustomExpBottle(event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile instanceof ThrownExpBottle))
            return;

        if (projectile.getShooter() instanceof BlockProjectileSource) {
            BlockProjectileSource source = (BlockProjectileSource) projectile.getShooter();
            if (source.getBlock().getType() == Material.DISPENSER) {
                ThrownExpBottle thrownBottle = (ThrownExpBottle) projectile;
                ItemStack item = thrownBottle.getItem();
                if (isCustomExpBottle(item)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean isCustomExpBottle(ItemStack item) {
        if (item == null || item.getType() != Material.EXPERIENCE_BOTTLE)
            return false;
        if (!item.hasItemMeta())
            return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(experienceKey, PersistentDataType.INTEGER);
    }

    private int getExperienceFromBottle(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return 0;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(experienceKey, PersistentDataType.INTEGER)) {
            Integer value = container.get(experienceKey, PersistentDataType.INTEGER);
            return value != null ? value : 0;
        } else {
            return 0;
        }
    }
}