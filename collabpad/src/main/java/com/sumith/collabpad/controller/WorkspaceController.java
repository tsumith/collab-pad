package com.sumith.collabpad.controller;

import com.sumith.collabpad.dto.WorkspaceResponse;
import com.sumith.collabpad.entity.User;
import com.sumith.collabpad.service.UserService;
import com.sumith.collabpad.service.WorkspaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/workspaces")
@CrossOrigin(origins = "*") // Allow React app to fetch
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final UserService userService;

    public WorkspaceController(WorkspaceService workspaceService, UserService userService) {
        this.workspaceService = workspaceService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceResponse>> getAllWorkspaces(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.getOrCreateUserFromJwt(jwt);
        Long tenantId = user.getTenant().getId();
        
        List<WorkspaceResponse> responses = workspaceService.getWorkspacesByTenant(tenantId)
                .stream()
                .map(w -> new WorkspaceResponse(w.getId(), w.getName(), w.getDescription()))
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(responses);
    }
}
