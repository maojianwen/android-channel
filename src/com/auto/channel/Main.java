package com.auto.channel;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.tools.ant.filters.StringInputStream;


public class Main {

	public static void main(String[] args) {
		
		for(int i =0 ;i < args.length;i++) {
			System.out.println(args[i]);
		}
		
		StringBuilder buffer = new StringBuilder();
        BufferedReader br = null;
        OutputStreamWriter osw = null;
        try {

            File file_ = new File("channel_config.txt");
            if(!file_.exists()){
                System.out.println("缺少 channel_config.txt 配置文件");
                return;
            }
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file_),"UTF-8");
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String readLine = "";
            buffer.setLength(0);
            while((readLine = reader.readLine()) != null){
                buffer.append(readLine);
            }
            inputStreamReader.close();
            reader.close();
            String readTxt = buffer.toString();
            System.out.println(readTxt);
            String[] readTxts = readTxt.split(";");
            String srcPath = "";
            String keystore = "";
            String keyAlias = "";
            String content1 = "";
            String apkName = "";
            String keyPassword = "";
            String storePassword = "";
            for (String txt:readTxts){
                if(txt.contains("app_path")){
                    srcPath = txt.split("=")[1];
                }else if(txt.contains("keystore")){
                    keystore = txt.split("=")[1];
                }else if(txt.contains("keyAlias")){
                    keyAlias = txt.split("=")[1];
                }else if(txt.contains("channels")){
                    content1 = txt.split("=")[1];
                }else if(txt.contains("apkname")){
//                    apkName = txt.split("=")[1];
                }else if(txt.contains("keyPassword")){
                    keyPassword = txt.split("=")[1];
                }else if(txt.contains("storePassword")){
                    storePassword = txt.split("=")[1];
                }
            }

            File srcFile = new File(srcPath);
            if (!srcFile.exists()) {
                System.out.println("文件不存在");
                return;
            }
//            String parentPath = srcFile.getParent();    //源文件目录
            String fileName = srcFile.getName();        //源文件名称
            String prefixName = fileName.substring(0, fileName.lastIndexOf("."));
            apkName = prefixName;
            buffer.setLength(0);
            String sourcePath = buffer.
                    append(prefixName).append("/").toString();
            System.out.println("sourcePath:"+sourcePath);
            ZipUtil.unZip(srcPath, sourcePath);
            //------删除解压后的签名文件
            String signPathName = sourcePath + ZipUtil.SIGN_PATH_NAME;
            File signFile = new File(signPathName);
            if (signFile.exists()) {
                File sonFiles[] = signFile.listFiles();
                if (sonFiles != null && sonFiles.length > 0) {
                    //循环删除签名目录下的文件
                    for (File f : sonFiles) {
                        f.delete();
                    }
                }
                signFile.delete();
            }
            String[] contents = content1.split(",");
//            String[] apkNames = apkName.split(",");

            //------打包
            String targetPath = "target";
            //判断创建文件夹
            File targetFile = new File(targetPath);
            if(!targetFile.exists()){
                targetFile.mkdir();
            }
            String sing = "sing";
            File filesing = new File(targetFile,sing);
            if(!filesing.exists()){
                filesing.mkdir();
            }
            String unsing = "unsing";
            File fileunsing = new File(targetFile,unsing);
            if(!fileunsing.exists()){
                fileunsing.mkdir();
            }
            String batStr = "";
            for (int i = 0;i<contents.length;i++) {
                //------修改内容
                String content = contents[i];
//                String apkn = apkNames[i];
                String apkn = apkName+"_"+content;
                buffer.setLength(0);
                String path = buffer
                        .append(prefixName).append(ZipUtil.UPDATE_PATH_NAME).toString();
                System.out.println("path："+path);
                br = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
                while ((br.readLine()) != null) {
                    osw = new OutputStreamWriter(new FileOutputStream(path),"UTF-8");
                    osw.write(content, 0, content.length());
                    osw.flush();
                }

                ZipUtil.compress(prefixName,targetPath+"/"+unsing+"/"+apkn+"_unsin.apk");

                batStr+="jarsigner -verbose -keystore "+keystore+" -storepass "+storePassword+" -keypass "+keyPassword+" -signedjar "+targetPath+"/"+sing+"/"+apkn+".apk "+targetPath+"/"+unsing+"/"+apkn+"_unsin.apk"+" "+keyAlias+"\n";
                batStr+="java -jar apksigner.jar sign --ks "+keystore+" --ks-key-alias "+keyAlias+" --ks-pass pass:"+storePassword+" --key-pass pass:"+keyPassword+" --out "+targetPath+"/"+sing+"/"+apkn+".apk "+targetPath+"/"+sing+"/"+apkn+".apk"+"\n";

                System.out.println("jarsigner -verbose -keystore "+keystore+" -storepass "+storePassword+" -keypass "+keyPassword+" -signedjar "+targetPath+"/"+sing+"/"+apkn+".apk "+targetPath+"/"+unsing+"/"+apkn+"_unsin.apk"+" "+keyAlias);
            }
            File file = null;
            if ("windows".equals(getCurrentSystemType())) {
            	file = new File("singnature.bat");
			}else {
				file = new File("singnature.sh");
			}
             
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos,1024);
            InputStream inputStream = new StringInputStream(batStr);
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(bytes))!=-1){
                bos.write(bytes,0,len);
            }
            bos.close();
            fos.close();
            inputStream.close();
            runbat(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static String getCurrentSystemType() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("linux")>=0) {
			return "linux";
		}else if (os.indexOf("mac")>=0) {
			return "mac";
		}else if(os.indexOf("windows")>=0) {
			return "windows";
		}else {
			return "";
		}
	}

	 public static void runbat(String batName) {
		 StringBuffer cmd = new StringBuffer("");
//	        String cmd = "cmd /c start "+ batName;// pass
	        if ("windows".equals(getCurrentSystemType())) {
	        	cmd.append("cmd /c start ").append(batName);
			}else {
				cmd.append("/bin/sh ").append(batName);
//				cmd.append("chmod 777 ").append(batName).append("\n").append("/bin/sh ").append(batName);
			}
	        System.out.println(cmd.toString());
	        try {
	            Process ps = Runtime.getRuntime().exec(cmd.toString());
	            new Thread()
				{
					@Override
					public void run()
					{
						BufferedReader in = new BufferedReader(new InputStreamReader(ps.getInputStream())); 
						String line = null;
						
						try 
						{
							while((line = in.readLine()) != null)
							{
								System.out.println("output: " + line);
							}
						} 
						catch (IOException e) 
						{						
							e.printStackTrace();
						}
						finally
						{
							try 
							{
								in.close();
							} 
							catch (IOException e) 
							{
								e.printStackTrace();
							}
						}
					}
				}.start();
				
				new Thread()
				{
					@Override
					public void run()
					{
						BufferedReader err = new BufferedReader(new InputStreamReader(ps.getErrorStream())); 
						String line = null;
						
						try 
						{
							while((line = err.readLine()) != null)
							{
								System.out.println("err: " + line);
							}
						} 
						catch (IOException e) 
						{						
							e.printStackTrace();
						}
						finally
						{
							try 
							{
								err.close();
							} 
							catch (IOException e) 
							{
								e.printStackTrace();
							}
						}
					}
				}.start();
	            ps.waitFor();
	        } catch (IOException ioe) {
	            ioe.printStackTrace();
	        }
	        catch (InterruptedException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        System.out.println("done");
	    }
	
}
