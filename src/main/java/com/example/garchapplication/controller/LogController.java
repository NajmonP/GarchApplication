package com.example.garchapplication.controller;

import com.example.garchapplication.model.dto.api.AuditLogDTO;
import com.example.garchapplication.model.dto.api.PageResponse;
import com.example.garchapplication.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;

@Controller
public class LogController {

    private final AuditLogService auditLogService;

    @Autowired
    public LogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/log")
    public String log() {
        return "log";
    }

    @GetMapping("/log/data")
    @ResponseBody
    public PageResponse<AuditLogDTO> getAuditLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return PageResponse.responseFromPage(auditLogService.findBetween(from, to, page, size));
    }
}
