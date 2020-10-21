package File;

import Block.Block;

import java.io.IOException;

public interface FileMeta {
    FileMeta addBlock(Block[] blocks, long row) throws IOException;
    void setFileSize(long fileSize) throws IOException;
    long getFileSize() throws IOException;
    byte[] readFile(int length, long curr) throws IOException;
    String getPath();
}
