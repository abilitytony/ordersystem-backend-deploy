package com.example.ordersystem.common.controller;

import com.example.ordersystem.common.repository.SseEmitterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/sse")
public class SseController {
    private final SseEmitterRegistry sseEmitterRegistry;
    @Autowired
    public SseController(SseEmitterRegistry sseEmitterRegistry) {
        this.sseEmitterRegistry = sseEmitterRegistry;
    }

    @GetMapping("/connect")
    public SseEmitter connect() throws IOException {
        System.out.println("connect start");
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        SseEmitter sseEmitter = new SseEmitter(60*60*1000L); //1시간 유효시간
        sseEmitterRegistry.addSseEmitter(email, sseEmitter);

        sseEmitter.send(SseEmitter.event().name("connect").data("연결완료"));
        return sseEmitter;
    }

    @GetMapping("/disconnect")
    public void disconnect() throws IOException {
        System.out.println("disconnect start");
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        sseEmitterRegistry.removeEmitter(email);
    }

}
