package File;

import Block.Block;
import Controller.BlockManagerController;
import Controller.DefaultBlockManagerControllerImpl;
import Exception.BlockException.BlockConstructFailException;
import Exception.BlockException.BlockManagerFullException;
import Exception.BlockException.BlockNullException;
import Exception.BlockException.MD5Exception;
import Exception.FileException.*;
import Exception.IDException.IDNullInFilenameException;
import Id.*;
import Manager.*;
import Utils.IOUtils;
import Utils.Properties;

import java.io.IOException;

public class DefaultFileImpl implements File {
    Id<File> id;
    FileManager fileManager;
    FileMeta fileMeta;
    long curr;
    static String path = Properties.FILE_PATH;
    final static int MOVE_CURR = 0;
    final static int MOVE_HEAD = 1;
    final static int MOVE_TAIL = 2;

    public DefaultFileImpl(Id<File> id, FileManager fileManager, FileMeta fileMeta) {
        this.id = id;
        this.fileManager = fileManager;
        this.fileMeta = fileMeta;
        this.curr = 0;
    }

    public DefaultFileImpl(FileManager fileManager, Id<File> id) throws FileMetaConstructException {
        this.id = id;
        this.fileManager = fileManager;
        this.fileMeta = new DefaultFileMetaImpl(this.fileManager, this.id);
        this.curr = 0;
    }

    @Override
    public long size() throws IOException {
        return fileMeta.getFileSize();
    }

    @Override
    public void setSize(long newSize) throws FileWriteFailException, IllegalCursorException, IOException {
        if (newSize < 0) {
            System.out.println("[SetSizeException] file size cannot be under zero. ");
            return;
        }
        long oldSize = fileMeta.getFileSize();
        if (newSize > oldSize) {
            byte[] newData = new byte[(int) (newSize - oldSize)];
            write(newData);
        } else if (newSize < oldSize) {
            long newBlockIndex = newSize / Properties.BLOCK_SIZE;
            //TODO：注意写入那个无法被整除的Block，除此之外用 dropBlock 方法。
        }
        fileMeta.setFileSize(newSize);
    }

    @Override
    public Id getFileId() {
        return this.id;
    }

    @Override
    public FileManager getFileManager() {
        return this.fileManager;
    }

    @Override
    public byte[] read(int length) throws IOException {
        long fileSize = fileMeta.getFileSize();
        if (length > fileSize - curr) {
            System.out.println("[OverReadingFileException] Reading more bytes than file's length. ");
        }
        try {
            move(length, MOVE_CURR);
        } catch (IllegalCursorException e) {
            System.out.println(e.getMessage());
        }
        return fileMeta.readFile(length, this.curr);
    }

    @Override
    public void write(byte[] b) throws IOException, IllegalCursorException, FileWriteFailException {
        long currentBlock = curr / Properties.BLOCK_SIZE;
        long row = currentBlock + 2;
        for (int i = 0; i < b.length; i = i + Properties.BLOCK_SIZE) {
            byte[] chunk;
            if (i + Properties.BLOCK_SIZE >= b.length) {// 最后一个 Logic Block，文件内容不足一整块
                chunk = new byte[b.length - i];
                System.arraycopy(b, i, chunk, 0, b.length - i);
            } else {
                chunk = new byte[Properties.BLOCK_SIZE];
                System.arraycopy(b, i, chunk, 0, Properties.BLOCK_SIZE);
            }
            BlockManagerController blockManagerController = DefaultBlockManagerControllerImpl.getInstance();
            Block[] blocks = new Block[Properties.DUPLICATED_BLOCK_NUMBER];
            for (int j = 0; j < Properties.DUPLICATED_BLOCK_NUMBER; j++) {
                try {
                    blocks[j] = blockManagerController.assignBlock(chunk);
                } catch (BlockConstructFailException | MD5Exception | BlockManagerFullException e) {
                    throw new FileWriteFailException("[FileWriteFailException] write file failed. "+e.getMessage());
                }
            }
            fileMeta.addBlock(blocks, row++);
        }
        fileMeta.setFileSize(b.length);
        move(b.length, MOVE_CURR);
    }

    @Override
    public long move(long offset, int where) throws IllegalCursorException, IOException {
        switch (where) {
            case MOVE_CURR:
                if (curr + offset > size() || curr + offset < 0) {
                    throw new IllegalCursorException("illegal cursor move");
                }
                curr += offset;
                break;
            case MOVE_HEAD:
                if (offset > size() || offset < 0) {
                    throw new IllegalCursorException("illegal cursor move");
                }
                curr = offset;
                break;
            case MOVE_TAIL:
                if (offset > 0 || (size() + offset) < 0) {
                    throw new IllegalCursorException("illegal cursor move");
                }
                curr = size() + offset;
                break;
            default:
                throw new IllegalCursorException("illegal cursor move, MOVE_CURR = 0; MOVE_HEAD = 1; MOVE_TAIL = 2;");
        }
        return this.curr;
    }

    @Override
    public void close() {
    }

    @Override
    public long pos(){
        return curr;
    }

    public static File recoverFile(java.io.File file, FileManager fileManager) throws IOException, BlockNullException, IDNullInFilenameException{
        int indexId = 0;
        try {
            indexId = IOUtils.getIntInFileName(file.getName());
        } catch (IDNullInFilenameException e) {
            System.out.println(e.getMessage() + "recover file failed, path is " + file.getPath());
        }
        Id<File> id = IdImplFactory.getIdWithIndex(File.class, indexId);
        byte[] data = Utils.IOUtils.readByteArrayFromFile(file, file.length());
        String[] lines = new String(data).split("\n");
        long fileSize = Long.parseLong(lines[0]);

        FileMeta fileMeta = DefaultFileMetaImpl.recoverFileMeta(lines, id, fileSize, fileManager);

        return new DefaultFileImpl(id, fileManager, fileMeta);
    }
}
