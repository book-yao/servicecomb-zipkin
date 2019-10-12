package com.sky.servicecombzipkin.inter;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface CreateSpan {

    String name() default "";

    String success() default "";

    String fail() default "";

    /**
     * 是否创建新的span
     * @return
     */
    boolean create() default false;

}
