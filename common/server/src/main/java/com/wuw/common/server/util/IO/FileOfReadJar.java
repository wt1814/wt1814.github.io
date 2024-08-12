package com.wuw.common.server.util.IO;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.InputStream;

/**
 * todo 读取项目中文件
 */
public class FileOfReadJar {


    // todo 报错：cannot be resolved to absolute file path because it does not reside in the file system
    // https://blog.csdn.net/m0_59092234/article/details/125402107

    public static void main(String[] args) throws Exception{

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        Resource[] resources = resolver.getResources("static/data_template/biz/");

        Resource resource = resolver.getResource("static/data_template/biz/XXXX.xlsx");
        InputStream is = resource.getInputStream();


    }

}
