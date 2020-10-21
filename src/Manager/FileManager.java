package Manager;

import Exception.FileException.FileMetaConstructException;
import File.File;
import Id.Id;

public interface FileManager {
    File getFile(Id<File> fileId);
    File newFile(Id<File> fileId) throws FileMetaConstructException;
    File registerFile(File file);
    int getSize();
    Id<FileManager> getId();
}
