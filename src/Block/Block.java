package Block;

import Id.Id;
import Manager.BlockManager;

public interface Block {
    Id getIndexId();
    BlockManager getBlockManager();
    byte[] read();
    int blockSize();
}




