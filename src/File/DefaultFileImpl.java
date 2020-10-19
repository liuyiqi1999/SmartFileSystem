package File;

import Block.Block;
import Controller.BlockManagerController;
import Controller.DefaultBlockManagerControllerImpl;
import Id.*;
import Manager.*;
import Utils.IOUtils;
import Utils.Properties;

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

    public DefaultFileImpl(FileManager fileManager, Id<File> id) {
        this.id = id;
        this.fileManager = fileManager;
        this.fileMeta = new DefaultFileMetaImpl(this.fileManager, this.id);
        this.curr = 0;
    }

    @Override
    public long size() {
        return fileMeta.getFileSize();
    }

    @Override
    public void setSize(long newSize) {
        long oldSize = fileMeta.getFileSize();
        if (newSize > oldSize) {
            byte[] newData = new byte[(int) (newSize - oldSize)];

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
    public byte[] read(int length) {
        long fileSize = fileMeta.getFileSize();
        if(length>fileSize-curr) return null;//TODO: read 超出文件长度报错
        return fileMeta.readFile(length, this.curr);
    }

    @Override
    public void write(byte[] b) {
        long currentBlock = curr/Properties.BLOCK_SIZE;
        long row = currentBlock + 2;
        for (int i = 0; i < b.length; i = i + Properties.BLOCK_SIZE) {
            byte[] chunk = new byte[Properties.BLOCK_SIZE];
            if(i+Properties.BLOCK_SIZE>=b.length) {// 最后一个 Logic Block，文件内容不足一整块
                System.arraycopy(b,i,chunk,0 ,b.length-i);
            }else{
                System.arraycopy(b, i, chunk, 0, Properties.BLOCK_SIZE);
            }
            BlockManagerController blockManagerController = DefaultBlockManagerControllerImpl.getInstance();
            Block[] blocks = new Block[Properties.DUPLICATED_BLOCK_NUMBER];
            for (int j = 0; j < Properties.DUPLICATED_BLOCK_NUMBER; j++) {
                blocks[j] = blockManagerController.assignBlock(chunk);
            }
            fileMeta.addBlock(blocks, row++);
        }
        fileMeta.setFileSize(b.length);
    }

    @Override
    public long move(long offset, int where) {
        if (where != MOVE_CURR && where != MOVE_HEAD && (where != MOVE_TAIL && offset > 0)) return 0;// TODO: 参数错误报错
        switch (where) {
            case MOVE_CURR:
                curr += offset;
                break;
            case MOVE_HEAD:
                curr = offset;
                break;
            case MOVE_TAIL:
                curr = fileMeta.getFileSize() + offset;
                break;
            default:
                break; // TODO: 参数错误报错
        }
        return this.curr;
    }

    @Override
    public void close() {
    }

    public static File recoverFile(java.io.File file, FileManager fileManager){
        int indexId = IOUtils.getIntInFileName(file.getName());
        Id<File> id = IdImplFactory.getIdWithIndex(File.class, indexId);

        byte[] data = Utils.IOUtils.readByteArrayFromFile(file, file.length());
        if(data==null) return null;//TODO: 读到空文件报错！
        String[] lines = new String(data).split("\n");
        long fileSize = Long.parseLong(lines[0]);

        FileMeta fileMeta = DefaultFileMetaImpl.recoverFileMeta(lines, id, fileSize, fileManager);

        return new DefaultFileImpl(id, fileManager, fileMeta);
    }
}
