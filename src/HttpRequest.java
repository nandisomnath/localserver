import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
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
                path = Path.of(cwd.getCanonicalPath(), "index.html");
            } else {
                path = Path.of(cwd.getCanonicalPath(), requestPath);
            }
            byte[] content = Files.readAllBytes(path);

            String response = String.format("HTTP/1.1 200 OK\r\n\r\n%s", new String(content));

            // TODO: parse this when all the headers are needed
            // while (line != null) {
            // line = in.readLine();
            // System.out.println(line);
            // }

            return response;
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
