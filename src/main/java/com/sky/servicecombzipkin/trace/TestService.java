package com.sky.servicecombzipkin.trace;

import com.sky.servicecombzipkin.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.cloud.sleuth.annotation.TagValueResolver;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
public class TestService {
    @Autowired
    TestService tranceService;
    @Autowired
    TracerService tracer;
    @Autowired
    Util util;
    @Qualifier("lazyTraceExecutor")
    @Autowired
    Executor executor;

    @NewSpan(name = "服务测试")
    public void test(@SpanTag(key = "tag", expression = "'hello' + tag + ' characters'") String s) {
        System.out.println("------------------------------->test");
        tracer.addToContext("baz", "父类的foo");
        executor.execute(() -> System.out.println("asd---------"));
        executor.execute(() -> tranceService.thread("thread----"));
//        try{
        tranceService.test1("s");
//        }catch (Exception e){
//            tracer.addTagToCurrentSpan("error",e.toString());
//        }
    }

    @NewSpan("asyncSpan")
    public void thread(@SpanTag(key = "param") String param) {
        System.out.println("thread----->" + param);
        this.tranceService.test1("oo");
    }

    @NewSpan(name = "服务测试1")
//    @SpanName("服务测试2")
//    @ContinueSpan(log = "aa")
    public void test1(@SpanTag(key = "s", expression = "'hello' + ' characters'", resolver = TagValueResolver.class) String s) {
        util.test("cesh");
        System.out.println("-------------------------->test1" + s);
        tracer.addTagToCurrentSpan("success", "成功l");
        throw new NullPointerException("空指针。。。");
    }

    @Scheduled(cron = "0/15 * * * * *")
    @NewSpan("测试定时器")
    public void schedule() {
        tranceService.test("scheme");
    }
}
