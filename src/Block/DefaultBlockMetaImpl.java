package Block;

import Utils.IOUtils;

import java.io.File;
import java.io.IOException;

public class DefaultBlockMetaImpl implements BlockMeta {
    int size;
    String checksum;
    String path;

    public DefaultBlockMetaImpl(int size, String checksum, String path) throws IOException {
        this.size = size;
        this.checksum = checksum;
        this.path = path;
        String data = size + "\n" + checksum;
        File file = new File(path);
        if (!file.exists()) {// 若不存在则为新建，若存在则只是恢复到内存
            file.createNewFile();
            IOUtils.writeByteArrayToFile(data.getBytes(), file);
        }
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getCheckSum() {
        return checksum;
    }
}
