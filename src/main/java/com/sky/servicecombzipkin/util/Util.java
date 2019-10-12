package com.sky.servicecombzipkin.util;

import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.stereotype.Component;

@Component
public class Util {

    @NewSpan(name = "static Span")
    public void test(@SpanTag(key = "ssasd") String ss){
        System.out.println("=================>"+ss);
    }
}
