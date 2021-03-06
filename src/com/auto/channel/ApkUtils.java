package com.auto.channel;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

/**
 * Created by 83642 on 2017/8/9.
 */
public class ApkUtils {
    private void copy(InputStream inputStream, OutputStream outputStream) throws IOException{
        byte[] b = new byte[2048];
        int len = 0;
        while((len = inputStream.read(b)) != -1){
            outputStream.write(b, 0, len);
        }
    }
    /**
     * 往Apk中byte[]
     * @param apkPath
     * @param outputPath
     * @param entryName 希望在apk中显示的路径、名称
     */
    public void addByteToApk(String apkPath,String outputPath,byte[] addFileBytes,String entryName){
        File file = new File(apkPath);
        File outputFile = new File(outputPath);
        if(!file.exists()){
            System.out.println("not found "+apkPath);
            return;
        }
        if(outputFile.exists()){
            outputFile.delete();
        }
        try {
            ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputFile);

            ZipFile zipFile = new ZipFile(file);
            Enumeration<ZipArchiveEntry> enumeration = zipFile.getEntries();
            //原样拷贝
            while (enumeration.hasMoreElements()) {
                ZipArchiveEntry zipArchiveEntry =  enumeration.nextElement();
//                System.out.println(zipArchiveEntry.getName());
                if(zipArchiveEntry.getName().equals(entryName)){
                    System.out.println("Wranning! found "+entryName+" cover it !");
                    continue;
                }

                zipArchiveOutputStream.putArchiveEntry(zipArchiveEntry);
                if(!zipArchiveEntry.isDirectory()){
                    copy(zipFile.getInputStream(zipArchiveEntry), zipArchiveOutputStream);
                }
                zipArchiveOutputStream.closeArchiveEntry();
            }
            //添加文件


            ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(entryName);

            zipArchiveOutputStream.putArchiveEntry(zipArchiveEntry);
            zipArchiveOutputStream.write(addFileBytes);
            zipArchiveOutputStream.closeArchiveEntry();

            zipFile.close();
            zipArchiveOutputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void addStrToApk(String apkPath,String outputPath,String addStr,String entryName){
        try {
            addByteToApk(apkPath, outputPath, addStr.getBytes("utf-8"), entryName);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 往Apk中添加文件
     * @param apkPath
     * @param outputPath
     * @param addFilePath 文件路径
     * @param entryName 希望在apk中显示的路径、名称
     */
    public void addFileToApk(String apkPath,String outputPath,String addFilePath,String entryName){
        try {
            File addFile = new File(addFilePath);
            FileInputStream fileInputStream = new FileInputStream(addFile);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int len =0;
            byte[] buffer = new byte[2048];
            while((len = bufferedInputStream.read(buffer)) != -1){
                bos.write(buffer, 0 ,len);
            }
            bos.flush();

            bufferedInputStream.close();
            fileInputStream.close();

            addByteToApk(apkPath, outputPath, bos.toByteArray(), entryName);
        } catch (IOException e) {
            // TODO: handle exception
        }


    }

    /**
     * 解压apk
     * @param apkPath
     * @param outPath
     */
    public void unZipApk(String apkPath,String outPath){
        File file = new File(apkPath);
        if(!file.exists()){
            System.out.println("not found "+apkPath);
            return;
        }
        try {

            File outputDir = new File(outPath);
            outputDir.mkdir();

            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            ArchiveInputStream inputStream = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP, bufferedInputStream);
            ZipArchiveEntry zipArchiveEntry = null;
            try {
                while((zipArchiveEntry = (ZipArchiveEntry) inputStream.getNextEntry()) != null){

                    if(zipArchiveEntry.isDirectory()){
                        new File(outputDir,zipArchiveEntry.getName()).mkdirs();
                    }else{
                        File tempFile = new File(outputDir,zipArchiveEntry.getName());
                        if(!tempFile.getParentFile().exists()){
                            tempFile.getParentFile().mkdirs();
                        }

                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(tempFile);
                            //IOUtils.copy(inputStream, fos);
                            byte[] b = new byte[2048];
                            int len = 0;
                            while((len = inputStream.read(b)) != -1){
                                fos.write(b, 0, len);
                            }
                            fos.flush();
                        } catch (IOException e) {
                            e.printStackTrace();

                        }finally{
                            if(fos != null){
                                fos.close();
                            }
                        }

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                if(inputStream != null){
                    inputStream.close();
                }
            }

            System.out.println("unZipApk done !");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
