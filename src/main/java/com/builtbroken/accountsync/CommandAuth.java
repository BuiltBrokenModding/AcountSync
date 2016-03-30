package com.builtbroken.accountsync;

import com.builtbroken.jlib.helpers.MathHelper;
import com.builtbroken.jlib.lang.EnglishLetters;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

import java.sql.*;
import java.util.concurrent.TimeUnit;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/29/2016.
 */
public class CommandAuth extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "authAccount";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_)
    {
        return "/authAccount";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] string)
    {
        if (sender instanceof EntityPlayerMP)
        {
            Connection conn = null;
            Mod.structure = "test";
            Mod.username = "root";
            Mod.password = "";
            try
            {
                conn = DriverManager.getConnection("jdbc:mysql://" + Mod.url + ":" + Mod.port + "/" + Mod.structure, Mod.username, Mod.password);
            }
            catch (SQLException e)
            {
                ((EntityPlayerMP) sender).addChatComponentMessage(new ChatComponentText("Failed to connect to database! Contact an admins about this problem."));
                e.printStackTrace();
                return;
            }

            if (conn != null)
            {

                try
                {
                    DatabaseMetaData meta = conn.getMetaData();
                    ResultSet res = meta.getTables(null, null, Mod.table.toUpperCase(), new String[]{"TABLE"});
                    if (!res.next())
                    {
                        Statement stmt = conn.createStatement();

                        String sql = "CREATE TABLE " + Mod.table.toUpperCase() + " " +
                                "(time datetime, " +
                                " username VARCHAR(255), " +
                                " uuid VARCHAR(255), " +
                                " token VARCHAR(255), " +
                                " ip VARCHAR(255), " +
                                " PRIMARY KEY (username))";

                        stmt.executeUpdate(sql);

                    }
                }
                catch (SQLException e)
                {
                    ((EntityPlayerMP) sender).addChatComponentMessage(new ChatComponentText("Failed to find or create database tables! Contact an admins about this problem."));
                    e.printStackTrace();
                    return;
                }

                Date startDate = new Date(System.currentTimeMillis());

                try
                {
                    final String queryCheck = "SELECT * from " + Mod.table + " WHERE username = ?";
                    final PreparedStatement ps = conn.prepareStatement(queryCheck);
                    ps.setString(1, ((EntityPlayerMP) sender).getGameProfile().getName());
                    final ResultSet resultSet = ps.executeQuery();
                    if (resultSet.next())
                    {
                        final Date time = resultSet.getDate(1);
                        if (startDate.getTime() - time.getTime() > TimeUnit.HOURS.toMillis(3))
                        {
                            String query = "delete from " + Mod.table + " where username = ?";
                            PreparedStatement preparedStmt = conn.prepareStatement(query);
                            preparedStmt.setString(1, ((EntityPlayerMP) sender).getGameProfile().getName());
                            preparedStmt.execute();
                        }
                        else
                        {
                            sender.addChatMessage(new ChatComponentText("Old entry found and retrieved, See " + Mod.outputURL + "/" + resultSet.getString(4) + " to finish the auth process."));
                            return;
                        }
                    }
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }

                String query = " insert into " + Mod.table + " (time, username, uuid, token, ip) values (?, ?, ?, ?, ?)";
                String token = getRandomString();
                try
                {
                    PreparedStatement preparedStmt = conn.prepareStatement(query);
                    preparedStmt.setDate(1, startDate);
                    preparedStmt.setString(2, sender.getCommandSenderName());
                    preparedStmt.setString(3, "" + ((EntityPlayerMP) sender).getGameProfile().getId());
                    preparedStmt.setString(4, token);
                    preparedStmt.setString(5, ((EntityPlayerMP) sender).getPlayerIP());
                    preparedStmt.execute();

                    sender.addChatMessage(new ChatComponentText("Command completed, See " + Mod.outputURL + "/" + token + " to finish the auth process."));
                }
                catch (SQLException e)
                {
                    if (e.getMessage().contains("Duplicate entry"))
                    {
                        ((EntityPlayerMP) sender).addChatComponentMessage(new ChatComponentText("Duplicate entry found for your username!"));
                    }
                    else
                    {
                        ((EntityPlayerMP) sender).addChatComponentMessage(new ChatComponentText("Failed to insertion into database! Contact an admins about this problem."));
                        e.printStackTrace();
                    }
                }

                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    ((EntityPlayerMP) sender).addChatComponentMessage(new ChatComponentText("Failed to close connection to database! Contact an admins about this problem."));
                    e.printStackTrace();
                }


            }
        }
        else
        {
            sender.addChatMessage(new ChatComponentText("This command does not work from the console"));
        }
    }

    /**
     * Generates a random string containing numbers and letters between a length of 10 - 30
     * 1,264,020,397,516,800 to 2,730,903,391,116,338,302,840,472,139,202,560,000,000 possible permutations
     * using this method. Not including the number of permutation if capital letters are considered.
     *
     * @return new String
     */
    protected String getRandomString()
    {
        String string = "";
        //Generate random default string
        int[] l = MathHelper.generateRandomIntArray(MathHelper.rand, EnglishLetters.values().length + 9, 10 + MathHelper.rand.nextInt(20));
        for (int i : l)
        {
            if (i < 10)
            {
                string += i;
            }
            else if (MathHelper.rand.nextBoolean())
            {
                string += EnglishLetters.values()[i - 10].name();
            }
            else
            {
                string += EnglishLetters.values()[i - 10].name().toLowerCase();
            }
        }
        return string;
    }
}
