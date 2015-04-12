package me.kapehh.UnicornMailbox.serialize;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Karen on 08.04.2015.
 */
public class ItemStackSerializer {

    private ItemStackSerializer() { /* blocked */ }

    public static byte[] toBytes(ItemStack itemStack) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream bukkitObjectOutputStream = new BukkitObjectOutputStream(byteArrayOutputStream);
        bukkitObjectOutputStream.writeObject(itemStack);
        bukkitObjectOutputStream.flush();
        bukkitObjectOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static ItemStack fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(byteArrayInputStream);
        ItemStack ret = (ItemStack) bukkitObjectInputStream.readObject();
        bukkitObjectInputStream.close();
        return ret;
    }

}
