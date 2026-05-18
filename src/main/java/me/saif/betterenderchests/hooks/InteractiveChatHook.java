package me.saif.betterenderchests.hooks;

import me.saif.betterenderchests.VariableEnderChests;
import me.saif.betterenderchests.enderchest.EnderChest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class InteractiveChatHook implements Listener {

    private static final String EVENT_CLASS_NAME = "com.loohp.interactivechat.api.events.InventoryPlaceholderEvent";
    private static final String ENDERCHEST_PLACEHOLDER_TYPE_NAME = "ENDERCHEST";

    private final VariableEnderChests plugin;

    public InteractiveChatHook(VariableEnderChests plugin) {
        this.plugin = plugin;

        Plugin interactiveChat = Bukkit.getPluginManager().getPlugin("InteractiveChat");
        if (interactiveChat == null) {
            return;
        }

        try {
            Class<?> eventClass = Class.forName(EVENT_CLASS_NAME, true, interactiveChat.getClass().getClassLoader());
            if (!Event.class.isAssignableFrom(eventClass)) {
                plugin.getLogger().warning("InteractiveChat hook failed: InventoryPlaceholderEvent is not a Bukkit Event");
                return;
            }

            @SuppressWarnings("unchecked")
            Class<? extends Event> bukkitEventClass = (Class<? extends Event>) eventClass;
            Bukkit.getPluginManager().registerEvent(
                    bukkitEventClass,
                    this,
                    EventPriority.NORMAL,
                    new InventoryPlaceholderEventExecutor(plugin),
                    plugin
            );
            plugin.getLogger().info("Hooked into InteractiveChat");
        } catch (ClassNotFoundException ignored) {
            plugin.getLogger().warning("InteractiveChat hook failed: InventoryPlaceholderEvent class was not found");
        }
    }

    private static class InventoryPlaceholderEventExecutor implements EventExecutor {

        private final VariableEnderChests plugin;

        private InventoryPlaceholderEventExecutor(VariableEnderChests plugin) {
            this.plugin = plugin;
        }

        @Override
        public void execute(Listener listener, Event event) throws EventException {
            try {
                Object placeholderType = event.getClass().getMethod("getType").invoke(event);
                if (!ENDERCHEST_PLACEHOLDER_TYPE_NAME.equals(String.valueOf(placeholderType))) {
                    return;
                }

                Object sender = event.getClass().getMethod("getSender").invoke(event);
                Object localPlayer = sender.getClass().getMethod("getLocalPlayer").invoke(sender);
                if (!(localPlayer instanceof Player)) {
                    return;
                }

                Player player = (Player) localPlayer;
                plugin.getEnderChestManager().updateRows(player);
                EnderChest enderChest = plugin.getEnderChestManager().getEnderChest(player);

                Method setInventoryMethod = event.getClass().getMethod("setInventory", org.bukkit.inventory.Inventory.class);
                setInventoryMethod.invoke(event, enderChest.getInventory());
            } catch (ReflectiveOperationException e) {
                throw new EventException(e);
            }
        }
    }
}
