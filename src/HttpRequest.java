import java.io.BufferedReader;
import java.io.File;
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

    private String generateResponse(BufferedReader in) throws Exception {
        // first line is the get request line
        String line = in.readLine();
        // This is the request info
        System.out.printf("Request: %s\n", line);
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
            path =  Path.of(cwd.getCanonicalPath(), requestPath);
        }
        byte[] content = Files.readAllBytes(path);

        String response = String.format("HTTP/1.1 200 OK\r\n\r\n%s", new String(content));


        // TODO: parse this when all the headers are needed
        // while (line != null) {
        //     line = in.readLine();
        //     System.out.println(line);
        // }

        return response;

    }

    public void sendResponse(BufferedReader in, PrintWriter out) throws Exception {

        String response = this.generateResponse(in);
        // // printing the request string
        // System.out.println(new String(in.readLine()));

        out.printf(response);
    }

}
