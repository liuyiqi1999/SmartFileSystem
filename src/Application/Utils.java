package Application;

import Block.Block;
import Exception.BlockException.BlockCheckSumException;
import Exception.BlockException.BlockManagerFullException;
import Exception.BlockException.MD5Exception;
import Exception.FileException.FileWriteFailException;
import Exception.FileException.IllegalCursorException;
import Exception.FileException.OverReadingFileException;
import File.File;

import java.io.IOException;

public class Utils {
    public static void smartCat(File file) throws IOException, IllegalCursorException, MD5Exception, OverReadingFileException, BlockCheckSumException {
        file.move(0,File.MOVE_HEAD);
        byte[] data = file.read((int) file.size());
        System.out.println(new String(data));
    }

    public static void smartHex(Block block) throws BlockCheckSumException, MD5Exception, IOException {
        if(block==null){
            System.out.println("block doesn't exist. ");
            return;
        }
        byte[] data = block.read();
        String hexString;
        StringBuilder output= new StringBuilder();
        for(byte b: data){
            hexString = Integer.toHexString(b & 0xFF);
            output.append((hexString.length() == 1) ? "0" + hexString : hexString);//位数不够，高位补0
        }
        System.out.println(output.toString().trim());
    }

    public static void smartWrite(File file, int index, String data) throws IllegalCursorException, FileWriteFailException, BlockManagerFullException, MD5Exception, IOException {
        file.move(index,File.MOVE_HEAD);
        file.write(data.getBytes());
    }

    public static void smartCopy(File src, File dest) throws IllegalCursorException, FileWriteFailException, BlockManagerFullException, MD5Exception, IOException, OverReadingFileException, BlockCheckSumException {
        byte[] srcData = src.read((int)src.size());
        dest.write(srcData);
    }
}
