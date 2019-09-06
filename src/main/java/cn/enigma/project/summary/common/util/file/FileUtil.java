package cn.enigma.project.summary.common.util.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author luzh
 * Create: 2019-08-12 17:37
 * Modified By:
 * Description:
 */
public class FileUtil {

    public static Optional<String> readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.isDirectory(path) || Files.notExists(path)) {
            return Optional.empty();
        }
        return readFile(path);
    }

    /**
     * 读文件，默认utf-8编码
     *
     * @param filePath 文件路径
     * @return 字符串
     * @throws IOException e
     */
    public static Optional<String> readFile(Path filePath) throws IOException {
        return Optional.of(Files.lines(filePath, StandardCharsets.UTF_8).collect(Collectors.joining()));
    }

    /**
     * 写文件，默认utf-8编码
     *
     * @param content  文件内容
     * @param folder   文件目录
     * @param fileName 文件名
     * @throws IOException e
     */
    public static void writeFile(String content, String folder, String fileName) throws IOException {
        Path folderPath = Paths.get(folder);
        if (Files.notExists(folderPath)) {
            Files.createDirectories(folderPath);
        }
        Path filePath = Paths.get(folder + File.separator + fileName);
        BufferedWriter bufferedWriter = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8);
        bufferedWriter.write(content);
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    /**
     * 列出文件夹内所有的文件
     *
     * @param folder 文件夹路径
     * @return 文件列表
     * @throws IOException e
     */
    public static List<Path> listFiles(String folder) throws IOException {
        Path folderPath = Paths.get(folder);
        if (Files.notExists(folderPath) || Files.isRegularFile(folderPath)) {
            return Collections.emptyList();
        }
        Stream<Path> pathStream = Files.list(folderPath);
        return pathStream.filter(Files::isRegularFile).collect(Collectors.toList());
    }

    /**
     * 检查目录下某个文件是否存在
     *
     * @param folder   目录路径
     * @param fileName 文件名
     * @return 是否存在
     */
    public static boolean checkFileExist(String folder, String fileName) {
        Path filePath = Paths.get(folder + File.separator + fileName);
        return Files.exists(filePath);
    }

    /**
     * 复制文件
     *
     * @param sourceFolder 源目录
     * @param targetFolder 目标目录
     * @param fileName     文件名
     * @throws IOException e
     */
    public static void copyFile(String sourceFolder, String targetFolder, String fileName) throws IOException {
        Path source = Paths.get(sourceFolder + File.separator + fileName);
        if (!Files.exists(source)) {
            return;
        }
        Path target = Paths.get(targetFolder + File.separator + fileName);
        Path targetParent = target.getParent();
        if (Files.notExists(targetParent)) {
            Files.createDirectories(targetParent);
        }
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    public static boolean deleteDirectory(String dir) {
        Path path = Paths.get(dir);
        if (Files.exists(path)) {
            return deleteDirectory(path.toFile());
        }
        return true;
    }

    private static boolean deleteDirectory(File file) {
        if (null == file) {
            return true;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteDirectory(f);
                }
            }
        }
        file.deleteOnExit();
        return file.delete();
    }

    public static void main(String[] args) throws IOException {
        String sourcePath = "/Users/luzhihao/tmp";
        String fileName = "123/123.txt";
        String targetPath = "/Users/luzhihao/456";
        copyFile(sourcePath, targetPath, fileName);
        deleteDirectory(targetPath);
    }
}
