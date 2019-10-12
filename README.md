# servicecomb-zipkin
spring cloud sleuth集成zipkin

# 1.简介
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;微服务框架是一个分布式结构框架，一般按业务划分为多个服务单元，服务单元间会通信进行数据交互。由于服务单元数量单元多，业务复杂，如果出现了异常或者错误，很难去定位。所以微服务架构中，必须实现分布式链路追踪，去跟进一个请求到底有哪些服务参与，参与的顺序又是怎样的，从而达到每个请求的步骤清晰可见，出了问题，很快定位。
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Spring Cloud Sleuth则实现了分布式跟踪解决方案。集成Zipkin进行日志链路查看，非常方便。
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;官网地址：https://cloud.spring.io/spring-cloud-sleuth
# 2.术语
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Spring Cloud Sleuth借鉴了Dapper的术语。
**Span**:基本工作单位。例如，发送RPC是一个新的跨度，就像发送响应到RPC一样。跨度由跨度的唯一64位ID和跨度所属的跟踪的另一个64位ID标识。跨区还具有其他数据，例如描述，带有时间戳的事件，键值注释（标签），引起跨度的跨区的ID和进程ID（通常为IP地址）。
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;跨度可以启动和停止，并且可以跟踪其时序信息。创建跨度后，您必须在将来的某个时间点将其停止。

**Trace**:一组形成树状结构的跨度。例如，如果您运行分布式大数据存储，则跟踪可能是由PUT请求形成的。

**Annotation**:用来及时记录一个事件的，一些核心注解用来定义一个请求的开始和结束 。这些注解包括以下：
cs：Client Sent -客户端发送一个请求，这个注解描述了这个Span的开始
sr：Server Received -服务端获得请求并准备开始处理它，如果将其sr减去cs时间戳便可得到网络传输的时间。
ss：Server Sent （服务端发送响应）–该注解表明请求处理的完成(当请求返回客户端)，如果ss的时间戳减去sr时间戳，就可以得到服务器请求的时间。
cr：Client Received （客户端接收响应）-此时Span的结束，如果cr的时间戳减去cs时间戳便可以得到整个请求所消耗的时间。
# 3.架构
下图显示了Span和Trace以及Zipkin在系统中的追踪：
![架构图](https://img-blog.csdnimg.cn/20191011131456178.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzODY1ODYz,size_16,color_FFFFFF,t_70)
# 4.实战
##  4.1.Zipkin服务端
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;从github上git clone项目，本地运行。后续可以直接下载jar运行，可另行百度。
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;git地址：https://github.com/openzipkin/zipkin.git
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Zipkin接收spring cloud sleuth传过来的追踪数据默认是保存在内存中，直接启动即可。
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;若需要保存到elasticsearch，需部署启动elasticsearch后，在zipkin-server中配置如下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191011131624647.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzODY1ODYz,size_16,color_FFFFFF,t_70)
1.新增配置文件zipkin-server-param.yml，内容如下：
```yml
STORAGE_TYPE: elasticsearch
ES_HOSTS: localhost:9200
```
2.在zipkin-server.yml中增加配置：
```yml
spring.profiles.include: shared,param
```
3.重新启动，则数据保存至elasticsearch中。
启动成功后，浏览器访问http://127.0.0.1:9411即可访问追踪页面
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191011131753298.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzODY1ODYz,size_16,color_FFFFFF,t_70)
## 4.2.spring boot服务
### 4.2.1.构建一个测试的spring boot服务，pom.xml中引用依赖
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
```
### 4.2.2配置文件application.yml中内容分如下
```yml
spring:
  sleuth:
    enabled: true
    trace-id128: true
    sampler:
      probability: 1.0
  application:
    name: teseService

  zipkin:
    service:
      name: myService
#  zipkin:
#    base-url: http://localhost:9411
logging:
  level:
    org.springframework.web: debug
server:
  port: 8080
```
### 4.2.3主程序类
```java
@SpringBootApplication
@ComponentScan(basePackages = {"com.sky"})
@EnableScheduling
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
```
**注意**：Spring cloud sleuth要追踪日志，需要时spring管理的bean才被追踪到，所以使用过程中，要被追踪的操作需要注册bean到spring中。
### 4.2.4创建一个TraceService.java，负责追踪的一些操作全封装在这个bean中
```java
import brave.Span;
import brave.Tracer;
import brave.propagation.ExtraFieldPropagation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TracerService {
    @Autowired(required = false)
    private Tracer tracer;
    public Span currentSpan() {
        if (tracer == null) {
            return null;
        }
        return tracer.currentSpan();
    }

    public void addTagToCurrentSpan(String key, String value) {
        Span span = this.currentSpan();
        if (span == null) {
            return;
        }
//        span.tag("baz",
//                ExtraFieldPropagation.get(span.context(), "foo"));
        span.tag(key, value);
    }

    public void addToContext(String key,String value){
        Span span = this.currentSpan();
        if(span == null){
            return;
        }
        ExtraFieldPropagation.set(span.context(), key, value);
    }

    public Tracer tracer(){
        return tracer;
    }
}
```
**注意:**@Autowired(required = false) private Tracer tracer；
以免关闭功能时spring.sleuth.enabled=false bean注入报错。
### 4.2.5测试服务TestService.java
```java
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
        tranceService.test1("s");
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
```
调用本类中的方法时，需在本类中注入@Autowired TestService tranceService;
在通过tranceService.test("");这样，才会被追踪到span中。

支持@Scheduled方式的定时器、LazyTraceExecutor方式的线程池、bean方法调用信息追踪。
@NewSpan是在tracer中新建一个span，@ContinueSpan是沿用本span。

追踪信息会异步传输到zipkin服务端保存，直接在zipkin-ui中查看追踪信息。
## 4.3.Zipkin展示
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191011132608690.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzODY1ODYz,size_16,color_FFFFFF,t_70)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191011132616656.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191011132622670.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzODY1ODYz,size_16,color_FFFFFF,t_70)
