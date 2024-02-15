import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private int PORT;

    private ServerSocket serverSocket;
    private Socket clientSocketConn;
    private PrintWriter out;
    private BufferedReader in;

    public Server(int port) {
        System.out.println("Starting memcached server on port " + port);
        this.PORT = port;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            clientSocketConn = serverSocket.accept();
            out = new PrintWriter(clientSocketConn.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader((clientSocketConn.getInputStream())));
            while(true) {
                String test = in.readLine();
                if (test == null) {
                    break;
                }
                System.out.println(test);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            in.close();
            out.close();
            clientSocketConn.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
