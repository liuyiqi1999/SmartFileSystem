package Controller;

import Block.Block;
import Exception.BlockException.BlockConstructFailException;
import Exception.BlockException.BlockManagerFullException;
import Exception.BlockException.MD5Exception;
import Id.Id;
import Manager.BlockManager;

import java.io.IOException;

public interface BlockManagerController {
    BlockManager getBlockManager(Id<BlockManager> id);
    Block assignBlock(byte[] data) throws BlockManagerFullException, IOException, MD5Exception, BlockConstructFailException;
}
