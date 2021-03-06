package Mr_Krab.CommandSyncServer.Bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

public class CommandThread extends Thread {

    private final CSS plugin;
    private final ProxiedPlayer player;
    private final String name;
    private final List<String> commands;

    public CommandThread(CSS plugin, ProxiedPlayer player) {
        this.plugin = plugin;
        this.player = player;
        this.name = player.getName();
        this.commands = plugin.pq.get(name);
    }

    public void run() {
        while(true) {
            try {
                for(String command : commands) {
                    player.chat(command);
                    plugin.getLogger().info(plugin.getLocale().getString("BungeeRanPlayerSingle", command, name));
                }
                plugin.pq.remove(name);
                return;
            } catch(IllegalStateException e1) {
                try {
                    sleep(1000);
                } catch(InterruptedException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }
}