package Application;

import Controller.DefaultFileManagerControllerImpl;
import Controller.FileManagerController;
import File.File;
import Id.IdImplFactory;
import Manager.FileManager;

import java.util.Arrays;
import java.util.Optional;

public class Application {
    public static void main(String[] args) {
        FileManagerController fileManagerController = DefaultFileManagerControllerImpl.getInstance();
        FileManager fileManager = fileManagerController.getFileManager(IdImplFactory.getIdWithIndex(FileManager.class, 3));
        File file = fileManager.getFile(IdImplFactory.getIdWithIndex(File.class, 9));
        //File file = fileManager.getFile(IdImplFactory.getIdWithIndex(File.class, 17));
        //file.write("file123".getBytes());
        //file.setSize(15);
        System.out.println(new String(file.read(7)));
        file.move(2,1);
        System.out.println(new String(file.read(5)));
        //file.move(4,1);
    }
}
