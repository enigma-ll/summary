package cn.enigma.project.summary.common.util.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author luzh
 * Create: 2019/9/2 下午7:13
 * Modified By:
 * Description:
 */
public class PathUtil {

    /**
     * 获取目录对应的 Path 对象（实际目录不存在的时候默认创建目录）
     *
     * @param folderPath 目录路径
     * @return path
     * @throws IOException e
     */
    public static Path getFolderPath(String folderPath) throws IOException {
        Path path = Paths.get(folderPath);
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new IOException("创建目录失败：" + e.getMessage());
            }
        }
        return path;
    }
}
