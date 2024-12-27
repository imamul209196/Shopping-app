package com.onlineshopping.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
public class StorageServiceImpl implements StorageService {

    @Value("${disk.upload.basepath}")
    private String BASEPATH;

    // Automatically create the folder on startup if it doesn't exist
    @PostConstruct
    public void init() {
        File uploadDir = new File(BASEPATH);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                System.out.println("Upload directory created at: " + uploadDir.getAbsolutePath());
            } else {
                throw new RuntimeException("Failed to create upload directory at: " + BASEPATH);
            }
        } else {
            System.out.println("Upload directory already exists at: " + uploadDir.getAbsolutePath());
        }
    }

    @Override
    public List<String> loadAll() {
        File dirPath = new File(BASEPATH);
        if (!dirPath.exists() || !dirPath.isDirectory()) {
            System.out.println("Upload directory does not exist: " + BASEPATH);
            return Arrays.asList();
        }
        return Arrays.asList(dirPath.list());
    }

    @Override
    public String store(MultipartFile file) {
        System.out.println("Original File Name: " + file.getOriginalFilename());
        
        // Ensure file has a valid extension
        String ext = "";
        if (file.getOriginalFilename().lastIndexOf(".") != -1) {
            ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        } else {
            System.out.println("File does not have an extension, skipping upload.");
            throw new RuntimeException("Invalid file format, extension is missing.");
        }
        
        String fileName = UUID.randomUUID().toString().replaceAll("-", "") + ext;
        File filePath = new File(BASEPATH, fileName);

        try (FileOutputStream out = new FileOutputStream(filePath)) {
            FileCopyUtils.copy(file.getInputStream(), out);
            System.out.println("File stored successfully at: " + filePath.getAbsolutePath());
            return fileName; // Return the file name for database storage
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to store the file: " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public Resource load(String fileName) {
        File filePath = new File(BASEPATH, fileName);
        if (filePath.exists()) {
            System.out.println("Loading file: " + filePath.getAbsolutePath());
            return new FileSystemResource(filePath);
        } else {
            System.out.println("File not found: " + fileName);
            return null;
        }
    }

    @Override
    public void delete(String fileName) {
        File filePath = new File(BASEPATH, fileName);
        if (filePath.exists()) {
            boolean deleted = filePath.delete();
            if (deleted) {
                System.out.println("File deleted successfully: " + filePath.getAbsolutePath());
            } else {
                System.out.println("Failed to delete file: " + fileName);
            }
        } else {
            System.out.println("File not found for deletion: " + fileName);
        }
    }
}
