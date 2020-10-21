package Manager;

import Block.*;
import Exception.BlockException.BlockConstructFailException;
import Id.*;
import Utils.Properties;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DefaultBlockManagerImpl implements BlockManager {
    Id<BlockManager> id;
    Map<Id<Block>, Block> idBlockMap;

    public DefaultBlockManagerImpl() {
        IdImplFactory idImplFactory = IdImplFactory.getInstance();
        this.id = idImplFactory.getNewId(BlockManager.class);
        this.idBlockMap = new HashMap<>();

    }

    @Override
    public Block getBlock(Id indexId) {
        return idBlockMap.get(indexId);
    }

    @Override
    public void registerBlock(Block block) {
        Id<Block> bid = block.getIndexId();
        if (idBlockMap.get(bid) == null) {
            idBlockMap.put(bid, block);
        } else {
            idBlockMap.replace(bid, block);
        }
    }

    @Override
    public Block newBlock(byte[] b) throws IOException, BlockConstructFailException {
        File root = null;
        root = new File(Properties.BLOCK_PATH + "/" + this.id.toString());
        if (!root.exists()) {
            root.mkdir();
        }
        Block block = null;
        block = new DefaultBlockImpl(this, b);
        registerBlock(block);
        return block;
    }

    @Override
    public Block newEmptyBlock(int blockSize) throws IOException, BlockConstructFailException {
        byte[] b = new byte[blockSize];
        return this.newBlock(b);
    }

    @Override
    public Id<BlockManager> getId() {
        return this.id;
    }

    @Override
    public int getSize() {
        return idBlockMap.size();
    }
}
