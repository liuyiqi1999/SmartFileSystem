package Block;

import Utils.IOUtils;

import java.io.File;

public class DefaultBlockMetaImpl implements BlockMeta{
    int size;
    String checksum;
    String path;

    public DefaultBlockMetaImpl(int size, String checksum, String path) {
        this.size = size;
        this.checksum = checksum;
        this.path = path;
        String data = "size: "+size+"\nchecksum: "+checksum;
        File file = new File(path+".meta");
        IOUtils.writeByteArrayToFile(data.getBytes(),file);
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
