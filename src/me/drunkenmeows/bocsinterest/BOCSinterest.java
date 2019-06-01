package me.drunkenmeows.bocsinterest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

public class BOCSinterest extends JavaPlugin {
	
	private Essentials ess;
	
	public BukkitTask task;
	
	public static Economy econ = null;
	
	public final Logger logger = Logger.getLogger("Minecraft");
	
	double interest;
	
	@Override
	public void onEnable()
	{		
		//Date date = new Date();
		//logger.info("time long:"+ date.getTime());
		
		long interval = this.getConfig().getLong("Interval", 3600L) * 20L;
		
		this.saveDefaultConfig();
		
		if (!setupEconomy() ) {
			logger.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		
		if (!setupEssentials() ) {
			logger.severe(String.format("[%s] - Disabled due to no Essentials dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		
		initialise();
		//start interest scheduler
		task = this.getServer().getScheduler().runTaskTimer(this, new Runnable(){
			public void run() {
				giveInterest();
			}		
		}, interval, interval);
		
		logger.info("["+getDescription().getName()+"] - Runnning with a payment interval of ["+interval+"] ticks");
	}
	
	public void initialise() {
		interest = this.getConfig().getDouble("Interest", 0.00);
	}
	
	private boolean setupEssentials() {
		if(getServer().getPluginManager().getPlugin("Essentials") == null) {
			return false;
		} else {
			ess = (Essentials)this.getServer().getPluginManager().getPlugin("Essentials");
			return true;
		}
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	@Override
	public void onDisable()
	{
		
	}
	
	public void giveInterest() {
		for(Player p: this.getServer().getOnlinePlayers())
		{
			//get Essentials User.
			User user = ess.getUser(p);
			//calculate earnings.
			double earnings = econ.getBalance(p.getName()) * interest;
			//round to 2 decimal places
			BigDecimal bd = new BigDecimal(earnings);
		    BigDecimal rounded = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
		    earnings = rounded.doubleValue();
			
		    //for each player with permission, whom is not AFK,
			if((p.hasPermission("bocsi.player")) && !(user.isAfk())) {				
				if(earnings > 0) {
						EconomyResponse r = econ.depositPlayer(p.getName(), earnings);
				        if(r.transactionSuccess())
				        	p.sendMessage(c("&2[&fMoney&2]&2 You earned:&f "+earnings+"&2 for being online."));
				}	
			}	
		}
	}
	
	//colourise text
	public String c(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

}
