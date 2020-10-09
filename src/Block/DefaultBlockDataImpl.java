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
        File file = new File(path+".data");
        IOUtils.writeByteArrayToFile(data, file);
    }

    @Override
    public byte[] getData() {
        return data;
    }
}
