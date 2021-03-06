package Mr_Krab.CommandSyncClient.Bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandSynchronize implements CommandExecutor {

    private final CSC plugin;

    public CommandSynchronize(CSC plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender.hasPermission("sync.use")) {
            if(args.length <= 2) {
                sender.sendMessage(plugin.getLocale().getString("HelpAuthors"));
                if(args.length >= 1) {
                    if(args[0].equalsIgnoreCase("console")){
                        sender.sendMessage(plugin.getLocale().getString("HelpCommands8"));
                        sender.sendMessage(plugin.getLocale().getString("HelpCommands7"));
                        sender.sendMessage(plugin.getLocale().getString("HelpCommands6"));
                    } else if(args[0].equalsIgnoreCase("player")) {
                        sender.sendMessage(plugin.getLocale().getString("HelpCommands5"));
                        sender.sendMessage(plugin.getLocale().getString("HelpCommands4"));
                    } else {
                        sender.sendMessage(plugin.getLocale().getString("HelpCommands9"));
                    }
                } else {
                    sender.sendMessage(plugin.getLocale().getString("HelpCommands3"));
                    sender.sendMessage(plugin.getLocale().getString("HelpCommands2"));
                    sender.sendMessage(plugin.getLocale().getString("HelpCommands1"));
                }
                sender.sendMessage(plugin.getLocale().getString("HelpLink"));
            } else if(args.length >= 3) {
                if(args[0].equalsIgnoreCase("console") || args[0].equalsIgnoreCase("player")) {
                    String[] newArgs = new String[3];
                    newArgs[0] = args[0];
                    newArgs[1] = args[1];
                    StringBuilder sb = new StringBuilder();
                    for(int i = 2; i < args.length; i++) {
                        sb.append(args[i]);
                        if(i < args.length - 1) {
                            sb.append("+");
                        }
                    }
                    newArgs[2] = sb.toString();
                    makeData(newArgs, !args[1].equalsIgnoreCase("all") && !args[1].equalsIgnoreCase("bungee"), sender);
                } else {
                    sender.sendMessage(plugin.getLocale().getString("HelpCommands9"));
                }
            }
        } else {
            sender.sendMessage(plugin.getLocale().getString("NoPerm"));
        }
        return true;
    }

    private void makeData(String[] args, Boolean single, CommandSender sender) {
        String data;
        String message;
        if(args[0].equalsIgnoreCase("console")) {
            if(args[1].equalsIgnoreCase("all")) {
                message = plugin.getLocale().getString("SyncingCommand", args[2].replaceAll("\\+", " "),  plugin.getLocale().getString("SyncConsoleAll"));
            } else {
                message = plugin.getLocale().getString("SyncingCommand", args[2].replaceAll("\\+", " "),  plugin.getLocale().getString("SyncConsole", args[1]));
            }
        } else if(args[0].equalsIgnoreCase("bungee")) {
            message = plugin.getLocale().getString("SyncingCommand", args[2].replaceAll("\\+", " "),  plugin.getLocale().getString("SyncConsole", args[1]));
        } else {
            if(args[1].equalsIgnoreCase("all")) {
                message = plugin.getLocale().getString("SyncingCommand", args[2].replaceAll("\\+", " "),  plugin.getLocale().getString("SyncPlayerAll"));
            } else {
                message = plugin.getLocale().getString("SyncingCommand", args[2].replaceAll("\\+", " "),  plugin.getLocale().getString("SyncPlayer", args[1]));
            }
        }
        if(single) {
            data = args[0].toLowerCase() + plugin.spacer + "single" + plugin.spacer + args[2] + plugin.spacer + args[1];
        } else {
            data = args[0].toLowerCase() + plugin.spacer + args[1].toLowerCase() + plugin.spacer + args[2];
        }
        plugin.oq.add(data);
        sender.sendMessage(message);
    }
}