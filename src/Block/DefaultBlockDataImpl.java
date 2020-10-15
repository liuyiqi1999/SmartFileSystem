package Block;

import Manager.BlockManager;
import Utils.IOUtils;

import java.io.*;

public class DefaultBlockDataImpl implements BlockData {
    byte[] data;
    String path;

    public DefaultBlockDataImpl(byte[] data, String path) {
        this.data = data;
        this.path = path;
        File file = new File(path);
        if(!file.exists()){// 如果不存在则新建文件，如果存在则只恢复到内存
            try {
                file.createNewFile();
                IOUtils.writeByteArrayToFile(data, file);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public byte[] getData() {
        File file = new File(path);
        return Utils.IOUtils.readByteArrayFromFile(file, file.length());
    }
}
