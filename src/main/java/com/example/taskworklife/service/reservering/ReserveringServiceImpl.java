package com.example.taskworklife.service.reservering;

import com.example.taskworklife.converter.ReserveringDtoToReservering;
import com.example.taskworklife.converter.ReserveringToReserveringResponseDto;
import com.example.taskworklife.dto.reservering.ReserveringResponseDto;
import com.example.taskworklife.dto.user.ReservatieDto;
import com.example.taskworklife.exception.kamer.EindTijdIsBeforeStartTijd;
import com.example.taskworklife.exception.kamer.KamerNaamIsLeegException;
import com.example.taskworklife.exception.kamer.KamerNaamNotFoundException;
import com.example.taskworklife.exception.kamer.KamerNotFoundException;
import com.example.taskworklife.exception.kamer.KamerReserveringBestaat;
import com.example.taskworklife.exception.reservering.ReservationAccessDeniedException;
import com.example.taskworklife.exception.reservering.ReserveringNotFoundException;
import com.example.taskworklife.exception.user.EmailNotFoundException;
import com.example.taskworklife.models.Kamer;
import com.example.taskworklife.models.Reservering;
import com.example.taskworklife.models.user.User;
import com.example.taskworklife.repo.KamerRepo;
import com.example.taskworklife.repo.ReserveringRepo;
import com.example.taskworklife.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReserveringServiceImpl implements ReserveringService {
    private final ReserveringRepo reserveringRepo;
    private final KamerRepo kamerRepo;
    private final UserService userService;
    private final ReserveringDtoToReservering reserveringDtoToReservering;
    private final ReserveringToReserveringResponseDto reserveringResponseDtoConverter;

    @Autowired
    public ReserveringServiceImpl(ReserveringRepo reserveringRepo, KamerRepo kamerRepo, UserService userService, ReserveringDtoToReservering reserveringDtoToReservering, ReserveringToReserveringResponseDto reserveringResponseDtoConverter) {
        this.reserveringRepo = reserveringRepo;
        this.kamerRepo = kamerRepo;
        this.userService = userService;
        this.reserveringDtoToReservering = reserveringDtoToReservering;
        this.reserveringResponseDtoConverter = reserveringResponseDtoConverter;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReserveringResponseDto> getReserveringen(String email, boolean admin) {
        List<Reservering> reserveringen = admin ? reserveringRepo.findAll() : reserveringRepo.findAllByUser_Email(email);
        return reserveringen.stream().map(reserveringResponseDtoConverter::convert).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReserveringResponseDto getReserveringById(Long id, String email, boolean admin) {
        Reservering reservering = getReserveringEntity(id);
        ensureReservationAccess(reservering, email, admin);
        return reserveringResponseDtoConverter.convert(reservering);
    }

    @Override
    public ReserveringResponseDto createReservering(ReservatieDto reservatieDto, String email) throws KamerReserveringBestaat, EindTijdIsBeforeStartTijd, KamerNaamIsLeegException, KamerNaamNotFoundException, KamerNotFoundException, EmailNotFoundException {
        return createReservering(reservatieDto.getKamerNaam(), reservatieDto, email);
    }

    @Override
    public ReserveringResponseDto createReservering(String kamerNaam, ReservatieDto reservatieDto, String email) throws KamerReserveringBestaat, EindTijdIsBeforeStartTijd, KamerNaamIsLeegException, KamerNaamNotFoundException, KamerNotFoundException, EmailNotFoundException {
        User user = userService.findUserByEmail(email);
        Kamer kamer = validateAndGetKamer(kamerNaam);
        validateOverlap(kamer, reservatieDto, null);

        Reservering reservering = reserveringDtoToReservering.convert(reservatieDto);
        if (reservering == null) {
            throw new IllegalStateException("Reservation conversion failed");
        }

        kamer.addReservering(reservering);
        user.addReservering(reservering);
        Reservering saved = reserveringRepo.save(reservering);
        return reserveringResponseDtoConverter.convert(saved);
    }

    @Override
    public ReserveringResponseDto updateReservering(Long id, ReservatieDto reservatieDto, String email, boolean admin) throws KamerReserveringBestaat, EindTijdIsBeforeStartTijd, KamerNaamIsLeegException, KamerNaamNotFoundException, KamerNotFoundException, EmailNotFoundException {
        Reservering existing = getReserveringEntity(id);
        ensureReservationAccess(existing, email, admin);

        String targetKamerNaam = StringUtils.isBlank(reservatieDto.getKamerNaam()) ? existing.getKamer().getNaam() : reservatieDto.getKamerNaam();
        Kamer kamer = validateAndGetKamer(targetKamerNaam);
        validateOverlap(kamer, reservatieDto, id);

        if (existing.getKamer() != null && !existing.getKamer().getId().equals(kamer.getId())) {
            existing.getKamer().removeReservering(existing);
            kamer.addReservering(existing);
        }
        existing.setStart(reservatieDto.getStartTijd());
        existing.setEnd(reservatieDto.getEindTijd());

        Reservering saved = reserveringRepo.save(existing);
        return reserveringResponseDtoConverter.convert(saved);
    }

    @Override
    public void deleteReservering(Long id, String email, boolean admin) {
        Reservering existing = getReserveringEntity(id);
        ensureReservationAccess(existing, email, admin);
        if (existing.getKamer() != null) {
            existing.getKamer().removeReservering(existing);
        }
        if (existing.getUser() != null) {
            existing.getUser().removeReservering(existing);
        }
        reserveringRepo.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReserveringResponseDto> getReserveringenForKamerOnDate(String kamerNaam, Date date) throws KamerNotFoundException, KamerNaamNotFoundException {
        Kamer kamer = validateAndGetKamer(kamerNaam);
        return kamer.getReservering().stream()
                .filter(reservering -> {
                    Date startDate = Date.valueOf(reservering.getStart().toLocalDate());
                    Date endDate = Date.valueOf(reservering.getEnd().toLocalDate());
                    return !date.before(startDate) && !date.after(endDate);
                })
                .map(reserveringResponseDtoConverter::convert)
                .collect(Collectors.toList());
    }

    private Reservering getReserveringEntity(Long id) {
        return reserveringRepo.findById(id).orElseThrow(() -> new ReserveringNotFoundException("Reservering niet gevonden"));
    }

    private Kamer validateAndGetKamer(String kamerNaam) throws KamerNaamNotFoundException, KamerNotFoundException {
        if (!StringUtils.isNotBlank(kamerNaam) || "undefined".equalsIgnoreCase(kamerNaam)) {
            throw new KamerNaamNotFoundException("Kamer naam is leeg");
        }
        Kamer kamer = kamerRepo.findByNaam(kamerNaam);
        if (kamer == null) {
            throw new KamerNotFoundException("Kamer niet gevonden");
        }
        return kamer;
    }

    private void validateOverlap(Kamer kamer, ReservatieDto reservatieDto, Long currentReservationId) throws KamerReserveringBestaat {
        for (Reservering reservering : kamer.getReservering()) {
            if (currentReservationId != null && currentReservationId.equals(reservering.getId())) {
                continue;
            }
            if (reservatieDto.getStartTijd().isBefore(reservering.getEnd()) && reservatieDto.getEindTijd().isAfter(reservering.getStart())) {
                throw new KamerReserveringBestaat("De reservering bestaat al op dit tijdstip");
            }
        }
    }

    private void ensureReservationAccess(Reservering reservering, String email, boolean admin) {
        if (admin) {
            return;
        }
        if (reservering.getUser() == null || !reservering.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new ReservationAccessDeniedException("Geen toegang tot deze reservering");
        }
    }
}
