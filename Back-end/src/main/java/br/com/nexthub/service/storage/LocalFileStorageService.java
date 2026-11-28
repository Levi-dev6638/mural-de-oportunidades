package br.com.nexthub.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
@ConditionalOnProperty(name = "nexthub.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {
    private final Path root;

    public LocalFileStorageService(@Value("${nexthub.upload-dir}") String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public String store(String storagePath, byte[] content, String contentType) throws IOException {
        Path target = resolve(storagePath);
        Files.createDirectories(target.getParent());
        Files.write(target, content, StandardOpenOption.CREATE_NEW);
        return storagePath;
    }

    @Override
    public Resource load(String storagePath) throws IOException {
        Path path = resolve(storagePath);
        if (!Files.isRegularFile(path)) throw new NoSuchFileException(storagePath);
        return new FileSystemResource(path);
    }

    @Override
    public void delete(String storagePath) throws IOException {
        Files.deleteIfExists(resolve(storagePath));
    }

    private Path resolve(String storagePath) throws IOException {
        Path candidate = Path.of(storagePath);
        Path resolved = candidate.isAbsolute() ? candidate.normalize() : root.resolve(candidate).normalize();
        if (!resolved.startsWith(root)) throw new IOException("Storage path is outside the configured directory.");
        return resolved;
    }
}
