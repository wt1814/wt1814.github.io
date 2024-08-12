package com.wuw.jenkinstest.test;



import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jenkinstest/test")
@Slf4j
public class TestController {



    @GetMapping("/test1")
    public String test1(){

        log.info("I'm test1");
        return "I'm test1";
    }


}
