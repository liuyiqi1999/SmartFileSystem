package File;

import Block.*;
import Controller.BlockManagerController;
import Controller.DefaultBlockManagerControllerImpl;
import Exception.BlockException.BlockCheckSumException;
import Exception.BlockException.BlockNullException;
import Exception.BlockException.MD5Exception;
import Exception.BlockException.RecoverBlockFailException;
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

    DefaultFileMetaImpl(FileManager fileManager, Id<File> fileId) {
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
    public FileMeta addBlock(Block[] blocks, long row) throws IOException {
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
    public void setFileSize(long fileSize) throws IOException {
        this.fileSize = fileSize;
        java.io.File file = new java.io.File(getPath());
        IOUtils.writeByteArrayToFileRow(String.valueOf(this.fileSize).getBytes(), file, 0);
    }

    public byte[] readFile(int length, long curr) throws IOException, MD5Exception, BlockCheckSumException {
        long startBlockIndex = curr / Properties.BLOCK_SIZE;
        long cutHead = curr % Properties.BLOCK_SIZE;
        int readLength = 0;
        StringBuilder data = new StringBuilder();
        for (int i = (int) startBlockIndex; i < blocksList.size(); i++) {
            int random = new Random().nextInt(blocksList.get(i).size());
            Block readBlock = blocksList.get(i).get(random);
            byte[] fileData = readBlock.read();
            if (fileData == null) {
                for (int j = 0; j < Properties.DUPLICATED_BLOCK_NUMBER; j++) {
                    if (j == random) continue;
                    readBlock = blocksList.get(i).get(j);
                    fileData = readBlock.read();
                    if (fileData != null) break;
                }
            }//通过是否为null来判断，而不是Exception
            if (fileData == null) {//只有在所有 Duplicated Block 都失效时才抛出异常
                throw new BlockCheckSumException("[BlockCheckSumException] some block in file is corrupted. ");
            }

            int blockSize = readBlock.blockSize();
            data.append(new String(fileData));
            readLength += blockSize;
            if (readLength >= length) break;
        }

        return data.substring((int) cutHead, (int) (cutHead + length)).getBytes();
    }

    public static FileMeta recoverFileMeta(String[] lines, Id<File> fileId, long fileSize, FileManager fileManager) throws IOException, BlockNullException, IDNullInFilenameException, RecoverBlockFailException {
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

    @Override
    public void dropBlocks(int startIndex, int endIndex) throws IllegalDropBlocksException, IOException {
        if (endIndex < startIndex)
            throw new IllegalDropBlocksException("[IllegalDropBlocksException] endIndex is less than startIndex");
        for (int i = startIndex; i < endIndex; i++) {
            blocksList.remove(startIndex);
        }
        java.io.File file = new java.io.File(getPath());
        IOUtils.deleteByteArrayInFileRow(file, startIndex + 2, endIndex + 2);
    }

    @Override
    public byte[] readBlock(int blockIndex) throws IOException, MD5Exception, BlockCheckSumException {
        int random = new Random().nextInt(blocksList.get(blockIndex).size());
        Block readBlock = this.blocksList.get(blockIndex).get(random);
        byte[] fileData = readBlock.read();
        if (fileData == null) {
            for (int j = 0; j < Properties.DUPLICATED_BLOCK_NUMBER && j != random; j++) {
                readBlock = blocksList.get(blockIndex).get(j);
                fileData = readBlock.read();
                if (fileData != null) break;
            }
        }//通过是否为null来判断，而不是Exception
        if (fileData == null) {//只有在所有 Duplicated Block 都失效时才抛出异常
            throw new BlockCheckSumException("[BlockCheckSumException] some block in file is corrupted. ");
        }
        return fileData;
    }

    @Override
    public int getBlockNum() {
        return blocksList.size();
    }
}
