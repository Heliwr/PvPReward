package com.codisimus.plugins.pvpreward;

import com.codisimus.plugins.pvpreward.Rewarder.RewardType;
import java.util.Iterator;
import java.util.LinkedList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Executes Player Commands
 * 
 * @author Codisimus
 */
public class PvPRewardCommand implements CommandExecutor {
    private static enum Action { HELP, OUTLAWS, KARMA, KDR, RANK, TOP, RESET }
    static String command;
    static int top;
    
    /**
     * Listens for PvPReward commands to execute them
     * 
     * @param sender The CommandSender who may not be a Player
     * @param command The command that was executed
     * @param alias The alias that the sender used
     * @param args The arguments for the command
     * @return true always
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        //Display the help page if the Player did not add any arguments
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        Action action;
        
        try {
            action = Action.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException notEnum) {
            sendHelp(sender);
            return true;
        }
        
        //Execute the correct command
        switch (action) {
            case OUTLAWS:
                if (args.length == 1) {
                	outlaws(sender);	
                } else {
                	sendHelp(sender);	
                }
                return true;
                
            case KARMA:
                switch (args.length) {
                    case 1:
                    	karma(sender, sender.getName());
                    	return true;
                    case 2:
                    	karma(sender, args[1]);
                    	return true;
                    default:
                    	sendHelp(sender);
                    	return true;
                }
                
            case KDR:
                switch (args.length) {
                    case 1:
                    	kdr(sender, sender.getName());
                    	return true;
                    case 2:
                    	kdr(sender, args[1]);
                    	return true;
                    default:
                    	sendHelp(sender);
                    	return true;
                }
                
            case RANK:
                switch (args.length) {
                    case 1:
                    	rank(sender, sender.getName());
                    	return true;
                    case 2:
                    	rank(sender, args[1]);
                    	return true;
                    default:
                    	sendHelp(sender);
                    	return true;
                }
                
            case TOP:
                switch (args.length) {
                    case 1:
                    	top(sender, 5);
                    	return true;
                    case 2:
                        try {
                            top(sender, Integer.parseInt(args[1]));
                            return true;
                        }
                        catch (Exception notInt) {
                            break;
                        }
                        
                    default: break;
                }
                
                sendHelp(sender);
                return true;
                
            case RESET:
                switch (args.length) {
                    case 2:
                        if (args[1].equals("kdr")) {
                        	reset(sender, true, sender.getName());	
                        } else if (args[1].equals("karma")) {
                        	reset(sender, false, sender.getName());	
                        } else {
                        	break;	
                        }
                        return true;
                        
                    case 3:
                        if (args[1].equals("kdr")) {
                        	reset(sender, true, args[2]);	
                        } else if (args[1].equals("karma")) {
                        	reset(sender, false, args[2]);	
                        } else {
                        	break;	
                        }
                        return true;
                        
                    default: break;
                }
                
                sendResetHelp(sender);
                return true;
                
            default:
            	sendHelp(sender);
            	return true;
        }
    }
    
    /**
     * Displays the current Outlaws
     *
     * @param player The Player executing the command
     */
    private static void outlaws(CommandSender sender) {
        String outlaws = "§eCurrent " + PvPReward.outlawName + "s (<count>):§2  ";
        Integer count = 0;
        
        //Append the name of each Outlaw
        for (Record record: PvPReward.getRecords()) {
        	if (record.isOutlaw()) {
                outlaws = outlaws.concat(record.name + ", ");
                count++;
            }
        }
            
        outlaws = outlaws.replace("<count>", Integer.toString(count));
        sender.sendMessage(outlaws.substring(0, outlaws.length() - 2));
    }
    
    /**
     * Displays the current karma value of the specified Record
     *
     * @param player The Player executing the command
     * @param name The name of the Record
     */
    private static void karma(CommandSender sender, String name) {
        //Return if the Record does not exist
        Record record = PvPReward.findRecord(name);
        if (record == null) {
            sender.sendMessage("No PvP Record found for " + name);
            return;
        }
        
        //Add '-' before the karma values if negative is set to true
        if (PvPReward.negative) {
            if (record.karma == 0) {
                sender.sendMessage("§2Current " + PvPReward.karmaName + " level:§b " + record.karma);        	
            } else {
            	sender.sendMessage("§2Current " + PvPReward.karmaName + " level:§b -" + record.karma);
            }
            sender.sendMessage("§2" + PvPReward.outlawName + " status at §b-" + Record.outlawLevel);
        } else {
            sender.sendMessage("§2Current " + PvPReward.karmaName + " level:§b " + record.karma);
            sender.sendMessage("§2" + PvPReward.outlawName + " status at §b" + Record.outlawLevel);
        }
    }
    
    /**
     * Displays the current kdr of the specified Record
     *
     * @param player The Player executing the command
     * @param name The name of the Record
     */
    private static void kdr(CommandSender sender, String name) {
        //Return if the Record does not exist
        Record record = PvPReward.findRecord(name);
        if (record == null) {
            sender.sendMessage("No PvP Record found for " + name);
            return;
        }
        
        sender.sendMessage("§2Current Kills:§b " + record.kills);
        sender.sendMessage("§2Current Deaths:§b " + record.deaths);
        sender.sendMessage("§2Current KDR:§b " + record.kdr);
    }
    
    /**
     * Displays the current kdr rank of the specified Record
     *
     * @param player The Player executing the command
     * @param name The name of the Record
     */
    private static void rank(CommandSender sender, String name) {
        int rank = 1;
        String playerName = sender.getName();
        
        //Increase rank by one for each Record that has a higher kdr
        for (Record record: PvPReward.getRecords()) {
        	if (record.name.equals(playerName)) {
                sender.sendMessage("§2Current Rank:§b " + rank);
                return;
            } else {
            	rank++;	
            }
        }
            
        sender.sendMessage("No PvP Record found for " + name);
    }
    
    /**
     * Displays the top KDRs
     *
     * @param player The Player executing the command
     * @param amount The amount of KDRs to be displayed
     */
    private static void top(CommandSender sender, int amount) {
        sender.sendMessage("§eKDR Leaderboard:");
        
        //Sort the Records
        LinkedList<Record> records = PvPReward.getRecords();
        
        //Verify that amount is not too big
        int size = records.size();
        if (amount > size) {
        	amount = size;	
        }
        
        Iterator<Record> itr = PvPReward.getRecords().iterator();
        Record record;
        
        //Display the name and KDR of the first x Records
        for (int i = 0; i < amount; i++) {
            record = itr.next();
            sender.sendMessage("§2" + record.name + ":§b " + record.kdr);
        }
    }
    
    /**
     * Resets kdr or karma values
     *
     * @param player The Player executing the command
     * @param kdr True if reseting kdr, false if reseting karma
     * @param name The name of the Record, 'all' to specify all Records,
     *          or null to specify the record of the given player
     */
    @SuppressWarnings("unused")
	private static void reset(CommandSender sender, boolean kdr, String name) {
        //Cancel if the Player does not have the proper permissions
    	Player player = null;
    	if (sender instanceof Player) {
    		player = (Player)sender;	
    	}

    	if (!PvPReward.hasPermisson(player, "reset")) {
            sender.sendMessage("You do not have permission to do that.");
            return;
        }
        
        if (kdr) //Reset kdr
            if (name.equals("all")) { //Reset all Records
                for (Record record: PvPReward.getRecords()) {
                    record.kills = 0;
                    record.deaths = 0;
                    record.kdr = 0;
                }            	
            } else { //Reset a specified Record
                //Use the Record of the given Player if name is null
                if (name == null) {
                	name = player.getName();	
                }
                
                //Return if the Record does not exist
                Record record = PvPReward.findRecord(name);
                if (record == null) {
                    player.sendMessage("No PvP Record found for "+name);
                    return;
                }
                
                record.kills = 0;
                record.deaths = 0;
                record.kdr = 0;
            } else { //Reset karma
                if (name.equals("all")) { //Reset all Records
                    for (Record record: PvPReward.getRecords()) {
                    	while (record.karma != 0) {
                    		record.decrementKarma(player);	
                    	}	
                    }                	
                } else { //Reset a specified Record
                    //Use the Record of the given Player if name is null
                    if (name == null) {
                    	name = player.getName();	
                    }
                    
                    //Return if the Record does not exist
                    Record record = PvPReward.findRecord(name);
                    if (record == null) {
                        player.sendMessage("No PvP Record found for "+name);
                        return;
                    }
                    
                    while (record.karma != 0) {
                    	record.decrementKarma(player);	
                    }
                }
            }
        
        PvPReward.save();
    }
    
    /**
     * Displays the Reset Help Page to the given Player
     *
     * @param player The Player needing help
     */
    private static void sendResetHelp(CommandSender sender) {
        sender.sendMessage("§e  PvPReward Reset Help Page:");
        sender.sendMessage("§2/"+command+" reset kdr (Player)§b Set kills and deaths to 0");
        sender.sendMessage("§2/"+command+" reset kdr all§b Set everyone's kills and deaths to 0");
        
        //Only display karma commands if the reward type is set to karma
        if (Rewarder.rewardType.equals(RewardType.KARMA)) {
            sender.sendMessage("§2/"+command+" reset "+PvPReward.karmaName+" (Player)§b Set "+PvPReward.karmaName+" level to 0");
            sender.sendMessage("§2/"+command+" reset "+PvPReward.karmaName+" all§b Set everyone's "+PvPReward.karmaName+" level to 0");
        }
    }
    
    /**
     * Displays the PvPReward Help Page to the given Player
     *
     * @param player The Player needing help
     */
    private static void sendHelp(CommandSender sender) {
        sender.sendMessage("§e  PvPReward Help Page:");
        
        //Only display karma commands if the reward type is set to karma
        if (Rewarder.rewardType.equals(RewardType.KARMA)) {
            sender.sendMessage("§2/"+command+" "+PvPReward.outlawName+"s§b List current "+PvPReward.outlawName+"s");
            sender.sendMessage("§2/"+command+" "+PvPReward.karmaName+" (Player)§b List current "+PvPReward.karmaName+" level");
        }
        
        sender.sendMessage("§2/"+command+" kdr (Player)§b List current KDR");
        sender.sendMessage("§2/"+command+" rank (Player)§b List current rank");
        sender.sendMessage("§2/"+command+" top (amount)§b List top x KDRs");
        sender.sendMessage("§2/"+command+" reset§b List Admin reset commands");
    }
}
