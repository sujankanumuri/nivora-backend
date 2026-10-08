package com.nivora.api.controller;

import com.nivora.api.dto.GuidanceRequest;
import com.nivora.api.dto.GuidanceResponse;
import com.nivora.api.service.GuidanceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class GuidanceController {
    private final GuidanceService guidanceService;

    public GuidanceController(GuidanceService guidanceService) {
        this.guidanceService = guidanceService;
    }

    @PostMapping("/guidance")
    public GuidanceResponse guidance(@Valid @RequestBody GuidanceRequest request) {
        return new GuidanceResponse(
                guidanceService.getGuidance(
                        request.message(), request.previousMessages(), request.conversationHistory()));
    }
}
