package Manager;

import Block.Block;
import Exception.BlockException.BlockConstructFailException;
import Id.Id;

import java.io.IOException;

public interface BlockManager {
    Id<BlockManager> getId();
    Block getBlock(Id indexId);
    Block newBlock(byte[] b) throws IOException, BlockConstructFailException;
    void registerBlock(Block block);
    default Block newEmptyBlock(int blockSize) throws IOException, BlockConstructFailException {
        return newBlock(new byte[blockSize]);
    }

    int getSize();
}
