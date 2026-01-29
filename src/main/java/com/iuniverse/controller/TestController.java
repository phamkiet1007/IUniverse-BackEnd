package com.iuniverse.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Tag(name = "Test Controller", description = "Tao la chua te bong dem")
public class TestController {

    @Operation(summary = "Test", description = "still Test but in description")
    @GetMapping("/hello")
    public String test(@RequestParam(required = false) String name){
        return "Hello " + name;
    }
}
