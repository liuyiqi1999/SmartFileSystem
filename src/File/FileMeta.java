package File;

import Block.Block;
import Exception.BlockException.BlockCheckSumException;
import Exception.BlockException.MD5Exception;
import Exception.FileException.IllegalDropBlocksException;

import java.io.IOException;

public interface FileMeta {
    FileMeta addBlock(Block[] blocks, long row) throws IOException;
    void setFileSize(long fileSize) throws IOException;
    long getFileSize() throws IOException;
    byte[] readFile(int length, long curr) throws IOException, BlockCheckSumException, MD5Exception;

    void dropBlocks(int startIndex, int endIndex) throws IllegalDropBlocksException, IOException;

    byte[] readBlock(int blockIndex) throws IOException, MD5Exception, BlockCheckSumException;
    String getPath();

    int getBlockNum();
}
