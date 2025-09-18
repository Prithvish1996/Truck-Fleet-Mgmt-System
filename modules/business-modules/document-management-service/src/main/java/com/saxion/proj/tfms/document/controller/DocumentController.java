package com.saxion.proj.tfms.document.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private Map<Long, Map<String, Object>> documents = new HashMap<>();
    private Long idCounter = 1L;

    @GetMapping
    public List<Map<String, Object>> getAllDocuments() {
        return new ArrayList<>(documents.values());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getDocumentById(@PathVariable Long id) {
        return documents.get(id);
    }

    @PostMapping
    public Map<String, Object> uploadDocument(@RequestBody Map<String, Object> documentRequest) {
        Map<String, Object> document = new HashMap<>();
        document.put("id", idCounter);
        document.put("name", documentRequest.get("name"));
        document.put("type", documentRequest.get("type"));
        document.put("size", documentRequest.get("size"));
        document.put("uploadedAt", LocalDateTime.now());
        document.put("status", "UPLOADED");

        documents.put(idCounter, document);
        idCounter++;

        return document;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteDocument(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        if (documents.containsKey(id)) {
            documents.remove(id);
            response.put("message", "Document deleted successfully");
            response.put("status", "SUCCESS");
        } else {
            response.put("message", "Document not found");
            response.put("status", "ERROR");
        }
        return response;
    }

    @GetMapping("/types")
    public Map<String, Object> getDocumentTypes() {
        Map<String, Object> response = new HashMap<>();
        response.put("types", Arrays.asList(
            "DRIVER_LICENSE", "VEHICLE_REGISTRATION", "INSURANCE", 
            "MAINTENANCE_RECORD", "DELIVERY_RECEIPT", "CONTRACT"
        ));
        return response;
    }
}
