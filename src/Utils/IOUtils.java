package Utils;

import java.io.*;
import java.util.Arrays;

public class IOUtils {
    static int BUFFER_SIZE = 8192 * 2;

    public static void writeByteArrayToFile(byte[] data, File dest) {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(dest));
            bos.write(data, 0, data.length);//将缓存区数据写入输出流中
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) close(bos);
        }
    }

    public static void writeByteArrayToFileRow(byte[] data, File dest, int row) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(dest));
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dest)));
            String line;
            int count = 1;
            while ((line = in.readLine()) != null) {
                if (count == row) {
                    out.println(data);
                } else {
                    out.println(line);
                }
                count++;
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void writeBytesToEndOfFile(byte[] newData, File file) {
        byte[] oldData = Utils.IOUtils.readByteArrayFromFile(file, file.length());
        byte[] outData = null;
        if (oldData != null) outData = (Arrays.toString(oldData) + Arrays.toString(newData)).getBytes();
        Utils.IOUtils.writeByteArrayToFile(outData, file);
    }

    public static byte[] readByteArrayFromFile(File src, long size) {
        BufferedInputStream bis = null;
        StringBuilder output = new StringBuilder();
        try {
            bis = new BufferedInputStream(new FileInputStream(src));
            int b = 0;
            int count = 0;
            byte[] buff = new byte[BUFFER_SIZE];
            while ((b = bis.read(buff)) != -1) {
                output.append(new String(buff));
                count += BUFFER_SIZE;
                if (count >= size) break;
            }
            return output.toString().getBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (bis != null) close(bis);
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

    public static int getIntInFileName(String str){
        str = str.trim();
        String str2 = "";
        if (str != null && !"".equals(str)) {
            for (int i = 0; i < str.length(); i ++) {
                if (str.charAt(i) >= 48 && str.charAt(i) <= 57) {
                    str2 += str.charAt(i);
                }
            }
        }
        return Integer.getInteger(str2);
    }
}
