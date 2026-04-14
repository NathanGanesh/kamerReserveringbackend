package com.example.taskworklife.service.kamer;

import com.example.taskworklife.converter.KamerDtoToKamer;
import com.example.taskworklife.dto.kamer.KamerDto;
import com.example.taskworklife.dto.reservering.ReserveringResponseDto;
import com.example.taskworklife.dto.user.ReservatieDto;
import com.example.taskworklife.exception.kamer.EindTijdIsBeforeStartTijd;
import com.example.taskworklife.exception.kamer.KamerAlreadyExist;
import com.example.taskworklife.exception.kamer.KamerNaamIsLeegException;
import com.example.taskworklife.exception.kamer.KamerNaamNotFoundException;
import com.example.taskworklife.exception.kamer.KamerNotFoundException;
import com.example.taskworklife.exception.kamer.KamerReserveringBestaat;
import com.example.taskworklife.exception.user.EmailNotFoundException;
import com.example.taskworklife.models.Kamer;
import com.example.taskworklife.repo.KamerRepo;
import com.example.taskworklife.service.reservering.ReserveringService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class KamerServiceImpl implements KamerService {
    private final KamerRepo kamerRepo;
    private final KamerDtoToKamer kamerDtoToKamer;
    private final ReserveringService reserveringService;
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    public KamerServiceImpl(KamerRepo kamerRepo, KamerDtoToKamer kamerDtoToKamer, ReserveringService reserveringService) {
        this.kamerRepo = kamerRepo;
        this.kamerDtoToKamer = kamerDtoToKamer;
        this.reserveringService = reserveringService;
    }

    @Override
    public List<Kamer> getKamers() {
        List<Kamer> kamerList = new ArrayList<>();
        kamerRepo.findAll().iterator().forEachRemaining(kamerList::add);
        return kamerList;
    }

    @Override
    public List<ReserveringResponseDto> getAllKamerReservationsOnCertainDay(String naam, Date date) throws KamerNotFoundException, KamerNaamNotFoundException {
        return reserveringService.getReserveringenForKamerOnDate(naam, date);
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
        if (kamerByNaam != null) {
            throw new KamerAlreadyExist("Kamer bestaat al");
        }

        Kamer kamer = kamerDtoToKamer.convert(kamerDto);
        if (kamer != null) {
            LOGGER.info("Kamer toegevoegd met naam " + kamerDto.getNaam());
            kamerRepo.save(kamer);
        }
    }

    @Override
    public void editKamer(KamerDto kamerDto, String vorigNaam) throws KamerNotFoundException, KamerAlreadyExist, KamerNaamNotFoundException {
        if (!StringUtils.isNotBlank(vorigNaam)) {
            throw new KamerNaamNotFoundException("Vorige naam niet gevonden");
        }
        Kamer kamerByNaam = kamerRepo.findByNaam(vorigNaam);
        if (kamerByNaam == null) {
            throw new KamerNotFoundException("Kamer niet gevonden");
        }

        Kamer kamer = kamerDtoToKamer.convert(kamerDto);
        if (kamer != null) {
            kamer.setId(kamerByNaam.getId());
            LOGGER.info("Kamer veranderd met naam " + kamerDto.getNaam());
            kamerRepo.save(kamer);
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
        reserveringService.createReservering(kamerNaam, reservatieDto, email);
        LOGGER.info("reservatie toegevoegd aan kamer met naam: " + kamerNaam);
    }
}
