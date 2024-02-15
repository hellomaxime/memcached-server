import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {

    private HashMap<String, String> store = new HashMap<>();

    private int PORT;

    private ServerSocket serverSocket;
    private Socket clientSocketConn;

    public Server(int port) {
        System.out.println("Starting memcached server on port " + port);
        this.PORT = port;
    }

    void processCommand(String command, BufferedReader in, PrintWriter out) throws IOException {

        if (command.matches("set [a-zA-Z]* [0-9]* [0-9]* [0-9]*\\\\r\\\\n")) {
            String[] split = command.replace("\\r\\n", "").split(" ");
            String value = in.readLine();
            String storeValue = value.replace("\\r\\n", "");
            store.put(split[1], storeValue);
            out.println("STORED");
        } else if(command.matches("get [a-zA-Z]*\\\\r\\\\n")) {
            String[] split = command.replace("\\r\\n", "").split(" ");
            if(store.containsKey(split[1])) {
                out.println(store.get(split[1]));
            } else {
                out.println("ERROR");
            }
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
