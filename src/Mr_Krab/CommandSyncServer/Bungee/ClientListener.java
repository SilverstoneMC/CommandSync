package Mr_Krab.CommandSyncServer.Bungee;

import java.io.IOException;
import java.net.SocketException;

public class ClientListener extends Thread {

    private final CSS plugin;
    private final Integer heartbeat;
    private final String pass;

    public ClientListener(CSS plugin, Integer heartbeat, String pass) {
        this.plugin = plugin;
        this.heartbeat = heartbeat;
        this.pass = pass;
    }

    public void run() {
        while (true) {
            try {
                new ClientHandler(plugin, plugin.server.accept(), heartbeat, pass).start();
            } catch (SocketException | NullPointerException ignored) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}