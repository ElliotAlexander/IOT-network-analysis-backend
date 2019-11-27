import java.net.Socket;

class PortRunner implements Runnable {

    final private int startPort;
    final private String hostname;
    final private int portsPerThread;

    public PortRunner(String hostname, int startPort) {
        this.hostname = hostname;
        this.startPort = startPort;
    }

    public void run() {
        for (int port = startPort; port <= startPort + portsPerThread; port++) {
            Socket socket;
            try {
                socket = new Socket(host, port);
                // Port accepted connection.
                // Add port to database.
                socket.close();
            } catch (IOException ioEx) {
                // Port not accepted connection.
            }
            Thread.yield();
        }
    }
}