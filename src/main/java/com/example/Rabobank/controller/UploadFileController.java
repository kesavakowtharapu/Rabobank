package com.example.Rabobank.controller;

import com.example.Rabobank.services.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/v1/upload")
public class UploadFileController {

    @Autowired
    private UploadService uploadService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file){

        if(file != null && file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")) {
            String[] fileName = file.getOriginalFilename().split("\\.");

            if (fileName != null && "csv".equalsIgnoreCase(fileName[1])) {
                return uploadService.validateCSV(file);
            } else if (fileName != null && "xml".equalsIgnoreCase(fileName[1])) {
                return uploadService.validateXML(file);
            } else {
                return new ResponseEntity("Please upload a valid CSV/XML file ", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity("Please select a valid file", HttpStatus.BAD_REQUEST);
        }
    }
}
