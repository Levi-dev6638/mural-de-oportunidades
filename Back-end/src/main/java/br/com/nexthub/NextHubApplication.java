package br.com.nexthub;

import br.com.nexthub.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class NextHubApplication {
    public static void main(String[] args) throws IOException {
        DotenvLoader.load();
        SpringApplication.run(NextHubApplication.class, args);
    }
}
