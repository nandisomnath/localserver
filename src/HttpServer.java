import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    private ServerSocket server;
    private int port;

    public HttpServer(int port) throws Exception {
        this.port = port;
        this.server = new ServerSocket(port);
        System.out.printf("http://0.0.0.0:%d\n", server.getLocalPort());
    }

    public void serve() throws Exception {
        Socket client;
        HttpRequest request;
        PrintWriter out;
        BufferedReader in;

        while (true) {
            client = server.accept();

            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);

            request = new HttpRequest();
            request.sendResponse(in, out);

            client.close();
        }

    }

}
