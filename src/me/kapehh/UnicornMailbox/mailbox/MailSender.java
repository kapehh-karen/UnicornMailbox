package me.kapehh.UnicornMailbox.mailbox;

import me.kapehh.UnicornMailbox.serialize.ItemStackSerializer;
import me.kapehh.main.pluginmanager.db.PluginDatabase;
import me.kapehh.main.pluginmanager.db.PluginDatabaseResult;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Karen on 12.04.2015.
 */
public class MailSender {

    public static boolean isCorrectItem(ItemStack itemStack) {
        return !(itemStack == null || itemStack.getType().equals(Material.AIR));
    }

    public static void sendMail(PluginDatabase dbHelper, ItemStack itemStack, String from, String to) throws IOException, SQLException {
        byte[] bItem = ItemStackSerializer.toBytes(itemStack);
        dbHelper.prepareQueryUpdate(
            "INSERT INTO `mail`(`raw`, `info`, `sended_date`, `received_date`, `from`, `to`, `is_received`) VALUES (?, ?, NOW(), '0000-00-00 00:00:00', ?, ?, 0)",
            bItem, itemStack.toString(),
            from, to
        );
    }

    /*public static ItemStack receiveMail(PluginDatabase dbHelper, int id) throws SQLException, IOException, ClassNotFoundException {
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
    }*/

    public static MailPack receiveMails(PluginDatabase dbHelper, String playerName, int limit) throws SQLException, IOException, ClassNotFoundException {
        MailPack mailPack = new MailPack();
        if (limit == 0) return mailPack;
        List<ItemStack> ret = new ArrayList<ItemStack>();
        List<Integer> ids = new ArrayList<Integer>();
        PluginDatabaseResult pResult = dbHelper.prepareQueryStart("SELECT `id`, `raw` FROM `mail` WHERE (`to` = ?) AND (`is_received` = 0)", playerName);
        ResultSet result = pResult.getResultSet();
        byte[] bItem;
        ItemStack itemStack;
        int count = 0;
        while (result.next()) {
            ids.add(result.getInt("id"));
            bItem = result.getBytes("raw");
            if (bItem != null) {
                itemStack = ItemStackSerializer.fromBytes(bItem);
                ret.add(itemStack);
            }
            count++;
            if (count >= limit) break;
        }
        if (result.last()) count = result.getRow();
        dbHelper.queryEnd(pResult);
        mailPack.setItemStacks(ret);
        mailPack.setSize_pack(ret.size());
        mailPack.setSize_all(count);
        if (ids.size() > 0) {
            dbHelper.prepareQueryUpdate(
                "UPDATE `mail` SET `received_date` = NOW(), `is_received` = 1 WHERE `id` IN (" + StringUtils.join(ids, ", ") + ")"
            );
        }
        return mailPack;
    }

}
