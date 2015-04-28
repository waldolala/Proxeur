package waldo.lala.proxeur;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;

public class ProxStreamsIO {
    private final static int TIMEOUT = 20 * 1000;

    protected static final Logger L = LoggerFactory.getLogger(ProxStreamsIO.class);

    protected BufferedInputStream inputStream;
    protected BufferedOutputStream outputStream;

    protected ProxStreamsIO(final Socket socket) throws IOException {
        L.trace("* ProxStreamsIO() - socket:{}", socket);
        socket.setSoTimeout(TIMEOUT);
        inputStream = new BufferedInputStream(socket.getInputStream());
        outputStream = new BufferedOutputStream(socket.getOutputStream());
    }

    protected void send(byte[] request) {
        L.trace("> send() - request={}", byteString(request, 50));
        try {
            outputStream.write(request, 0, request.length);
            outputStream.flush();
        } catch (IOException e) { L.trace("- exception: {}", e.getMessage()); }
        L.trace("< send");
    }

    protected void close() {
        try {
            inputStream.close();
            outputStream.close();
        } catch (IOException e) { /* just ignore */ }
    }

    protected String readLine (InputStream stream) {
        L.trace("> readLine()");
        StringBuffer data = new StringBuffer();
        try {
            for (;;) {
                int item = stream.read();
                if ((item == -1) || (item == 0) || (item == 10) || (item == 13)) {
                    break;
                }
                data.append((char) item);
            }
            // consume additional EOL character
            stream.mark(1);
            int item = stream.read();
            if ((item != -1) && (item != 10) && (item != 13)) {
                stream.reset();
            }
        }  catch (IOException e)  { /* just ignore */ }
        String line = data.toString();
        L.trace("< readLine() - line={}", line);
        return line;
    }

    protected String byteString(byte[] bytes, int length) {
        final StringBuilder string = new StringBuilder();
        for(int i=0; i < bytes.length && i < length; i++) {
            final int b=bytes[i];
            if (31 < b && b < 127) {
                string.append((char) b);
            } else {
                string.append(".");
            }
        }
        return string.toString();
    }
}
