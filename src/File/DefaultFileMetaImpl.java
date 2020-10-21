package File;

import Block.*;
import Controller.BlockManagerController;
import Controller.DefaultBlockManagerControllerImpl;
import Exception.BlockException.BlockNullException;
import Exception.FileException.FileMetaConstructException;
import Exception.FileException.IllegalDropBlocksException;
import Exception.IDException.IDNullInFilenameException;
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
    public long getFileSize() throws IOException {
        java.io.File file = new java.io.File(getPath());
        long fileSize = Integer.parseInt(new String(IOUtils.readByteArrayFromFileRow(file, 0)));
        return fileSize;
    }

    DefaultFileMetaImpl(Id<FileMeta> id, long fileSize, List<List<Block>> blocksList, FileManager fileManager) {
        this.id = id;
        this.fileSize = fileSize;
        this.blocksList = blocksList;
        this.fileManager = fileManager;
    }

    DefaultFileMetaImpl(FileManager fileManager, Id<File> fileId) throws FileMetaConstructException {
        this.id = IdImplFactory.getIdWithIndex(FileMeta.class, fileId.getId());
        this.fileSize = 0;
        this.fileManager = fileManager;
        blocksList = new ArrayList<>();
        java.io.File file = null;
        java.io.File root = null;

        file = new java.io.File(Properties.FILE_PATH + "/" + this.fileManager.getId().toString() + "/" + fileId.toString() + ".file");
        root = new java.io.File(Properties.FILE_PATH + "/" + this.fileManager.getId().toString());

        if (!root.exists()) {
            root.mkdir();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
                String data = "0\n" + Properties.BLOCK_SIZE + "\n";
                Utils.IOUtils.writeByteArrayToFile(data.getBytes(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public FileMeta addBlock(Block[] blocks, long row) throws IOException{
        List<Block> duplicatedList = new ArrayList<>();
        java.io.File file = new java.io.File(getPath());
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
        this.blocksList.add((int) (row - 2), duplicatedList);
        return this;
    }

    @Override
    public void setFileSize(long fileSize) throws IOException{
        long oldFileSize = getFileSize();
        this.fileSize = oldFileSize + fileSize;
        java.io.File file = new java.io.File(getPath());
        try {
            IOUtils.writeByteArrayToFileRow(String.valueOf(this.fileSize).getBytes(), file, 0);
        } catch (IOException e) {
            System.out.println("set file size failed, path is " + file.getPath());
        }
    }

    public byte[] readFile(int length, long curr) throws IOException {
        long startBlockIndex = curr / Properties.BLOCK_SIZE;
        int readLength = 0;
        StringBuilder data = new StringBuilder();
        for (int i = (int) startBlockIndex; i < blocksList.size(); i++) {
            Block readBlock = blocksList.get(i).get(new Random().nextInt(blocksList.get(i).size()));
            byte[] fileData = readBlock.read();
            int blockSize = readBlock.blockSize();
            if (fileData != null) data.append(new String(fileData));
            readLength += blockSize;
            if (readLength >= length) break;
        }
        return data.substring(0, length).getBytes();
    }

    public static FileMeta recoverFileMeta(String[] lines, Id<File> fileId, long fileSize, FileManager fileManager) throws IOException, BlockNullException, IDNullInFilenameException {
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

    @Override
    public String getPath() {
        return Properties.FILE_PATH + "/" + this.fileManager.getId().toString() + "/" + this.id.toString() + ".file";
    }

    public void dropBlocks(int startIndex, int endIndex) throws IllegalDropBlocksException, IOException {
        if (endIndex < startIndex)
            throw new IllegalDropBlocksException("[IllegalDropBlocksException] endIndex is less than startIndex");
        for (int i = startIndex; i <= endIndex; i++) {
            blocksList.remove(i);
        }
        java.io.File file = new java.io.File(getPath());
        IOUtils.deleteByteArrayInFileRow(file, startIndex + 2, endIndex + 2);
    }
}
