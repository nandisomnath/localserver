import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class HttpRequest {
    private HashMap<String, String> headers;

    public HttpRequest() {
        headers = new HashMap<>();
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getHeader(String key) {
        return headers.get(key);
    }


    private String dirTableTemplate(String dirName, String path) throws Exception {
        Path filePath = Paths.get(path);
       
        FileTime fileTime = Files.getLastModifiedTime(filePath);
        Date lastModifiedTime = new Date(fileTime.toMillis());

        SimpleDateFormat modifiedDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat modifiedTimeFormat = new SimpleDateFormat("HH:mm:ss");

        String modifiedDate = modifiedDateFormat.format(lastModifiedTime);
        String modifiedTime = modifiedTimeFormat.format(lastModifiedTime);


        StringBuilder table = new StringBuilder();

        table.append("<tr>");
        table.append("<td>");
        table.append("<table>");
        table.append("<tbody>");
        table.append("<tr>");
        table.append(String.format("<td><a href=\"%s\">%s</a></td>", path, dirName));
        table.append("</tr>");
        table.append("</tbody>");
        table.append("</table>");
        table.append("</td>");
        table.append("<td></td>");
        table.append(String.format("<td>%s</td>", modifiedDate));
        table.append(String.format("<td>%s</td>", modifiedTime));
        table.append("</tr>");


        return table.toString();
      
    }


    private String fileTableTemplate(String fileName, String path) throws Exception {

        Path filePath = Paths.get(path);
        long fileSizeInBytes = Files.size(filePath);
        int fileSizeInKb = (int)fileSizeInBytes / 1024;
        FileTime fileTime = Files.getLastModifiedTime(filePath);
        Date lastModifiedTime = new Date(fileTime.toMillis());

        SimpleDateFormat modifiedDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat modifiedTimeFormat = new SimpleDateFormat("HH:mm:ss");

        String modifiedDate = modifiedDateFormat.format(lastModifiedTime);
        String modifiedTime = modifiedTimeFormat.format(lastModifiedTime);

        StringBuilder tableRow = new StringBuilder();
        tableRow.append("<tr>");
        tableRow.append("<td>");
        tableRow.append("<table>");
        tableRow.append("<tbody>");
        tableRow.append("<tr>");
        tableRow.append(String.format("<td><a href=\"%s\">%s</a></td>", path, fileName));
        tableRow.append("</tr>");
        tableRow.append("</tbody>");
        tableRow.append("</table>");
        tableRow.append("</td>");
        tableRow.append(String.format("<td>%d KB</td>", fileSizeInKb));
        tableRow.append(String.format("<td>%s</td>", modifiedDate));
        tableRow.append(String.format("<td>%s</td>", modifiedTime));
        tableRow.append("</tr>");

        return tableRow.toString();
    }


    private String generateIndex(String cwdPath) {

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

        // TODO: add table content

        content.append("</tbody>");
        content.append("</table>");
        content.append("</body>");
        content.append("</html>");

        return String.format("HTTP/1.1 200 OK\r\n\r\n%s", new String(content));
    }


    private String generateResponse(BufferedReader in) {
        try {
            // first line is the get request line
            String line = in.readLine();
            // This is the request info
            System.out.printf("%s", line);
            String[] tokens;
            tokens = line.split(" ");
            String requestType = tokens[0].strip();
            String requestPath = tokens[1].strip();

            File cwd = new File(".");
            // File target = new File(String.format("%s%s%s", , File.pathSeparator,));
            // FileReader reader = new FileReader(target);
            // BufferedReader reader2;
            Path path;
            if (requestPath.equals("/")) {
                // TODO: if index.html wasnot there then make a directory list and send it
                path = Path.of(cwd.getCanonicalPath(), "index.html");
                File index = new File(path.toUri());
                if (!index.exists()) {
                    return generateIndex(cwd.getCanonicalPath());
                }
            } else {
                path = Path.of(cwd.getCanonicalPath(), requestPath);
            }
            byte[] content = Files.readAllBytes(path);

            // TODO: parse this when all the headers are needed
            // while (line != null) {
            // line = in.readLine();
            // System.out.println(line);
            // }

            return String.format("HTTP/1.1 200 OK\r\n\r\n%s", new String(content));
        } catch (IOException e) {
            e.fillInStackTrace();
        }

        StringBuilder content = new StringBuilder();
        content.append("<html>");
        content.append("<head><title>404 Not Found</title></head>");
        content.append("<body>");
        content.append("<h1>Not Found</h1>");
        content.append("<p>The requested Url was Not found</p>");
        content.append("</body>");
        content.append("</html>");

        return String.format("HTTP/1.1 404 Not Found\r\n\r\n%s", content.toString());
    }

    public void sendResponse(BufferedReader in, PrintWriter out) {

        String response = this.generateResponse(in);
        // // printing the request string
        // System.out.println(new String(in.readLine()));
        if (response.contains("200")) {
            System.out.println("   200 OK");
        } else {
            System.out.println("   404 Not Found");
        }
        out.printf(response);
    }

}
