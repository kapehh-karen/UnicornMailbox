package me.kapehh.UnicornMailbox.mailbox;

import me.kapehh.UnicornMailbox.serialize.ItemStackSerializer;
import me.kapehh.main.pluginmanager.db.PluginDatabase;
import me.kapehh.main.pluginmanager.db.PluginDatabaseInfo;
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

    public static void sendMail(PluginDatabase dbHelper, PluginDatabaseInfo dbInfo, ItemStack itemStack, String from, String to) throws IOException, SQLException {
        if (dbHelper == null || dbInfo == null) return;
        byte[] bItem = ItemStackSerializer.toBytes(itemStack);
        dbHelper.prepareQueryUpdate(
            "INSERT INTO `" + dbInfo.getTable() + "`(`raw`, `info`, `sended_date`, `received_date`, `from`, `to`, `is_received`) VALUES (?, ?, NOW(), '0000-00-00 00:00:00', ?, ?, 0)",
            bItem, itemStack.toString(),
            from.toLowerCase(), to.toLowerCase()
        );
    }

    public static void sendMails(PluginDatabase dbHelper, PluginDatabaseInfo dbInfo, ItemStack[] itemStacks, String from, String to) throws SQLException, IOException {
        if (dbHelper == null || dbInfo == null) return;
        Connection connection = dbHelper.getConnection();
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO `" + dbInfo.getTable() + "`(`raw`, `info`, `sended_date`, `received_date`, `from`, `to`, `is_received`) VALUES (?, ?, NOW(), '0000-00-00 00:00:00', ?, ?, 0)"
        );
        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
            preparedStatement.setBytes(1, ItemStackSerializer.toBytes(itemStack));
            preparedStatement.setString(2, itemStack.toString());
            preparedStatement.setString(3, from.toLowerCase());
            preparedStatement.setString(4, to.toLowerCase());
            preparedStatement.executeUpdate();
        }
        connection.commit();
        connection.setAutoCommit(autoCommit);
    }

    public static int countMails(PluginDatabase dbHelper, PluginDatabaseInfo dbInfo, String playerName) throws SQLException {
        if (dbHelper == null || dbInfo == null) return -1;
        PluginDatabaseResult pResult = dbHelper.prepareQueryStart("SELECT COUNT(*) AS count_all_mails FROM `" + dbInfo.getTable() + "` WHERE (`to` = ?) AND (`is_received` = 0)", playerName.toLowerCase());
        ResultSet result = pResult.getResultSet();
        int count = 0;
        if (result.next()) {
            count = result.getInt("count_all_mails");
        }
        dbHelper.queryEnd(pResult);
        return count;
    }

    public static MailPack receiveMails(PluginDatabase dbHelper, PluginDatabaseInfo dbInfo, String playerName, int limit) throws SQLException, IOException, ClassNotFoundException {
        if (dbHelper == null || dbInfo == null) return null;
        MailPack mailPack = new MailPack();
        if (limit == 0) return mailPack;
        List<ItemStack> ret = new ArrayList<ItemStack>();
        List<Integer> ids = new ArrayList<Integer>();
        PluginDatabaseResult pResult = dbHelper.prepareQueryStart("SELECT `id`, `raw` FROM `" + dbInfo.getTable() + "` WHERE (`to` = ?) AND (`is_received` = 0)", playerName.toLowerCase());
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
                "UPDATE `" + dbInfo.getTable() + "` SET `received_date` = NOW(), `is_received` = 1 WHERE `id` IN (" + StringUtils.join(ids, ", ") + ")"
            );
        }
        return mailPack;
    }

}
