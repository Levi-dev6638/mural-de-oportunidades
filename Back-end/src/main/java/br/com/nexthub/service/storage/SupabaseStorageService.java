package br.com.nexthub.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

@Service
@ConditionalOnProperty(name = "nexthub.storage.provider", havingValue = "supabase")
public class SupabaseStorageService implements FileStorageService {
    private final RestClient client;
    private final String bucket;

    public SupabaseStorageService(
            RestClient.Builder builder,
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.storage.bucket}") String bucket,
            @Value("${supabase.storage.service-key}") String serviceKey) {
        this.client = builder
                .baseUrl(supabaseUrl)
                .defaultHeader("apikey", serviceKey)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + serviceKey)
                .build();
        this.bucket = bucket;
    }

    @Override
    public String store(String storagePath, byte[] content, String contentType) throws IOException {
        try {
            client.put()
                    .uri(uri -> uri.pathSegment("storage", "v1", "object", bucket, storagePath).build())
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("x-upsert", "false")
                    .body(content)
                    .retrieve()
                    .toBodilessEntity();
            return storagePath;
        } catch (RestClientResponseException error) {
            throw storageFailure("upload", error);
        }
    }

    @Override
    public Resource load(String storagePath) throws IOException {
        try {
            byte[] content = client.get()
                    .uri(uri -> uri.pathSegment("storage", "v1", "object", bucket, storagePath).build())
                    .retrieve()
                    .body(byte[].class);
            if (content == null) throw new IOException("Supabase Storage returned an empty response.");
            return new ByteArrayResource(content);
        } catch (RestClientResponseException error) {
            if (error.getStatusCode().value() == 404) throw new NoSuchFileException(storagePath);
            throw storageFailure("download", error);
        }
    }

    @Override
    public void delete(String storagePath) throws IOException {
        try {
            client.method(HttpMethod.DELETE)
                    .uri(uri -> uri.pathSegment("storage", "v1", "object", bucket).build())
                    .body(java.util.Map.of("prefixes", java.util.List.of(storagePath)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException error) {
            if (error.getStatusCode().value() != 404) throw storageFailure("delete", error);
        }
    }

    private IOException storageFailure(String operation, RestClientResponseException error) {
        return new IOException(
                "Supabase Storage " + operation + " failed with HTTP " + error.getStatusCode().value(),
                error);
    }
}
