package waldo.lala.proxeur;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class ProxServer extends ProxStreamsIO {

    private byte[] responseHeader = new byte[0];
    private byte[] responseBody = new byte[0];
    private int responseCode = 200;
    private int contentLength = 0;

    public ProxServer(String siteHost, int sitePort) throws IOException {
        super(new Socket(siteHost, sitePort));
    }

    public byte[] getResponseHeader() {
        L.trace("> getResponseHeader()");
        parseHttpResponse();
        L.trace("< getResponseHeader() - responseHeader={}", byteString(responseHeader,50));
        return responseHeader;
    }

    public void streamResponseBody(final BufferedOutputStream clientOut) {
        L.trace("> streamResponseBody()");
        parseHttpBody(clientOut);
        L.trace("< streamResponseBody() - responseBody={}", byteString(responseBody,50));
    }

    private void parseHttpResponse() {
        L.trace("> parseHttpResponse()");
        /* sample responseHeader
        HTTP/1.1 200 OK
        Last-Modified: Wed, 22 Apr 2015 09:33:17 GMT
        ETag: "23057f5-57225-55376add"
        Accept-Ranges: bytes
        Content-Type: text/html
        Vary: Accept-Encoding
        Content-Encoding: gzip
        Expires: Wed, 22 Apr 2015 09:34:27 GMT
        Cache-Control: max-age=0, no-cache, no-store
        Pragma: no-cache
        Date: Wed, 22 Apr 2015 09:34:27 GMT
        Transfer-Encoding:  chunked
        Connection: keep-alive
        Connection: Transfer-Encoding

         */
        StringBuilder responseBuffer = new StringBuilder();

        String line = readLine(inputStream); // HTTP/1.1 200 OK
        if (!line.isEmpty()) {
            responseBuffer.append(line).append("\r\n");
            int pos = line.indexOf(" ");
            if (line.startsWith("HTTP") && (pos > 4) && (line.indexOf(" ", pos + 1) > 5)) {
                String responseString = line.substring(pos + 1, line.indexOf(" ", pos + 1));
                responseCode = Integer.parseInt(responseString);
                L.trace("- responseCode: {}", responseCode);
            }
        }
        line = readLine(inputStream);
        while (!line.isEmpty()) {
            responseBuffer.append(line).append("\r\n");
            int pos = line.indexOf("Content-Length:");
            if (pos >= 0) {
                contentLength = Integer.parseInt(line.substring(pos + 15).trim());
                L.trace("- contentLength: {}", contentLength);
            }
            line = readLine(inputStream);
        }
        responseBuffer.append("\r\n");
        String responseString = responseBuffer.toString();
        responseHeader = responseString.getBytes();
        L.trace("< parseHttpResponse()");
    }

    private void parseHttpBody(final BufferedOutputStream clientOut) {
        L.trace("> parseHttpBody()");
        final byte[] buffer = new byte[4096];
        int byteCount = 0;
        try {
            int bytesRead = inputStream.read(buffer);
            while (bytesRead > 0) {
                L.trace("- bytesRead={}", bytesRead);
                clientOut.write(buffer, 0, bytesRead);
                if (byteCount == 0) { // keep a copy of the head of the body for logging
                    responseBody = Arrays.copyOf(buffer, 100);
                }
                byteCount += bytesRead;
                bytesRead = inputStream.read(buffer);
            }
            clientOut.flush();
        } catch (IOException e) {
            L.trace("- exception: {}", e.getMessage());
        }
        L.trace("< parseHttpBody()");
    }
}
