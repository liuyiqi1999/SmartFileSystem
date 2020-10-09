package Utils;

import java.io.*;

public class IOUtils {
    static int BUFFER_SIZE = 8192 * 2;

    public static int writeByteArrayToFile(byte[] data, File dest) {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(dest));
            int i = 0;
            byte[] buff = new byte[BUFFER_SIZE];
            while (i < data.length) {
                bos.write(buff, 0, data[i]);//将缓存区数据写入输出流中
            }
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(bos);
        }
    }

    private static void close(OutputStream os) {
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void close(InputStream is) {
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
