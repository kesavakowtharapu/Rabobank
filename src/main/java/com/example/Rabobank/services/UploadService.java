package com.example.Rabobank.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    ResponseEntity validateCSV(MultipartFile file);
    ResponseEntity validateXML(MultipartFile file);

}
