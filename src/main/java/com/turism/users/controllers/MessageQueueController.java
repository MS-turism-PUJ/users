package com.turism.users.controllers;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 

import com.turism.users.dtos.UserMessageDTO;
import com.turism.users.services.MessageQueueService;
@RestController
@RequestMapping("/message-queue")
public class MessageQueueController {
    @Autowired
    private MessageQueueService messageQueueService;
    @PostMapping("/send-simple-message")
    public ResponseEntity<String> sendSimpleMessage(@RequestBody String message) {
    messageQueueService.sendMessage(new UserMessageDTO(LocalDateTime.now(), "this is a test message"));
    System.out.println("Message sent: {}"+ message);
    return ResponseEntity.ok().body(String.format("Message sent: %s", message));
    }
}
