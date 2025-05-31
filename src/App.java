import java.io.File;

public class App {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: localserver [directory] [port]");
        }
        File file = new File(args[0].strip());

        try {
            HttpServer server = new HttpServer(Integer.parseInt(args[1].strip()));
            System.out.printf("Listening at: %s\n", file.getCanonicalFile());
            server.serve(file.getCanonicalPath());
        }catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
