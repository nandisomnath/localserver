import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WebPage {
    SimpleDateFormat modifiedDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat modifiedTimeFormat = new SimpleDateFormat("HH:mm:ss");
    String dirTable;
    String fileTable;

    final byte[] STATUS_OK_BYTES = "HTTP/1.1 200 OK\r\n".getBytes();



    public WebPage() {
        // headers = new HashMap<>();
        // path, dirname, modifieddate, modifiedtime
        this.dirTable = "<tr><td><table><tbody><tr><td><a href=\"\\%s\">%s</a></td></tr></tbody></table></td><td></td><td>%s</td><td>%s</td></tr>";

        // path, filename, filesizeinkb, modifieddate, modifiedtime
        this.fileTable = "<tr><td><table><tbody><tr><td><a href=\"\\%s\">%s</a></td></tr></tbody></table></td><td>%d KB</td><td>%s</td><td>%s</td></tr>";

    }

    private String dirTableTemplate(String dirPath, String path) throws Exception {
        Path filePath = Paths.get(dirPath);

        // System.out.println(path);
        // System.out.println(filePath.toString());
        // System.out.println(this.dirTable);

        FileTime fileTime = Files.getLastModifiedTime(filePath);
        Date lastModifiedTime = new Date(fileTime.toMillis());

        String modifiedDate = modifiedDateFormat.format(lastModifiedTime);
        String modifiedTime = modifiedTimeFormat.format(lastModifiedTime);

        String table = String.format(this.dirTable, path, filePath.getFileName().toString(), modifiedDate,
                modifiedTime);
        // System.out.println(table);
        return table;
    }

    private String fileTableTemplate(String filePathString, String path) throws Exception {

        Path filePath = Paths.get(filePathString);
        int fileSizeInKb = (int) Files.size(filePath) / 1024;
        FileTime fileTime = Files.getLastModifiedTime(filePath);
        Date lastModifiedTime = new Date(fileTime.toMillis());

        String modifiedDate = modifiedDateFormat.format(lastModifiedTime);
        String modifiedTime = modifiedTimeFormat.format(lastModifiedTime);

        return String.format(this.fileTable, path, filePath.getFileName().toString(), fileSizeInKb, modifiedDate,
                modifiedTime);
    }

    public void sendIndex(String basePath, String cwdPath, OutputStream out) throws Exception {

        StringBuilder content = new StringBuilder();

        content.append("<!DOCTYPE html>");
        content.append("<html>");
        content.append("<body dir=\"ltr\">");
        content.append("<h1>Index of ").append(cwdPath).append("</h1>");
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
        String filePath;
        Path relativePath;
        Path base = Paths.get(basePath);
        File[] all_paths = new File(cwdPath).listFiles();

        for (int i = 0; i < all_paths.length; i++) {

            filePath = all_paths[i].getCanonicalPath();
            relativePath = base.relativize(Paths.get(filePath));
            if (all_paths[i].isDirectory()) {
                content.append(dirTableTemplate(filePath, relativePath.toString()));
            } else {
                content.append(fileTableTemplate(filePath, relativePath.toString()));
            }
        }

        content.append("</tbody>");
        content.append("</table>");
        content.append("</body>");
        content.append("</html>");

        out.write(STATUS_OK_BYTES);
        this.sendHeaders(out, "index.html");
        out.write("\r\n".getBytes());
        out.write(content.toString().getBytes());
        out.flush();
    }

     private void sendHeaders(OutputStream out,String filePath) throws Exception {
        String mime = getMimeType(filePath);
        out.write(String.format("Content-Type: %s\r\n", mime).getBytes());
        out.flush();
    }

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



}
