package File;

import Exception.BlockException.BlockManagerFullException;
import Exception.BlockException.MD5Exception;
import Exception.FileException.FileWriteFailException;
import Exception.FileException.IllegalCursorException;
import Id.Id;
import Manager.FileManager;

import java.io.IOException;

public interface File {
    final int MOVE_CURR = 0;
    final int MOVE_HEAD = 1;
    final int MOVE_TAIL = 2;
    Id getFileId();
    FileManager getFileManager();
    byte[] read(int length) throws IOException;
    void write(byte[] b) throws BlockManagerFullException, MD5Exception, IOException, IllegalCursorException, FileWriteFailException;
    default long pos() throws IOException, IllegalCursorException {
        return move(0, MOVE_CURR);
    }
    long move(long offset, int where) throws IllegalCursorException, IOException;
    void close();
    long size() throws IOException;
    void setSize(long newSize) throws FileWriteFailException, IllegalCursorException, IOException;
}
