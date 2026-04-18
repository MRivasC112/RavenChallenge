package com.demo.employees.controller;


import com.demo.employees.dto.response.ApiResp;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class VersionController {

    private final String appVersion;
    private final String buildTimestamp;
    private final Environment environment;

    public VersionController(
            @Value("${app.version}") String appVersion,
            @Value("${app.build-timestamp}") String buildTimestamp,
            Environment environment) {
        this.appVersion = appVersion;
        this.buildTimestamp = buildTimestamp;
        this.environment = environment;
    }

    @GetMapping("/version")
    public ResponseEntity<ApiResp<Map<String,String>>> getVersion(){
        Map<String,String> versionInfo = new LinkedHashMap<>();

        versionInfo.put("version",appVersion);
        versionInfo.put("buildTimestamp",buildTimestamp);

        String[] activeProfiles = environment.getActiveProfiles();
        String activeProfile = activeProfiles.length > 0 ? String.join(",", activeProfiles) : "default";
        versionInfo.put("activeProfile",activeProfile);

        ApiResp<Map<String,String>> response = ApiResp.success(
                HttpStatus.OK.value(),
                "Version info retrieved successfully",
                versionInfo
        );

        return ResponseEntity.ok(response);

    }

}
