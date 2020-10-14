package Controller;

import File.File;
import Manager.FileManager;
import Id.Id;

public interface FileManagerController {
    FileManager getFileManager(Id<FileManager> id);
    File assignFile(int indexId);
}
