package Manager;

import File.File;
import Id.Id;

public interface FileManager {
    File getFile(Id<File> fileId);
    File newFile(Id<File> fileId);
    File registerFile(File file);
    int getSize();
    Id<FileManager> getId();
}
