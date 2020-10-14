package Manager;

import File.File;
import Id.Id;

public interface FileManager {
    File getFile(Id<File> fileId);
    File newFile(Id<File> fileId);
    int getSize();
    Id<FileManager> getId();
}
