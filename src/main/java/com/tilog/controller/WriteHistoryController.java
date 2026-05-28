package com.tilog.controller;

import com.tilog.dto.history.WriteHistoryRequest;
import com.tilog.dto.history.WriteHistoryResponse;
import com.tilog.service.WriteHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/write-histories")
@RequiredArgsConstructor
public class WriteHistoryController {
    private final WriteHistoryService writeHistoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WriteHistoryResponse recordWriteHistory(
            @Valid @RequestBody WriteHistoryRequest request
    ) {
        return writeHistoryService.recordWriteHistory(request);
    }
}
