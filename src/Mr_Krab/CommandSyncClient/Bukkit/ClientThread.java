package Mr_Krab.CommandSyncClient.Bukkit;

import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class ClientThread extends Thread {

    private final CSC plugin;
    private final InetAddress ip;
    private final Integer port;
    private Boolean connected = false;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Integer heartbeat;
    private final String name;
    private final String pass;

    public ClientThread(CSC plugin, InetAddress ip, Integer port, Integer heartbeat, String name, String pass) {
        this.plugin = plugin;
        this.ip = ip;
        this.port = port;
        this.heartbeat = heartbeat;
        this.name = name;
        this.pass = pass;
        connect(false);
    }

    public void run() {
        while(true) {
            if(connected) {
                out.println("heartbeat");
                if(out.checkError()) {
                    connected = false;
                    Bukkit.getConsoleSender().sendMessage(plugin.getLocale().getString("ConnectLost"));
                } else {
                    try {
                        int size = plugin.oq.size();
                        Integer count = plugin.qc;
                        if(size > count) {
                            for(int i = count; i < size; i++) {
                                count++;
                                String output = plugin.oq.get(i);
                                out.println(output);
                                Bukkit.getConsoleSender().sendMessage(plugin.getLocale().getString("SentOutput", socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()), output));
                            }
                            plugin.qc = count;
                        }
                        while(in.ready()) {
                            String input = in.readLine();
                            if(!input.equals("heartbeat")) {
                                Bukkit.getConsoleSender().sendMessage(plugin.getLocale().getString("ReceivedInput", socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()), input));
                                String[] data = input.split(plugin.spacer);
                                if(data[0].equals("console")) {
                                    String command = data[2].replaceAll("\\+", " ");
                                    Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command), 0);
                                    Bukkit.getConsoleSender().sendMessage(plugin.getLocale().getString("RanCommand", command));
                                }
                            }
                        }
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                connect(true);
            }
            try {
                sleep(heartbeat);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void connect(Boolean sleep) {
        if(sleep) {
            try {
                sleep(10000);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(name);
            if(in.readLine().equals("n")) {
                Bukkit.getConsoleSender().sendMessage(plugin.getLocale().getString("NameError", name));
                socket.close();
                return;
            }
            out.println(pass);
            if(in.readLine().equals("n")) {
                Bukkit.getConsoleSender().sendMessage(plugin.getLocale().getString("InvalidPassword"));
                socket.close();
                return;
            }
            String version = "2.5";
            out.println(version);
            if(in.readLine().equals("n")) {
                Bukkit.getConsoleSender().sendMessage(plugin.getLocale().getString("VersionError", version, in.readLine()));
                socket.close();
                return;
            }
            connected = true;
            Bukkit.getConsoleSender().sendMessage(plugin.getLocale().getString("ConnectInfo", ip.getHostName(), String.valueOf(port), name));
        } catch(IOException e) {
            Bukkit.getConsoleSender().sendMessage(plugin.getLocale().getString("NoConnect"));
        }
    }
}