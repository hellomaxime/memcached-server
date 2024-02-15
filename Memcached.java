
public class Memcached {

    public static void main(String[] args) {

        int PORT = 11211;

        if(args.length == 2) {
            PORT = Integer.parseInt(args[1]);
        }

        Server s = new Server(PORT);
        s.start();
        s.stop();
    }

}