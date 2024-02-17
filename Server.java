import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {

    private HashMap<String, String[]> store = new HashMap<>(); // <key, [value, flags, byte_count]>
    private HashMap<String, Long> expired = new HashMap<>();

    private int PORT;

    private ServerSocket serverSocket;
    private Socket clientSocketConn;

    public Server(int port) {
        System.out.println("Starting memcached server on port " + port);
        this.PORT = port;
    }

    void processCommand(String command, BufferedReader in, PrintWriter out) throws IOException {
        if(command.length() == 0) {
            return;
        }

        if (command.matches("set [a-zA-Z0-9]* [0-9]* [-0-9]* [0-9]*[ a-zA-Z]*?\\\\r\\\\n")) {
            String[] split = command.replace("\\r\\n", "").split(" ");
            String value = in.readLine();
            while(!value.matches("[a-zA-Z0-9]*\\\\r\\\\n")) {
                out.println("ERROR");
                value = in.readLine();
            }
            String storeValue = value.replace("\\r\\n", "");
            String[] setInfo = new String[]{storeValue, split[2], split[4]};

            // store expiration time
            if(Integer.parseInt(split[3]) > 0) {
                long currentTimeSeconds = System.currentTimeMillis() / 1000 + Long.parseLong(split[3]);
                expired.put(split[1], currentTimeSeconds);
            } else if(Integer.parseInt(split[3]) < 0) {
                if(split.length < 6) {
                    out.println("STORED");
                }
                return;
            }

            store.put(split[1], setInfo);

            if(split.length < 6) {
                out.println("STORED");
            }
        } else if(command.matches("get [a-zA-Z0-9]*\\\\r\\\\n")) {
            String[] split = command.replace("\\r\\n", "").split(" ");

            if(expired.containsKey(split[1]) && expired.get(split[1]) < System.currentTimeMillis()/1000) {
                expired.remove(split[1]);
                store.remove(split[1]);
                out.println("END");
                return;
            }

            if(store.containsKey(split[1])) {
                String[] getInfo = store.get(split[1]);
                out.printf("VALUE %s %s %s\r\n%s\r\nEND\r\n", split[1], getInfo[1], getInfo[2], getInfo[0]);
            } else {
                out.println("END");
            }
        } else {
            out.println("ERROR");
        }
    }

    void handleConnection(Socket conn) throws IOException {
        try {
            PrintWriter out = new PrintWriter(conn.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            while (true) {
                String command = in.readLine();
                if (command == null) {
                    break;
                }
                processCommand(command, in, out);
            }
            in.close();
            out.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            while(true) {
                clientSocketConn = serverSocket.accept();
                new Thread(() -> {
                    try {
                        handleConnection(clientSocketConn);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            clientSocketConn.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
