package com.example.taskworklife;

import com.example.taskworklife.dto.kamer.KamerDto;
import com.example.taskworklife.dto.user.ReservatieDto;
import com.example.taskworklife.models.Kamer;
import com.example.taskworklife.repo.KamerRepo;
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
import java.time.format.DateTimeFormatter;
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
class KamerControllerIntegrationTest {
    private static final DateTimeFormatter RESERVATION_DAY_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KamerRepo kamerRepo;

    @Test
    void getKamersReturnsSeededRoomsForAuthorizedUser() throws Exception {
        mockMvc.perform(get("/kamer/all").header(HttpHeaders.AUTHORIZATION, basicAuth("pokemon@gmail.com", "Pokemon!23")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].naam").exists());
    }

    @Test
    void adminCanCreateReadUpdateAndDeleteKamer() throws Exception {
        String uniqueName = "kamer-" + UUID.randomUUID().toString().substring(0, 8);
        String updatedName = uniqueName + "-updated";
        KamerDto dto = kamerRequest(uniqueName, LocalDateTime.of(2026, 4, 3, 8, 0), LocalDateTime.of(2026, 4, 3, 18, 0));

        mockMvc.perform(
                post("/kamer/new")
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("admin@gmail.com", "AdminUser!1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isOk());

        Kamer createdKamer = kamerRepo.findByNaam(uniqueName);
        assertThat(createdKamer).isNotNull();

        mockMvc.perform(get("/kamer/{kamerNaam}", uniqueName)
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("pokemon@gmail.com", "Pokemon!23")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naam").value(uniqueName));

        KamerDto updateDto = kamerRequest(updatedName, LocalDateTime.of(2026, 4, 3, 9, 0), LocalDateTime.of(2026, 4, 3, 19, 0));
        mockMvc.perform(put("/kamer/edit/{vorigeNaam}", uniqueName)
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("admin@gmail.com", "AdminUser!1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/kamer/{kamerNaam}", updatedName)
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("pokemon@gmail.com", "Pokemon!23")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naam").value(updatedName));

        mockMvc.perform(delete("/kamer/delete/{naam}", updatedName)
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("admin@gmail.com", "AdminUser!1")))
                .andExpect(status().isOk());

        assertThat(kamerRepo.findByNaam(updatedName)).isNull();
    }

    @Test
    void kamerReservationsByDayReturnsExistingReservations() throws Exception {
        String today = LocalDate.now().format(RESERVATION_DAY_FORMAT);

        MvcResult result = mockMvc.perform(get("/kamer/{kamerNaam}/reserveringen/{datum}", "Kamer1", today)
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("pokemon@gmail.com", "Pokemon!23")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].startTijd").exists())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains(LocalDate.now().toString());
    }

    @Test
    void reserveerKamerRejectsOverlappingReservation() throws Exception {
        ReservatieDto dto = new ReservatieDto();
        dto.setStartTijd(LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 30)));
        dto.setEindTijd(LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 45)));

        mockMvc.perform(
                post("/kamer/Kamer1/reserveer")
                        .header(HttpHeaders.AUTHORIZATION, basicAuth("pokemon@gmail.com", "Pokemon!23"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    private String basicAuth(String username, String password) {
        String token = Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }

    private KamerDto kamerRequest(String naam, LocalDateTime start, LocalDateTime sluit) {
        KamerDto dto = new KamerDto();
        dto.setNaam(naam);
        dto.setStart(start);
        dto.setSluit(sluit);
        return dto;
    }
}
