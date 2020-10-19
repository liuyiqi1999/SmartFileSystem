package Controller;

import Block.Block;
import Id.Id;
import Manager.DefaultFileManagerImpl;
import Manager.FileManager;
import Utils.Properties;
import Manager.BlockManager;
import Manager.DefaultBlockManagerImpl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DefaultBlockManagerControllerImpl implements BlockManagerController {
    Map<Id<BlockManager>, BlockManager> idBlockManagerMap;
    private static DefaultBlockManagerControllerImpl defaultBlockManagerController;

    private DefaultBlockManagerControllerImpl(Map<Id<BlockManager>, BlockManager> map) {
        this.idBlockManagerMap = map;
    }

    public static DefaultBlockManagerControllerImpl getInstance() {
        if(defaultBlockManagerController == null) {
            Map<Id<BlockManager>, BlockManager> map = new HashMap<>();
            for(int i = 0;i<Properties.BLOCK_MANAGER_COUNT;i++) {
                BlockManager blockManager = new DefaultBlockManagerImpl();
                map.put(blockManager.getId(),blockManager);
            }
            defaultBlockManagerController = new DefaultBlockManagerControllerImpl(map);
        }
        return defaultBlockManagerController;
    }

    @Override
    public BlockManager getBlockManager(Id<BlockManager> id) {
        return idBlockManagerMap.get(id);
    }

    @Override
    public Block assignBlock(byte[] data) { // 分配 Block 到 BlockManager 方法
        int min = Properties.BLOCK_MANAGER_COUNT;
        BlockManager minBlockManager = null;
        // 遍历所有的 BlockManager，查找负载最小的 BlockManager 管理这个 Block
        for (BlockManager blockManager : idBlockManagerMap.values()) {
            int temp = blockManager.getSize();
            if (temp < min) {
                minBlockManager = blockManager;
                min = temp;
            }
        }
        if(minBlockManager!=null){
            return minBlockManager.newBlock(data);
        } else {
            //TODO: 如果全部满了就报错！
            return null;
        }
    }
}
