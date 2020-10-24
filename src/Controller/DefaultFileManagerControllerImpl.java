package Controller;

import Exception.BlockException.BlockNullException;
import Exception.BlockException.RecoverBlockFailException;
import Exception.FileException.FileManagerFullException;
import Exception.FileException.RecoverFileFailException;
import Exception.IDException.IDNullInFilenameException;
import File.*;
import Id.*;
import Manager.DefaultFileManagerImpl;
import Manager.FileManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import Utils.Properties;

public class DefaultFileManagerControllerImpl implements FileManagerController {
    Map<Id<FileManager>, FileManager> idFileManagerMap;
    private static DefaultFileManagerControllerImpl defaultFileManagerController;

    private DefaultFileManagerControllerImpl(Map<Id<FileManager>, FileManager> map) {
        this.idFileManagerMap = map;
    }

    public static DefaultFileManagerControllerImpl getInstance() throws IOException, BlockNullException, IDNullInFilenameException, RecoverFileFailException, RecoverBlockFailException {
        if (defaultFileManagerController == null) {
            Map<Id<FileManager>, FileManager> map = new HashMap<>();
            for (int i = 0; i < Properties.FILE_MANAGER_COUNT; i++) {
                FileManager fileManager = new DefaultFileManagerImpl();
                map.put(fileManager.getId(), fileManager);
            }
            defaultFileManagerController = new DefaultFileManagerControllerImpl(map);
        }
        defaultFileManagerController.recoverHierarchy();
        return defaultFileManagerController;
    }

    @Override
    public FileManager getFileManager(Id<FileManager> id) {
        return idFileManagerMap.get(id);
    }

    @Override
    public File assignFile(int indexId) throws FileManagerFullException {// 分配 File 到 FileManager 方法
        int min = Integer.MAX_VALUE;
        FileManager minFileManager = null;
        // 遍历所有的 FileManager，查找负载最小的 FileManager 管理这个 File
        Id<File> id = IdImplFactory.getIdWithIndex(File.class, indexId);
        for (FileManager fileManager : idFileManagerMap.values()) {
            if (fileManager.getFile(id) != null) continue; // 如果有同 id 的 File 则直接跳过
            int temp = fileManager.getSize();
            if (temp < min) {
                minFileManager = fileManager;
                min = temp;
            }
        }
        if (minFileManager != null) {
            return minFileManager.newFile(id);
        } else {
            throw new FileManagerFullException("[FileManagerFullException] FileManagers cannot manage more files with this id. ");
        }
    }

    private void recoverHierarchy() throws IOException, BlockNullException, IDNullInFilenameException, RecoverFileFailException, RecoverBlockFailException {
        for (int i = 0; i < Properties.FILE_MANAGER_COUNT; i++) {
            Id<FileManager> fmId = IdImplFactory.getIdWithIndex(FileManager.class, i);
            FileManager fileManager = getFileManager(fmId);
            java.io.File fmFile = new java.io.File(Properties.FILE_PATH + "/fm" + i);
            if (fmFile.exists()) {
                java.io.File[] files = fmFile.listFiles();
                for (int j = 0; j < files.length; j++) {
                    if (!(files[j].getName().substring(files[j].getName().lastIndexOf("."))).equals(".file"))
                        continue; //排除不相关文件
                    File file = null;
                    file = DefaultFileImpl.recoverFile(files[j], fileManager);
                    fileManager.registerFile(file);
                }
            }
        }
    }
}
