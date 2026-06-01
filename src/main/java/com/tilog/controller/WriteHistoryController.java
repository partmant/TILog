package com.tilog.controller;

import com.tilog.dto.history.WriteHistoryRequest;
import com.tilog.dto.history.WriteHistoryResponse;
import com.tilog.global.response.ApiResponse;
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
    public ApiResponse<WriteHistoryResponse> recordWriteHistory(
            @Valid @RequestBody WriteHistoryRequest request
    ) {
        WriteHistoryResponse response = writeHistoryService.recordWriteHistory(request);
        return ApiResponse.success(response, "작성 이력이 기록되었습니다.");
    }
}
