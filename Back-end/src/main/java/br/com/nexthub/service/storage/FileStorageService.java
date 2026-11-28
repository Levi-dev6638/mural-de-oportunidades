package br.com.nexthub.service.storage;

import org.springframework.core.io.Resource;

import java.io.IOException;

public interface FileStorageService {
    String store(String storagePath, byte[] content, String contentType) throws IOException;
    Resource load(String storagePath) throws IOException;
    void delete(String storagePath) throws IOException;
}
