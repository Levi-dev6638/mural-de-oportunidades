package br.com.nexthub.controller;

import br.com.nexthub.model.*;
import br.com.nexthub.repository.*;
import br.com.nexthub.service.storage.FileStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class NextHubController {
    private static final String SESSION_USER = "nexthubUserId";
    private static final TypeReference<List<Map<String, String>>> COURSE_LIST_TYPE = new TypeReference<>() {};
    private static final Logger log = LoggerFactory.getLogger(NextHubController.class);
    private final UserRepository users;
    private final OpportunityRepository opportunities;
    private final ApplicationRepository applications;
    private final FavoriteRepository favorites;
    private final ResumeRepository resumes;
    private final PasswordEncoder encoder;
    private final ObjectMapper mapper;
    private final FileStorageService fileStorage;

    public NextHubController(UserRepository users, OpportunityRepository opportunities,
                             ApplicationRepository applications, FavoriteRepository favorites,
                             ResumeRepository resumes, PasswordEncoder encoder, ObjectMapper mapper,
                             FileStorageService fileStorage) {
        this.users = users;
        this.opportunities = opportunities;
        this.applications = applications;
        this.favorites = favorites;
        this.resumes = resumes;
        this.encoder = encoder;
        this.mapper = mapper;
        this.fileStorage = fileStorage;
    }

    private User requireUser(HttpServletRequest request) {
        Object id = request.getSession(false) == null ? null : request.getSession(false).getAttribute(SESSION_USER);
        if (id == null) throw new UnauthorizedException();
        return users.findById(id.toString()).orElseThrow(UnauthorizedException::new);
    }

    private void requireRole(User user, String role) {
        if (!role.equals(user.getRole())) throw new ForbiddenException();
    }

    private Map<String, Object> userMap(User u) {
        Map<String, Object> profile = new LinkedHashMap<>();
        put(profile, "phone", u.getPhone());
        put(profile, "institution", u.getInstitution());
        put(profile, "education", u.getEducation());
        put(profile, "interests", u.getInterests());
        put(profile, "format", u.getFormat());
        put(profile, "availability", u.getAvailability());
        if (u.getCourses() != null) {
            try {
                profile.put("courses", mapper.readValue(u.getCourses(), COURSE_LIST_TYPE));
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException("Stored course data is invalid.", ex);
            }
        }
        resumes.findByStudentId(u.getId()).ifPresent(r ->
                {
                    put(profile, "resume", "/api/resume/" + r.getId());
                    put(profile, "resumeName", r.getOriginalName());
                });
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", u.getId());
        map.put("name", u.getName());
        map.put("email", u.getEmail());
        map.put("role", u.getRole());
        map.put("createdAt", u.getCreatedAt());
        map.put("profile", profile);
        return map;
    }

    private static void put(Map<String, Object> map, String key, Object value) {
        if (value != null) map.put(key, value);
    }

    private Map<String, Object> opportunityMap(Opportunity o) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", o.getId());
        map.put("ownerId", o.getOwner().getId());
        map.put("title", o.getTitle());
        map.put("type", o.getType());
        map.put("description", o.getDescription());
        map.put("company", o.getCompany());
        map.put("area", o.getArea());
        map.put("subject", o.getSubject());
        map.put("format", o.getFormat());
        map.put("deadline", o.getDeadline());
        map.put("hours", o.getHours());
        map.put("location", o.getLocation());
        map.put("link", o.getLink());
        map.put("requirements", o.getRequirements());
        map.put("responsibilities", o.getResponsibilities());
        map.put("internal", o.isInternalApplication());
        map.put("createdAt", o.getCreatedAt());
        map.put("active", o.isActive());
        return map;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> body,
                                                         HttpServletRequest request) {
        String name = text(body, "name", 120);
        String email = text(body, "email", 254).toLowerCase(Locale.ROOT);
        String password = Objects.toString(body.get("password"), "");
        String role = Objects.toString(body.get("role"), "");
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") || password.length() < 8 ||
                password.length() > 128 || !List.of("student", "company").contains(role)) {
            throw new IllegalArgumentException("Informe nome, e-mail válido, perfil e senha com 8 a 128 caracteres.");
        }
        if (users.findByEmailIgnoreCase(email).isPresent()) throw new ConflictException("Este e-mail já está cadastrado.");
        User u = new User();
        u.setId(UUID.randomUUID().toString());
        u.setName(name);
        u.setEmail(email);
        u.setPassword(encoder.encode(password));
        u.setRole(role);
        u.setCreatedAt(Instant.now());
        users.save(u);
        establishSession(request, u);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("user", userMap(u)));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> body,
                                                     HttpServletRequest request) {
        String email = Objects.toString(body.get("email"), "").trim().toLowerCase(Locale.ROOT);
        String password = Objects.toString(body.get("password"), "");
        String requestedRole = Objects.toString(body.get("role"), "");
        User u = users.findByEmailIgnoreCase(email).orElse(null);
        if (u == null || !encoder.matches(password, u.getPassword()) ||
                (!requestedRole.isBlank() && !requestedRole.equals(u.getRole()))) {
            throw new UnauthorizedException("E-mail, senha ou perfil incorretos.");
        }
        establishSession(request, u);
        return ResponseEntity.ok(Map.of("user", userMap(u)));
    }

    @PostMapping("/auth/logout")
    public Map<String, Boolean> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return Map.of("ok", true);
    }

    private void establishSession(HttpServletRequest request, User u) {
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER, u.getId());
    }

    private String text(Map<String, Object> body, String key, int max) {
        String value = Objects.toString(body.get(key), "").trim();
        if (value.isEmpty() || value.length() > max) throw new IllegalArgumentException("Dados inválidos.");
        return value;
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpServletRequest request) {
        return Map.of("user", userMap(requireUser(request)));
    }

    @PatchMapping("/profile")
    public Map<String, Object> profile(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User u = requireUser(request);
        requireRole(u, "student");
        Set<String> allowed = Set.of("phone", "institution", "education", "interests", "format", "availability", "courses");
        if (body.keySet().stream().anyMatch(k -> !allowed.contains(k))) {
            throw new IllegalArgumentException("O perfil contém campos não permitidos.");
        }
        for (Map.Entry<String, Object> entry : body.entrySet()) {
            if (entry.getKey().equals("courses")) continue;
            if (!(entry.getValue() instanceof String value) || value.length() > 500) {
                throw new IllegalArgumentException("Os campos do perfil devem ser textos de até 500 caracteres.");
            }
            switch (entry.getKey()) {
                case "phone" -> u.setPhone(value);
                case "institution" -> u.setInstitution(value);
                case "education" -> u.setEducation(value);
                case "interests" -> u.setInterests(value);
                case "format" -> u.setFormat(value);
                case "availability" -> u.setAvailability(value);
            }
        }
        if (body.containsKey("courses")) {
            u.setCourses(mapper.valueToTree(parseCourses(body.get("courses"))).toString());
        }
        users.save(u);
        return Map.of("user", userMap(u));
    }

    private List<Map<String, String>> parseCourses(Object value) {
        if (!(value instanceof List<?> courses) || courses.size() > 50) {
            throw new IllegalArgumentException("Informe até 50 cursos válidos.");
        }
        List<Map<String, String>> result = new ArrayList<>();
        Set<String> allowed = Set.of("name", "institution", "hours");
        for (Object item : courses) {
            if (!(item instanceof Map<?, ?> course) ||
                    course.keySet().stream().anyMatch(key -> !(key instanceof String field) || !allowed.contains(field))) {
                throw new IllegalArgumentException("Os dados dos cursos são inválidos.");
            }
            String name = courseText(course, "name");
            String institution = courseText(course, "institution");
            String hours = courseText(course, "hours");
            if (name.isBlank() && institution.isBlank() && hours.isBlank()) continue;
            if (!hours.isBlank()) {
                try {
                    int parsedHours = Integer.parseInt(hours);
                    if (parsedHours < 1 || parsedHours > 10000) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("A carga horária deve ser um número entre 1 e 10000.");
                }
            }
            result.add(Map.of("name", name, "institution", institution, "hours", hours));
        }
        return result;
    }

    private String courseText(Map<?, ?> course, String field) {
        Object value = course.get(field);
        if (value == null) return "";
        if (!(value instanceof String text) || text.length() > 160) {
            throw new IllegalArgumentException("Cada campo do curso deve ser um texto de até 160 caracteres.");
        }
        return text.trim();
    }

    @GetMapping("/opportunities")
    public List<Map<String, Object>> listOpportunities(@RequestParam(required=false) String q,
                                                        @RequestParam(required=false) String type,
                                                        @RequestParam(required=false) String format,
                                                        @RequestParam(required=false) String area,
                                                        @RequestParam(required=false) String subject,
                                                        HttpServletRequest request) {
        requireUser(request);
        String query = q == null ? "" : q.toLowerCase(Locale.ROOT);
        return opportunities.findByActiveTrueOrderByCreatedAtDesc().stream()
                .filter(o -> query.isBlank() || (o.getTitle()+ " " + o.getCompany()+ " " + o.getSubject()+ " " + o.getArea()+ " " + o.getDescription()).toLowerCase(Locale.ROOT).contains(query))
                .filter(o -> type == null || type.isBlank() || type.equalsIgnoreCase(o.getType()))
                .filter(o -> format == null || format.isBlank() || format.equalsIgnoreCase(o.getFormat()))
                .filter(o -> area == null || area.isBlank() || area.equalsIgnoreCase(o.getArea()))
                .filter(o -> subject == null || subject.isBlank() || subject.equalsIgnoreCase(o.getSubject()))
                .map(this::opportunityMap).collect(Collectors.toList());
    }

    @PostMapping("/opportunities")
    public ResponseEntity<Map<String, Object>> createOpportunity(@RequestBody Map<String, Object> body,
                                                                  HttpServletRequest request) {
        User u = requireUser(request);
        requireRole(u, "company");
        Opportunity o = opportunityFromBody(body);
        o.setId(UUID.randomUUID().toString());
        o.setOwner(u);
        o.setCompany(text(body, "company", 160, u.getName()));
        o.setCreatedAt(Instant.now());
        opportunities.save(o);
        return ResponseEntity.status(HttpStatus.CREATED).body(opportunityMap(o));
    }

    @PatchMapping("/opportunities/{id}")
    public Map<String, Object> updateOpportunity(@PathVariable String id,
                                                  @RequestBody Map<String, Object> body,
                                                  HttpServletRequest request) {
        User u = requireUser(request);
        requireRole(u, "company");
        Opportunity o = opportunities.findById(id).filter(x -> x.getOwner().getId().equals(u.getId()))
                .orElseThrow(() -> new NotFoundException());
        Opportunity updated = opportunityFromBody(body);
        copyOpportunity(updated, o);
        opportunities.save(o);
        return opportunityMap(o);
    }

    @DeleteMapping("/opportunities/{id}")
    public Map<String, Boolean> deleteOpportunity(@PathVariable String id, HttpServletRequest request) {
        User u = requireUser(request);
        requireRole(u, "company");
        Opportunity o = opportunities.findById(id).filter(x -> x.getOwner().getId().equals(u.getId()))
                .orElseThrow(() -> new NotFoundException());
        o.setActive(false);
        opportunities.save(o);
        return Map.of("ok", true);
    }

    @GetMapping("/company/opportunities")
    public List<Map<String, Object>> companyOpportunities(HttpServletRequest request) {
        User u = requireUser(request);
        requireRole(u, "company");
        return opportunities.findByOwnerIdAndActiveTrueOrderByCreatedAtDesc(u.getId())
                .stream().map(this::opportunityMap).collect(Collectors.toList());
    }

    private Opportunity opportunityFromBody(Map<String, Object> body) {
        Set<String> allowed = Set.of("title","type","description","company","area","subject","format","deadline","hours","location","link","requirements","responsibilities","internal");
        if (body.keySet().stream().anyMatch(k -> !allowed.contains(k))) throw new IllegalArgumentException("A oportunidade contém campos não permitidos.");
        if (!(body.get("title") instanceof String title) || title.isBlank() ||
            !(body.get("type") instanceof String type) || type.isBlank() ||
            !(body.get("description") instanceof String description) || description.isBlank()) {
            throw new IllegalArgumentException("Preencha título, tipo e descrição com valores válidos.");
        }
        Opportunity o = new Opportunity();
        o.setTitle(title); o.setType(type); o.setDescription(description);
        o.setCompany(string(body, "company"));
        o.setArea(string(body, "area")); o.setSubject(string(body, "subject"));
        o.setFormat(string(body, "format")); o.setDeadline(string(body, "deadline"));
        o.setHours(string(body, "hours")); o.setLocation(string(body, "location"));
        o.setLink(string(body, "link")); o.setRequirements(string(body, "requirements"));
        o.setResponsibilities(string(body, "responsibilities"));
        o.setInternalApplication(body.get("internal") instanceof Boolean b ? b : false);
        return o;
    }

    private void copyOpportunity(Opportunity src, Opportunity dst) {
        dst.setTitle(src.getTitle()); dst.setType(src.getType()); dst.setDescription(src.getDescription());
        dst.setCompany(src.getCompany()); dst.setArea(src.getArea()); dst.setSubject(src.getSubject());
        dst.setFormat(src.getFormat()); dst.setDeadline(src.getDeadline()); dst.setHours(src.getHours());
        dst.setLocation(src.getLocation()); dst.setLink(src.getLink()); dst.setRequirements(src.getRequirements());
        dst.setResponsibilities(src.getResponsibilities()); dst.setInternalApplication(src.isInternalApplication());
    }
    private String string(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v instanceof String s ? s : null;
    }
    private String text(Map<String, Object> body, String key, int max, String fallback) {
        Object v = body.get(key);
        if (!(v instanceof String s) || s.isBlank()) return fallback;
        if (s.length() > max) throw new IllegalArgumentException("Dados inválidos.");
        return s.trim();
    }

    @GetMapping("/favorites")
    public List<Map<String, Object>> favorites(HttpServletRequest request) {
        User u = requireUser(request);
        requireRole(u, "student");
        return favorites.findByStudentId(u.getId()).stream()
                .map(f -> opportunityMap(f.getOpportunity()))
                .collect(Collectors.toList());
    }

    @PostMapping("/favorites/{id}")
    public Map<String, Boolean> toggleFavorite(@PathVariable String id, HttpServletRequest request) {
        User u = requireUser(request);
        requireRole(u, "student");
        Opportunity o = opportunities.findById(id).orElseThrow(() -> new NotFoundException());
        Optional<Favorite> existing = favorites.findByStudentIdAndOpportunityId(u.getId(), id);
        if (existing.isPresent()) {
            favorites.delete(existing.get());
            return Map.of("favorited", false);
        }
        Favorite f = new Favorite();
        f.setStudent(u);
        f.setOpportunity(o);
        favorites.save(f);
        return Map.of("favorited", true);
    }

    @PostMapping("/applications/{id}")
    public ResponseEntity<?> apply(@PathVariable String id, HttpServletRequest request) {
        User u = requireUser(request);
        requireRole(u, "student");
        Opportunity o = opportunities.findById(id).filter(Opportunity::isActive)
                .orElseThrow(() -> new NotFoundException());
        if (resumes.findByStudentId(u.getId()).isEmpty()) {
            throw new IllegalArgumentException("Cadastre seu currículo PDF antes de se candidatar.");
        }
        if (applications.findByStudentIdAndOpportunityId(u.getId(), id).isPresent()) {
            throw new ConflictException("Você já se candidatou.");
        }
        Application a = new Application();
        a.setId(UUID.randomUUID().toString());
        a.setStudent(u);
        a.setOpportunity(o);
        a.setStatus("Enviada");
        a.setCreatedAt(Instant.now());
        resumes.findByStudentId(u.getId()).ifPresent(r ->
                a.setResumePath("/api/resume/" + r.getId()));
        applications.save(a);
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationMap(a));
    }

    @GetMapping("/applications")
    public List<Map<String, Object>> applications(HttpServletRequest request) {
        User u = requireUser(request);
        if ("student".equals(u.getRole())) {
            return applications.findByStudentIdOrderByCreatedAtDesc(u.getId()).stream()
                    .map(this::applicationMap).collect(Collectors.toList());
        }
        requireRole(u, "company");
        return applications.findByOpportunityOwnerIdOrderByCreatedAtDesc(u.getId()).stream()
                .map(this::applicationMap).collect(Collectors.toList());
    }

    @DeleteMapping("/applications/{id}")
    public Map<String, Boolean> withdrawApplication(@PathVariable String id, HttpServletRequest request) {
        User u = requireUser(request);
        requireRole(u, "student");
        Application application = applications.findById(id)
                .filter(item -> item.getStudent().getId().equals(u.getId()))
                .orElseThrow(NotFoundException::new);
        applications.delete(application);
        return Map.of("deleted", true);
    }

    @PatchMapping("/applications/{id}")
    public Map<String, Object> updateApplication(@PathVariable String id,
                                                   @RequestBody Map<String, Object> body,
                                                   HttpServletRequest request) {
        User u = requireUser(request);
        requireRole(u, "company");
        Application a = applications.findById(id)
                .filter(x -> x.getOpportunity().getOwner().getId().equals(u.getId()))
                .orElseThrow(() -> new NotFoundException());
        String status = Objects.toString(body.get("status"), "");
        if (!List.of("Em análise", "Aprovada", "Recusada").contains(status)) {
            throw new IllegalArgumentException("Status inválido.");
        }
        a.setStatus(status);
        applications.save(a);
        return applicationMap(a);
    }

    private Map<String, Object> applicationMap(Application a) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", a.getId());
        map.put("userId", a.getStudent().getId());
        map.put("opportunityId", a.getOpportunity().getId());
        map.put("status", a.getStatus());
        map.put("createdAt", a.getCreatedAt());
        map.put("resumePath", a.getResumePath());
        map.put("opportunity", opportunityMap(a.getOpportunity()));
        map.put("user", userMap(a.getStudent()));
        return map;
    }

    @PostMapping("/resume")
    public ResponseEntity<Map<String, Object>> uploadResume(@RequestParam("resume") MultipartFile file,
                                                             HttpServletRequest request) throws Exception {
        User u = requireUser(request);
        requireRole(u, "student");
        String originalFilename = Objects.toString(file.getOriginalFilename(), "");
        if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024 ||
                !"application/pdf".equalsIgnoreCase(file.getContentType()) ||
                !originalFilename.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException("Envie um arquivo PDF válido de até 5 MB.");
        }
        byte[] bytes = file.getBytes();
        if (bytes.length < 5 ||
                !new String(bytes, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).startsWith("%PDF-")) {
            throw new IllegalArgumentException("O arquivo enviado não é um PDF válido.");
        }
        Resume resume = resumes.findByStudentId(u.getId()).orElseGet(Resume::new);
        String previousStoragePath = resume.getStoragePath();
        String storagePath = UUID.randomUUID() + ".pdf";
        String storedPath = fileStorage.store(storagePath, bytes, "application/pdf");
        resume.setStudent(u);
        resume.setOriginalName(originalFilename);
        resume.setMimeType("application/pdf");
        resume.setStoragePath(storedPath);
        resume.setUploadedAt(Instant.now());
        try {
            resumes.save(resume);
        } catch (RuntimeException saveError) {
            try {
                fileStorage.delete(storedPath);
            } catch (IOException cleanupError) {
                saveError.addSuppressed(cleanupError);
            }
            throw saveError;
        }
        if (previousStoragePath != null && !previousStoragePath.equals(storedPath)) {
            try {
                fileStorage.delete(previousStoragePath);
            } catch (IOException cleanupError) {
                log.warn("Could not remove replaced résumé from storage.", cleanupError);
            }
        }
        return ResponseEntity.ok(Map.of("user", userMap(u)));
    }

    @GetMapping("/resume/{id}")
    public ResponseEntity<Resource> downloadResume(@PathVariable String id, HttpServletRequest request) throws Exception {
        User u = requireUser(request);
        Resume resume = resumes.findById(UUID.fromString(id)).orElseThrow(() -> new NotFoundException());
        if ("student".equals(u.getRole()) && !resume.getStudent().getId().equals(u.getId())) {
            throw new ForbiddenException();
        }
        if ("company".equals(u.getRole())) {
            boolean authorized = applications.findByStudentIdOrderByCreatedAtDesc(resume.getStudent().getId()).stream()
                    .anyMatch(a -> a.getOpportunity().getOwner().getId().equals(u.getId()));
            if (!authorized) throw new ForbiddenException();
        }
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(fileStorage.load(resume.getStoragePath()));
        } catch (NoSuchFileException missingFile) {
            throw new NotFoundException();
        }
    }
}
