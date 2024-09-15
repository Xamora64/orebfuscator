package orebfuscator;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

@Mod(modid = Orebfuscator.MODID, version = Orebfuscator.VERSION, acceptableRemoteVersions = "*")
public class Orebfuscator
{
    public static final String MODID = "orebfuscator";
    public static final String VERSION = "0.5";

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	FMLCommonHandler.instance().bus().register(this);
    	MinecraftForge.EVENT_BUS.register(new PlayerHandler());

    	Options.load(event.getModConfigurationDirectory());
    }
    
    @SubscribeEvent
    public void onPlayerLogged(PlayerEvent.PlayerLoggedInEvent event)
    {
    	PlayerInjector.hookPlayer((EntityPlayerMP)event.player);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event)
    {
    	PlayerInjector.cleanupPlayer((EntityPlayerMP) event.player);
    }
}
