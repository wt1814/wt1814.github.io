package com.wt.monitor.config;

import lombok.extern.slf4j.Slf4j;
import org.hyperic.jni.ArchNotSupportedException;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarLoader;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.*;

/**
 * @Author: wt1814
 * @Date: 2021/5/31 5:56 下午
 * @Description:
 */
@Slf4j
@Configuration
public class SigarConfig {

    static {
        initSigar();
    }

    /**
     * 初始化sigar的配置文件
     */
    public static void initSigar() {
        log.info("==initSigar==");
        SigarLoader loader = new SigarLoader(Sigar.class);
        String lib = null;

        try {
            lib = loader.getLibraryName();
            log.info("sigar lib:{}", lib);
        } catch (ArchNotSupportedException e) {
            log.error("error:", e);
        }
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:/sigar.so/" + lib);
        if (resource.exists()) {
            log.info("==exists==");
            InputStream is = null;
            BufferedOutputStream os = null;
            try {
                is = resource.getInputStream();
                File tempDir = new File("./log");
                if (!tempDir.exists()) {
                    tempDir.mkdirs();
                }
                os = new BufferedOutputStream(new FileOutputStream(new File(tempDir, lib), false));
                int lentgh = 0;
                while ((lentgh = is.read()) != -1) {
                    os.write(lentgh);
                }

                System.setProperty("org.hyperic.sigar.path", tempDir.getCanonicalPath());
            } catch (IOException e) {
                log.error("init siagr fail:", e);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException e) {
                    log.error("关闭错误:", e.getMessage());
                }
            }
        }
    }
}
