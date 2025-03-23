public class App {
    public static void main(String[] args) {
        DirectoryManager manager = new DirectoryManager("D:\\");
        HttpServer server = new HttpServer(9090);
        server.serve();
    }
}
