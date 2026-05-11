package com.example.taskworklife;

import com.example.taskworklife.dto.user.UserLoginDto;
import com.example.taskworklife.dto.user.UserRegisterDto;
import com.example.taskworklife.dto.user.UserUpdateDto;
import com.example.taskworklife.models.user.User;
import com.example.taskworklife.repo.UserRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepo;

    @Test
    void registerCreatesUserAndReturnsProfileShape() throws Exception {
        String uniqueEmail = "user" + UUID.randomUUID().toString().replace("-", "") + "@example.com";
        UserRegisterDto dto = new UserRegisterDto();
        dto.setNaam("Test");
        dto.setAchterNaam("User");
        dto.setEmail(uniqueEmail);
        dto.setWachtwoord("StrongPass1");
        dto.setTerms(true);

        mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naam").value("Test"))
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        User createdUser = userRepo.findUserByEmail(uniqueEmail);
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getNaam()).isEqualTo("Test");
    }

    @Test
    void loginWithSeededUserReturnsProfileData() throws Exception {
        UserLoginDto dto = new UserLoginDto("pokemon@gmail.com", "Pokemon!23");

        mockMvc.perform(
                post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naam").value("jan"))
                .andExpect(jsonPath("$.email").value("pokemon@gmail.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void adminCanReadUpdateAndDeleteUser() throws Exception {
        Long userId = registerUserAndReturnId("managed" + UUID.randomUUID().toString().replace("-", "") + "@example.com");
        String managedEmail = userRepo.findById(userId).orElseThrow().getEmail();
        String adminAuthorization = bearerToken("admin@gmail.com", "AdminUser!1");

        MvcResult listResult = mockMvc.perform(get("/users")
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(listResult.getResponse().getContentAsString()).contains(managedEmail);

        mockMvc.perform(get("/users/{id}", userId)
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").exists());

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setNaam("Updated");
        updateDto.setAchternaam("Manager");
        updateDto.setEmail("updated" + userId + "@example.com");
        updateDto.setProfileImageUrl("https://example.com/profile.png");
        updateDto.setActive(true);
        updateDto.setNotLocked(true);

        mockMvc.perform(put("/users/{id}", userId)
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naam").value("Updated"))
                .andExpect(jsonPath("$.achternaam").value("Manager"))
                .andExpect(jsonPath("$.email").value("updated" + userId + "@example.com"));

        mockMvc.perform(delete("/users/{id}", userId)
                        .header(HttpHeaders.AUTHORIZATION, adminAuthorization))
                .andExpect(status().isNoContent());

        assertThat(userRepo.findById(userId)).isEmpty();
    }

    @Test
    void normalUserCannotManageAdminOnlyUserEndpoints() throws Exception {
        Long userId = registerUserAndReturnId("forbidden" + UUID.randomUUID().toString().replace("-", "") + "@example.com");
        String userAuthorization = bearerToken("pokemon@gmail.com", "Pokemon!23");

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setNaam("Nope");
        updateDto.setAchternaam("User");
        updateDto.setEmail("nope" + userId + "@example.com");
        updateDto.setActive(true);
        updateDto.setNotLocked(true);

        mockMvc.perform(put("/users/{id}", userId)
                        .header(HttpHeaders.AUTHORIZATION, userAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    private Long registerUserAndReturnId(String email) throws Exception {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setNaam("Crud");
        dto.setAchterNaam("Target");
        dto.setEmail(email);
        dto.setWachtwoord("StrongPass1");
        dto.setTerms(true);

        MvcResult result = mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        User createdUser = userRepo.findUserByEmail(response.get("email").asText());
        assertThat(createdUser).isNotNull();
        return createdUser.getId();
    }

    private String bearerToken(String username, String password) throws Exception {
        UserLoginDto dto = new UserLoginDto(username, password);
        MvcResult result = mockMvc.perform(
                post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return "Bearer " + response.get("token").asText();
    }
}
