package me.saif.betterenderchests.enderchest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.BiConsumer;

public class EnderChest implements InventoryHolder {

    public static final Map<Integer, String> INVENTORY_NAMES;
    public static final String PREFIX = "VECEnderChest";
    public static final String RETRIEVAL_PREFIX = "VECEnderChestRetrieval";

    static {
        Map<Integer, String> temp = new HashMap<>();
        temp.put(1, PREFIX + ";1;<player>");
        temp.put(2, PREFIX + ";2;<player>");
        temp.put(3, PREFIX + ";3;<player>");
        temp.put(4, PREFIX + ";4;<player>");
        temp.put(5, PREFIX + ";5;<player>");
        temp.put(6, PREFIX + ";6;<player>");

        INVENTORY_NAMES = Collections.unmodifiableMap(temp);
    }

    private final UUID UUID;
    private final String name;
    private ItemStack[] contents;
    private Inventory inventory;
    private EnderChestRetreiver retriever;
    private final Map<Integer, String> inventoryNames = new HashMap<>();
    private int lastNumRows = 6;

    protected EnderChest(UUID owner, String name, ItemStack[] contents) {
        this.UUID = owner;
        this.name = name;
        INVENTORY_NAMES.forEach((integer, s) -> {
            this.inventoryNames.put(integer, s.replace("<player>", name));
        });
        this.contents = contents.length == 54 ? contents : Arrays.copyOf(contents, 54);
        this.inventory = Bukkit.createInventory(this, lastNumRows * 6, this.inventoryNames.get(lastNumRows));


        populateInventory();
    }

    protected EnderChest(UUID owner, String name, ItemStack[] contents, int lastNumRows) {
        this(owner, name, contents);
        this.setRows(lastNumRows);
    }

    public void setRows(int rows) {
        if (rows < 1)
            rows = 1;
        else if (rows > 6)
            rows = 6;

        lastNumRows = rows;

        if (inventory.getSize() == rows * 9)
            return;

        //else
        updateContentsArray();
        List<HumanEntity> viewers = new ArrayList<>(this.inventory.getViewers());

        this.inventory = Bukkit.createInventory(this, rows * 9, this.inventoryNames.get(rows));

        populateInventory();
        for (HumanEntity viewer : viewers) {
            viewer.openInventory(inventory);
        }
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public void clearContents() {
        updateRetrieverContentsArray();
        this.contents = new ItemStack[54];
        this.retriever = null;
        this.populateInventory();
    }

    public void setContents(ItemStack[] contents) {
        updateRetrieverContentsArray();
        this.contents = Arrays.copyOf(contents, 54);
        this.retriever = null;
        this.populateInventory();
    }

    public boolean hasViewers() {
        List<HumanEntity> viewers = this.getInventory().getViewers();
        return viewers.size() > 0;
    }

    protected void openInventory(Player player) {
        if (player.getOpenInventory().getTopInventory().equals(this.inventory))
            return;
        player.openInventory(this.inventory);
    }

    //sets each item in the inventory to what the contents array says it should be
    private void populateInventory() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, this.contents[i]);
        }
    }

    public ItemStack[] getContents() {
        updateContentsArray();
        updateRetrieverContentsArray();
        return Arrays.copyOf(contents, 54);
    }

    //this updates the contents array either because we want to save some data or the inventory is being recreated
    private void updateContentsArray() {
        for (int i = 0; i < inventory.getSize(); i++) {
            this.contents[i] = inventory.getItem(i);
        }
    }

    private void updateRetrieverContentsArray() {
        if (this.retriever != null) {
            this.retriever.updateContentsArray();
        }
    }

    public EnderChestRetreiver getRetriever() {
        updateContentsArray();
        updateRetrieverContentsArray();
        this.retriever = new EnderChestRetreiver(this);
        return this.retriever;
    }

    public int getLastNumRows() {
        return lastNumRows;
    }

    public String getName() {
        return name;
    }

    public UUID getUUID() {
        return UUID;
    }

    public EnderChestSnapshot snapshot() {
        return new EnderChestSnapshot(this);
    }

    public static class EnderChestRetreiver implements InventoryHolder {

        private final EnderChest owner;
        private final Inventory inventory;
        private final int startSlot;

        private EnderChestRetreiver(EnderChest owner) {
            this.owner = owner;
            this.startSlot = owner.lastNumRows * 9;
            this.inventory = Bukkit.createInventory(this, 54 - this.startSlot, RETRIEVAL_PREFIX + ";" + owner.name);
            populateInventory();
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public EnderChest getOwner() {
            return owner;
        }

        private void populateInventory() {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, owner.contents[startSlot + i]);
            }
        }

        private void updateContentsArray() {
            for (int i = 0; i < inventory.getSize(); i++) {
                owner.contents[startSlot + i] = inventory.getItem(i);
            }
        }
    }
}
