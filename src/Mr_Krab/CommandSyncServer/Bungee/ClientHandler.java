package Mr_Krab.CommandSyncServer.Bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientHandler extends Thread {

    private final CSS plugin;
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final String name;
    private final Integer heartbeat;

    public ClientHandler(CSS plugin, Socket socket, Integer heartbeat, String pass) throws IOException {
        this.plugin = plugin;
        this.socket = socket;
        this.heartbeat = heartbeat;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        plugin.getLogger().info(plugin.getLocale().getString("BungeeConnect", socket.getInetAddress().getHostName(), String.valueOf(socket.getPort())));
        name = in.readLine();
        if (plugin.c.contains(name)) {
            plugin.getLogger().info(plugin.getLocale().getString("NameErrorBungee", socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()), name));
            out.println("n");
            socket.close();
            return;
        }
        out.println("y");
        try {
            if (!in.readLine().equals(pass)) {
                plugin.getLogger().info(plugin.getLocale().getString("PassError", socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()), name));
                out.println("n");
                socket.close();
                return;
            }
            out.println("y");
            String version = in.readLine();
            String version1 = "2.5";
            if (!version.equals(version1)) {
                plugin.getLogger().info(plugin.getLocale().getString("VersionErrorBungee", socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()), name, version, version1));
                out.println("n");
                out.println(version1);
                socket.close();
                return;
            }
            out.println("y");
            if (!plugin.qc.containsKey(name)) {
                plugin.qc.put(name, 0);
            }
            plugin.c.add(name);
            plugin.getLogger().info(plugin.getLocale().getString("ConnectFrom", socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()), name));
        } catch (SocketException | NullPointerException ignored) {
        }
    }

    public void run() {
        while (true) {
            try {
                out.println("heartbeat");
                if (out.checkError()) {
                    plugin.getLogger().info(plugin.getLocale().getString("Disconect", socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()), name));
                    plugin.c.remove(name);
                    return;
                }
                while (in.ready()) {
                    String input = in.readLine();
                    if (!input.equals("heartbeat")) {
                        plugin.getLogger().info(plugin.getLocale().getString("BungeeInput", socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()), name, input));
                        String[] data = input.split(plugin.spacer);
                        if (data[0].equals("player")) {
                            String command = "/" + data[2].replaceAll("\\+", " ");
                            if (data[1].equals("single")) {
                                String name = data[3];
                                boolean found = false;
                                for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                                    if (name.equals(player.getName())) {
                                        player.chat(command);
                                        plugin.getLogger().info(plugin.getLocale().getString("BungeeRanPlayerSingle", command, name));
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    if (plugin.pq.containsKey(name)) {
                                        List<String> commands = plugin.pq.get(name);
                                        commands.add(command);
                                        plugin.pq.put(name, commands);
                                    } else {
                                        plugin.pq.put(name, new ArrayList<>(Collections.singletonList(command)));
                                    }
                                    plugin.getLogger().info(plugin.getLocale().getString("BungeeRanPlayerOffline", name, command));
                                }
                            } else if (data[1].equals("all")) {
                                for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                                    player.chat(command);
                                }
                                plugin.getLogger().info(plugin.getLocale().getString("BungeeRanAll", command));
                            }
                        } else {
                            if (data[1].equals("bungee")) {
                                String command = data[2].replaceAll("\\+", " ");
                                plugin.getProxy().getPluginManager().dispatchCommand(plugin.getProxy().getConsole(), command);
                                plugin.getLogger().info(plugin.getLocale().getString("BungeeRanServer", command));
                            } else {
                                plugin.oq.add(input);
                            }
                        }
                    }
                }
                int size = plugin.oq.size();
                Integer count = plugin.qc.get(name);
                if (size > count) {
                    for (int i = count; i < size; i++) {
                        count++;
                        String output = plugin.oq.get(i);
                        String[] data = output.split(plugin.spacer);
                        if (data[1].equals("single")) {
                            if (data[3].equals(name)) {
                                out.println(output);
                                plugin.getLogger().info(plugin.getLocale().getString("BungeeSentOutput", socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()), name, output));
                            }
                        } else {
                            out.println(output);
                            plugin.getLogger().info(plugin.getLocale().getString("BungeeSentOutput", socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()), name, output));
                        }
                    }
                    plugin.qc.put(name, count);
                }
                sleep(heartbeat);
            } catch (NullPointerException ignored) {
            } catch (Exception e) {
                plugin.c.remove(name);
                e.printStackTrace();
            }
        }
    }
}