package File;

import Block.*;
import Controller.BlockManagerController;
import Controller.DefaultBlockManagerControllerImpl;
import Id.*;
import Manager.BlockManager;
import Manager.FileManager;
import Utils.IOUtils;
import Utils.Properties;

import java.io.IOException;
import java.util.*;

public class DefaultFileMetaImpl implements FileMeta {
    Id<FileMeta> id;
    long fileSize;
    List<List<Block>> blocksList;
    FileManager fileManager;

    @Override
    public long getFileSize() {
        java.io.File file = new java.io.File(Properties.FILE_PATH + "/" + this.fileManager.getId().toString() + "/" + this.id.toString() + ".file");
        long fileSize = Integer.parseInt(new String(IOUtils.readByteArrayFromFileRow(file, 0)));
        return fileSize;
    }

    DefaultFileMetaImpl(Id<FileMeta> id, long fileSize, List<List<Block>> blocksList, FileManager fileManager) {
        this.id = id;
        this.fileSize = fileSize;
        this.blocksList = blocksList;
        this.fileManager = fileManager;
    }

    DefaultFileMetaImpl(FileManager fileManager, Id<File> fileId) {
        this.id = IdImplFactory.getIdWithIndex(FileMeta.class, fileId.getId());
        this.fileSize = 0;
        this.fileManager = fileManager;
        blocksList = new ArrayList<>();
        java.io.File file = new java.io.File(Properties.FILE_PATH + "/" + this.fileManager.getId().toString() + "/" + fileId.toString() + ".file");
        java.io.File root = new java.io.File(Properties.FILE_PATH + "/" + this.fileManager.getId().toString());
        if (!root.exists()) {
            root.mkdir();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
                String data = "0\n"+Properties.BLOCK_SIZE+"\n";
                Utils.IOUtils.writeByteArrayToFile(data.getBytes(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public FileMeta addBlock(Block[] blocks, long row) {
        List<Block> duplicatedList = new ArrayList<>();
        java.io.File file = new java.io.File(Properties.FILE_PATH + "/" + this.fileManager.getId().toString() + "/" + this.id.toString() + ".file");
        String logicBlock = "";
        for (Block block : blocks) {
            BlockManager manager = block.getBlockManager();
            duplicatedList.add(block);

            String bmId = manager.getId().toString();
            String bId = block.getIndexId().toString();

            //Utils.IOUtils.writeBytesToEndOfFile((bmId + "-" + bId + ";").getBytes(), file);
            //改为向 Logic Block List 中任意行（指针）插入一行 Logic Block 数据
            logicBlock += bmId + "-" + bId + ";";
        }
        Utils.IOUtils.insertByteArrayToFileRow(logicBlock.getBytes(), file, row);
        this.blocksList.add(duplicatedList);
        return this;
    }

    @Override
    public void setFileSize(long fileSize) {
        long oldFileSize = getFileSize();
        this.fileSize = oldFileSize + fileSize;
        java.io.File file = new java.io.File(Properties.FILE_PATH + "/" + this.fileManager.getId().toString() + "/" + this.id.toString() + ".file");
        IOUtils.writeByteArrayToFileRow(String.valueOf(this.fileSize).getBytes(), file, 0);
    }

    public byte[] readFile(int length, long curr) {
        long startBlockIndex = curr / Properties.BLOCK_SIZE;
        int readLength = 0;
        StringBuilder data = new StringBuilder();
        for (int i = (int) startBlockIndex; i < blocksList.size(); i++) {
            byte[] fileData = blocksList.get(i).get(new Random().nextInt(blocksList.get(i).size())).read();
            if (fileData != null) data.append(new String(fileData));
            readLength = data.length();
            if (readLength >= length) break;
        }
        return data.substring(0, length).getBytes();
    }

    public static FileMeta recoverFileMeta(String[] lines, Id<File> fileId, long fileSize, FileManager fileManager) {
        List<List<Block>> blocksList = new ArrayList<>();
        for (int i = 2; i < lines.length; i++) {
            String[] blockNames = lines[i].split(";");
            BlockManagerController blockManagerController = DefaultBlockManagerControllerImpl.getInstance();
            List<Block> blocks = new ArrayList<Block>();
            for (int j = 0; j < blockNames.length; j++) {
                Id<BlockManager> bmId = IdImplFactory.getIdWithIndex(BlockManager.class, IOUtils.getIntInFileName(blockNames[j].split("-")[0]));
                BlockManager blockManager = blockManagerController.getBlockManager(bmId);
                Id<Block> bId = IdImplFactory.getIdWithIndex(Block.class, IOUtils.getIntInFileName(blockNames[j].split("-")[1]));
                java.io.File metaFile = new java.io.File(Properties.BLOCK_PATH + "/" + bmId.toString() + "/" + bId.toString() + ".meta");
                java.io.File dataFile = new java.io.File(Properties.BLOCK_PATH + "/" + bmId.toString() + "/" + bId.toString() + ".data");
                Block block = DefaultBlockImpl.recoverBlock(blockManager, metaFile, dataFile);
                blocks.add(block);
            }
            blocksList.add(blocks);
        }
        return new DefaultFileMetaImpl(IdImplFactory.getIdWithIndex(FileMeta.class, fileId.getId()), fileSize, blocksList, fileManager);
    }
}
