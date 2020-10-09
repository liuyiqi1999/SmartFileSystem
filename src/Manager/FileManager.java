package Manager;

import File.File;
import Id.Id;

public interface FileManager {
    File getFile(Id fileId);
    File newFile(Id fileId);
}
