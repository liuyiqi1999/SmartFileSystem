package Application;

import Block.Block;
import Controller.BlockManagerController;
import Controller.DefaultBlockManagerControllerImpl;
import Controller.DefaultFileManagerControllerImpl;
import Controller.FileManagerController;
import Manager.BlockManager;
import Utils.*;
import Exception.*;

import Exception.BlockException.*;
import Exception.FileException.*;
import Exception.IDException.*;
import File.File;
import Id.IdImplFactory;
import Manager.DefaultBlockManagerImpl;
import Manager.FileManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Application {
    public static BlockManagerController blockManagerController;
    public static FileManagerController fileManagerController;
    private static Map<String, String> commandHelpers;
    private static BufferedReader reader;

    public static void main(String[] args) {
        try {
            initialize();
        } catch (InitializeException e) {
            System.out.println("[InitializeException] system initializing failed. \n" + e.getMessage());
        }

        System.out.println("Welcome to SmartFileSystem! ");

        boolean shutDown = false;
        reader = new BufferedReader(new InputStreamReader(System.in));

        while (!shutDown) {
            System.out.print("SFS>: ");
            String input;
            try {
                input = reader.readLine();
            } catch (IOException e) {
                System.out.println("[ReadInputFailException] reading command failed. ");
                shutDown();
                shutDown = true;
                continue;
            }

            String[] commandWithArgs = input.split(" ");
            switch (commandWithArgs[0]) {
                case "quit":
                    shutDown();
                    shutDown = true;
                    break;
                case "help":
                    help(commandWithArgs);
                    break;
                case "new-file":
                case "newFile":
                    newFile(commandWithArgs);
                    break;
                case "read":
                    read(commandWithArgs);
                    break;
                case "write":
                    write(commandWithArgs);
                    break;
                case "pos":
                    pos(commandWithArgs);
                    break;
                case "move":
                    move(commandWithArgs);
                    break;
                case "size":
                    size(commandWithArgs);
                    break;
                case "close":
                    close(commandWithArgs);
                    break;
                case "setSize":
                case "set-size":
                    setSize(commandWithArgs);
                    break;
                case "smartCat":
                case "smart-cat":
                    smartCat(commandWithArgs);
                    break;
                case "smartHex":
                case "smart-hex":
                    smartHex(commandWithArgs);
                    break;
                case "smartWrite":
                case "smart-write":
                    smartWrite(commandWithArgs);
                    break;
                case "smartCopy":
                case "smart-copy":
                    smartCopy(commandWithArgs);
                    break;
                default:
                    defaultInvalidCommandOutput();
                    printHelp();
                    break;
            }
        }
    }

    private static void initialize() throws InitializeException {
        try {
            fileManagerController = DefaultFileManagerControllerImpl.getInstance();
            blockManagerController = DefaultBlockManagerControllerImpl.getInstance();
        } catch (IOException | IDNullInFilenameException | BlockNullException | RecoverFileFailException | RecoverBlockFailException e) {
            throw new InitializeException(e.getMessage());
        }

        commandHelpers = new HashMap<>();
        commandHelpers.put("newFile", "new-file||newFile file_id\n\texample: new-file 1");
        commandHelpers.put("read", "read file_full_id length\n\texample: read fm0-f1 6");
        commandHelpers.put("write", "write file_full_id data\n\texample: write fm0-f1 test");
        commandHelpers.put("pos", "pos file_full_id\n\texample: pos fm0-f1");
        commandHelpers.put("move", "move file_full_id index cursor_place(0 for current cursor, 1 for head of the file, 2 for end of the file. 2 needs a negative index. )\n\texample: move fm0-f1 0 1");
        commandHelpers.put("size", "size file_full_id\n\texample: size fm0-f1");
        commandHelpers.put("close", "close file_full_id\n\texample: close fm0-f1");
        commandHelpers.put("setSize", "set-size||setSize file_full_id new_size\n\texample: set-size fm0-f1 1");
        commandHelpers.put("smartCat", "smart-cat||smartCat file_full_id\n\texample: smart-cat fm0-f1");
        commandHelpers.put("smartHex", "smart-hex||smartHex block_full_id\n\texample: smart-hex bm0-b1");
        commandHelpers.put("smartWrite", "smart-write||smartWrite file_full_id index(from the head of the file) data\n\texample: smart-write fm0-f1 2 test");
        commandHelpers.put("smartCopy", "smart-copy||smartCopy src_file_full_id dest_file_id\n\texample: smart-copy fm0-f1 fm0-f2");
        commandHelpers.put("help", "help\n\t");
        commandHelpers.put("quit", "quit\n\t");
    }

    private static void printCommandHelper(String command) {
        System.out.println(command + ": " + commandHelpers.get(command));
    }

    private static void printHelp() {
        System.out.println("commands: \n" +
                "\tnew-file: create a new file with the given id. file_full_id will be assigned with also a fm_id. \n" +
                "\tread: read data of given length from the file. \n" +
                "\twrite: write data to the file. \n" +
                "\tpos: show current cursor position of the file. \n" +
                "\tmove: move the cursor of the file. \n" +
                "\tsize: get the size of the file. \n" +
                "\tclose: close the file. \n" +
                "\tset-size: set the size of the file. (extra bytes would be 0x00, shorten bytes would be abandoned from tail. )\n" +
                "\tsmart-cat: read all data from the file start from the cursor. \n" +
                "\tsmart-write: write to a specific index of the file. \n" +
                "\tsmart-hex: read the data of the block in hex numbers. \n" +
                "\tsmart-copy: copy the data from src file to dest file. \n" +
                "\thelp: look up the help. ");
    }

    private static void defaultInvalidCommandOutput() {
        System.out.println("invalid command, use -help for help");
    }

    private static void invalidCommandOutput(String message) {
        System.out.println(message + "use help for help. ");
    }

    private static void shutDown() {
        try {
            reader.close();
        } catch (IOException e) {
            System.out.println("[ShutDownException] closing input reader failed. ");
        }
    }

    private static File getFileWithFullId(String fileFullId, String command) {
        int fmId;
        int fId;
        try {
            fmId = IOUtils.getIntInFileName(fileFullId.split("-")[0]);
            fId = IOUtils.getIntInFileName(fileFullId.split("-")[1]);
        } catch (IDNullInFilenameException e) {
            invalidCommandOutput("invalid file_full_id format. " + e.getMessage() + " ");
            printCommandHelper(command);
            return null;
        }

        FileManager fileManager = fileManagerController.getFileManager(IdImplFactory.getIdWithIndex(FileManager.class, fmId));
        File file = fileManager.getFile(IdImplFactory.getIdWithIndex(File.class, fId));

        return file;
    }

    private static void newFile(String[] commandWithArgs) {
        if (commandWithArgs.length != 2) {
            printCommandHelper("newFile");
            defaultInvalidCommandOutput();
            return;
        }

        String fileId = commandWithArgs[1];
        int id;
        try {
            id = Integer.parseInt(fileId);
        } catch (NumberFormatException e) {
            invalidCommandOutput("invalid file_id format. " + e.getMessage() + " ");
            printCommandHelper("newFile");
            return;
        }

        File file = null;
        try {
            file = fileManagerController.assignFile(id);
        } catch (FileManagerFullException e) {
            invalidCommandOutput(e.getMessage());
            printCommandHelper("newFile");
            return;
        }

        if (file == null) {
            invalidCommandOutput("creating file failed. ");
            printCommandHelper("newFile");
            return;
        }

        System.out.println(file.getFileManager().getId().toString() + "-" + file.getFileId().toString());
    }

    private static void read(String[] commandWithArgs) {
        if (commandWithArgs.length != 3) {
            printCommandHelper("read");
            defaultInvalidCommandOutput();
            return;
        }

        String fileFullId = commandWithArgs[1];

        String lengthStr = commandWithArgs[2];
        int length;
        try {
            length = Integer.parseInt(lengthStr);
        } catch (NumberFormatException e) {
            invalidCommandOutput("invalid length format. " + e.getMessage() + " ");
            printCommandHelper("read");
            return;
        }

        File file = getFileWithFullId(fileFullId, "read");
        if (file == null) {
            invalidCommandOutput("file doesn't exist. ");
            return;
        }

        byte[] data;
        try {
            data = file.read(length);
        } catch (IOException | IllegalCursorException | MD5Exception e) {
            invalidCommandOutput("reading file failed. ");
            return;
        } catch (OverReadingFileException e) {
            invalidCommandOutput("reading length is longer than file rest size. " + e.getMessage() + " ");
            return;
        } catch (BlockCheckSumException e) {
            invalidCommandOutput("file may be corrupted. " + e.getMessage() + " ");
            return;
        }

        String output = new String(data);
        if (output.length() != length) {
            invalidCommandOutput("reading length is longer than file rest size. ");
            return;
        }

        System.out.println(output);
    }

    private static void write(String[] commandWithArgs) {
        if (commandWithArgs.length < 3) {
            printCommandHelper("write");
            defaultInvalidCommandOutput();
            return;
        }

        String fileFullId = commandWithArgs[1];
        File file = getFileWithFullId(fileFullId, "write");
        if (file == null) {
            invalidCommandOutput("file doesn't exist. ");
            return;
        }

        StringBuilder data = new StringBuilder();
        for (int i = 2; i < commandWithArgs.length; i++) {
            data.append(commandWithArgs[i]);
            if (i != commandWithArgs.length - 1) {
                data.append(" ");
            }
        }

        try {
            file.write(data.toString().getBytes());
        } catch (BlockManagerFullException e) {
            invalidCommandOutput("this system cannot save more data. " + e.getMessage() + " ");
            return;
        } catch (MD5Exception | IOException | IllegalCursorException | FileWriteFailException e) {
            invalidCommandOutput("writing to file failed. " + e.getMessage() + " ");
            return;
        }
    }

    private static void pos(String[] commandWithArgs) {
        if (commandWithArgs.length != 2) {
            printCommandHelper("pos");
            defaultInvalidCommandOutput();
            return;
        }

        String fileFullId = commandWithArgs[1];
        File file = getFileWithFullId(fileFullId, "pos");
        if (file == null) {
            invalidCommandOutput("file doesn't exist. ");
            return;
        }

        try {
            System.out.println(file.pos());
        } catch (IOException e) {
            invalidCommandOutput("finding cursor failed. " + e.getMessage() + " ");
            return;
        } catch (IllegalCursorException e) {
            invalidCommandOutput("illegal cursor location. " + e.getMessage() + " ");
            return;
        }
    }

    private static void move(String[] commandWithArgs) {
        if (commandWithArgs.length != 4) {
            printCommandHelper("move");
            defaultInvalidCommandOutput();
            return;
        }

        String fileFullId = commandWithArgs[1];
        File file = getFileWithFullId(fileFullId, "move");
        if (file == null) {
            invalidCommandOutput("file doesn't exist. ");
            return;
        }

        int offset;
        int where;
        try {
            offset = Integer.parseInt(commandWithArgs[2]);
            where = Integer.parseInt(commandWithArgs[3]);
        } catch (NumberFormatException e) {
            invalidCommandOutput("invalid argument format. " + e.getMessage() + " ");
            printCommandHelper("move");
            return;
        }

        try {
            file.move(offset, where);
        } catch (IllegalCursorException e) {
            invalidCommandOutput("illegal cursor location. " + e.getMessage() + " ");
            return;
        } catch (IOException e) {
            invalidCommandOutput("moving cursor failed. " + e.getMessage() + " ");
            return;
        }
    }

    private static void size(String[] commandWithArgs) {
        if (commandWithArgs.length != 2) {
            printCommandHelper("size");
            defaultInvalidCommandOutput();
            return;
        }

        String fileFullId = commandWithArgs[1];
        File file = getFileWithFullId(fileFullId, "size");
        if (file == null) {
            invalidCommandOutput("file doesn't exist. ");
            return;
        }

        try {
            System.out.println(file.size());
        } catch (IOException e) {
            invalidCommandOutput("getting file size failed. " + e.getMessage() + " ");
            return;
        }
    }

    private static void close(String[] commandWithArgs) {
        if (commandWithArgs.length != 2) {
            printCommandHelper("close");
            defaultInvalidCommandOutput();
            return;
        }

        String fileFullId = commandWithArgs[1];
        File file = getFileWithFullId(fileFullId, "close");
        if (file == null) {
            invalidCommandOutput("file doesn't exist. ");
            return;
        }

        file.close();
    }

    private static void setSize(String[] commandWithArgs) {
        if (commandWithArgs.length != 3) {
            printCommandHelper("setSize");
            defaultInvalidCommandOutput();
            return;
        }

        String fileFullId = commandWithArgs[1];
        File file = getFileWithFullId(fileFullId, "setSize");
        if (file == null) {
            invalidCommandOutput("file doesn't exist. ");
            return;
        }

        String newSizeStr = commandWithArgs[2];
        long newSize;
        try {
            newSize = Long.parseLong(newSizeStr);
        } catch (NumberFormatException e) {
            invalidCommandOutput("invalid newSize format. " + e.getMessage() + " ");
            printCommandHelper("move");
            return;
        }

        try {
            file.setSize(newSize);
        } catch (IllegalDropBlocksException | MD5Exception | SetFileSizeFailException | FileWriteFailException | IllegalCursorException | IOException e) {
            invalidCommandOutput("writing file failed. " + e.getMessage() + " ");
            return;
        } catch (BlockCheckSumException e) {
            invalidCommandOutput("file is corrupted. " + e.getMessage() + " ");
            return;
        }
    }

    private static void smartCat(String[] commandWithArgs) {
        if (commandWithArgs.length != 2) {
            printCommandHelper("smartCat");
            defaultInvalidCommandOutput();
            return;
        }

        String fileFullId = commandWithArgs[1];
        File file = getFileWithFullId(fileFullId, "smartCat");
        if (file == null) {
            invalidCommandOutput("file doesn't exist. ");
            return;
        }

        try {
            Utils.smartCat(file);
        } catch (IOException | IllegalCursorException | MD5Exception | OverReadingFileException e) {
            invalidCommandOutput("reading file failed. " + e.getMessage() + " ");
            return;
        } catch (BlockCheckSumException e) {
            invalidCommandOutput("file is corrupted. " + e.getMessage() + " ");
            return;
        }
    }

    private static void smartHex(String[] commandWithArgs) {
        if (commandWithArgs.length != 2) {
            printCommandHelper("smartHex");
            defaultInvalidCommandOutput();
            return;
        }

        String blockFullId = commandWithArgs[1];
        int bmId;
        int bId;
        try {
            bmId = IOUtils.getIntInFileName(blockFullId.split("-")[0]);
            bId = IOUtils.getIntInFileName(blockFullId.split("-")[1]);
        } catch (IDNullInFilenameException e) {
            invalidCommandOutput("invalid file_full_id format. " + e.getMessage() + " ");
            printCommandHelper("smartHex");
            return;
        }

        BlockManager blockManager = blockManagerController.getBlockManager(IdImplFactory.getIdWithIndex(BlockManager.class, bmId));
        Block block = blockManager.getBlock(IdImplFactory.getIdWithIndex(Block.class, bId));

        try {
            Utils.smartHex(block);
        } catch (BlockCheckSumException e) {
            invalidCommandOutput("block is corrupted. " + e.getMessage() + " ");
            return;
        } catch (MD5Exception | IOException e) {
            invalidCommandOutput("reading block failed. " + e.getMessage() + " ");
            return;
        }
    }

    private static void smartWrite(String[] commandWithArgs) {
        if (commandWithArgs.length < 4) {
            printCommandHelper("smartWrite");
            defaultInvalidCommandOutput();
            return;
        }

        String fileFullId = commandWithArgs[1];
        File file = getFileWithFullId(fileFullId, "smartWrite");
        if (file == null) {
            invalidCommandOutput("file doesn't exist. ");
            return;
        }

        String indexStr = commandWithArgs[2];
        int index;
        try {
            index = Integer.parseInt(indexStr);
        } catch (NumberFormatException e) {
            invalidCommandOutput("invalid index format. " + e.getMessage() + " ");
            printCommandHelper("smartWrite");
            return;
        }

        StringBuilder data = new StringBuilder();
        for (int i = 3; i < commandWithArgs.length; i++) {
            data.append(commandWithArgs[i]);
            if (i != commandWithArgs.length - 1) {
                data.append(" ");
            }
        }

        try {
            Utils.smartWrite(file, index, data.toString());
        } catch (IllegalCursorException e) {
            invalidCommandOutput("illegal cursor location. " + e.getMessage() + " ");
            return;
        } catch (FileWriteFailException | MD5Exception | IOException e) {
            invalidCommandOutput("writing file failed. " + e.getMessage() + " ");
            return;
        } catch (BlockManagerFullException e) {
            invalidCommandOutput("this system cannot save more data. " + e.getMessage() + " ");
            return;
        }
    }

    private static void smartCopy(String[] commandWithArgs) {
        if (commandWithArgs.length != 3) {
            printCommandHelper("smartCopy");
            defaultInvalidCommandOutput();
            return;
        }

        String srcFullId = commandWithArgs[1];
        File src = getFileWithFullId(srcFullId, "smartCopy");
        if (src == null) {
            invalidCommandOutput("src doesn't exist. ");
            return;
        }

        String destFullId = commandWithArgs[2];
        File dest = getFileWithFullId(destFullId, "smartCopy");
        if (dest == null) {
            invalidCommandOutput("dest doesn't exist. ");
            return;
        }

        try {
            Utils.smartCopy(src, dest);
        } catch (IllegalCursorException e) {
            invalidCommandOutput("illegal cursor location. " + e.getMessage() + " ");
            return;
        } catch (FileWriteFailException | OverReadingFileException | MD5Exception | IOException e) {
            invalidCommandOutput("copying file failed. " + e.getMessage() + " ");
        } catch (BlockManagerFullException e) {
            invalidCommandOutput("this system cannot save more data. " + e.getMessage() + " ");
            return;
        } catch (BlockCheckSumException e) {
            invalidCommandOutput("file is corrupted. "+e.getMessage()+" ");
            return;
        }
    }

    private static void help(String[] commandWithArgs){
        if (commandWithArgs.length != 1) {
            printCommandHelper("help");
            defaultInvalidCommandOutput();
            return;
        }

        printHelp();
    }

}
