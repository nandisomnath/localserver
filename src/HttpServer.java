import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    private ServerSocket server;
    private int port;

    public HttpServer(int port) {
        this.port = port;

        try {
            this.server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            server = null;
        }
        System.out.printf("http://0.0.0.0:%d\n", server.getLocalPort());
    }

    public void serve() {
        // checking if the server is ready or not
        if (this.server == null) {
            return;
        }

        Socket client;
        HttpRequest request;
        PrintWriter out;
        BufferedReader in;

        while (true) {
            try {
                client = server.accept();

                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new PrintWriter(client.getOutputStream(), true);

                request = new HttpRequest();
                request.sendResponse(in, out);

                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
