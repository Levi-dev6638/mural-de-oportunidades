package br.com.nexthub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite:./target/test-happy-path.sqlite",
        "nexthub.upload-dir=./target/test-happy-path-uploads"
})
class NextHubHappyPathTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void studentCanRegisterUploadResumeApplyAndReadApplication() throws Exception {
        String email = "student-" + UUID.randomUUID() + "@nexthub.test";
        MvcResult registration = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "name", "Estudante de Teste",
                                "email", email,
                                "password", "senha-teste-123",
                                "role", "student"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value(email))
                .andReturn();

        MockHttpSession session = (MockHttpSession) registration.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mvc.perform(get("/api/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("student"));

        mvc.perform(patch("/api/profile")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "phone", "(11) 99999-1234",
                                "institution", "Universidade de Teste",
                                "interests", "Tecnologia",
                                "format", "Remoto",
                                "availability", "30 horas semanais",
                                "courses", List.of(Map.of(
                                        "name", "Java",
                                        "institution", "Universidade de Teste",
                                        "hours", "40"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.profile.phone").value("(11) 99999-1234"))
                .andExpect(jsonPath("$.user.profile.courses[0].name").value("Java"));

        mvc.perform(get("/api/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.profile.institution").value("Universidade de Teste"))
                .andExpect(jsonPath("$.user.profile.courses[0].hours").value("40"));

        MockMultipartFile resume = new MockMultipartFile(
                "resume",
                "curriculo.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "%PDF-1.4\n%%EOF".getBytes(StandardCharsets.US_ASCII));
        MvcResult resumeUpload = mvc.perform(multipart("/api/resume").file(resume).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.profile.resumeName").value("curriculo.pdf"))
                .andReturn();
        String resumeUrl = mapper.readTree(resumeUpload.getResponse().getContentAsString())
                .path("user").path("profile").path("resume").asText();
        mvc.perform(get(resumeUrl).session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes("%PDF-1.4\n%%EOF".getBytes(StandardCharsets.US_ASCII)));

        MvcResult application = mvc.perform(post("/api/applications/o1").session(session))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.opportunityId").value("o1"))
                .andExpect(jsonPath("$.status").value("Enviada"))
                .andReturn();
        String applicationId = mapper.readTree(application.getResponse().getContentAsString())
                .path("id").asText();

        mvc.perform(get("/api/applications").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].opportunityId").value("o1"))
                .andExpect(jsonPath("$[0].status").value("Enviada"));

        mvc.perform(delete("/api/applications/" + applicationId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));

        mvc.perform(get("/api/applications").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        MvcResult externalApplication = mvc.perform(post("/api/applications/o3").session(session))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.opportunityId").value("o3"))
                .andExpect(jsonPath("$.status").value("Enviada"))
                .andReturn();
        String externalApplicationId = mapper.readTree(externalApplication.getResponse().getContentAsString())
                .path("id").asText();

        mvc.perform(post("/api/applications/o3").session(session))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/applications").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].opportunityId").value("o3"));
        mvc.perform(delete("/api/applications/" + externalApplicationId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));
    }

    @Test
    void companyCanLoginPublishAndReadBackOpportunity() throws Exception {
        MvcResult login = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"talentos@nexthub.demo","password":"demo1234"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("company"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertThat(session).isNotNull();
        String title = "Oportunidade " + UUID.randomUUID();

        mvc.perform(post("/api/opportunities")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "title", title,
                                "type", "Estágio",
                                "company", "Empresa de Teste",
                                "description", "Descrição da oportunidade de teste.",
                                "internal", true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(title));

        mvc.perform(get("/api/company/opportunities").session(session))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .contains(title));
    }
}
