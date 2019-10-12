package com.sky.servicecombzipkin.control;

import com.sky.servicecombzipkin.trace.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TraceController {

    @Autowired
    TestService tranceService;

    @GetMapping("/test")
    public void traceTest(){
        tranceService.test("asd");
    }
}
