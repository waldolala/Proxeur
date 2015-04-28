package waldo.lala.proxeur;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;

public class ProxThread extends Thread {
    private static final Logger L = LoggerFactory.getLogger(ProxThread.class);

    private final Socket proxySocket;

    public ProxThread(final Socket socket) {
        L.trace("* ProxThread() - socket: {}", socket);
        proxySocket = socket;
    }

    public void run() {
        L.trace("> run()");
        final long startTime = System.currentTimeMillis();
        ProxClient client = null;
        ProxServer server = null;

        try {
            client = new ProxClient(proxySocket);
            final byte[] request = client.getClientRequest();
            final String hostName = client.getHostName();
            final int hostPort = client.getHostPort();

            server = new ProxServer(hostName, hostPort);
            server.send(request);
            final byte[] response = server.getResponseHeader();
            client.send(response);
            server.streamResponseBody(client.outputStream);

        }  catch (IOException e) {
            L.trace("- exception: {}", e.getMessage());
        } finally {
            if (client != null) { client.close(); }
            if (server != null) { server.close(); }
            try {
                proxySocket.close();
            } catch (IOException e) { /* ignore */ }
        }

        long endTime = System.currentTimeMillis();
        L.debug("Execution time: {} ms", endTime - startTime);
    }
}
