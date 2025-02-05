package Utils;

import Exception.IDException.IDNullInFilenameException;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.util.Arrays;
import java.util.Optional;

public class IOUtils {
    public static void writeByteArrayToFile(byte[] data, File dest) throws IOException {
        BufferedOutputStream bos = null;
        bos = new BufferedOutputStream(new FileOutputStream(dest));
        bos.write(data, 0, data.length);//将缓存区数据写入输出流中
        bos.flush();
        if (bos != null) close(bos);
    }

    public static void writeByteArrayToFileRow(byte[] data, File dest, long row) throws IOException {
        byte[] oldData = readByteArrayFromFile(dest, dest.length());
        String[] lines = new String(oldData).split("\n");
        lines[(int) row] = new String(data);
        Optional<String> s = Arrays.stream(lines).reduce((s1, s2) -> s1 + "\n" + s2);
        writeByteArrayToFile(s.get().getBytes(), dest);
    }

    public static void insertByteArrayToFileRow(byte[] data, File dest, long row) throws IOException {
        byte[] oldData = readByteArrayFromFile(dest, dest.length());
        String[] lines = new String(oldData).split("\n");
        String[] newLines = new String[lines.length + 1];
        for (int i = 0; i < newLines.length; i++) {
            if (i < row) {
                newLines[i] = lines[i];
            } else if (i == row) {
                newLines[i] = new String(data);
            } else {
                newLines[i] = lines[i - 1];
            }
        }
        Optional<String> s = Arrays.stream(newLines).reduce((s1, s2) -> s1 + "\n" + s2);
        writeByteArrayToFile(s.get().getBytes(), dest);
    }

    public static void deleteByteArrayInFileRow(File dest, int startRow, int endRow) throws IOException{
        if(endRow<startRow) throw new IOException();
        byte[] oldData = readByteArrayFromFile(dest, dest.length());
        String[] lines = new String(oldData).split("\n");
        String[] newLines = new String[lines.length-(endRow-startRow+1)];
        System.arraycopy(lines,0,newLines,0,startRow);
        System.arraycopy(lines,endRow+1,newLines,startRow,lines.length-endRow-1);
        Optional<String> s = Arrays.stream(newLines).reduce((s1, s2) -> s1 + "\n" + s2);
        writeByteArrayToFile(s.get().getBytes(), dest);
    }

    public static byte[] readByteArrayFromFileRow(File src, int row) throws IOException{
        byte[] fullData = readByteArrayFromFile(src, src.length());
        String[] lines = new String(fullData).split("\n");
        return lines[row].getBytes();
    }

    public static void writeBytesToEndOfFile(byte[] newData, File file) throws IOException {
        byte[] oldData = Utils.IOUtils.readByteArrayFromFile(file, file.length());
        byte[] outData = null;
        if (oldData != null) outData = ((new String(oldData)) + (new String(newData))).getBytes();
        Utils.IOUtils.writeByteArrayToFile(outData, file);
    }

    public static byte[] readByteArrayFromFile(File src, long size) throws IOException {
        BufferedInputStream bis = null;
        StringBuilder output = new StringBuilder();
        long count = 0;
        int b;
        bis = new BufferedInputStream(new FileInputStream(src));
        while ((b = bis.read()) != -1) {
            output.append((char) b);
            count++;
            if (count >= size) break;
        }
        if (bis != null) close(bis);
        return output.toString().getBytes();
    }

    private static void close(OutputStream os) throws IOException {
        os.close();
    }

    private static void close(InputStream is) throws IOException {
        is.close();
    }

    public static int getIntInFileName(String str) throws IDNullInFilenameException {
        str = str.trim();
        StringBuilder str2 = new StringBuilder();
        if (str != null && !"".equals(str)) {
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) >= 48 && str.charAt(i) <= 57) {
                    str2.append(str.charAt(i));
                }
            }
        }
        if(str2.toString().equals("")) throw new IDNullInFilenameException("[IDNullInFilenameException] ");
        return Integer.parseInt(str2.toString());
    }
}
