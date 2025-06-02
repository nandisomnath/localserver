import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
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

    private void sendFile(OutputStream out, Path path) throws Exception {
        out.write(STATUS_OK_BYTES);
        this.sendHeaders(out, path.toString());
        out.write("\r\n".getBytes());
        out.flush();
        FileInputStream reader = new FileInputStream(path.toFile());
        FileChannel channel = reader.getChannel();
        channel.transferTo(0, channel.size(), Channels.newChannel(out));
        reader.close();
    }

    private void generateResponse(InputStream in, OutputStream out, File cwd) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String cwdCanonicalPath = cwd.getCanonicalPath();

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
                File index = path.toFile();
                if (!index.exists()) {
                    WebPage page = new WebPage();
                    page.sendIndex(cwdCanonicalPath, cwdCanonicalPath, out);
                    return;
                } else {
                    sendFile(out, path);
                    return;
                }
            }

            path = Paths.get(cwdCanonicalPath, requestPath);
            File currentObj = path.toFile();

            if (currentObj.isFile()) {
                sendFile(out, path);
                return;
            }

            // When file is a path
            if (currentObj.isDirectory()) {
                WebPage page = new WebPage();
                page.sendIndex(cwdCanonicalPath, currentObj.getCanonicalPath(), out);
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        out.write("HTTP/1.1 404 Not Found\r\n".getBytes());
        out.write("Content-Length: 0\r\n".getBytes());
        out.flush();
    }

    public void sendResponse(Socket client, File cwd) {

        try {
            var in = client.getInputStream();
            var out = client.getOutputStream();
            this.generateResponse(in, out, cwd);
            // out.printf("%s", response);

            // if (response.contains("200")) {
            // System.out.println(" 200 OK");
            // } else {
            // System.out.println(" 404 Not Found");
            // }
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
