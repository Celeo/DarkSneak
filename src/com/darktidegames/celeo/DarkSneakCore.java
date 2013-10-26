package com.darktidegames.celeo;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.CalculableType;
import de.bananaco.bpermissions.api.Permission;

/**
 * <b>DarkSneak</b> started <i>August 6th, 2012</i><br>
 * <br>
 * DeadTide's sneak plugin
 * 
 * @author Celeo
 */
public class DarkSneakCore extends JavaPlugin implements Listener
{

	static Logger log = Logger.getLogger("Minecraft");
	private List<Player> areSneaking = new ArrayList<Player>();
	private final String sneakNode = "darksneak.sneak";
	private final String sneakOptOutNode = "darksneak.optout";
	private Refresher refresher = null;

	private final String world = "world";
	private final CalculableType type = CalculableType.USER;

	@Override
	public void onEnable()
	{
		getCommand("sneak").setExecutor(this);
		getCommand("darksneak").setExecutor(this);
		getServer().getPluginManager().registerEvents(this, this);
		refresher = new Refresher(this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, refresher, 60L, 600L);
		for (Player player : getServer().getOnlinePlayers())
			if (!player.hasPermission(sneakOptOutNode)
					&& player.hasPermission(sneakNode))
				setSneaking(player, true);
		getLogger().info("Enabled");
	}

	@Override
	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);
		getLogger().info("Disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			if (args != null && args.length == 1
					&& args[0].equalsIgnoreCase("-opt"))
			{
				if (!player.hasPermission(sneakOptOutNode))
				{
					ApiLayer.addPermission("world", CalculableType.USER, player.getName(), Permission.loadFromString(sneakOptOutNode));
					player.sendMessage("§7Opted out of sneaking");
				}
				else
				{
					ApiLayer.removePermission(world, type, player.getName(), sneakOptOutNode);
					player.sendMessage("§7Opted back into sneaking!");
				}
				ApiLayer.update();
				return true;
			}
			if (args != null && args.length == 1
					&& args[0].equalsIgnoreCase("-c"))
			{
				player.sendMessage(String.format("§7Sneaking with DarkSneak: §6%b§7, sneaking in-game: §6%b", Boolean.valueOf(isSneaking(player)), Boolean.valueOf(player.isSneaking())));
				return true;
			}
			if (player.hasPermission(sneakNode))
				toggleSneak(player);
			else
				player.sendMessage("§cYou cannot do that");
			return true;
		}
		return false;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		player.setSneaking(false);
		if (player.hasPermission(sneakNode)
				&& !player.hasPermission(sneakOptOutNode))
			setSneaking(player, true);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		if (isSneaking(player))
			setSneaking(player, false);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		Player player = event.getPlayer();
		if (isSneaking(player))
			refreshSneaking(player);
	}

	@EventHandler
	public void onPlayerChangeSneak(PlayerToggleSneakEvent event)
	{
		Player player = event.getPlayer();
		if (isSneaking(player))
		{
			event.setCancelled(true);
			refreshSneaking(player);
		}
	}

	public void toggleSneak(Player player)
	{
		setSneaking(player, !isSneaking(player));
	}

	public void setSneaking(Player player, boolean isSneaking)
	{
		if (isSneaking)
		{
			player.sendMessage("§7You are now sneaking - /help sneak");
			player.setSneaking(true);
			areSneaking.add(player);
		}
		else
		{
			player.sendMessage("§7You are no longer sneaking");
			player.setSneaking(false);
			areSneaking.remove(player);
		}
	}

	public void refreshSneaking(Player player)
	{
		player.setSneaking(false);
		player.setSneaking(true);
	}

	public boolean isSneaking(Player player)
	{
		return areSneaking.contains(player);
	}

	public List<Player> getAllSneakers()
	{
		return areSneaking;
	}

}