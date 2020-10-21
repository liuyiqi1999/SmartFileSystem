package Block;

import Manager.BlockManager;
import Utils.IOUtils;

import java.io.*;

public class DefaultBlockDataImpl implements BlockData {
    byte[] data;
    String path;

    public DefaultBlockDataImpl(byte[] data, String path) throws IOException{
        this.data = data;
        this.path = path;
        File file = new File(path);
        if (!file.exists()) {// 如果不存在则新建文件
            file.createNewFile();
            IOUtils.writeByteArrayToFile(data, file);
        }
    }

    @Override
    public byte[] getData() throws IOException {
        File file = new File(path);
            return IOUtils.readByteArrayFromFile(file, file.length());
    }
}
