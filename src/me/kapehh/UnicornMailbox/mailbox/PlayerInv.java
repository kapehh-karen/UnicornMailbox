package me.kapehh.UnicornMailbox.mailbox;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by Karen on 14.04.2015.
 */
public class PlayerInv {
    public static int getPlayerEmptySize(Player player) {
        ItemStack[] content = player.getInventory().getContents();
        int count = 0;
        for (ItemStack itemStack : content) {
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
                count++;
            }
        }
        return count;
    }

    public static void putPlayerMailPack(Player player, MailPack mailPack) {
        if (mailPack.getSize_pack() <= 0) return;
        ItemStack[] content = player.getInventory().getContents();
        List<ItemStack> mailItems = mailPack.getItemStacks();
        for (int i = 0, g = 0; i < content.length; i++) {
            if (content[i] == null || content[i].getType().equals(Material.AIR)) {
                content[i] = mailItems.get(g);
                g++;
                if (g >= mailPack.getSize_pack()) break;
            }
        }
        player.getInventory().setContents(content);
    }
}
