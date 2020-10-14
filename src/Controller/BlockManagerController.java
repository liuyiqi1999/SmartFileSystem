package Controller;

import Block.Block;
import Id.Id;
import Manager.BlockManager;

public interface BlockManagerController {
    BlockManager getBlockManager(Id<BlockManager> id);
    Block assignBlock(byte[] data);
}
