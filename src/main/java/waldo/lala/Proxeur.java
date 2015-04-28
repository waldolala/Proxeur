package waldo.lala;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import waldo.lala.proxeur.ProxThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Proxeur {
    private static final Logger L = LoggerFactory.getLogger(Proxeur.class);
    private static final int PROXY_PORT = 8899;
    static {
        L.error("Error logging enabled");
        L.warn("Warning logging enabled");
        L.debug("Debug logging enabled");
        L.trace("Trace logging enabled");
    }

    public static void main (String args[]) {
        L.trace("> main()");
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PROXY_PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                new ProxThread(socket).start();
            }
        }  catch (IOException e)  {
            L.trace("- serverSocket exception: {}", e.getMessage());
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) { /* just ignore */ }
        }
        L.trace("< main()");
    }
}
