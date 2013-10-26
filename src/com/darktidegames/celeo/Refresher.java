package com.darktidegames.celeo;

import org.bukkit.entity.Player;

/**
 * @author Celeo
 */
public class Refresher implements Runnable
{

	final DarkSneakCore plugin;

	public Refresher(DarkSneakCore instance)
	{
		plugin = instance;
	}

	@Override
	public void run()
	{
		for (Player player : plugin.getAllSneakers())
			plugin.refreshSneaking(player);
	}

}