package me.saif.betterenderchests.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class FoliaScheduler {

    private static final boolean FOLIA = hasMethod(Bukkit.getServer().getClass(), "getGlobalRegionScheduler");

    private FoliaScheduler() {
    }

    public static void runSync(Plugin plugin, Runnable runnable) {
        if (!FOLIA) {
            Bukkit.getScheduler().runTask(plugin, runnable);
            return;
        }

        Object globalScheduler = invoke(Bukkit.getServer(), "getGlobalRegionScheduler");
        if (globalScheduler == null) {
            Bukkit.getScheduler().runTask(plugin, runnable);
            return;
        }

        if (!invokeVoid(globalScheduler, "execute", new Class<?>[]{Plugin.class, Runnable.class}, new Object[]{plugin, runnable})) {
            Consumer<Object> consumer = ignored -> runnable.run();
            invokeVoid(globalScheduler, "run", new Class<?>[]{Plugin.class, Consumer.class}, new Object[]{plugin, consumer});
        }
    }

    public static void runSyncLater(Plugin plugin, Runnable runnable, long delayTicks) {
        if (!FOLIA) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, runnable, delayTicks);
            return;
        }

        Object globalScheduler = invoke(Bukkit.getServer(), "getGlobalRegionScheduler");
        if (globalScheduler == null) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, runnable, delayTicks);
            return;
        }

        Consumer<Object> consumer = ignored -> runnable.run();
        invokeVoid(globalScheduler, "runDelayed", new Class<?>[]{Plugin.class, Consumer.class, long.class}, new Object[]{plugin, consumer, delayTicks});
    }

    public static void runSyncRepeating(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
        if (!FOLIA) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, runnable, delayTicks, periodTicks);
            return;
        }

        Object globalScheduler = invoke(Bukkit.getServer(), "getGlobalRegionScheduler");
        if (globalScheduler == null) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, runnable, delayTicks, periodTicks);
            return;
        }

        Consumer<Object> consumer = ignored -> runnable.run();
        invokeVoid(globalScheduler, "runAtFixedRate", new Class<?>[]{Plugin.class, Consumer.class, long.class, long.class}, new Object[]{plugin, consumer, delayTicks, periodTicks});
    }

    public static void runAsync(Plugin plugin, Runnable runnable) {
        if (!FOLIA) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
            return;
        }

        Object asyncScheduler = invoke(Bukkit.getServer(), "getAsyncScheduler");
        if (asyncScheduler == null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
            return;
        }

        Consumer<Object> consumer = ignored -> runnable.run();
        invokeVoid(asyncScheduler, "runNow", new Class<?>[]{Plugin.class, Consumer.class}, new Object[]{plugin, consumer});
    }

    private static Object invoke(Object target, String name) {
        try {
            Method method = target.getClass().getMethod(name);
            return method.invoke(target);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean invokeVoid(Object target, String name, Class<?>[] parameterTypes, Object[] args) {
        try {
            Method method = target.getClass().getMethod(name, parameterTypes);
            method.invoke(target, args);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean hasMethod(Class<?> clazz, String methodName) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }
}
