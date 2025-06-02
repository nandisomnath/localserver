import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private ServerSocket server;

    public HttpServer(int port) throws Exception {
        // throwing is required
        // only 100 connections at a time other are not acceptable.
        this.server = new ServerSocket(port, 100);
        System.out.printf("http://0.0.0.0:%d\n", port);
    }

    public void serve(String cwdPath) {
        HttpRequest request = new HttpRequest();
    
        ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        while (true) {
            try {
                Socket client = server.accept();
                service.submit(() -> request.sendResponse(client, cwdPath));
                // request.sendResponse(client, cwd);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
