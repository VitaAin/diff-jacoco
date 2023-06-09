package org.jacoco.core.utils;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DecimalFormat;

public class FileUtils {

    private static final String TAG = "FileUtils";

    /**
     * 文件大小单位为B
     */
    public static final int SIZE_TYPE_B = 1;

    /**
     * 文件大小单位为KB
     */
    public static final int SIZE_TYPE_KB = 2;

    /**
     * 文件大小单位为MB
     */
    public static final int SIZE_TYPE_MB = 3;

    /**
     * 文件大小单位为GB
     */
    public static final int SIZE_TYPE_GB = 4;

    public static String getFileName(String url) {
        if (StringUtils.isNullOrEmpty(url)) {
            return "";
        } else if (url.lastIndexOf("/") <= 0) {
            return url;
        } else {
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            if (StringUtils.isNullOrEmpty(fileName)) {
                return "";
            }
            return fileName;
        }
    }

    public static String getFileNameFromUrl(String url) {
        if (StringUtils.isNullOrEmpty(url)) {
            return "";
        } else if (url.lastIndexOf("/") <= 0) {
            return url;
        } else {
            String fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
            if (StringUtils.isNullOrEmpty(fileName)) {
                return "";
            } else {
                if (fileName.contains("?") && fileName.lastIndexOf("?") < fileName.length() - 1) {
                    fileName = fileName.substring(fileName.lastIndexOf("?") + 1, fileName.length());
                }

                return fileName;
            }
        }
    }

    public static void writeFile(String path, String info) {
        writeFile(new File(path), info);
    }

    public static void writeFile(File file, String info) {
        if (file == null || info == null) {
            return;
        }
        File parent = file.getParentFile();
        boolean exist = true;
        if (!parent.exists()) {
            exist = parent.mkdirs();
        }
        if (exist) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file, false);
                fos.write(info.getBytes("UTF-8"));
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static String readFile(String filepath) {
        return readFile(new File(filepath));
    }

    public static String readFile(File file) {
        ByteArrayOutputStream baos = readFileToOs(file);
        try {
            if (baos != null) {
                return baos.toString("UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ByteArrayOutputStream readFileToOs(File file) {
        if (file.isFile() && file.exists()) {
            FileInputStream fis = null;
            ByteArrayOutputStream os = null;
            try {
                fis = new FileInputStream(file);
                os = new ByteArrayOutputStream();
                int len;
                byte[] buffer = new byte[1024];
                while ((len = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                return os;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static byte[] readFileToByteArray(String path) {
        ByteArrayOutputStream baos = readFileToOs(new File(path));
        return baos == null ? null : baos.toByteArray();
    }

    public static byte[] readFileToByteArray(File file) {
        ByteArrayOutputStream baos = readFileToOs(file);
        return baos == null ? null : baos.toByteArray();
    }

    /**
     * 递归删除目录
     *
     * @param file
     */
    public static void deleteDir(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File subFile : files) {
                        if (subFile.isDirectory())
                            deleteDir(subFile);
                        else
                            subFile.delete();
                    }
                }
            }
            file.delete();
        }
    }

    /**
     * 获取文件大小
     *
     * @param file 待测量大小的文件
     * @return 文件大小
     */
    public static long getFileSize(File file) throws IOException {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                size = fis.available();
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }
        } else {
            System.out.println(TAG + " file does not exist");
        }

        return size;
    }

    /**
     * 获取文件大小按指定单位返回
     *
     * @param filePath 待测量大小的文件
     * @param sizeType 单位类型： SIZE_TYPE_B：B SIZE_TYPE_KB：KB SIZE_TYPE_MB：MB
     *                 SIZE_TYPE_GB：GB
     * @return 按指定单位返回文件大小
     * @throws IOException
     */
    public static String getFileSize(String filePath, int sizeType) throws IOException {
        File file = new File(filePath);
        long size = 0;

        if (file.isDirectory()) {
            size = getDirSize(file);
        } else {
            size = getFileSize(file);
        }

        return FormatFileSize(size, sizeType);
    }

    /**
     * 获取文件夹大小
     *
     * @param dir 待测量大小的文件夹
     * @return 文件夹的大小
     * @throws IOException
     */
    public static long getDirSize(File dir) throws IOException {
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    size += getDirSize(file);
                } else {
                    size += getFileSize(file);
                }
            }
        }

        return size;
    }

    /**
     * 根据指定的单位格式化文件大小
     *
     * @param fileSize 文件大小
     * @param sizeType 单位类型： SIZE_TYPE_B：B SIZE_TYPE_KB：KB SIZE_TYPE_MB：MB
     *                 SIZE_TYPE_GB：GB
     * @return 按指定单位格式化后的文件大小
     */
    public static String FormatFileSize(long fileSize, int sizeType) {
        String size = null;
        DecimalFormat df = new DecimalFormat("0.00");

        switch (sizeType) {
            case SIZE_TYPE_B:
                size = df.format((double) fileSize) + "B";
                break;
            case SIZE_TYPE_KB:
                size = df.format((double) fileSize / 1024) + "KB";
                break;
            case SIZE_TYPE_MB:
                size = df.format((double) fileSize / (1024 * 1024)) + "MB";
                break;
            case SIZE_TYPE_GB:
                size = df.format((double) fileSize / (1024 * 1024 * 1024)) + "GB";
                break;
            default:
                break;
        }

        return size;
    }

    /**
     * Mutate the given filename to make it valid for a FAT filesystem,
     * replacing any invalid characters with "_".
     */
    public static String buildValidFatFilename(String name) {
        if (name == null || name.isEmpty() || ".".equals(name) || "..".equals(name)) {
            return "(invalid)";
        }
        final StringBuilder res = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            final char c = name.charAt(i);
            if (isValidFatFilenameChar(c)) {
                res.append(c);
            } else {
                res.append('_');
            }
        }
        // Even though vfat allows 255 UCS-2 chars, we might eventually write to
        // ext4 through a FUSE layer, so use that limit.
        trimFilename(res, 255);
        return res.toString();
    }

    private static boolean isValidFatFilenameChar(char c) {
        if ((0x00 <= c && c <= 0x1f)) {
            return false;
        }
        switch (c) {
            case '"':
            case '*':
            case '/':
            case ':':
            case '<':
            case '>':
            case '?':
            case '\\':
            case '|':
            case 0x7F:
                return false;
            default:
                return true;
        }
    }

    private static void trimFilename(StringBuilder res, int maxBytes) {
        byte[] raw = res.toString().getBytes(Charset.forName("UTF-8"));
        if (raw.length > maxBytes) {
            maxBytes -= 3;
            while (raw.length > maxBytes) {
                res.deleteCharAt(res.length() / 2);
                raw = res.toString().getBytes(Charset.forName("UTF-8"));
            }
            res.insert(res.length() / 2, "...");
        }
    }

    public static boolean deleteFile(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        try {
            File file = new File(path);
            if (file.exists()) {
                if (!file.canWrite()) {
                    String[] args1 = {"chmod", "771", file.getPath().substring(0, file.getPath().lastIndexOf("/"))};
                    Process p1 = Runtime.getRuntime().exec(args1);
                    p1.waitFor();
                    p1.destroy();
                    String[] args2 = {"chmod", "777", file.getPath()};
                    Process p2 = Runtime.getRuntime().exec(args2);
                    p2.waitFor();
                    p2.destroy();
                }
                return file.delete();
            }
        } catch (Throwable ignore) {
        }
        return false;
    }

}
