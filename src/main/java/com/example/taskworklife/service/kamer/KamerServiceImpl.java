package com.example.taskworklife.service.kamer;

import com.example.taskworklife.config.ReserveringConfiguration;
import com.example.taskworklife.converter.KamerDtoToKamer;
import com.example.taskworklife.converter.KamerToKamerDto;
import com.example.taskworklife.converter.ReserveringDtoToReservering;
import com.example.taskworklife.dto.kamer.KamerDto;
import com.example.taskworklife.dto.user.ReservatieDto;
import com.example.taskworklife.exception.kamer.*;
import com.example.taskworklife.exception.user.EmailNotFoundException;
import com.example.taskworklife.fileservice.FileService;
import com.example.taskworklife.models.FileAttachment;
import com.example.taskworklife.models.Kamer;
import com.example.taskworklife.models.Reservering;
import com.example.taskworklife.models.user.User;
import com.example.taskworklife.repo.FileAttachmentRepo;
import com.example.taskworklife.repo.KamerRepo;


import com.example.taskworklife.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class KamerServiceImpl implements KamerService {
    private final KamerRepo kamerRepo;
    KamerDtoToKamer kamerDtoToKamer;
    KamerToKamerDto kamerToKamerDto;
    ReserveringDtoToReservering reserveringDtoToReservering;
    UserService userService;

    FileAttachmentRepo fileAttachmentRepository;
    FileService fileService;
    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    public KamerServiceImpl(KamerRepo kamerRepo, KamerDtoToKamer kamerDtoToKamer, KamerToKamerDto kamerToKamerDto, ReserveringDtoToReservering reserveringDtoToReservering, FileService fileService, FileAttachmentRepo fileAttachmentRepository, UserService userService) {
        this.kamerRepo = kamerRepo;
        this.kamerDtoToKamer = kamerDtoToKamer;
        this.kamerToKamerDto = kamerToKamerDto;
        this.reserveringDtoToReservering = reserveringDtoToReservering;
        this.fileService = fileService;
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.userService = userService;
    }

    //    @Override
    public List<Kamer> getKamers() {
        List<Kamer> kamerList = new ArrayList<>();
        kamerRepo.findAll().iterator().forEachRemaining(kamerList::add);
//        LocalDateTime localDateTime = LocalDateTime.now();
//        kamerRepo.findAll().iterator().forEachRemaining((kamer) ->{
//            kamer.getId().iterator().forEachRemaining((reservering) ->{
//
//            });
//        });
//        Kamer kamer =new Kamer();

        return kamerList;
    }


    public List<Object> getAllKamerReservationsOnCertainDay(String naam, Date date) throws KamerNotFoundException, KamerNaamNotFoundException {
        getKamerByNaam(naam);
        var getAllReserveringenOnSpeicifedDay = kamerRepo.findByNaamAndGetAllReserveringenOnSpeicifedDay(date, naam);
        if (getAllReserveringenOnSpeicifedDay.isEmpty()) {
            return new ArrayList<Object>();
        } else {
            return getAllReserveringenOnSpeicifedDay.get();
        }
    }


    @Override
    public Kamer getKamerByNaam(String naam) throws KamerNotFoundException, KamerNaamNotFoundException {
        if (!StringUtils.isNotBlank(naam) || naam.equalsIgnoreCase("undefined")) {
            throw new KamerNaamNotFoundException("Naam is leeg");
        }
        Kamer kamerByNaam = kamerRepo.findByNaam(naam);
        if (kamerByNaam == null) {
            throw new KamerNotFoundException("Kamer niet gevonden");
        }
        return kamerByNaam;
    }

    @Override
    public void maakNieuweKamerAan(KamerDto kamerDto) throws KamerAlreadyExist, KamerNaamNotFoundException {
        if (!StringUtils.isNotBlank(kamerDto.getNaam())) {
            throw new KamerNaamNotFoundException("Naam niet gevonden");
        }
        Kamer kamerByNaam = kamerRepo.findByNaam(kamerDto.getNaam());
        if (kamerByNaam == null) {
            Kamer kamer = kamerDtoToKamer.convert(kamerDto);
            if (kamer != null) {
                LOGGER.info("Kamer toegevoegd met naam " + kamerDto.getNaam());
                kamerRepo.save(kamer);
            }
        } else {
            throw new KamerAlreadyExist("Kamer bestaat al");
        }
    }

    @Override
    public void editKamer(KamerDto kamerDto, String vorigNaam) throws KamerNotFoundException, KamerAlreadyExist, KamerNaamNotFoundException {
        if (!StringUtils.isNotBlank(vorigNaam)) {
            throw new KamerNaamNotFoundException("Vorige naam niet gevonden");
        }
        Kamer kamerByNaam = kamerRepo.findByNaam(vorigNaam);
        if (kamerByNaam != null) {
            Kamer kamer = kamerDtoToKamer.convert(kamerDto);
            if (kamer != null) {
                kamer.setId(kamerByNaam.getId());
                LOGGER.info("Kamer veranderd met naam " + kamerDto.getNaam());
                kamerRepo.save(kamer);
            }
        } else {
            throw new KamerNotFoundException("Kamer niet gevonden");
        }
    }

    @Override
    public void deleteKamerByNaam(String naam) throws KamerNotFoundException, KamerNaamNotFoundException {
        Kamer kamerByNaam = getKamerByNaam(naam);
        LOGGER.info("Kamer verwijderd met naam " + naam);
        kamerRepo.delete(kamerByNaam);
    }

    @Override
    public void reserveerKamer(String kamerNaam, ReservatieDto reservatieDto, String email) throws KamerNaamNotFoundException, KamerNaamIsLeegException, KamerNotFoundException, EindTijdIsBeforeStartTijd, KamerReserveringBestaat, EmailNotFoundException {
        //check of kamer bestaat
        User user = userService.findUserByEmail(email);
        Kamer kamerByNaam = getKamerByNaam(kamerNaam);
        //check of het niet een lege string is of null
        if (!StringUtils.isNotBlank(kamerNaam)) {
            throw new KamerNaamIsLeegException("Kamer naam is leeg");
        }
        //check voor overlap de reserveringlijst van de kamer.
        for (Reservering reservering : kamerByNaam.getReservering()) {
            if (reservatieDto.getStartTijd().isBefore(reservering.getEnd()) && reservatieDto.getEindTijd().isAfter(reservering.getStart())) {
                throw new KamerReserveringBestaat("De reservering bestaat al op dit tijdstip");
            }
        }
        Reservering convertedReservatie = reserveringDtoToReservering.convert(reservatieDto);
        if (convertedReservatie != null) {
            convertedReservatie.setUser(user);
            kamerByNaam.addReservering(convertedReservatie);
        }
//        kamerByNaam.setReservering(reserveringList);
        LOGGER.info("reservatie toegevoegd aan kamer met naam: " + kamerNaam);
        kamerRepo.save(kamerByNaam);
    }


}
