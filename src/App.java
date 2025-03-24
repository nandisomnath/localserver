import java.io.File;
import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: localserver [directory] [port]");
        }
        File file = new File(args[0].strip());
        HttpServer server = new HttpServer(Integer.parseInt(args[1].strip()));
        try {
            System.out.printf("Listening at: %s\n", file.getCanonicalFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            server.serve(file.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
