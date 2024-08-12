package com.wuw.common.server.controller;

import com.wuw.common.api.apiResult.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@RestController
@RequestMapping("fileApi")
@Slf4j
public class UploadFileController {


    /**
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "uploadFile")
    public String uploadFile(MultipartHttpServletRequest request, HttpServletResponse response) throws Exception {

        String fileDownUrl = "";

        MultipartFile file = request.getFile("file"); // 文件
        String fileCommonRequest = request.getParameter("fileCommonRequestVo");


        String fileOriginalName = ""; //原名称
        String fileName = "";  //新名称
        String storePath = ""; //路径+
        String tempPath = "";
        try {
            fileOriginalName=file.getOriginalFilename();
            tempPath = File.separator+"";

            storePath=""+ File.separator+tempPath;
            fileName = ""+fileOriginalName.substring(fileOriginalName.indexOf("."));

            File filePath=new File(storePath);
            File destFile = new File(filePath.getAbsolutePath(), fileName); // todo 绝对路径
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
            // todo BIO方式上传
            //file.transferTo(destFile);

            FileInputStream in = (FileInputStream) file.getInputStream();
            FileOutputStream out =  new FileOutputStream(new File(filePath.getAbsolutePath()+"/"+fileName));
            try {
                FileChannel fcIn = in.getChannel();
                FileChannel fcOut = out.getChannel();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                while (true) {
                    buffer.clear();
                    int r = fcIn.read(buffer);
                    if (r == -1) {
                        break;
                    }
                    buffer.flip();
                    fcOut.write(buffer);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("上传失败：{}",e);

            } finally {
                in.close();
                out.close();
            }

        }catch (Exception e){
            log.error("上传失败：{}",e);

        }

        // todo 拼接下载地址，使用nginx做图片服务器
        fileDownUrl  = "";
        return fileDownUrl;
    }

}
