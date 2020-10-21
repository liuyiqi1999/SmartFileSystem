package Block;

import Exception.BlockException.BlockCheckSumException;
import Exception.BlockException.BlockConstructFailException;
import Exception.BlockException.BlockNullException;
import Exception.BlockException.MD5Exception;
import Exception.IDException.IDNullInFilenameException;
import Id.*;
import Manager.BlockManager;
import Utils.IOUtils;
import Utils.MD5;
import Utils.Properties;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class DefaultBlockImpl implements Block {
    Id<Block> id;
    BlockManager blockManager;
    BlockMeta blockMeta;
    BlockData blockData;

    public DefaultBlockImpl(BlockManager blockManager, byte[] data) throws IOException, BlockConstructFailException {
        IdImplFactory idImplFactory = IdImplFactory.getInstance();
        this.id = idImplFactory.getNewId(Block.class);
        this.blockManager = blockManager;
        String md5 = null;
        try {
            md5 = MD5.getByteArrayMD5(data);
        } catch (NoSuchAlgorithmException e) {
            throw new BlockConstructFailException("[BlockConstructFailException] constructing block failed. ");
        }
        String path = null;
        path = Properties.BLOCK_PATH + "/" + blockManager.getId().toString() + "/" + this.getIndexId().toString();
        this.blockMeta = new DefaultBlockMetaImpl(data.length, md5, path + ".meta");
        this.blockData = new DefaultBlockDataImpl(data, path + ".data");
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
    public byte[] read() throws IOException {
        byte[] data = blockData.getData();
        try {
            if (check(data)) return data;
        } catch (BlockCheckSumException | MD5Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public boolean check(byte[] data) throws BlockCheckSumException, MD5Exception {
        String oldMD5 = blockMeta.getCheckSum();
        String newMD5 = null;
        try {
            newMD5 = MD5.getByteArrayMD5(data);
        } catch (NoSuchAlgorithmException e) {
            throw new MD5Exception("[MD5Exception] getting block md5 failed. ");
        }
        if (newMD5.equals(oldMD5)) return true;
        else {
            throw new BlockCheckSumException("[BlockCheckSumException] block check failed, file may be broken.");
        }
    }

    @Override
    public int blockSize() {
        return blockMeta.getSize();
    }

    public static Block recoverBlock(BlockManager blockManager, File meta, File data) throws IOException, BlockNullException {
        Id<Block> id = null;
        try {
            id = IdImplFactory.getIdWithIndex(Block.class, IOUtils.getIntInFileName(meta.getName()));
        } catch (IDNullInFilenameException e) {
            System.out.println(e.getMessage() + " failed to recover file data.");
        }
        byte[] metaData = IOUtils.readByteArrayFromFile(meta, meta.length());
        if (metaData == null) {
            throw new BlockNullException("[BlockNullException] ");
        }
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
