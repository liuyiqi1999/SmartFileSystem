package File;

import Id.*;
import Manager.FileManager;

public class DefaultFileImpl {
    Id<File> id;
    FileManager fileManager;
    FileMeta fileMeta;

    public DefaultFileImpl(FileManager fileManager, byte[] data) {
        IdImplFactory idImplFactory = IdImplFactory.getInstance();
        this.id = idImplFactory.getNewId(File.class);
        this.fileManager  = fileManager;
        
    }


}
