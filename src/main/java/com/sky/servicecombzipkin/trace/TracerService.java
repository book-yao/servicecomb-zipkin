package com.sky.servicecombzipkin.trace;

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
