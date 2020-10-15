package Manager;

import Block.*;
import Id.*;
import Utils.Properties;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DefaultBlockManagerImpl implements BlockManager{
    Id<BlockManager> id;
    Map<Id<Block>,Block> idBlockMap;

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
    public Block newBlock(byte[] b) {
        File root = new File(Properties.BLOCK_PATH+"/"+this.id.toString());
        if(!root.exists()){
            root.mkdir();
        }
        Block block = new DefaultBlockImpl(this, b);
        Id<Block> bid = block.getIndexId();
        if(idBlockMap.get(bid)==null){
            idBlockMap.put(bid,block);
        } else {
            idBlockMap.replace(bid,block);
        }
        return block;
    }

    @Override
    public Block newEmptyBlock(int blockSize) {
        byte[] b = new byte[blockSize];
        return this.newBlock(b);
    }

    @Override
    public Id<BlockManager> getId() {
        return this.id;
    }

    @Override
    public int getSize(){
        return idBlockMap.size();
    }
}
