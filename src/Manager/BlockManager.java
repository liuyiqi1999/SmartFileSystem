package Manager;

import Block.Block;
import Id.Id;

public interface BlockManager {
    Id<BlockManager> getId();
    Block getBlock(Id indexId);
    Block newBlock(byte[] b);
    void registerBlock(Block block);
    default Block newEmptyBlock(int blockSize) {
        return newBlock(new byte[blockSize]);
    }

    int getSize();
}
