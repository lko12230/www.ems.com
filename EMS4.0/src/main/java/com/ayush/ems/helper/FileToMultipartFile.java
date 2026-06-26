package com.ayush.ems.helper;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class FileToMultipartFile implements MultipartFile {

    private final File file;

    public FileToMultipartFile(File file) {
        this.file = file;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getOriginalFilename() {
        return file.getName();
    }

    @Override
    public String getContentType() {
        return "application/pdf";
    }

    @Override
    public boolean isEmpty() {
        return file.length() == 0;
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public byte[] getBytes() throws IOException {
        return new FileInputStream(file).readAllBytes();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        new FileInputStream(file).transferTo(new FileOutputStream(dest));
    }
}
