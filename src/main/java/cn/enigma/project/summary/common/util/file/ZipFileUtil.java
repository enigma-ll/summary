package cn.enigma.project.summary.common.util.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author luzh
 * Create: 2019/9/2 下午6:24
 * Modified By:
 * Description:
 */
public class ZipFileUtil {

    /**
     * 解压zip文件
     *
     * @param unzipFolder 解压目录
     * @param zipFile     文件流
     * @return 解压后的所有文件
     * @throws IOException e
     */
    public static List<File> unzipFile(String unzipFolder, InputStream zipFile) throws IOException {
        File directory = PathUtil.getFolderPath(unzipFolder).toFile();
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(zipFile);
        ZipEntry zipEntry = zis.getNextEntry();
        try {
            while (zipEntry != null) {
                File newFile = newFile(directory, zipEntry.getName());
                if (zipEntry.getName().endsWith(File.separator)) {
                    if (newFile.mkdir()) {
                        zipEntry = zis.getNextEntry();
                    } else {
                        throw new IOException("创建目录失败");
                    }
                } else {
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    zipEntry = zis.getNextEntry();
                }
            }
            File[] files = directory.listFiles();
            return null != files && files.length > 0 ? Arrays.asList(files) : Collections.emptyList();
        } finally {
            try {
                zis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                zipFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static File newFile(File destinationDir, String name) throws IOException {
        File destFile = new File(destinationDir, name);

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("文件不在目录范围内：" + name);
        }
        return destFile;
    }

    public static void main(String[] args) throws IOException {
        String zipFile = "/Users/luzhihao/tmp/12345.zip";
        List<File> files = unzipFile("/Users/luzhihao/tmp/12345", Files.newInputStream(Paths.get(zipFile)));
        for (File file : files) {
            System.out.println(file.getAbsolutePath());
        }
    }
}
