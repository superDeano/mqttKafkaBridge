package com.deano.mqttkafkabridge.controller;

import com.deano.mqttkafkabridge.component.Bridge;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("bridge")
public class BridgeController {

    @GetMapping("/health")
    public ResponseEntity<String> getHealth() {
        return Bridge.testForHealth();
    }

    @GetMapping("/reconnectToMqtt")
    public ResponseEntity<String> reconnectToMqtt() {
        return Bridge.reconnectToMqttBroker();
    }

}
