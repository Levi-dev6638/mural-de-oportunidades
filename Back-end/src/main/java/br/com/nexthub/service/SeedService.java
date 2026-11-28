package br.com.nexthub.service;

import br.com.nexthub.model.*;
import br.com.nexthub.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class SeedService implements ApplicationRunner {
    private final UserRepository users;
    private final OpportunityRepository opportunities;
    private final ApplicationRepository applications;
    private final FavoriteRepository favorites;
    private final PasswordEncoder encoder;
    private final ObjectMapper mapper;

    @Value("${nexthub.seed.enabled:true}")
    private boolean seedEnabled;

    public SeedService(UserRepository users, OpportunityRepository opportunities,
                       ApplicationRepository applications, FavoriteRepository favorites,
                       PasswordEncoder encoder, ObjectMapper mapper) {
        this.users = users;
        this.opportunities = opportunities;
        this.applications = applications;
        this.favorites = favorites;
        this.encoder = encoder;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (!seedEnabled || users.count() > 0) return;

        ClassPathResource seed = new ClassPathResource("data/seed.json");
        if (!seed.exists()) throw new IllegalStateException("Classpath resource data/seed.json is missing.");

        JsonNode root;
        try (var input = seed.getInputStream()) {
            root = mapper.readTree(input);
        }
        for (JsonNode item : root.path("users")) {
            User user = new User();
            user.setId(item.path("id").asText());
            user.setName(item.path("name").asText());
            user.setEmail(item.path("email").asText());
            String existingHash = item.path("password").asText();
            user.setPassword(existingHash.startsWith("$2") ? existingHash : encoder.encode("demo1234"));
            user.setRole(item.path("role").asText());
            JsonNode profile = item.path("profile");
            user.setPhone(profile.path("phone").asText(null));
            user.setInstitution(profile.path("institution").asText(null));
            user.setEducation(profile.path("education").asText(null));
            user.setInterests(profile.path("interests").asText(null));
            user.setFormat(profile.path("format").asText(null));
            user.setAvailability(profile.path("availability").asText(null));
            user.setCreatedAt(item.path("createdAt").isMissingNode()
                    ? Instant.now() : Instant.parse(item.path("createdAt").asText()));
            users.save(user);
        }

        for (JsonNode item : root.path("opportunities")) {
            Opportunity o = new Opportunity();
            o.setId(item.path("id").asText());
            o.setOwner(users.findById(item.path("ownerId").asText()).orElseThrow());
            o.setTitle(item.path("title").asText(null));
            o.setType(item.path("type").asText(null));
            o.setDescription(item.path("description").asText(null));
            o.setCompany(item.path("company").asText(null));
            o.setArea(item.path("area").asText(null));
            o.setSubject(item.path("subject").asText(null));
            o.setFormat(item.path("format").asText(null));
            o.setDeadline(item.path("deadline").asText(null));
            o.setHours(item.path("hours").asText(null));
            o.setLocation(item.path("location").asText(null));
            o.setLink(item.path("link").asText(null));
            o.setRequirements(item.path("requirements").asText(null));
            o.setResponsibilities(item.path("responsibilities").asText(null));
            o.setInternalApplication(item.path("internal").asBoolean(false));
            o.setActive(item.path("active").asBoolean(true));
            o.setCreatedAt(item.path("createdAt").isMissingNode()
                    ? Instant.now() : Instant.parse(item.path("createdAt").asText()));
            opportunities.save(o);
        }

        for (JsonNode item : root.path("applications")) {
            Application a = new Application();
            a.setId(item.path("id").asText());
            a.setStudent(users.findById(item.path("userId").asText()).orElseThrow());
            a.setOpportunity(opportunities.findById(item.path("opportunityId").asText()).orElseThrow());
            a.setStatus(item.path("status").asText("Enviada"));
            a.setCreatedAt(item.path("createdAt").isMissingNode()
                    ? Instant.now() : Instant.parse(item.path("createdAt").asText()));
            applications.save(a);
        }
        for (JsonNode item : root.path("favorites")) {
            Favorite f = new Favorite();
            f.setStudent(users.findById(item.path("userId").asText()).orElseThrow());
            f.setOpportunity(opportunities.findById(item.path("opportunityId").asText()).orElseThrow());
            favorites.save(f);
        }
    }
}
