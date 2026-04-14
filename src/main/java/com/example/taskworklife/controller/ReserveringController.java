package com.example.taskworklife.controller;

import com.example.taskworklife.dto.reservering.ReserveringResponseDto;
import com.example.taskworklife.dto.user.ReservatieDto;
import com.example.taskworklife.exception.kamer.EindTijdIsBeforeStartTijd;
import com.example.taskworklife.exception.kamer.KamerNaamIsLeegException;
import com.example.taskworklife.exception.kamer.KamerNaamNotFoundException;
import com.example.taskworklife.exception.kamer.KamerNotFoundException;
import com.example.taskworklife.exception.kamer.KamerReserveringBestaat;
import com.example.taskworklife.exception.reservering.ReserveringNotFoundException;
import com.example.taskworklife.exception.user.EmailNotFoundException;
import com.example.taskworklife.service.reservering.ReserveringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/reservering")
@CrossOrigin(origins = "http://localhost:3000")
public class ReserveringController {
    private final ReserveringService reserveringService;

    @Autowired
    public ReserveringController(ReserveringService reserveringService) {
        this.reserveringService = reserveringService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReserveringResponseDto>> getReserveringen(Authentication authentication) {
        boolean admin = isAdmin(authentication);
        return new ResponseEntity<>(reserveringService.getReserveringen(authentication.getName(), admin), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReserveringResponseDto> getReserveringById(@PathVariable Long id, Authentication authentication) throws ReserveringNotFoundException {
        boolean admin = isAdmin(authentication);
        return new ResponseEntity<>(reserveringService.getReserveringById(id, authentication.getName(), admin), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ReserveringResponseDto> createReservering(@Valid @RequestBody ReservatieDto reservatieDto, Authentication authentication) throws KamerReserveringBestaat, EindTijdIsBeforeStartTijd, KamerNaamIsLeegException, KamerNaamNotFoundException, KamerNotFoundException, EmailNotFoundException {
        return new ResponseEntity<>(reserveringService.createReservering(reservatieDto, authentication.getName()), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReserveringResponseDto> updateReservering(@PathVariable Long id, @Valid @RequestBody ReservatieDto reservatieDto, Authentication authentication) throws KamerReserveringBestaat, EindTijdIsBeforeStartTijd, KamerNaamIsLeegException, KamerNaamNotFoundException, KamerNotFoundException, EmailNotFoundException {
        boolean admin = isAdmin(authentication);
        return new ResponseEntity<>(reserveringService.updateReservering(id, reservatieDto, authentication.getName(), admin), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservering(@PathVariable Long id, Authentication authentication) throws ReserveringNotFoundException {
        boolean admin = isAdmin(authentication);
        reserveringService.deleteReservering(id, authentication.getName(), admin);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(grantedAuthority -> "userAdmin:read".equals(grantedAuthority.getAuthority()));
    }
}
