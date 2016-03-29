package com.builtbroken.accountsync;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLModDisabledEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/29/2016.
 */
@cpw.mods.fml.common.Mod(modid = "bbm_accountSync", name = "Account Sync", version = "@MAJOR@.@MINOR@.@REVIS@.@BUILD@", acceptableRemoteVersions = "*", canBeDeactivated = true)
public class Mod
{
    //TODO auto download http://mvnrepository.com/artifact/mysql/mysql-connector-java
    public static Logger LOGGER;

    public static String username, password, port, url, table, structure, outputURL;

    @cpw.mods.fml.common.Mod.EventHandler
    public void disableEvent(FMLModDisabledEvent event)
    {
        LOGGER.info("Disabling mod");
    }

    @cpw.mods.fml.common.Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        LOGGER = LogManager.getLogger("Account Sync");
        Configuration config = new Configuration(new File(event.getModConfigurationDirectory(), "AccountSync.cfg"));
        config.load();
        username = config.getString("Username", Configuration.CATEGORY_GENERAL, "user", "User name to log into the database");
        password = config.getString("Pass", Configuration.CATEGORY_GENERAL, "pass", "Password to use to log into the database");
        port = config.getString("Port", Configuration.CATEGORY_GENERAL, "", "Port number to access database");
        url = config.getString("URL", Configuration.CATEGORY_GENERAL, "", "URL used to access database");
        structure = config.getString("Structure", Configuration.CATEGORY_GENERAL, "AccountSyncMod", "Area of the database to access");
        table = config.getString("Table", Configuration.CATEGORY_GENERAL, "UserSyncData", "Table to insert data into");
        outputURL = config.getString("OutputUrl", Configuration.CATEGORY_GENERAL, "", "Url to give a user after the command as completed");
        config.save();

        System.out.println("Loading driver...");
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Driver loaded!");
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException("Cannot find the driver in the classpath!", e);
        }
    }

    @cpw.mods.fml.common.Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        // Setup command
        ICommandManager commandManager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
        ServerCommandManager serverCommandManager = ((ServerCommandManager) commandManager);
        serverCommandManager.registerCommand(new CommandAuth());

    }
}
