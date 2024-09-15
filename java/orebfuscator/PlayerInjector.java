package orebfuscator;

import io.netty.channel.Channel;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;

public class PlayerInjector {

	public static void hookPlayer(EntityPlayerMP player) 
	{
		NetworkManager nm = player.playerNetServerHandler.netManager;
		Channel channel = (Channel)Fields.getValue(nm, Fields.NetworkManager.getChannelIndex());
		channel = new ProxyChannel(channel, player);
		Fields.setValue(nm, Fields.NetworkManager.getChannelIndex(), channel);
	}

	public static void cleanupPlayer(EntityPlayerMP player) 
	{
	}
}
