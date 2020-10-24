package Manager;

import File.*;
import Id.*;
import Utils.Properties;

import java.util.HashMap;
import java.util.Map;

public class DefaultFileManagerImpl implements FileManager{
    Id<FileManager> id;
    Map<Id<File>,File> idFileMap;

    public DefaultFileManagerImpl() {
        IdImplFactory idImplFactory = IdImplFactory.getInstance();
        this.id = idImplFactory.getNewId(FileManager.class);
        this.idFileMap = new HashMap<>();
    }

    public Id<FileManager> getId() {
        return id;
    }

    @Override
    public File getFile(Id<File> fileId) {
        return idFileMap.get(fileId);
    }

    @Override
    public File newFile(Id<File> fileId){
        java.io.File root = new java.io.File(Properties.FILE_PATH+"/"+this.id.toString());
        if(!root.exists()){
            root.mkdir();
        }
        File file = new DefaultFileImpl(this, fileId);
        registerFile(file);
        return file;
    }

    @Override
    public File registerFile(File file){
        Id<File> fileId = file.getFileId();
        if(idFileMap.get(fileId)==null){
            idFileMap.put(fileId, file);
        } else {
            idFileMap.replace(fileId, file);
        }
        return file;
    }

    @Override
    public int getSize() {
        return idFileMap.size();
    }
}
