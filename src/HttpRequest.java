import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
// import java.util.HashMap;

public class HttpRequest {
    SimpleDateFormat modifiedDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat modifiedTimeFormat = new SimpleDateFormat("HH:mm:ss");
    String dirTable;
    String fileTable;
    // private HashMap<String, String> headers;

    public HttpRequest() {
        // headers = new HashMap<>();
        // path, dirname, modifieddate, modifiedtime
        this.dirTable = "<tr><td><table><tbody><tr><td><a href=\"%s\">%s</a></td></tr></tbody></table></td><td></td><td>%s</td><td>%s</td></tr>";

        // path, filename, filesizeinkb, modifieddate, modifiedtime
        this.fileTable = "<tr><td><table><tbody><tr><td><a href=\"%s\">%s</a></td></tr></tbody></table></td><td>%d KB</td><td>%s</td><td>%s</td></tr>";

    }

    // public void setHeader(String key, String value) {
    // headers.put(key, value);
    // }

    // public String getHeader(String key) {
    // return headers.get(key);
    // }

    private String dirTableTemplate(String dirName, String path) throws Exception {
        Path filePath = Paths.get(path);

        FileTime fileTime = Files.getLastModifiedTime(filePath);
        Date lastModifiedTime = new Date(fileTime.toMillis());

        String modifiedDate = modifiedDateFormat.format(lastModifiedTime);
        String modifiedTime = modifiedTimeFormat.format(lastModifiedTime);

        // StringBuilder dirTable = new StringBuilder();

        // dirTable.append("<tr>");
        // dirTable.append("<td>");
        // dirTable.append("<table>");
        // dirTable.append("<tbody>");
        // dirTable.append("<tr>");
        // dirTable.append(String.format("<td><a href=\"%s\">%s</a></td>", path,
        // dirName));
        // dirTable.append("</tr>");
        // dirTable.append("</tbody>");
        // dirTable.append("</table>");
        // dirTable.append("</td>");
        // dirTable.append("<td></td>");
        // dirTable.append(String.format("<td>%s</td>", modifiedDate));
        // dirTable.append(String.format("<td>%s</td>", modifiedTime));
        // dirTable.append("</tr>");

        return String.format(this.dirTable, path, dirName, modifiedDate, modifiedTime);

    }

    private String fileTableTemplate(String fileName, String path) throws Exception {

        Path filePath = Paths.get(path);
        int fileSizeInKb = (int) Files.size(filePath) / 1024;
        FileTime fileTime = Files.getLastModifiedTime(filePath);
        Date lastModifiedTime = new Date(fileTime.toMillis());

        String modifiedDate = modifiedDateFormat.format(lastModifiedTime);
        String modifiedTime = modifiedTimeFormat.format(lastModifiedTime);

        // StringBuilder fileTable = new StringBuilder();
        // fileTable.append("<tr>");
        // fileTable.append("<td>");
        // fileTable.append("<table>");
        // fileTable.append("<tbody>");
        // fileTable.append("<tr>");
        // fileTable.append(String.format("<td><a href=\"%s\">%s</a></td>", path,
        // fileName));
        // fileTable.append("</tr>");
        // fileTable.append("</tbody>");
        // fileTable.append("</table>");
        // fileTable.append("</td>");
        // fileTable.append(String.format("<td>%d KB</td>", fileSizeInKb));
        // fileTable.append(String.format("<td>%s</td>", modifiedDate));
        // fileTable.append(String.format("<td>%s</td>", modifiedTime));
        // fileTable.append("</tr>");

        return String.format(this.fileTable, path, fileName, fileSizeInKb, modifiedDate, modifiedTime);
    }

    private String getMimeType(String filePath) {
        if (filePath.endsWith(".ttf")) {
            return "font/ttf";
        }
        if (filePath.endsWith(".woff")) {
            return "aplication/font-woff";
        }
        if (filePath.endsWith(".woff2")) {
            return "aplication/font-woff2";
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
        // } if (filePath.endsWith(".ttf")) {
        // return "font/ttf";
        // }

        return "application/octect-stream";
    }


    private void sendHeaders(OutputStream out,String filePath) throws IOException {
        String mime = getMimeType(filePath);
        out.write(String.format("Content-Type: %s\r\n", mime).getBytes());
        out.flush();
    }

    private void sendIndex(String basePath, String cwdPath, OutputStream out) throws Exception {

        StringBuilder content = new StringBuilder();

        content.append("<!DOCTYPE html>");
        content.append("<html>");
        content.append("<body dir=\"ltr\">");
        content.append("<h1>Index of D:/</h1>");
        content.append("<table order=\"\">");
        content.append("<thead>");
        content.append("<tr>");
        content.append("<th><a href=\"\">Name</a></th>");
        content.append("<th><a href=\"\">Size</a></th>");
        content.append("<th colspan=\"2\"><a href=\"\">Last Modified</a></th>");
        content.append("</tr>");
        content.append("</thead>");
        content.append("<tbody>");

        // add table content complete
        String pathName;
        Path relativePath;
        Path base = Paths.get(basePath);
        File[] all_paths = new File(cwdPath).listFiles();

        for (int i = 0; i < all_paths.length; i++) {

            pathName = all_paths[i].getName();
            relativePath = base.relativize(Paths.get(all_paths[i].getCanonicalPath()));

            if (all_paths[i].isDirectory()) {
                content.append(dirTableTemplate(pathName, relativePath.toString()));
            } else {
                content.append(fileTableTemplate(pathName, relativePath.toString()));
            }
        }

        content.append("</tbody>");
        content.append("</table>");
        content.append("</body>");
        content.append("</html>");

        out.write("HTTP/1.1 200 OK\r\n".getBytes());
        this.sendHeaders(out, "index.html");
        out.write("\r\n".getBytes());
        out.write(content.toString().getBytes());
        out.flush();
    }

    private void sendFile(OutputStream out, Path path) throws Exception {
        out.write("HTTP/1.1 200 OK\r\n".getBytes());
        this.sendHeaders(out, path.toString());
        out.write("\r\n".getBytes());
        out.flush();
        FileInputStream reader = new FileInputStream(path.toFile());
        FileChannel channel = reader.getChannel();
        channel.transferTo(0, channel.size(), Channels.newChannel(out));
        // out.flush();
        reader.close();
    }

    private void generateResponse(InputStream in, OutputStream out, File cwd) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

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
                path = Path.of(cwd.getCanonicalPath(), "index.html");
                File index = path.toFile();
                if (!index.exists()) {
                    sendIndex(cwd.getCanonicalPath(), cwd.getCanonicalPath(), out);
                    return;
                } else {
                    sendFile(out, path);
                    return;
                }
            }

            path = Paths.get(cwd.getCanonicalPath(), requestPath);
            File currentObj = path.toFile();

            if (currentObj.isFile()) {
                sendFile(out, path);
                return;
            }

            // When file is a path
            if (currentObj.isDirectory()) {
                sendIndex(cwd.getCanonicalPath(), currentObj.getCanonicalPath(), out);
                return;
            }

            // TODO: parse this when all the headers are needed
            // while (line != null) {
            // line = in.readLine();
            // System.out.println(line);
            // }

        } catch (Exception e) {
            e.printStackTrace();
        }

        out.write("HTTP/1.1 404 Not Found\r\n".getBytes());
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
