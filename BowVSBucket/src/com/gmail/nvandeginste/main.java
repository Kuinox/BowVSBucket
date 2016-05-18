package com.gmail.nvandeginste;




import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;


 
public class main extends JavaPlugin {
	public EventClass ec;

	public ArrayList<Block> source = new ArrayList<Block>();
	public ArrayList<Block> sourceP = new ArrayList <Block>();
	public ArrayList<UUID> pIdSource = new ArrayList<UUID>();
	public ArrayList<Player> pWater = new ArrayList<Player>();
	public ArrayList<Player> blueTeam = new ArrayList<Player>();
	public ArrayList<Player> redTeam = new ArrayList<Player>();
	public ArrayList<Location> blueLocation = new ArrayList<Location>();
	public ArrayList<Location> redLocation = new ArrayList<Location>();
	public ArrayList<Location> redStart = new ArrayList<Location>();
	public ArrayList<Location> blueStart = new ArrayList<Location>();
	public ArrayList<Location> midBuffBlocks = new ArrayList<Location>();
	public ArrayList<Location> buffSpawn = new ArrayList<Location>();
	public ArrayList<Location> blueSpawnLocation = new ArrayList<Location>();
	public ArrayList<Location> redSpawnLocation = new ArrayList<Location>();
	public boolean lobby = true;
	public int buffMid = 0;
	public boolean moveAllowed = true;
	public boolean startup = true;
	public static String datapath = "plugins/BvB/config.yml";
	
    @Override
    public void onEnable() {
    	getConfig().options().copyDefaults(false);
    	this.saveConfig();
    	File pconfl1 = new File(datapath);
        FileConfiguration pconf1 = YamlConfiguration.loadConfiguration(pconfl1);
        if (!pconfl1.exists()) {
          try
          {
            pconfl1.createNewFile();
                 
            pconf1.save(pconfl1);
          }
          catch (IOException e)
          {
            System.out.println("§4Erreur critique, impossible de créer le fichier de divers");
            e.printStackTrace();
          }
        }
    	this.ec = new EventClass(this);
    	PluginManager pe =Bukkit.getServer().getPluginManager();
    	pe.registerEvents(ec,this);
        
    	
    }
    
    @Override
    public void onDisable() {

    	}
	}