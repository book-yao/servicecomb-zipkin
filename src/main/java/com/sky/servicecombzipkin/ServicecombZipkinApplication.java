package com.sky.servicecombzipkin;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.annotation.TagValueExpressionResolver;
import org.springframework.cloud.sleuth.annotation.TagValueResolver;
import org.springframework.cloud.sleuth.instrument.async.LazyTraceExecutor;
import org.springframework.cloud.sleuth.instrument.async.TraceableExecutorService;
import org.springframework.cloud.sleuth.instrument.async.TraceableScheduledExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@SpringBootApplication
@ComponentScan(basePackages = {"com.sky"})
//@EnableScheduling
public class ServicecombZipkinApplication {


    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    /**
     * 提取器 优先高于TagValueExpressionResolver
     * @return
     */
    @Bean(name = "myCustomTagValueResolver")
    public TagValueResolver TagValueResolver(){
        return parameter -> {
            if (parameter == null) {
                return null;
            }

            if(parameter instanceof List){
                return String.format("list's size is %d",((List)parameter).size());
            }

            if(parameter instanceof Map){
                return String.format("map's size is %d",((Map)parameter).size());
            }

            return (String)parameter;
        };
    }

    /**
     * tag 值解析表达式
     * @return
     */
    @Bean
    public TagValueExpressionResolver tagValueExpressionResolver(){
        return (expression, parameter) -> {
            System.out.println(expression);
            System.out.println(parameter);
            return expression;
        };
    }

    @Autowired
    private BeanFactory beanFactory;
    @Bean("lazyTraceExecutor")
    public Executor getAsyncExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("my-executor-");
        executor.initialize();
        return new LazyTraceExecutor(this.beanFactory,executor);
    }

    @Bean("traceableExecutorService")
    public TraceableExecutorService traceableExecutorService(){
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10,50,1000, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(10));
        return new TraceableExecutorService(this.beanFactory, threadPoolExecutor);
    }

    public static void main(String[] args) {
        SpringApplication.run(ServicecombZipkinApplication.class, args);
    }

}
