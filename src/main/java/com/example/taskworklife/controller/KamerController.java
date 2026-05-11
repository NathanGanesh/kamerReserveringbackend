package com.example.taskworklife.controller;

import com.example.taskworklife.dto.kamer.KamerDto;
import com.example.taskworklife.dto.reservering.ReserveringResponseDto;
import com.example.taskworklife.dto.user.ReservatieDto;
import com.example.taskworklife.exception.ExceptionHandlingKamer;
import com.example.taskworklife.exception.kamer.EindTijdIsBeforeStartTijd;
import com.example.taskworklife.exception.kamer.KamerAlreadyExist;
import com.example.taskworklife.exception.kamer.KamerNaamIsLeegException;
import com.example.taskworklife.exception.kamer.KamerNaamNotFoundException;
import com.example.taskworklife.exception.kamer.KamerNotFoundException;
import com.example.taskworklife.exception.kamer.KamerReserveringBestaat;
import com.example.taskworklife.exception.user.EmailNotFoundException;
import com.example.taskworklife.models.Kamer;
import com.example.taskworklife.service.kamer.KamerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping(path = "/kamers")
@CrossOrigin(origins = "http://localhost:3000")
public class KamerController extends ExceptionHandlingKamer {

    private final KamerService kamerService;

    @Autowired
    public KamerController(KamerService kamerService) {
        this.kamerService = kamerService;
    }

    @GetMapping("")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<List<Kamer>> getKamers() {
        return new ResponseEntity<>(kamerService.getKamers(), HttpStatus.OK);
    }

    @GetMapping("/{kamerNaam}")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<Kamer> getKamerMetNaam(@PathVariable String kamerNaam) throws KamerNotFoundException, KamerNaamNotFoundException {
        return new ResponseEntity<>(kamerService.getKamerByNaam(kamerNaam), HttpStatus.OK);
    }

    @PostMapping("")
    @CrossOrigin(origins = "http://localhost:3000")
    public void maakNieuweKamerAan(@RequestBody KamerDto kamerDto) throws KamerAlreadyExist, KamerNotFoundException, IOException, KamerNaamNotFoundException {
        kamerService.maakNieuweKamerAan(kamerDto);
    }

    @GetMapping("/{kamerNaam}/reserveringen/{datum}")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<List<ReserveringResponseDto>> getAllKamerByNaamAndGetAllReserverationsOnCertainDay(@PathVariable("kamerNaam") String kamerNaam, @PathVariable("datum") String datum) throws KamerNaamNotFoundException, KamerNotFoundException, ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        java.util.Date date2 = formatter.parse(datum);
        java.sql.Date sqlDate = new Date(date2.getTime());
        return new ResponseEntity<>(kamerService.getAllKamerReservationsOnCertainDay(kamerNaam, sqlDate), HttpStatus.OK);
    }

    @PutMapping("/{vorigeNaam}")
    @CrossOrigin(origins = "http://localhost:3000")
    public void editKamer(@RequestBody KamerDto kamerDto, @PathVariable("vorigeNaam") String vorigeNaam) throws KamerAlreadyExist, KamerNotFoundException, KamerNaamNotFoundException {
        kamerService.editKamer(kamerDto, vorigeNaam);
    }

    @DeleteMapping("/{naam}")
    @CrossOrigin(origins = "http://localhost:3000")
    public void deleteKamer(@PathVariable("naam") String naam) throws KamerNotFoundException, KamerNaamNotFoundException {
        kamerService.deleteKamerByNaam(naam);
    }

    @PostMapping("/{naam}/reserveer")
    @CrossOrigin(origins = "http://localhost:3000")
    public void reserveerKamer(@PathVariable("naam") String kamerNaam, @Valid @RequestBody ReservatieDto reservatieDto, Authentication authentication) throws KamerReserveringBestaat, EindTijdIsBeforeStartTijd, KamerNaamIsLeegException, KamerNaamNotFoundException, KamerNotFoundException, EmailNotFoundException {
        kamerService.reserveerKamer(kamerNaam, reservatieDto, authentication.getName());
    }
}
