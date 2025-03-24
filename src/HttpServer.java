import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private ServerSocket server;

    public HttpServer(int port) throws IOException {
        // throwing is required
        this.server = new ServerSocket(port, 100);
        System.out.printf("http://0.0.0.0:%d\n", port);
    }

    public void serve(String cwdPath) {
        // Socket client;
        HttpRequest request = new HttpRequest();
        // PrintWriter out;
        // BufferedReader in;
        File cwd = new File(cwdPath);
        ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        while (true) {
            try {
                Socket client = server.accept();
                service.submit(() -> request.sendResponse(client, cwd));
                // request.sendResponse(client, cwd);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
