package com.techstorepro.techstorepro.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminDashboardController {
    private final AdminDashboardService service;
    public AdminDashboardController(AdminDashboardService service) { this.service = service; }

    @GetMapping("/admin/resumen")
    public AdminDashboardResponse resumen() { return service.resumen(); }
}
