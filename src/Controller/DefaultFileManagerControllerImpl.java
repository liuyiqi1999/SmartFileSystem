package Controller;

import File.*;
import Id.*;
import Manager.DefaultFileManagerImpl;
import Manager.FileManager;

import java.util.HashMap;
import java.util.Map;

import Utils.IOUtils;
import Utils.Properties;

public class DefaultFileManagerControllerImpl implements FileManagerController{
    Map<Id<FileManager>, FileManager> idFileManagerMap;
    private static DefaultFileManagerControllerImpl defaultFileManagerController;

    private DefaultFileManagerControllerImpl(Map<Id<FileManager>, FileManager> map) {
        this.idFileManagerMap = map;
    }

    public static DefaultFileManagerControllerImpl getInstance() {
        if(defaultFileManagerController == null) {
            Map<Id<FileManager>, FileManager> map = new HashMap<>();
            for(int i = 0;i<Properties.FILE_MANAGER_COUNT;i++) {
                FileManager fileManager = new DefaultFileManagerImpl();
                map.put(fileManager.getId(),fileManager);
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
    public File assignFile(int indexId) {// 分配 File 到 FileManager 方法
        int min = Properties.FILE_MANAGER_COUNT;
        FileManager minFileManager = null;
        // 遍历所有的 FileManager，查找负载最小的 FileManager 管理这个 File
        Id<File> id = IdImplFactory.getIdWithIndex(File.class, indexId);
        for(FileManager fileManager: idFileManagerMap.values()) {
            if(fileManager.getFile(id)!=null) continue; // 如果有同 id 的 File 则直接跳过
            int temp = fileManager.getSize();
            if(temp<min) {
                minFileManager = fileManager;
                min = temp;
            }
        }
        if(minFileManager!=null){
            return minFileManager.newFile(id);
        } else {
            //TODO: 如果全部满了就报错！
            return null;
        }
    }

    private void recoverHierarchy(){
        for(int i=0;i<Properties.FILE_MANAGER_COUNT;i++) {
            Id<FileManager> fmId = IdImplFactory.getIdWithIndex(FileManager.class, i);
            FileManager fileManager = getFileManager(fmId);
            java.io.File fmFile = new java.io.File(Properties.FILE_PATH + "/fm" + i);
            if (fmFile.exists()) {
                String[] fileNames = fmFile.list();
                for (int j = 0; j < fileNames.length; j++) {
                    if(!fileNames[j].substring(fileNames[j].lastIndexOf(".")).equals("file")) continue;
                    Id<File> fId = IdImplFactory.getIdWithIndex(File.class, IOUtils.getIntInFileName(fileNames[j]));
                    File file = new DefaultFileImpl(fileManager, fId);
                    fileManager.registerFile(file);
                }
            }
        }
    }
}
