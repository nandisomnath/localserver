public class App {
    public static void main(String[] args) {
        HttpServer server = new HttpServer(9090);
        server.serve();
    }
}
