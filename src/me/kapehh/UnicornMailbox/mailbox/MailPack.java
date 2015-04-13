package me.kapehh.UnicornMailbox.mailbox;

import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by Karen on 13.04.2015.
 */
public class MailPack {
    List<ItemStack> itemStacks;
    int size_pack;
    int size_all;

    public MailPack() { }

    public MailPack(List<ItemStack> itemStacks, int size_all) {
        this.itemStacks = itemStacks;
        this.size_pack = itemStacks.size();
        this.size_all = size_all;
    }

    public List<ItemStack> getItemStacks() {
        return itemStacks;
    }

    public void setItemStacks(List<ItemStack> itemStacks) {
        this.itemStacks = itemStacks;
    }

    public int getSize_pack() {
        return size_pack;
    }

    public void setSize_pack(int size_pack) {
        this.size_pack = size_pack;
    }

    public int getSize_all() {
        return size_all;
    }

    public void setSize_all(int size_all) {
        this.size_all = size_all;
    }

    @Override
    public String toString() {
        return "MailPack{" +
                "itemStacks=" + itemStacks +
                ", size_pack=" + size_pack +
                ", size_all=" + size_all +
                '}';
    }
}
