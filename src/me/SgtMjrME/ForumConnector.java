package me.SgtMjrME;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ForumConnector extends JavaPlugin implements Listener{
	
	Random r = new Random();
	String host;
	String port;
	String username;
	String password;
	String database;
	MySQLDatabase mySQLDatabase = null;
	int nextid;
	Timestamp defTime;
	private String joinMessage;
	char[] vchar;
	final String zvalue = "0000-00-00 00:00:00";
	Date d;
	
	@Override
	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(getDataFolder().getAbsolutePath() + "/config.yml");
			host = config.getString("host", "localhost");
			port = config.getString("port", "3306");
			username = config.getString("username", "root");
			password = config.getString("password", "root");
			database = config.getString("database", null);
			joinMessage = config.getString("joinmessage", "Join the forums today!");
			mySQLDatabase = new MySQLDatabase(host, port, username, password, database);
			mySQLDatabase.open();
			vchar = new char[26];
			for(char c = 'a'; c <= 'z'; ++c){
				vchar[c - 'a'] = c;
			}
			ResultSet res = mySQLDatabase.query("SELECT MAKEDATE(1980, 1);");
			if (res.next()){
				d = res.getDate(1);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String MD5(String md5) {
		   try {
		        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
		        md.update(md5.getBytes());
		        byte[] array = md.digest();
		        StringBuffer sb = new StringBuffer();
		        for (int i = 0; i < array.length; ++i) {
		          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
		       }
		        return sb.toString();
		    } catch (java.security.NoSuchAlgorithmException e) {
		    }
		    return null;
		}
	
	private String generatePassword(String A){
		try{
		String B = "";
		for(int i = 0; i < 32; i++) B += vchar[r.nextInt(24)];
		String C = A + B;
		String D = MD5(C);
		return (D + ":" + B);
		}
		catch(Exception e){e.printStackTrace();}
		return null;
	}
	
	@Override
	public boolean onCommand(CommandSender snd, Command cmd, String label, String[] args){
		if (!(snd instanceof Player)) return false;
		Player p = (Player) snd;
		if (label.equalsIgnoreCase("resetpassword") || label.equalsIgnoreCase("password")){
			try {
				String randPass = "";
				for(int i = 0; i < 10; i++){
					randPass += vchar[r.nextInt(24)];
				}
				String storedPass = generatePassword(randPass);
				mySQLDatabase.update("UPDATE web_users SET password = '" + storedPass + "' WHERE username = '" + p.getName() + "';");
				p.sendMessage("Your password has been reset.  It is now " + randPass);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent e){
		if (mySQLDatabase == null) return;
		try {
			ResultSet res = mySQLDatabase.query("SELECT * FROM web_users WHERE username = \"" + e.getPlayer().getName() + "\";");
			if (res.next()){
				//Person found
				Timestamp ts = res.getTimestamp("lastvisitDate");
				if (ts.before(d)){
					//Hope this works
					e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', joinMessage));
					String randPass = "";
					for(int i = 0; i < 10; i++){
						randPass += vchar[r.nextInt(24)];
					}
					String storedPass = MD5(randPass);
					mySQLDatabase.update("UPDATE web_users SET password = '" + storedPass + "' WHERE username = '" + e.getPlayer().getName() + "';");
					e.getPlayer().sendMessage(ChatColor.GRAY + "Your temporary password is " + randPass);
				}
			}
			else{
				//person not found, create new entry
				ResultSet maxres = mySQLDatabase.query("SELECT MAX(id) AS MAXID FROM web_users");
				if (maxres.next()){
					nextid = maxres.getInt(1);
					nextid++;
				}
				else
					nextid = 1;
				String randPass = "";
//				for (int i = 0; i < 26; i++){
//					Bukkit.getLogger().info("" + vchar[i]);
//				}
				for(int i = 0; i < 10; i++){
					randPass += vchar[r.nextInt(24)];
				}
				String storedPass = generatePassword(randPass);
				mySQLDatabase.create("INSERT INTO web_users(id, name, username, email, password, block, sendEmail, registerDate, lastvisitDate, activation, params, lastResetTime, resetCount)" +
						"  VALUES(" + nextid + ", '" + e.getPlayer().getName() + "', '" + e.getPlayer().getName() + "', 'NA', '" + storedPass + "', 0, 0, NOW(), Timestamp('1975-01-01'), 0, " +
								"\"{'admin_style':'','admin_language':'','language':'','editor':'','helpsite':'','timezone':''}\", Timestamp('1975-01-01'), 0);");
				mySQLDatabase.create("INSERT INTO web_user_usergroup_map(user_id, group_id) VALUES(" + nextid + ", 2);");
//				res = mySQLDatabase.query("SELECT * FROM web_users WHERE username = \"" + e.getPlayer().getName() + "\";");
				final Player p = e.getPlayer();
				final String passOut = randPass;
				getServer().getScheduler().runTaskLater(this, new Runnable(){

					@Override
					public void run() {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', joinMessage));
						p.sendMessage(ChatColor.GRAY + "Your temporary password is " + passOut);
					}
					
				}, 200);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try{
			Player p = e.getPlayer();
			if (p.hasPermission("fc.admin")){}//do nothing (don't think this'll be a problem)
			else if (p.hasPermission("fc.mod")) updateRank(p, 8);
			else if (p.hasPermission("fc.helper")) updateRank(p, 22);
			else if (p.hasPermission("fc.demigod")) updateRank(p, 21);
			else if (p.hasPermission("fc.empress")) updateRank(p, 20);
			else if (p.hasPermission("fc.emperor")) updateRank(p, 19);
			else if (p.hasPermission("fc.queen")) updateRank(p, 18);
			else if (p.hasPermission("fc.king")) updateRank(p, 17);
			else if (p.hasPermission("fc.lady")) updateRank(p, 16);
			else if (p.hasPermission("fc.lord")) updateRank(p, 15);
			else if (p.hasPermission("fc.maiden")) updateRank(p, 14);
			else if (p.hasPermission("fc.knight")) updateRank(p, 13);
			else if (p.hasPermission("fc.merchant")) updateRank(p, 12);
		} catch(Exception er){er.printStackTrace();}
	}
	
	private void updateRank(Player p, int rank){
		try {
			mySQLDatabase.update("UPDATE web_kuena_users SET rank = " + rank + " WHERE id IN (SELECT * FROM web_users WHERE username = '" + p.getName() + "')");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
