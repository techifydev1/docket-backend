package com.qudus.docket_backend.health;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/healthz")
public class HealthController {

    @GetMapping
    public ResponseEntity<Void> getHealth() {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
