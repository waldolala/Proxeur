package waldo.lala.proxeur;

import java.io.IOException;
import java.net.Socket;

public class ProxClient extends ProxStreamsIO {

    private byte[] request = new byte[0];
    private String hostName = "";
    private int hostPort = 80;

    public ProxClient(Socket proxySocket) throws IOException {
        super(proxySocket);
    }

    public String getHostName() { return hostName; }

    public int getHostPort() { return hostPort; }

    public byte[] getClientRequest() {
        L.trace("> getClientRequest()");
        parseHttpRequest();
        L.trace("< getClientRequest() - request={}", byteString(request, 50));
        return request;
    }

    private void parseHttpRequest() {
        L.trace("> parseHttpRequest()");
            /* sample request
            GET http://www.site.com/ HTTP/1.1
            Host: www.site.com
            User-Agent: Mozilla/5.0 .....et cetera
            Accept: text/html,application/xhtml+xml,application/xml
            Accept-Language: nl,en-US
            Accept-Encoding: gzip, deflate
            Cookie: .....et cetera
            Connection: keep-alive
            Cache-Control: max-age=0

             */
        StringBuilder requestBuffer = new StringBuilder();
        String line = readLine(inputStream); // GET http://www.site.com/ HTTP/1.1
        if (!line.isEmpty()) {
            requestBuffer.append(line).append("\r\n");
        }
        line = readLine(inputStream); // Host: www.site.com
        while (!line.isEmpty()) {
            requestBuffer.append(line).append("\r\n");
            int pos = line.indexOf("Host:");
            if (pos >= 0) {
                String host = line.substring(pos + 5).trim();
                hostName = host;
                hostPort = 80;
                pos = hostName.indexOf(":");
                if (pos > 0) {
                    hostName = host.substring(0, pos);
                    L.trace("- hostName: {}", hostName);
                    hostPort = Integer.parseInt(host.substring(pos + 1));
                    L.trace("- hostPort: {}", hostPort);
                }
            }
            line = readLine(inputStream);
        }
        requestBuffer.append("\r\n");
        String requestString = requestBuffer.toString();
        request = requestString.getBytes();
        L.trace("< parseHttpRequest()");
    }
}