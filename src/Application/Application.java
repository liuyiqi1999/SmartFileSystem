package Application;

import Controller.DefaultFileManagerControllerImpl;
import Controller.FileManagerController;

public class Application {
    public static void main(String[] args) {
        FileManagerController fileManagerController = DefaultFileManagerControllerImpl.getInstance();
        fileManagerController.assignFile(2);
    }
}
