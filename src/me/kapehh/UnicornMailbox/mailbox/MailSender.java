package me.kapehh.UnicornMailbox.mailbox;

import me.kapehh.UnicornMailbox.serialize.ItemStackSerializer;
import me.kapehh.main.pluginmanager.db.PluginDatabase;
import me.kapehh.main.pluginmanager.db.PluginDatabaseResult;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Karen on 12.04.2015.
 */
public class MailSender {

    public static void sendMail(PluginDatabase dbHelper, ItemStack itemStack, String from, String to) throws IOException, SQLException {
        byte[] bItem = ItemStackSerializer.toBytes(itemStack);
        dbHelper.prepareQueryUpdate(
            "INSERT INTO `mail`(`raw`, `info`, `sended_date`, `received_date`, `from`, `to`, `is_received`) VALUES (?, ?, NOW(), '0000-00-00 00:00:00', ?, ?, 0)",
            bItem, itemStack.toString(),
            from, to
        );
    }

    public static ItemStack receiveMail(PluginDatabase dbHelper, int id) throws SQLException, IOException, ClassNotFoundException {
        ItemStack itemStack = null;
        PluginDatabaseResult pResult = dbHelper.prepareQueryStart("SELECT * FROM `mail` WHERE `id` = ?", id);
        ResultSet result = pResult.getResultSet();
        boolean isReceived = false;
        if (result.next()) {
            byte[] bItem = result.getBytes("raw");
            itemStack = ItemStackSerializer.fromBytes(bItem);
            isReceived = true;
        }
        if (isReceived) {
            dbHelper.prepareQueryUpdate("UPDATE `mail` SET `received_date` = NOW(), `is_received` = 1 WHERE `id` = ?", id);
        }
        dbHelper.queryEnd(pResult);
        return itemStack;
    }

}
