import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


public class GeoCmd extends JavaPlugin implements Listener {
	private String noperm;
	private FileConfiguration config;
	
	protected String prefix;
	
	protected boolean debug = false;
	protected boolean update = false;
	private Set<String> cc;
	
	
	public void onEnable() {
		loadconfig();
		new Updater(this);
    	getServer().getPluginManager().registerEvents(this, this);
	}

	
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
		boolean isplayer = false;
		Player p = null;
		
		if ((sender instanceof Player)) {
			p = (Player)sender;
			isplayer = true;
		}
			if(cmd.getName().equalsIgnoreCase("geocmd") && args.length != 0){
								
				// disable
				if(args[0].equalsIgnoreCase("disable")){
					if(isplayer){
						if(p.hasPermission("gc.disable")){
							this.setEnabled(false);
							p.sendMessage(prefix + " was disabled");
							System.out.println(ChatColor.stripColor(prefix + " was disabled by " + p.getName()));
						return true;
					}
						else{
							p.sendMessage(noperm);
							return true;
						}
					}
					else{
							this.setEnabled(false);
							System.out.println(ChatColor.stripColor(prefix + " was disabled"));
						return true;
					}
				}
				
				// reset
				if(args[0].equalsIgnoreCase("reset")){
					if(isplayer){
						if(p.hasPermission("gc.reset")){
							    File configFile = new File(getDataFolder(), "config.yml");
							    configFile.delete();
							    p.sendMessage(prefix + " config deleted");
								    loadconfig();
								    p.sendMessage(prefix + " config reloaded");
						    System.out.println(ChatColor.stripColor(prefix + " config reset by " + p.getName()));
						return true;
						}
						else{
							p.sendMessage(noperm);
							return true;
						}
					}
					else{
						    File configFile = new File(getDataFolder(), "config.yml");
						    configFile.delete();
						    System.out.println(ChatColor.stripColor(prefix + " config deleted"));
							    loadconfig();
							    System.out.println(ChatColor.stripColor(prefix + " config reloaded"));
					    return true;
					}
				}
				
				// reload
				if(args[0].equalsIgnoreCase("reload")){
					if(isplayer){
						if(p.hasPermission("gc.reload")){
								loadconfig();
								p.sendMessage(prefix + " config reloaded");
						    System.out.println(ChatColor.stripColor(prefix + " config reloaded by " + p.getName()));
						return true;
						}
						else{
							p.sendMessage(noperm);
							return true;
						}
					}
					else{
						    loadconfig();
						    System.out.println(ChatColor.stripColor(prefix + " config reloaded"));
					    return true;
					}
				}
			}
		
		// nothing to do here \o/
		return false;
	}
	
	
	@EventHandler
	private void Playerjoin(PlayerJoinEvent e){
		try {
			String code = check(e.getPlayer().getAddress().getAddress().getHostAddress());
			for(String land : cc) {
				if(code.equals(land)) {
					execute(e.getPlayer(), land);
					return;
				}
			}
			execute(e.getPlayer(), "DEFAULT");
		}catch(Exception e1) {}
	}

	
	private void execute(final Player p, String land) {
		List<String> list = config.getStringList("location." + land);
	
		for(String cmd : list) {
			final String[] args = cmd.split(",");
			boolean b = Boolean.parseBoolean(args[0]);
			
			args[1] = args[1].replace("%player%", p.getName());
			args[1] = args[1].replace("%ip%", p.getAddress().getAddress().getHostAddress());
			args[1] = args[1].replace("%cc%", land);
			
			if(b) {
		        new BukkitRunnable() {
		            @Override
		            public void run() {getServer().dispatchCommand(Bukkit.getConsoleSender(),args[1]);}
		        }.runTaskLater(this, 20);
			}else {
		        new BukkitRunnable() {
		            @Override
		            public void run() {getServer().dispatchCommand(p,args[1]);}
		        }.runTaskLater(this, 20);
				
			}
		}
	}
	
	
	private String check(String ip) {
		String value = "DEFAULT";
			try {
				URL link = new URL("http://freegeoip.net/json/" + ip);
		        BufferedReader in = new BufferedReader( new InputStreamReader(link.openStream()));
			        String land = in.readLine();
			        land = land.split("country_code")[1].split("country_name")[0].substring(3, 5);
			        if(!land.contains(",")) {value = land;}
		        in.close();
			}catch(Exception e2) {}
		return value;
	}

	
	private void loadconfig(){
		config = getConfig();
		config.options().copyDefaults(true);
		saveConfig();
		
		debug = config.getBoolean("debug");
		noperm = ChatColor.translateAlternateColorCodes('&', config.getString("msg.noperm"));
		prefix = ChatColor.translateAlternateColorCodes('&', config.getString("msg.prefix"));
		
		cc = config.getConfigurationSection("location").getKeys(false);
	}
  
	
	public void say(Player p, boolean b) {
		if(b) {
			System.out.println(ChatColor.stripColor(prefix + "----------------------------------------------"));
			System.out.println(ChatColor.stripColor(prefix + " GeoCmd is outdated. Get the new version here:"));
			System.out.println(ChatColor.stripColor(prefix + " http://www.pokemon-online.xyz/plugin"));
			System.out.println(ChatColor.stripColor(prefix + "----------------------------------------------"));
		}else {
		   	p.sendMessage(prefix + "----------------------------------------------");
		   	p.sendMessage(prefix + " GeoCmd is outdated. Get the new version here:");
		   	p.sendMessage(prefix + " http://www.pokemon-online.xyz/plugin");
		   	p.sendMessage(prefix + "----------------------------------------------");
		}
	}
	
	
}
