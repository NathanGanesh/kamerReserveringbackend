package com.example.taskworklife;

import com.example.taskworklife.dto.user.ReservatieDto;
import com.example.taskworklife.dto.user.UserLoginDto;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        String userAuthorization = bearerToken("pokemon@gmail.com", "Pokemon!23");
        Long reservationId = createReservation(userAuthorization, "pokemon@gmail.com", LocalTime.of(9, 0), LocalTime.of(10, 0));

        MvcResult listResult = mockMvc.perform(get("/reserveringen")
                        .header(HttpHeaders.AUTHORIZATION, userAuthorization))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode reservations = objectMapper.readTree(listResult.getResponse().getContentAsString());
        assertThat(reservations.findValuesAsText("userEmail"))
                .isNotEmpty()
                .contains("pokemon@gmail.com")
                .doesNotContain("admin@gmail.com");

        mockMvc.perform(get("/reserveringen/{id}", reservationId)
                        .header(HttpHeaders.AUTHORIZATION, userAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId))
                .andExpect(jsonPath("$.kamerNaam").value("kamer2"))
                .andExpect(jsonPath("$.userEmail").value("pokemon@gmail.com"));

        ReservatieDto updateDto = reservationRequest(LocalTime.of(10, 0), LocalTime.of(11, 0));
        mockMvc.perform(put("/reserveringen/{id}", reservationId)
                        .header(HttpHeaders.AUTHORIZATION, userAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTijd").exists())
                .andExpect(jsonPath("$.eindTijd").exists());

        mockMvc.perform(delete("/reserveringen/{id}", reservationId)
                        .header(HttpHeaders.AUTHORIZATION, userAuthorization))
                .andExpect(status().isNoContent());

        assertThat(reserveringRepo.findById(reservationId)).isEmpty();

        mockMvc.perform(get("/reserveringen/{id}", reservationId)
                        .header(HttpHeaders.AUTHORIZATION, userAuthorization))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatingReservationRejectsOverlap() throws Exception {
        String userAuthorization = bearerToken("pokemon@gmail.com", "Pokemon!23");
        Long reservationId = createReservation(userAuthorization, "pokemon@gmail.com", LocalTime.of(9, 0), LocalTime.of(10, 0));

        ReservatieDto overlapDto = reservationRequest(LocalTime.of(8, 30), LocalTime.of(9, 30));
        mockMvc.perform(put("/reserveringen/{id}", reservationId)
                        .header(HttpHeaders.AUTHORIZATION, userAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlapDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void anotherUserCannotReadReservationTheyDoNotOwn() throws Exception {
        String ownerAuthorization = bearerToken("pokemon@gmail.com", "Pokemon!23");
        Long reservationId = createReservation(ownerAuthorization, "pokemon@gmail.com", LocalTime.of(11, 0), LocalTime.of(12, 0));

        String email = "viewer" + UUID.randomUUID().toString().replace("-", "") + "@example.com";
        registerUser(email, "StrongPass1");
        String viewerAuthorization = bearerToken(email, "StrongPass1");

        mockMvc.perform(get("/reserveringen/{id}", reservationId)
                        .header(HttpHeaders.AUTHORIZATION, viewerAuthorization))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void unauthenticatedReservationListIsRejected() throws Exception {
        mockMvc.perform(get("/reserveringen"))
                .andExpect(status().isUnauthorized());
    }

    private Long createReservation(String authorizationHeader, String email, LocalTime start, LocalTime end) throws Exception {
        ReservatieDto dto = reservationRequest(start, end);

        MvcResult result = mockMvc.perform(post("/reserveringen")
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
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

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    private String bearerToken(String username, String password) throws Exception {
        UserLoginDto dto = new UserLoginDto(username, password);
        MvcResult result = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        return "Bearer " + objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
