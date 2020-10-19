package Block;

import Id.*;
import Manager.BlockManager;
import Utils.IOUtils;
import Utils.MD5;
import Utils.Properties;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.File;

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
        String path = Properties.BLOCK_PATH +"/"+ blockManager.getId().toString() + "/" + this.getIndexId().toString();
        this.blockMeta = new DefaultBlockMetaImpl(data.length, md5, path+".meta");
        this.blockData = new DefaultBlockDataImpl(data, path+".data");
    }

    public DefaultBlockImpl(BlockManager blockManager, BlockMeta blockMeta, BlockData blockData, Id<Block> id) {// 恢复用构造方法
        this.id = id;
        int indexId = id.getId();
        IdImplFactory idImplFactory = IdImplFactory.getInstance();
        idImplFactory.recoverLocalId(Block.class, indexId);// 防止因为ID工厂localID未更新导致的block覆盖的问题
        this.blockManager = blockManager;
        this.blockMeta = blockMeta;
        this.blockData = blockData;
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

    public static Block recoverBlock(BlockManager blockManager, File meta, File data){
        Id<Block> id = IdImplFactory.getIdWithIndex(Block.class,IOUtils.getIntInFileName(meta.getName()));
        byte[] metaData = IOUtils.readByteArrayFromFile(meta, meta.length());
        if(metaData==null) return null;//TODO: 文件为空时报错！
        String[] metaLines = (new String(metaData)).split("\n");
        int size = Integer.parseInt(metaLines[0]);
        String checksum = metaLines[1];
        BlockMeta blockMeta = new DefaultBlockMetaImpl(size, checksum, meta.getPath());
        byte[] dataData = IOUtils.readByteArrayFromFile(data, data.length());
        BlockData blockData = new DefaultBlockDataImpl(dataData, data.getPath());
        Block block = new DefaultBlockImpl(blockManager, blockMeta, blockData, id);
        blockManager.registerBlock(block);
        return block;
    }
}
