package File;

import Block.Block;
import Id.*;
import Manager.BlockManager;
import Manager.FileManager;
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
        return fileSize;
    }

    DefaultFileMetaImpl(FileManager fileManager, Id<File> fileId) {
        IdImplFactory idImplFactory = IdImplFactory.getInstance();
        this.id = idImplFactory.getNewId(FileMeta.class);
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
                String data = "0\n";
                Utils.IOUtils.writeByteArrayToFile(data.getBytes(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public FileMeta addBlock(Block[] blocks) {
        List<Block> duplicatedList = new ArrayList<>();
        java.io.File file = new java.io.File(Properties.FILE_PATH + this.id.toString() + ".file");
        for (Block block : blocks) {
            BlockManager manager = block.getBlockManager();
            duplicatedList.add(block);

            String bmId = manager.getId().toString();
            String bId = block.getIndexId().toString();

            Utils.IOUtils.writeBytesToEndOfFile((bmId + "-" + bId + ";").getBytes(), file);
        }
        Utils.IOUtils.writeBytesToEndOfFile("\n".getBytes(), file);
        this.blocksList.add(duplicatedList);
        return this;
    }

    @Override
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public byte[] readFile(int length, long curr) {
        long startBlock = curr / Properties.BLOCK_SIZE;
        long startBlockIndex = 0;
        if (startBlock != 0) startBlockIndex = startBlock - 1;
        int readLength = 0;
        StringBuilder data = new StringBuilder();
        for (int i = (int) startBlockIndex; i < blocksList.size(); i++) {
            byte[] fileData = blocksList.get(i).get(new Random().nextInt(blocksList.get(i).size())).read();
            if (fileData != null) data.append(Arrays.toString(fileData));
            readLength = data.length();
            if (readLength >= length) break;
        }
        return data.substring(0, length).getBytes();
    }

    public FileMeta recoverFileMeta(String[] lines){
        for(int i=2;i<lines.length;i++){
            String[] blocks = lines[i].split(";");

        }
    }
}
