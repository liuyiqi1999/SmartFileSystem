package Block;

import Exception.BlockException.BlockCheckSumException;
import Exception.BlockException.MD5Exception;
import Id.Id;
import Manager.BlockManager;

import java.io.IOException;

public interface Block {
    Id getIndexId();
    BlockManager getBlockManager();
    byte[] read() throws IOException, MD5Exception;
    int blockSize();
    boolean check(byte[] data) throws MD5Exception;
}




