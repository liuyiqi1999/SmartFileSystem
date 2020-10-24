# SmartFileSystem 文档

本项目实现了一个有多数据副本（duplicate）和在⽂件/数据管理层⾯上分组（partition）的⽂件系统 SmartFileSystem。



## 基础设计

系统自底向上由四层构成：

- File（FileMeta）、Block（BlockMeta，BlockData）：分别存储文件信息和数据，实现持久层交互
- FileManager、BlockManager：分别管理 File、Block
- FileManagerController、BlockManagerController：分别管理 FileManager、BlockManager
- Application：应用层，实现命令行界面、异常处理等

另有一个工具类 Utils，用于实现 IO 工具方法、MD5 算法，以及保存一些常量

此外定义了一些异常，后文将进一步说明。



## 流程介绍

1. 程序启动后，生成 fileManagerController 和 blockManagerController，根据配置类参数生成 fileManager 和 blockManager，生成相关文件夹结构。自动扫描 file 和 block 文件夹，恢复文件基本信息到内存中。

2. 进入应用层，用户进入命令行后输入相应命令完成操作。

   项目支持以下命令：

   ```text
   new-file: create a new file with the given id. file_full_id will be assigned with also a fm_id. 
   read: read data of given length from the file. 
   write: write data to the file. 
   pos: show current cursor position of the file. 
   move: move the cursor of the file. 
   size: get the size of the file. 
   close: close the file. 
   set-size: set the size of the file. (extra bytes would be 0x00, shorten bytes would be abandoned from tail. )
   smart-cat: read all data from the file start from the cursor. 
   smart-write: write to a specific index of the file. 
   smart-hex: read the data of the block in hex numbers. 
   smart-copy: copy the data from src file to dest file. 
   help: look up the help. 
   ```

   具体格式如下：

   ```text
   newFile: new-file||newFile file_id\n\texample: new-file 1
   read: read file_full_id length\n\texample: read fm0-f1 6
   write: write file_full_id data\n\texample: write fm0-f1 test
   pos: pos file_full_id\n\texample: pos fm0-f1
   move: move file_full_id index cursor_place(0 for current cursor, 1 for head of the file, 2 for end of the 		    file. 2 needs a negative index. )\n\texample: move fm0-f1 0 1
   size: size file_full_id\n\texample: size fm0-f1
   close: close file_full_id\n\texample: close fm0-f1
   setSize: set-size||setSize file_full_id new_size\n\texample: set-size fm0-f1 1
   smartCat: smart-cat||smartCat file_full_id\n\texample: smart-cat fm0-f1
   smartHex: smart-hex||smartHex block_full_id\n\texample: smart-hex bm0-b1
   smartWrite: smart-write||smartWrite file_full_id index(from the head of the file) data\n\texample: smart-write fm0-f1 2 test
   smartCopy: smart-copy||smartCopy src_file_full_id dest_file_id\n\texample: smart-copy fm0-f1 fm0-f2
   help: help\n\t
   quit: quit\n\t
   ```

3. 由 `fileManager.get(file)` 调用底层方法，实现对应功能。

4. 运行中如果产生异常，向上抛出至应用层处理。



## 补充说明

由于任务书中还有一些没有说明清楚的情况，本项目按照一定合理性作了以下安排。

1. 所有的读写操作（如 read，write，smart-write，smart-copy 等）都完全依赖相应文件的光标位置，并会自动相应地移动至上一次读/写末尾处。
2. smart-copy 可以理解为对源文件全文复制，再粘贴到目标文件的光标处，而不是整个文件级别的操作。
3. 由于内存中 File 对象已经包含了它的 FileManager 信息，因此本项目中的 smart-copy 参数只有两个 File 对象。
4. 改写配置信息中的 BLOCK_SIZE 并不会影响之前文件的 BLOCK_SIZE，但新文件会有新的 BLOCK_SIZE。同时，老文件的读写功能不受影响。
5. 本项目没有使用任务书中提到的 ErrorCode 作异常处理，而是自己定义了一些异常类，后文会具体解释。
6. new-size 方法如果扩大了文件大小，多余内容用 0x00 填充；如果缩小了文件大小，自动丢弃缩小部分的数据。
7. 由于任务书中没有对文件名的要求，本项目应用层中只用形如 fm0-f1 的 file_full_id 取得文件。



## 异常处理

本系统没有参考任务书中的 ErrorCode 样例，而是选择了

1. 底层方法抛出异常，应用层处理异常并显示给用户
2. 所有抛出的异常都必须传递完整的异常信息

不使用 ErrorCode 的原因有以下两点：

1. 系统异常复杂，有很多底层的异常信息不适合直接呈现给用户，需要其他层以及应用层的进一步分析处理。
2. 增加可读性，不用对表查询异常码所代表的信息，并且同一个异常可以有不同的信息被传出。

### 实现

为了保证每个被抛出的异常都有具有可读性的信息，每个被定义的异常的构造方法都有一个必须传入的 `String message` 参数。

系统共有以下若干异常：

**BlockException：与 Block、BlockManager、BlockManagerController 相关的异常**

1. BlockCheckSumsException：文件中某个 logic block （三个 duplication）完全损坏
2. BlockConstructionFailException：创建新 Block 失败
3. BlockManagerFullException：现有的 BlockManager 已无法管理更多的 Block，文件系统无法写入更多数据
4. BlockNullException：读取的 Block 中没有数据
5. MD5Exception：计算 Block 的 MD5 值时出错
6. RecoverBlockFailException：恢复已有 Block 失败

**FileException：与 File、FileManager、FileManagerController 相关的异常**

1. FileManagerFullException：现有的 FileManager 已无法管理更多的 File，文件系统无法写入更多符合要求的文件
2. FileWriteFailException：文件写入错误
3. IllegalCursorException：光标移动错误
4. IllegalDropBlocksException：在 set-size 丢弃 Block 时产生错误
5. OverReadingFileException：read 的长度超过了文件可供读取的剩余长度
6. RecoverFileFailException：恢复已有 File 失败
7. SetFileSizeFailException：设置文件大小失败

**IDException：与 ID 相关的异常**

1. IDNullInFilenameException：在 ID 字符串中没有正常读取到 ID

**InitializeException：文件系统初始化异常**



以上只是简单概括，具体信息会在产生异常时加入构造方法并抛出，并由应用层控制输出。