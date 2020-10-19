package File;

import Block.Block;

public interface FileMeta {
    FileMeta addBlock(Block[] blocks, long row);
    void setFileSize(long fileSize);
    long getFileSize();
    byte[] readFile(int length, long curr);
}
