import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HttpRequest {

    final byte[] STATUS_OK_BYTES = "HTTP/1.1 200 OK\r\n".getBytes();

    private String getMimeType(String filePath) {
        if (filePath.endsWith(".ttf")) {
            return "font/ttf";
        }
        if (filePath.endsWith(".woff")) {
            return "application/font-woff";
        }
        if (filePath.endsWith(".woff2")) {
            return "application/font-woff2";
        }
        if (filePath.endsWith(".html")) {
            return "text/html";
        }
        if (filePath.endsWith(".css")) {
            return "text/css";
        }
        if (filePath.endsWith(".js")) {
            return "application/javascript";
        }

        return "application/octect-stream";
    }

    private void sendHeaders(OutputStream out, String filePath) throws Exception {
        String mime = getMimeType(filePath);
        out.write(String.format("Content-Type: %s\r\n", mime).getBytes());
        out.flush();
    }

    private void sendFile(OutputStream out, String filePath) throws Exception {
        out.write(STATUS_OK_BYTES);
        this.sendHeaders(out, filePath);
        out.write("\r\n".getBytes());
        out.flush();
        FileInputStream reader = new FileInputStream(filePath);
        FileChannel channel = reader.getChannel();
        channel.transferTo(0, channel.size(), Channels.newChannel(out));
        reader.close();
    }

    private void generateResponse(InputStream in, OutputStream out, String cwdPath) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String cwdCanonicalPath = cwdPath;

        try {
            // first line is the get request line
            String line = reader.readLine();
            // This is the request info
            System.out.printf("%s\n", line);
            String[] tokens = line.split(" ");
            // String requestType = tokens[0].strip(); // No use
            String requestPath = tokens[1].strip();

            Path path;
            if (requestPath.equals("/")) {

                // if index.html wasnot there then make a directory list and send it
                path = Path.of(cwdCanonicalPath, "index.html");
                
                if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                    WebPage page = new WebPage();
                    page.sendIndex(cwdCanonicalPath, cwdCanonicalPath, out);
                    return;
                } else {
                   
                    sendFile(out,  path.toString());
                    return;
                }
            }

            path = Paths.get(cwdCanonicalPath, requestPath);
            // File currentObj = path.toFile();
            if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                sendFile(out,  path.toString());
                return;
            }

            // When file is a path
            if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                WebPage page = new WebPage();
                page.sendIndex(cwdCanonicalPath, path.toString(), out);
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        out.write("HTTP/1.1 404 Not Found\r\n".getBytes());
        out.write("Content-Length: 0\r\n".getBytes());
        out.flush();
    }

    public void sendResponse(Socket client, String cwdPath) {

        try {
            var in = client.getInputStream();
            var out = client.getOutputStream();
            this.generateResponse(in, out, cwdPath);
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
