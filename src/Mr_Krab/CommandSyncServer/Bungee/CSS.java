package Mr_Krab.CommandSyncServer.Bungee;

import net.md_5.bungee.api.plugin.Plugin;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class CSS extends Plugin {

    public ServerSocket server;
    public final Set<String> c = Collections.synchronizedSet(new HashSet<>());
    public final List<String> oq = Collections.synchronizedList(new ArrayList<>());
    public final Map<String, List<String>> pq = Collections.synchronizedMap(new HashMap<>());
    public final Map<String, Integer> qc = Collections.synchronizedMap(new HashMap<>());
    public final String spacer = "@#@";
    public Debugger debugger;
    private Locale loc;
    private boolean remove;
    public final Logger logger = Logger.getLogger("CommandSync");

    public Locale getLocale() {
        return loc;
    }

    public Logger getLogger() {
        return logger;
    }

    public void onEnable() {
        String[] data = loadConfig();
        if(data[3].equals("UNSET")) {
            logger.warning(loc.getString("UnsetValues"));
            return;
        }
        try {
            server = new ServerSocket(Integer.parseInt(data[1]), 50, InetAddress.getByName(data[0]));
            logger.info(loc.getString("OpenOn", data[0], data[1]));
            //ConsoleCommandSender.getInstance().sendMessage(loc.getString("OpenOn", data[0], data[1]));
            new ClientListener(this, Integer.parseInt(data[2]), data[3]).start();
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            workData();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void workData() throws IOException {
        File folder = getDataFolder();
        File data = new File(folder + File.separator + "data.txt");
        boolean remove = this.remove;
        if (remove) {
            if(data.delete()) {
                logger.info(loc.getString("DataRemoved"));
            } else logger.info(loc.getString("DataRemoveNotFound"));
        } else {
            loadData();
        }
    }

    public void onDisable() {
        saveData();
        debugger.close();
    }

    private String[] loadConfig() {
        String[] defaults = new String[] {
                "ip=localhost", "port=9190", "heartbeat=1000", "pass=UNSET", "debug=false", "removedata=false", "lang=en_US"
        };
        String[] data = new String[defaults.length];
        try {
            File folder = getDataFolder();
            if(!folder.exists()) {
                folder.mkdir();
            }
            File file = new File(folder, "config.txt");
            if(!file.exists()) {
                file.createNewFile();
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            for(int i = 0; i < defaults.length; i++) {
                String l = br.readLine();
                if(l == null || l.isEmpty()) {
                    data[i] = defaults[i].split("=")[1];
                } else {
                    data[i] = l.split("=")[1];
                    defaults[i] = l;
                }
            }
            br.close();
            file.delete();
            file.createNewFile();
            PrintStream ps = new PrintStream(new FileOutputStream(file));
            for (String aDefault : defaults) {
                ps.println(aDefault);
            }
            ps.close();
            debugger = new Debugger(this, Boolean.valueOf(data[4]));
            remove = Boolean.parseBoolean(data[5]);
            loc = new Locale(this, String.valueOf(data[6]));
            loc.init();
            logger.info(loc.getString("ConfigLoaded"));
        } catch(IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    private void saveData() {
        try {
            OutputStream os = new FileOutputStream(new File(getDataFolder(), "data.txt"));
            PrintStream ps = new PrintStream(os);
            for(String s : oq) {
                ps.println("oq:" + s);
            }
            for(Entry<String, List<String>> e : pq.entrySet()) {
                String name = e.getKey();
                for(String command : e.getValue()) {
                    ps.println("pq:" + name + spacer + command);
                }
            }
            for(Entry<String, Integer> e : qc.entrySet()) {
                ps.println("qc:" + e.getKey() + spacer + e.getValue());
            }
            ps.close();
            logger.info(loc.getString("DataSaved"));
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void loadData() {
        try {
            File file = new File(getDataFolder(), "data.txt");
            if(file.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String l = br.readLine();
                    while (l != null) {
                        if (l.startsWith("oq:")) {
                            oq.add(l.substring(3));
                        } else if (l.startsWith("pq:")) {
                            String[] parts = l.substring(3).split(spacer);
                            List<String> commands;
                            if (pq.containsKey(parts[0])) {
                                commands = pq.get(parts[0]);
                                commands.add(parts[1]);
                            } else {
                                commands = new ArrayList<>(Collections.singletonList(parts[1]));
                            }
                            pq.put(parts[0], commands);
                        } else if (l.startsWith("qc:")) {
                            String[] parts = l.substring(3).split(spacer);
                            qc.put(parts[0], Integer.parseInt(parts[1]));
                        }
                        l = br.readLine();
                    }
                    logger.info(loc.getString("DataLoaded"));
                }
            } else {
                logger.info(loc.getString("DataNotfound"));
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}