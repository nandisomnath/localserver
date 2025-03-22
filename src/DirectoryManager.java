import java.io.File;
import java.util.ArrayList;

public class DirectoryManager {
    private File handler;
    
    // TODO: make it private and encapsulate it
    public ArrayList<String> paths;

    public DirectoryManager(String path) {
        // TODO: check the handler is directory or not
        handler = new File(path);
        paths = new ArrayList<>();
    }

    private static void getPaths(ArrayList<String> paths, String path) throws Exception {
        File file = new File(path);
        File temp;
        File[] all_paths = file.listFiles();
        if (all_paths == null) {
            return;
        }
        for (int i = 0; i < all_paths.length; i++) {
            temp = all_paths[i];
            if (temp.isDirectory()) {
                paths.add(temp.getCanonicalPath());
                getPaths(paths, temp.getCanonicalPath());
            } else {
                paths.add(temp.getCanonicalPath());
            }
        }
    }

    // List all the file and folders
    public void listAll() throws Exception {
       
        
        DirectoryManager.getPaths(paths, handler.getCanonicalPath());
        handler.list();
    }


}
