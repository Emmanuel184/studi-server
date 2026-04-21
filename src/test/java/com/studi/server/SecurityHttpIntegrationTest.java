package com.studi.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:sqlite:file:studi-test?mode=memory&cache=shared",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.main.banner-mode=off",
        "logging.level.root=WARN",
        "logging.level.org.hibernate.SQL=OFF",
        "logging.level.org.springframework=ERROR",
        "app.jwt.secret=change-this-test-secret-change-this-test-secret",
        "app.jwt.expiration-ms=86400000"
})
@AutoConfigureMockMvc
class SecurityHttpIntegrationTest {

    private static final AtomicInteger USER_COUNTER = new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publicDocsAndOptionsAreOpenButStepsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());

        mockMvc.perform(options("/steps")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/steps"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void authenticatedUserCanCreateReadUpdateAndDeleteStudyPlanSteps() throws Exception {
        String username = "studyuser" + USER_COUNTER.incrementAndGet();
        String token = register(username);

        MvcResult createResult = mockMvc.perform(post("/study-plans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goal\":\"  Learn Spring Security  \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goal").value("Learn Spring Security"))
                .andExpect(jsonPath("$.steps.length()").value(5))
                .andReturn();
        Map<String, Object> plan = readJson(createResult);
        Number planId = (Number) plan.get("id");
        Number firstStepId = firstStepId(plan);

        mockMvc.perform(get("/study-plans/{id}", planId.longValue())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(planId.longValue()));

        mockMvc.perform(get("/steps")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));

        mockMvc.perform(get("/steps/{id}", firstStepId.longValue())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstStepId.longValue()));

        mockMvc.perform(put("/steps/{id}", firstStepId.longValue())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated",
                                  "description": "Updated description",
                                  "completed": true,
                                  "position": 9
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.position").value(9));

        mockMvc.perform(delete("/steps/{id}", firstStepId.longValue())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void defaultUserCanLogInAndUseBearerToken() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\" EMMANUEL \",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("emmanuel"))
                .andReturn();
        String token = (String) readJson(loginResult).get("token");

        mockMvc.perform(get("/steps")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String register(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "secret"
                                }
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andReturn();
        return (String) readJson(result).get("token");
    }

    private Map<String, Object> readJson(MvcResult result) throws Exception {
        return objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<Map<String, Object>>() {});
    }

    @SuppressWarnings("unchecked")
    private Number firstStepId(Map<String, Object> plan) {
        Object firstStep = ((java.util.List<Object>) plan.get("steps")).get(0);
        assertThat(firstStep).isInstanceOf(Map.class);
        return (Number) ((Map<String, Object>) firstStep).get("id");
    }
}
