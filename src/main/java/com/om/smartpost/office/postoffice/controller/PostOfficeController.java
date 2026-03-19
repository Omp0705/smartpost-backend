package com.om.smartpost.office.postoffice.controller;

import com.om.smartpost.office.postoffice.dto.request.PostOfficeRequest;
import com.om.smartpost.office.postoffice.dto.response.PostOfficeResponse;
import com.om.smartpost.office.postoffice.service.PostOfficeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/post-offices")
@RequiredArgsConstructor
public class PostOfficeController {

    private final PostOfficeService postOfficeService;

    @PostMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<PostOfficeResponse> create(@Valid @RequestBody PostOfficeRequest request) {
        return ResponseEntity.ok(postOfficeService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('POSTADMIN','SUPERADMIN')")
    public ResponseEntity<List<PostOfficeResponse>> getAll() {
        return ResponseEntity.ok(postOfficeService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('POSTADMIN','SUPERADMIN')")
    public ResponseEntity<PostOfficeResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(postOfficeService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<PostOfficeResponse> update(@PathVariable UUID id, @Valid @RequestBody PostOfficeRequest request) {
        return ResponseEntity.ok(postOfficeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        postOfficeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}




