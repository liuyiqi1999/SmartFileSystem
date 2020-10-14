package Block;

import Id.*;
import Manager.BlockManager;
import Utils.MD5;

public class DefaultBlockImpl implements Block {
    Id<Block> id;
    BlockManager blockManager;
    BlockMeta blockMeta;
    BlockData blockData;

    public DefaultBlockImpl(BlockManager blockManager, byte[] data) {
        IdImplFactory idImplFactory = IdImplFactory.getInstance();
        this.id = idImplFactory.getNewId(Block.class);
        this.blockManager = blockManager;
        String md5 = MD5.getByteArrayMD5(data);
        String path = "resources/" + blockManager.getId().toString() + "/" + this.getIndexId().toString();
        this.blockMeta = new DefaultBlockMetaImpl(data.length, md5, path);
        this.blockData = new DefaultBlockDataImpl(data, path);
    }

    @Override
    public Id<Block> getIndexId() {
        return id;
    }

    @Override
    public BlockManager getBlockManager() {
        return blockManager;
    }

    @Override
    public byte[] read() {
        byte[] data = blockData.getData();
        String oldMD5 = blockMeta.getCheckSum();
        String newMD5 = Utils.MD5.getByteArrayMD5(data);
        if (newMD5.equals(oldMD5)) return data;
        else return null; //TODO: Block 校验错误
    }

    @Override
    public int blockSize() {
        return blockMeta.getSize();
    }
}
