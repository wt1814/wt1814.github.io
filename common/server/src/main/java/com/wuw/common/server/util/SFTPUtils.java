package com.wuw.common.server.util;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Slf4j
public class SFTPUtils {

    public static void main(String[] args) {

        SFTPUtils sftpUtils = new SFTPUtils();
        // 建立连接
        ChannelSftp channelSftp = sftpUtils.channelSftp();



    }



    /**
     * 连接
     * @return
     */
    private ChannelSftp channelSftp(){
        ChannelSftp channelSftp = null;
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession("username", "host", 22);
            session.setPassword("password");
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
        }catch (Exception e){
            log.error("建立连接：{}",e);
        }
        return channelSftp;
    }

    /**
     * 上传整个目录
     * @param sourcePath
     * @param destinationPath
     * @param channelSftp
     * @throws SftpException
     * @throws FileNotFoundException
     */
    private void recursiveFolderUpload(String sourcePath, String destinationPath, ChannelSftp channelSftp)
            throws SftpException, FileNotFoundException {

        File sourceFile = new File(sourcePath);
        if (sourceFile.isFile()) {
            // copy if it is a file
            channelSftp.cd(destinationPath);
            if (!sourceFile.getName().startsWith("."))
                channelSftp.put(new FileInputStream(sourceFile), sourceFile.getName(), ChannelSftp.OVERWRITE);
        } else {
            File[] files = sourceFile.listFiles();
            if (files != null && !sourceFile.getName().startsWith(".")) {
                channelSftp.cd(destinationPath);
                SftpATTRS attrs = null;
                // check if the directory is already existing
                try {
                    attrs = channelSftp.stat(destinationPath + "/" + sourceFile.getName());
                } catch (Exception e) {
                    System.out.println(destinationPath + "/" + sourceFile.getName() + " not found");
                }
                // else create a directory
                if (attrs != null) {
                    System.out.println("Directory exists IsDir=" + attrs.isDir());
                } else {
                    System.out.println("Creating dir " + sourceFile.getName());
                    channelSftp.mkdir(sourceFile.getName());
                }
                for (File f : files) {
                    recursiveFolderUpload(f.getAbsolutePath(), destinationPath + "/" + sourceFile.getName(), channelSftp);
                }

            }
        }
    }


    /**
     * 下载整个目录
     * @param channel
     * @param remotePath
     * @param localPath
     * @throws SftpException
     */
    private void downloadDirectory(ChannelSftp channel, String remotePath, String localPath) throws SftpException {
        channel.cd(remotePath);
        Vector<ChannelSftp.LsEntry> entries = channel.ls("*");

        for (ChannelSftp.LsEntry entry : entries) {
            if (entry.getAttrs().isDir()) {
                if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")) {
                    String newRemotePath = remotePath + "/" + entry.getFilename();
                    String newLocalPath = localPath + "/" + entry.getFilename();
                    new File(newLocalPath).mkdirs();
                    downloadDirectory(channel, newRemotePath, newLocalPath);
                }
            } else {
                String filename = localPath + "/" + entry.getFilename();
                channel.get(remotePath + "/" + entry.getFilename(), filename);
            }
        }
    }


    /**
     * 执行linux命令
     * @param session  表示传递连接对话
     * @param commands  表示传递命令集合
     * @return
     */
    public List<String> getCmdResult(Session session , List<String> commands){
        //用来存放命令的返回值
        List<String> cmdResult = new ArrayList<>();
        for (String command : commands) {
            Channel channel = null;
            try {
                //创建执行通道
                channel = session.openChannel("exec");
                //设置命令
                ((ChannelExec) channel).setCommand(command);
                //连接通道
                channel.connect();
                //读取通道的输出
                InputStream in = channel.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                //存放命令的执行结果，如果结果有很多行，则每次line的值不同
                String line;
                //lines用来拼接line结果
                StringBuffer lines = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    //去除头尾的空格
                    line.trim();
                    lines = lines.append(line);
                }
                //如果命令执行没有返回值，则直接输出没有返回值
                if (String.valueOf(lines).equals("")){
                    cmdResult.add("命令["+command+"]执行成功,但没有返回值");
                }else {
                    //否则将每行返回直接存入到list中
                    cmdResult.add(String.valueOf(lines));
                }
                reader.close();
                channel.disconnect();
            } catch (JSchException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return cmdResult;
    }



}
