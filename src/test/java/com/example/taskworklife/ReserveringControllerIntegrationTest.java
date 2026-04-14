package com.example.taskworklife;

import com.example.taskworklife.dto.user.ReservatieDto;
import com.example.taskworklife.dto.user.UserRegisterDto;
import com.example.taskworklife.repo.ReserveringRepo;
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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;
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
class ReserveringControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReserveringRepo reserveringRepo;

    @Test
    void authenticatedUserCanCreateReadUpdateAndDeleteOwnReservation() throws Exception {
        Long reservationId = createReservation("pokemon@gmail.com", "Pokemon!23", LocalTime.of(9, 0), LocalTime.of(10, 0));

        MvcResult listResult = mockMvc.perform(get("/reservering/all")
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("pokemon@gmail.com", "Pokemon!23")))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode reservations = objectMapper.readTree(listResult.getResponse().getContentAsString());
        assertThat(reservations.findValuesAsText("userEmail"))
                .isNotEmpty()
                .contains("pokemon@gmail.com")
                .doesNotContain("admin@gmail.com");

        mockMvc.perform(get("/reservering/{id}", reservationId)
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("pokemon@gmail.com", "Pokemon!23")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId))
                .andExpect(jsonPath("$.kamerNaam").value("kamer2"))
                .andExpect(jsonPath("$.userEmail").value("pokemon@gmail.com"));

        ReservatieDto updateDto = reservationRequest(LocalTime.of(10, 0), LocalTime.of(11, 0));
        mockMvc.perform(put("/reservering/{id}", reservationId)
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("pokemon@gmail.com", "Pokemon!23"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTijd").exists())
                .andExpect(jsonPath("$.eindTijd").exists());

        mockMvc.perform(delete("/reservering/{id}", reservationId)
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("pokemon@gmail.com", "Pokemon!23")))
                .andExpect(status().isNoContent());

        assertThat(reserveringRepo.findById(reservationId)).isEmpty();

        mockMvc.perform(get("/reservering/{id}", reservationId)
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("pokemon@gmail.com", "Pokemon!23")))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatingReservationRejectsOverlap() throws Exception {
        Long reservationId = createReservation("pokemon@gmail.com", "Pokemon!23", LocalTime.of(9, 0), LocalTime.of(10, 0));

        ReservatieDto overlapDto = reservationRequest(LocalTime.of(8, 30), LocalTime.of(9, 30));
        mockMvc.perform(put("/reservering/{id}", reservationId)
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("pokemon@gmail.com", "Pokemon!23"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlapDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void anotherUserCannotReadReservationTheyDoNotOwn() throws Exception {
        Long reservationId = createReservation("pokemon@gmail.com", "Pokemon!23", LocalTime.of(11, 0), LocalTime.of(12, 0));

        String email = "viewer" + UUID.randomUUID().toString().replace("-", "") + "@example.com";
        registerUser(email, "StrongPass1");

        mockMvc.perform(get("/reservering/{id}", reservationId)
                        .header(HttpHeaders.AUTHORIZATION, basicAuth(email, "StrongPass1")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void unauthenticatedReservationListIsRejected() throws Exception {
        mockMvc.perform(get("/reservering/all"))
                .andExpect(status().isUnauthorized());
    }

    private Long createReservation(String email, String password, LocalTime start, LocalTime end) throws Exception {
        ReservatieDto dto = reservationRequest(start, end);

        MvcResult result = mockMvc.perform(post("/reservering")
                        .header(HttpHeaders.AUTHORIZATION, basicAuth(email, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.kamerNaam").value("kamer2"))
                .andExpect(jsonPath("$.userEmail").value(email))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asLong();
    }

    private ReservatieDto reservationRequest(LocalTime start, LocalTime end) {
        ReservatieDto dto = new ReservatieDto();
        dto.setKamerNaam("kamer2");
        dto.setStartTijd(LocalDateTime.of(LocalDate.now(), start));
        dto.setEindTijd(LocalDateTime.of(LocalDate.now(), end));
        return dto;
    }

    private void registerUser(String email, String password) throws Exception {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setNaam("Viewer");
        dto.setAchterNaam("User");
        dto.setEmail(email);
        dto.setWachtwoord(password);
        dto.setTerms(true);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    private String basicAuth(String username, String password) {
        String token = Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }
}
