package com.gmail.nvandeginste;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.inventivetalent.bossbar.BossBarAPI;

public class EventClass implements Listener {
	public main plugin;

	public EventClass(main main) {
		this.plugin = main;
	}

	/*************************************
	 * 
	 * Bukkit Runnables
	 *
	 */
	public class BowReload implements Runnable {
		private Player p;

		public BowReload(Player _p) {
			this.p = _p;
		}

		public void run() {
			int arrowNb = 0;
			ItemStack[] stacks = p.getInventory().getContents();
			for (ItemStack stack : stacks) {
				if (stack == null) {
					continue;
				}
				if (stack.getType() == Material.ARROW) {
					arrowNb = stack.getAmount() + arrowNb;
				}
			}
			if (arrowNb != 3 && plugin.redTeam.contains(p)) {
				p.getInventory().addItem(new ItemStack(Material.ARROW));
			} else {
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				scheduler.scheduleSyncDelayedTask(plugin, new BowReload(p), 5 * 20L);
			}
		}
	}

	public class bossHealth implements Runnable {
		private int health;

		public bossHealth(int _health) {
			this.health = _health;
		}

		public void run() {
			for (Player pl : Bukkit.getOnlinePlayers()) {
				BossBarAPI.setHealth(pl, 100 - health);
			}

		}

	}

	public class Runnabledelay implements Runnable {
		private Block water;
		private String pString;

		public Runnabledelay(Block _water, String _pString) {
			this.water = _water;
			this.pString = _pString;
		}

		@SuppressWarnings("deprecation")
		public void run() {
			Player p = Bukkit.getServer().getPlayer(pString);
			if (plugin.pIdSource.contains(p.getUniqueId())) {
				plugin.pIdSource.remove(p.getUniqueId());
				water.setData((byte) 1);
				if (p.getInventory().contains(new ItemStack(Material.WATER_BUCKET)) == false) {
					// est bien le joueur ciblé ???
					ItemStack[] stacks = p.getInventory().getContents();
					for (ItemStack stack : stacks) {
						if (stack == null) {
							continue;
						}
						if (stack.getType() == Material.BUCKET) {
							stack.setType(Material.WATER_BUCKET);
						}
					}
					plugin.source.remove(water);
				}

			}
		}
	}

	public class buffTimer implements Runnable {
		private int time;
		private Player p;

		public buffTimer(Player _p, int _time) {// timer for Prisarine Blocs.
			this.time = _time;
			this.p = _p;
		}

		@SuppressWarnings("deprecation")
		public void run() {

			if (time == 100) {
				for (Location blocs : plugin.midBuffBlocks) {
					blocs.getBlock().setData((byte) 1);
				}
				String msg;
				if (plugin.redTeam.contains(p)) {
					msg = "Warning: Bow Team got Power Up.";
				} else {
					msg = "Warning: Bucket Team got Power Up.";
				}
				for (Player pl : plugin.getServer().getOnlinePlayers()) {
					BossBarAPI.setMessage(pl, msg);
				}
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				for (int a = 0; a != 100; a++) {
					scheduler.scheduleSyncDelayedTask(plugin, new bossHealth(a), a * 3 + 4);
				}

				new BukkitRunnable() {

					@Override
					public void run() {
						plugin.buffMid = 0;
						for (Location blocs : plugin.midBuffBlocks) {
							blocs.getBlock().setData((byte) 2);
						}
						for (Player pl : Bukkit.getOnlinePlayers()) {
							ItemStack[] stacks = p.getInventory().getArmorContents();
							for (ItemStack stack : stacks) {
								if (stack == null) {
									continue;
								} else {
									GlowEffect.removeGlow(stack);
								}
							}
							pl.getInventory().setArmorContents(stacks);
						}
					}

				}.runTaskLater(plugin, 300);
				if (plugin.blueTeam.contains(p)) {
					plugin.buffMid = 1;
					for (Player pl : plugin.blueTeam) {
						ItemStack[] stacks = p.getInventory().getArmorContents();
						for (ItemStack stack : stacks) {
							if (stack == null) {
								continue;
							} else {
								stack = GlowEffect.addGlow(stack);
							}
						}
						pl.getInventory().setArmorContents(stacks);

						pl.removePotionEffect(PotionEffectType.JUMP);
						pl.removePotionEffect(PotionEffectType.SPEED);
						pl.addPotionEffect(PotionEffectType.JUMP.createEffect((int) 300L, 1),false);
						pl.addPotionEffect(PotionEffectType.SPEED.createEffect((int) 300L, 2),false);
					}
				} else {
					plugin.buffMid = 2;
					for (Player pl : plugin.redTeam) {
						ItemStack[] stacks = pl.getInventory().getArmorContents();
						for (ItemStack stack : stacks) {
							if (stack == null) {
								continue;
							} else {
								stack = GlowEffect.addGlow(stack);
							}
						}
						pl.getInventory().setArmorContents(stacks);
						pl.removePotionEffect(PotionEffectType.JUMP);
						pl.removePotionEffect(PotionEffectType.SPEED);
						pl.addPotionEffect(PotionEffectType.JUMP.createEffect((int) 300L, 1),false);
						pl.addPotionEffect(PotionEffectType.SPEED.createEffect((int) 300L, 2),false);
					}
				}

			} else if (p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.PRISMARINE) {
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				p.removePotionEffect(PotionEffectType.SPEED);
				p.addPotionEffect(PotionEffectType.SPEED.createEffect((int) 11, 1),false);
				scheduler.scheduleSyncDelayedTask(plugin, new buffTimer(p, time + 1), 1L);
				if (time % 10 == 0) {
					playParticle(p, time / 10);
				}
			} else if (time > 0) {
				p.removePotionEffect(PotionEffectType.SPEED);
				p.addPotionEffect(PotionEffectType.SPEED.createEffect((int) 11, 1),false);
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				scheduler.scheduleSyncDelayedTask(plugin, new buffTimer(p, time - 5), 1L);
			}
		}
	}

	public class buffSpawn implements Runnable {
		private Player p;

		public buffSpawn(Player _p) {
			this.p = _p;
		}

		public void run() {
			for (Location loc : plugin.buffSpawn) {
				if (p.getLocation().distance(loc) <= 4) {
					p.removePotionEffect(PotionEffectType.JUMP);
					p.addPotionEffect(PotionEffectType.JUMP.createEffect((int) 11, 8),false);
					BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
					scheduler.scheduleSyncDelayedTask(plugin, new buffSpawn(p), 10);
					break;
				}
			}
		}
	}

	public class blocBuffGold implements Runnable {
		private Player p;
		private int potInRun;

		public blocBuffGold(Player _p, int _potInRun) {
			this.p = _p;
			this.potInRun = _potInRun;
		}

		public void run() {
			if (p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.GOLD_BLOCK) {
				p.removePotionEffect(PotionEffectType.JUMP);
				p.addPotionEffect(PotionEffectType.JUMP.createEffect((int) 10L, 8),false);
				if (potInRun > 0) {
					potInRun = potInRun - 1;
				}
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				scheduler.scheduleSyncDelayedTask(plugin, new blocBuffGold(p, potInRun), 1L);

			} else if (potInRun >= 6 && plugin.buffMid == 1 && plugin.blueTeam.contains(p)) {
				p.removePotionEffect(PotionEffectType.JUMP);
				p.addPotionEffect(PotionEffectType.JUMP.createEffect((int) potInRun, 1),false);
			} else if (potInRun >= 6 && plugin.buffMid == 2 && plugin.redTeam.contains(p)) {
				p.removePotionEffect(PotionEffectType.JUMP);
				p.addPotionEffect(PotionEffectType.JUMP.createEffect((int) potInRun, 1),false);
			}
		}
	}

	public class blocBuffEmerald implements Runnable {
		private Player p;
		private int potInRun;

		public blocBuffEmerald(Player _p, int _potInRun) {
			this.p = _p;
			this.potInRun = _potInRun;
		}

		public void run() {
			boolean speedhere = false;
			for (PotionEffect eff : p.getActivePotionEffects()) {
				if (eff.getType().equals(PotionEffectType.SPEED)) {
					speedhere = true;
				}
			}
			if (p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.EMERALD_BLOCK
					&& !speedhere) {
				new BukkitRunnable() {

					@Override
					public void run() {
						p.removePotionEffect(PotionEffectType.SPEED);
						if (plugin.buffMid == 1 && plugin.blueTeam.contains(p)) {
							p.addPotionEffect(PotionEffectType.SPEED.createEffect((int) potInRun - 60, 2),false);
						} else if (plugin.buffMid == 2 && plugin.redTeam.contains(p)) {
							p.addPotionEffect(PotionEffectType.SPEED.createEffect((int) potInRun - 60, 2),false);
						}
					}
				}.runTaskLater(plugin, 6);
			} else if (potInRun != 0) {
				potInRun = potInRun - 1;
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				scheduler.scheduleSyncDelayedTask(plugin, new blocBuffEmerald(p, potInRun), 1L);

			}
		}
	}

	public class blueJoin implements Runnable {
		private Player p;
		private int count;

		public blueJoin(Player _p, int _count) {
			this.p = _p;
			this.count = _count;
		}

		@SuppressWarnings("deprecation")
		public void run() {
			if (p.getLocation().getBlock().getType() == Material.ENDER_PORTAL_FRAME && !plugin.blueTeam.contains(p)) {
				p.removePotionEffect(PotionEffectType.SLOW);
				p.addPotionEffect(PotionEffectType.SLOW.createEffect((int) 50L, 3),false);
				count++;
				if (count == 10) {
					if (plugin.redTeam.contains(p)) {
						plugin.redLocation.get(plugin.redTeam.indexOf(p)).getBlock().setData((byte) 0);
						plugin.redLocation.remove(plugin.redTeam.indexOf(p));
						plugin.redTeam.remove(p);
					}
					plugin.blueTeam.add(p);
					plugin.blueLocation.add(p.getLocation());
					p.getLocation().getBlock().setData((byte) 4);
					ItemStack bboots = new ItemStack(Material.LEATHER_BOOTS, 1);
					ItemStack blegs = new ItemStack(Material.LEATHER_LEGGINGS, 1);
					ItemStack bthorn = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
					ItemStack bhead = new ItemStack(Material.LEATHER_HELMET, 1);
					LeatherArmorMeta bbootsmeta = (LeatherArmorMeta) bboots.getItemMeta();
					LeatherArmorMeta blegsmeta = (LeatherArmorMeta) blegs.getItemMeta();
					LeatherArmorMeta bthornmeta = (LeatherArmorMeta) bthorn.getItemMeta();
					LeatherArmorMeta bheadmeta = (LeatherArmorMeta) bhead.getItemMeta();
					bbootsmeta.setColor(Color.fromRGB(0, 0, 255));
					blegsmeta.setColor(Color.fromRGB(0, 0, 255));
					bthornmeta.setColor(Color.fromRGB(0, 0, 255));
					bheadmeta.setColor(Color.fromRGB(0, 0, 255));
					bboots.setItemMeta(bbootsmeta);
					blegs.setItemMeta(blegsmeta);
					bthorn.setItemMeta(bthornmeta);
					bhead.setItemMeta(bheadmeta);
					p.getInventory().setBoots(bboots);
					p.getInventory().setLeggings(blegs);
					p.getInventory().setChestplate(bthorn);
					p.getInventory().setHelmet(bhead);
					p.setDisplayName(ChatColor.BLUE + p.getDisplayName() + ChatColor.WHITE);

					if (plugin.blueTeam.size() + plugin.redTeam.size() == 8) {
						gameStart();
					}
				} else {

					BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
					scheduler.scheduleSyncDelayedTask(plugin, new blueJoin(p, count), 10L);
					playParticle(p, count);
				}
			}
		}
	}

	public class redJoin implements Runnable {
		private Player p;
		private int count;

		public redJoin(Player _p, int _count) {
			this.p = _p;
			this.count = _count;
		}

		@SuppressWarnings("deprecation")
		public void run() {
			if (p.getLocation().getBlock().getType() == Material.ENDER_PORTAL_FRAME && !plugin.redTeam.contains(p)) {
				p.removePotionEffect(PotionEffectType.SLOW);
				p.addPotionEffect(PotionEffectType.SLOW.createEffect((int) 50L, 3),false);
				count++;
				if (count == 10) {
					if (plugin.blueTeam.contains(p)) {
						plugin.blueLocation.get(plugin.blueTeam.indexOf(p)).getBlock().setData((byte) 2);
						plugin.blueLocation.remove(plugin.blueTeam.indexOf(p));
						plugin.blueTeam.remove(p);

					}
					plugin.redTeam.add(p);
					plugin.redLocation.add(p.getLocation());
					p.getLocation().getBlock().setData((byte) 4);
					ItemStack rboots = new ItemStack(Material.LEATHER_BOOTS, 1);
					ItemStack rlegs = new ItemStack(Material.LEATHER_LEGGINGS, 1);
					ItemStack rthorn = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
					ItemStack rhead = new ItemStack(Material.LEATHER_HELMET, 1);
					LeatherArmorMeta rbootsmeta = (LeatherArmorMeta) rboots.getItemMeta();
					LeatherArmorMeta rlegsmeta = (LeatherArmorMeta) rlegs.getItemMeta();
					LeatherArmorMeta rthornmeta = (LeatherArmorMeta) rthorn.getItemMeta();
					LeatherArmorMeta rheadmeta = (LeatherArmorMeta) rhead.getItemMeta();
					rbootsmeta.setColor(Color.fromRGB(255, 0, 0));
					rlegsmeta.setColor(Color.fromRGB(255, 0, 0));
					rthornmeta.setColor(Color.fromRGB(255, 0, 0));
					rheadmeta.setColor(Color.fromRGB(255, 0, 0));
					rboots.setItemMeta(rbootsmeta);
					rlegs.setItemMeta(rlegsmeta);
					rthorn.setItemMeta(rthornmeta);
					rhead.setItemMeta(rheadmeta);
					p.getInventory().setBoots(rboots);
					p.getInventory().setLeggings(rlegs);
					p.getInventory().setChestplate(rthorn);
					p.getInventory().setHelmet(rhead);
					p.setDisplayName(ChatColor.RED + p.getDisplayName() + ChatColor.WHITE);

					if (plugin.blueTeam.size() + plugin.redTeam.size() == 8) {
						gameStart();
					}
				} else {
					BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
					scheduler.scheduleSyncDelayedTask(plugin, new redJoin(p, count), 10L);
					playParticle(p, count);
				}
			}
		}
	}

	public class allowMove implements Runnable {
		private boolean move;

		public allowMove(boolean _move) {
			this.move = _move;
		}

		public void run() {
			plugin.moveAllowed = move;
		}
	}

	/***********************
	 * 
	 * Event Processing: Bow Processing
	 */

	public void playParticle(Player p, int amp) {
		for (Player pl : Bukkit.getOnlinePlayers()) {
			pl.spigot().playEffect(p.getLocation(), Effect.FLYING_GLYPH, 0, 0, 0, 2, 0, 1, 100 * amp, 30);
			if (amp > 7) {
				for (int a = 1; a != 5; a++) {

					new BukkitRunnable() {

						@Override
						public void run() {
							pl.spigot().playEffect(p.getLocation(), Effect.FIREWORKS_SPARK, 0, 0, 0, 0, 0, 1, 300, 30);

						}
					}.runTaskLater(plugin, a);
				}
			}
		}

	}

	@SuppressWarnings("deprecation")
	public void endGame() {
		Bukkit.broadcastMessage("A minigame by Nixo");
		Bukkit.broadcastMessage("Check for update on http://www.reddit.com/r/Minecraft/comments/4jzcuz/test_my_first_mini_game/");
		new BukkitRunnable() {

			@Override
			public void run() {
				plugin.source.clear();
				plugin.sourceP.clear();
				plugin.pIdSource.clear();
				plugin.pWater.clear();

			}
		}.runTaskLater(plugin, 100);
		plugin.lobby = true;
		plugin.buffMid = 0;
		Location spawn = new Location(Bukkit.getWorlds().get(0), -698.0, 97.0, 979.0);
		for (Player p : plugin.blueTeam) {
			for (ItemStack i : p.getInventory()) {
				i.setType(Material.AIR);
			}
			p.getInventory().setHelmet(null);
			p.getInventory().setChestplate(null);
			p.getInventory().setLeggings(null);
			p.getInventory().setBoots(null);
			p.getInventory().clear();
			p.updateInventory();
			p.setDisplayName(ChatColor.WHITE + p.getDisplayName());
			p.teleport(spawn);
		}
		for (Player p : plugin.redTeam) {
			p.setDisplayName(ChatColor.WHITE + p.getDisplayName());
			for (ItemStack i : p.getInventory()) {
				i.setType(Material.AIR);
			}
			p.getInventory().clear();
			p.getInventory().setHelmet(null);
			p.getInventory().setChestplate(null);
			p.getInventory().setLeggings(null);
			p.getInventory().setBoots(null);
			p.updateInventory();
			p.teleport(spawn);
		}
		for (Location spawnbloc : plugin.blueStart) {
			spawnbloc.getBlock().setData((byte) 0);
		}
		for (Location spawnbloc : plugin.redStart) {
			spawnbloc.getBlock().setData((byte) 0);
		}
		plugin.redTeam.clear();
		plugin.blueTeam.clear();
	}

	@SuppressWarnings("deprecation")
	public void pDeath(Player p, boolean leave) {
        p.setHealth(20.0);
        p.setFoodLevel(20);
		Location spawn = new Location(Bukkit.getWorlds().get(0), -698.0, 97.0, 979.0);
		p.teleport(spawn);
		p.getInventory().clear();// remove items
		p.setDisplayName(ChatColor.WHITE + p.getDisplayName());
		if (plugin.redTeam.contains(p)) {
			plugin.redLocation.get(plugin.redTeam.indexOf(p)).getBlock().setData((byte) 0);
		} else if (plugin.blueTeam.contains(p)) {
			plugin.blueLocation.get(plugin.blueTeam.indexOf(p)).getBlock().setData((byte) 0);
		}

		if (!leave) {
			Bukkit.broadcastMessage(ChatColor.WHITE + p.getDisplayName() + " is dead.");
			if (plugin.blueTeam.contains(p)) {
				plugin.blueTeam.remove(p);
				if (plugin.blueTeam.size() == 0) {
					Bukkit.broadcastMessage(ChatColor.RED + "RED Won !!!");
					endGame();
				}
			} else if (plugin.redTeam.contains(p)) {
				plugin.redTeam.remove(p);
				if (plugin.redTeam.size() == 0) {
					Bukkit.broadcastMessage(ChatColor.BLUE + "BLUE Won !!!");
					endGame();
				}
			} else {
				if (!leave) {
					Bukkit.broadcastMessage("Error, player killed not in a team");
				}
			}
		}

	}

	public void gameStart() {
		plugin.lobby = false;
		plugin.moveAllowed = false;
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(plugin, new allowMove(true), 60);

		for (Player p : plugin.redTeam) {
			p.getInventory().addItem(new ItemStack(Material.BOW));
			p.getInventory().addItem(new ItemStack(Material.ARROW));
			p.getInventory().addItem(new ItemStack(Material.ARROW));
			p.getInventory().addItem(new ItemStack(Material.ARROW));
			p.teleport(plugin.redStart.get(plugin.redTeam.indexOf(p)));
		}
		for (Player p : plugin.blueTeam) {
			p.getInventory().addItem(new ItemStack(Material.WATER_BUCKET));
			p.teleport(plugin.blueStart.get(plugin.blueTeam.indexOf(p)));
		}

	}

	@SuppressWarnings("deprecation")
	public void checkifsource(Block b) { // Check if is player source or not.
		// Remove source if not from player
		boolean check = false;
		if (b.getType() == Material.WATER || b.getType() == Material.STATIONARY_WATER) {
			if (b.getData() == (byte) 0) {
				for (int i = 0; i < plugin.source.size(); i++) {

					if (b.getX() == plugin.source.get(i).getX() && b.getY() == plugin.source.get(i).getY()
							&& b.getZ() == plugin.source.get(i).getZ()) {

						check = true;
					}
				}
				if (check == false && b.getChunk().isLoaded()) {
					b.setData((byte) 1);

				}
			}
		}
	}

	@EventHandler
	public void onBlockFromToEvent(BlockFromToEvent e) { // start when water
															// flow. Launch
															// check if source
		e.setCancelled(false); // to two blocks.
		Block b = e.getBlock();
		Block bf = e.getToBlock();
		checkifsource(b);
		checkifsource(bf);
	}

	@EventHandler
	public void onPlayerBucketFillEvent(PlayerBucketFillEvent e) {
		Block b = e.getBlockClicked();
		Player p = e.getPlayer();
		for (int i = 0; i < plugin.source.size(); i++) {
			if (b.getX() == plugin.source.get(i).getX() && b.getY() == plugin.source.get(i).getY()
					&& b.getZ() == plugin.source.get(i).getZ()) {
				plugin.source.remove(i);

				if (plugin.pIdSource.contains(p.getUniqueId())) {
					plugin.pIdSource.remove(p.getUniqueId());
				}

			}
			e.setCancelled(false);
		}
	}

	@EventHandler
	public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent e) {
		Player p = e.getPlayer();
		Block b = e.getBlockClicked();
		BlockFace f = e.getBlockFace();
		Block water = b.getRelative(f);
		if (e.getBucket() != null && e.getBucket() == Material.WATER_BUCKET) {
			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
			scheduler.scheduleSyncDelayedTask(plugin, new Runnabledelay(water, p.getPlayerListName()), 5 * 20L);
			plugin.pIdSource.add(p.getUniqueId());
			plugin.source.add(water);

		}
		e.setCancelled(false);
	}

	@EventHandler
	public void onInventoryClickEvent(InventoryClickEvent e) {
		// int nbSlot= e.getSlot();
		// if (nbSlot <= 103 && nbSlot>=100){ Put it if you want enable moving
		// items in inventory.;
		Bukkit.getPlayer(e.getWhoClicked().getName()).updateInventory();
		e.setCancelled(true);
		// }
	}

	/***********************************************************************
	 * Bow processing
	 * 
	 ***********************************************************************/

	@EventHandler
	public void onEntityShootBowEvent(EntityShootBowEvent e) {
		e.setCancelled(false);
		Player p = Bukkit.getServer().getPlayer(e.getEntity().getName());
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(plugin, new BowReload(p), 5 * 20L);
	}

	@EventHandler
	public void onProjectileHitEvent(ProjectileHitEvent e) {
		e.getEntity().remove();
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamageEvent(EntityDamageEvent e) {
		if (e.getCause() == DamageCause.FALL) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
		Player p = Bukkit.getServer().getPlayer(e.getEntity().getName());
		if (!plugin.lobby && e.getDamager().getType().equals(EntityType.ARROW) && plugin.blueTeam.contains(p)) {
			pDeath(p, false);
		} else {
			e.setCancelled(true);
		}

	}

	/*******
	 * Blocs Buffs processing
	 */
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		e.setCancelled(!plugin.moveAllowed);
		/************
		 * Water Kill
		 */
		if (plugin.lobby && !p.hasPotionEffect(PotionEffectType.JUMP)) {
			for (Location loc : plugin.buffSpawn) {
				if (p.getLocation().distance(loc) <= 4) {
					p.addPotionEffect(PotionEffectType.JUMP.createEffect((int) 11, 8),false);
					BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
					scheduler.scheduleSyncDelayedTask(plugin, new buffSpawn(p), 10L);
				}
			}
		}
		Material m = e.getPlayer().getLocation().getBlock().getType();
		Material mTop = e.getPlayer().getLocation().add(0.0, 1.0, 0.0).getBlock().getType();
		if ((m == Material.STATIONARY_WATER || m == Material.WATER || mTop == Material.STATIONARY_WATER
				|| mTop == Material.WATER) && !plugin.lobby && !plugin.blueTeam.contains(p)) {
			// TP to kill
			pDeath(p, false);
		}
		// Middle Buff Timer
		if (!plugin.lobby && p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.PRISMARINE
				&& !p.hasPotionEffect(PotionEffectType.SPEED) && plugin.buffMid == 0 && (plugin.redTeam.contains(p) || plugin.blueTeam.contains(p))) {
			p.addPotionEffect(PotionEffectType.SPEED.createEffect((int) 11L, 1),false);
			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
			scheduler.scheduleSyncDelayedTask(plugin, new buffTimer(p, 0), 10L);
		}
		// Bloc Boost
		if (!plugin.lobby && p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.GOLD_BLOCK && (plugin.redTeam.contains(p) || plugin.blueTeam.contains(p))) {
			int potInRun = 0;
			int amp = 0;
			for (PotionEffect eff : p.getActivePotionEffects()) {
				if (eff.getType().equals(PotionEffectType.JUMP)) {
					potInRun = eff.getDuration();
					amp = eff.getAmplifier();
				}
			}
			if (amp != 8) {
				p.removePotionEffect(PotionEffectType.JUMP);
				p.addPotionEffect(PotionEffectType.JUMP.createEffect((int) 10L, 8),false);
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				scheduler.scheduleSyncDelayedTask(plugin, new blocBuffGold(p, potInRun), 10L);
			}
		}

		if (!plugin.lobby
				&& p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.EMERALD_BLOCK) {
			int potInRun = 0;
			int amp = 0;
			for (PotionEffect eff : p.getActivePotionEffects()) {
				if (eff.getType().equals(PotionEffectType.SPEED)) {
					potInRun = eff.getDuration();
					amp = eff.getAmplifier();
				}
			}
			p.removePotionEffect(PotionEffectType.SPEED);
			p.addPotionEffect(PotionEffectType.SPEED.createEffect((int) 60L, 3),false);
			if (potInRun > 6 && amp != 3) {
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				scheduler.scheduleSyncDelayedTask(plugin, new blocBuffEmerald(p, potInRun), 10L);
			}
		}

		if (plugin.lobby && p.getLocation().getBlock().getType() == Material.ENDER_PORTAL_FRAME) {
			int rota = p.getLocation().getBlock().getData();
			if (rota == 0 && !p.hasPotionEffect(PotionEffectType.SLOW) && !plugin.redTeam.contains(p)) {
				p.addPotionEffect(PotionEffectType.SLOW.createEffect((int) 100L, 3),false);
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				scheduler.scheduleSyncDelayedTask(plugin, new redJoin(p, 0), 10L);
			}
			if (rota == 2 && !p.hasPotionEffect(PotionEffectType.SLOW) && !plugin.blueTeam.contains(p)) {
				p.addPotionEffect(PotionEffectType.SLOW.createEffect((int) 100L, 3),false);
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				scheduler.scheduleSyncDelayedTask(plugin, new blueJoin(p, 0), 10L);
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if (!e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
			e.setCancelled(true);
		}
	}

	// Load Location, because plugin start before World is loaded.
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent e) {
		Location spawn = new Location(Bukkit.getWorlds().get(0), -698.0, 97.0, 979.0);
		Player p = e.getPlayer();
		p.getInventory().setHelmet(null);
		p.getInventory().setChestplate(null);
		p.getInventory().setLeggings(null);
		p.getInventory().setBoots(null);
		p.teleport(spawn);
		p.setGameMode(GameMode.SURVIVAL);
		if (plugin.startup) {
			plugin.startup = false;
			Location loc1 = new Location(Bukkit.getWorlds().get(0), -720.0, 81.5, 956.0);
			Location loc2 = new Location(Bukkit.getWorlds().get(0), -721.0, 81.5, 957.0);
			Location loc3 = new Location(Bukkit.getWorlds().get(0), -721.0, 81.5, 1000.0);
			Location loc4 = new Location(Bukkit.getWorlds().get(0), -720.0, 81.5, 1001.0);
			Location loc5 = new Location(Bukkit.getWorlds().get(0), -677.0, 81.5, 1001.0);
			Location loc6 = new Location(Bukkit.getWorlds().get(0), -676.0, 81.5, 1000.0);
			Location loc7 = new Location(Bukkit.getWorlds().get(0), -676.0, 81.5, 957.0);
			Location loc8 = new Location(Bukkit.getWorlds().get(0), -677.0, 81.5, 956.0);
			Location loc9 = new Location(Bukkit.getWorlds().get(0), -698.0, 77.0, 978.0);
			Location loc10 = new Location(Bukkit.getWorlds().get(0), -699.0, 77.0, 978.0);
			Location loc11 = new Location(Bukkit.getWorlds().get(0), -699.0, 77.0, 979.0);
			Location loc12 = new Location(Bukkit.getWorlds().get(0), -698.0, 77.0, 979.0);
			Location loc13 = new Location(Bukkit.getWorlds().get(0), -698.0, 96.0, 964.0);
			Location loc14 = new Location(Bukkit.getWorlds().get(0), -698.0, 96.0, 965.0);
			Location loc15 = new Location(Bukkit.getWorlds().get(0), -698.0, 96.0, 993.0);
			Location loc16 = new Location(Bukkit.getWorlds().get(0), -698.0, 96.0, 994.0);
			Location loc17 = new Location(Bukkit.getWorlds().get(0), -704.0, 104.0, 982.0);
			Location loc18 = new Location(Bukkit.getWorlds().get(0), -700.0, 104.0, 982.0);
			Location loc19 = new Location(Bukkit.getWorlds().get(0), -696.0, 104.0, 982.0);
			Location loc20 = new Location(Bukkit.getWorlds().get(0), -692.0, 104.0, 982.0);
			Location loc21 = new Location(Bukkit.getWorlds().get(0), -692.0, 104.0, 976.0);
			Location loc22 = new Location(Bukkit.getWorlds().get(0), -696.0, 104.0, 976.0);
			Location loc23 = new Location(Bukkit.getWorlds().get(0), -700.0, 104.0, 976.0);
			Location loc24 = new Location(Bukkit.getWorlds().get(0), -704.0, 104.0, 976.0);
			plugin.redStart.add(loc1);
			plugin.redStart.add(loc2);
			plugin.redStart.add(loc3);
			plugin.redStart.add(loc4);
			plugin.blueStart.add(loc5);
			plugin.blueStart.add(loc6);
			plugin.blueStart.add(loc7);
			plugin.blueStart.add(loc8);
			plugin.midBuffBlocks.add(loc9);
			plugin.midBuffBlocks.add(loc10);
			plugin.midBuffBlocks.add(loc11);
			plugin.midBuffBlocks.add(loc12);
			plugin.buffSpawn.add(loc13);
			plugin.buffSpawn.add(loc14);
			plugin.buffSpawn.add(loc15);
			plugin.buffSpawn.add(loc16);
			plugin.blueSpawnLocation.add(loc17);
			plugin.blueSpawnLocation.add(loc18);
			plugin.blueSpawnLocation.add(loc19);
			plugin.blueSpawnLocation.add(loc20);
			plugin.redSpawnLocation.add(loc21);
			plugin.redSpawnLocation.add(loc22);
			plugin.redSpawnLocation.add(loc23);
			plugin.redSpawnLocation.add(loc24);
			for (Location loc : plugin.blueSpawnLocation) {
				loc.getBlock().setData((byte) 0);
			}
			for (Location loc : plugin.redSpawnLocation) {
				loc.getBlock().setData((byte) 2);
			}
		}
	}

	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent e) {
		pDeath(e.getPlayer(), true);
	}

	@EventHandler
	public void onFoodLevelChangeEvent(FoodLevelChangeEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void OnPlayerRespawnEvent(PlayerRespawnEvent e) {
		new BukkitRunnable() {

			@Override
			public void run() {
				Player p = e.getPlayer();
				p.getInventory().setHelmet(null);
				p.getInventory().setChestplate(null);
				p.getInventory().setLeggings(null);
				p.getInventory().setBoots(null);
				pDeath(p, true);
			}

		}.runTaskLater(plugin, 30);

	}
}
