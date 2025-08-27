package com.renewsim.backend.auth_service.support;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test-secure")
public class TestSecuredController {

    @GetMapping(value = "/admin", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("ok-admin");
    }

    @GetMapping(value = "/read-simulations", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasAuthority('SCOPE_read:simulations')")
    public ResponseEntity<String> readSimulations() {
        return ResponseEntity.ok("ok-scope");
    }
}



