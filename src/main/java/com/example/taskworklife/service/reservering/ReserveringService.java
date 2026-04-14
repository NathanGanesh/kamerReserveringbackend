package com.example.taskworklife.service.reservering;

import com.example.taskworklife.dto.reservering.ReserveringResponseDto;
import com.example.taskworklife.dto.user.ReservatieDto;
import com.example.taskworklife.exception.kamer.EindTijdIsBeforeStartTijd;
import com.example.taskworklife.exception.kamer.KamerNaamIsLeegException;
import com.example.taskworklife.exception.kamer.KamerNaamNotFoundException;
import com.example.taskworklife.exception.kamer.KamerNotFoundException;
import com.example.taskworklife.exception.kamer.KamerReserveringBestaat;
import com.example.taskworklife.exception.reservering.ReserveringNotFoundException;
import com.example.taskworklife.exception.user.EmailNotFoundException;

import java.sql.Date;
import java.util.List;

public interface ReserveringService {
    List<ReserveringResponseDto> getReserveringen(String email, boolean admin);

    ReserveringResponseDto getReserveringById(Long id, String email, boolean admin) throws ReserveringNotFoundException;

    ReserveringResponseDto createReservering(ReservatieDto reservatieDto, String email) throws KamerReserveringBestaat, EindTijdIsBeforeStartTijd, KamerNaamIsLeegException, KamerNaamNotFoundException, KamerNotFoundException, EmailNotFoundException;

    ReserveringResponseDto createReservering(String kamerNaam, ReservatieDto reservatieDto, String email) throws KamerReserveringBestaat, EindTijdIsBeforeStartTijd, KamerNaamIsLeegException, KamerNaamNotFoundException, KamerNotFoundException, EmailNotFoundException;

    ReserveringResponseDto updateReservering(Long id, ReservatieDto reservatieDto, String email, boolean admin) throws KamerReserveringBestaat, EindTijdIsBeforeStartTijd, KamerNaamIsLeegException, KamerNaamNotFoundException, KamerNotFoundException, EmailNotFoundException;

    void deleteReservering(Long id, String email, boolean admin) throws ReserveringNotFoundException;

    List<ReserveringResponseDto> getReserveringenForKamerOnDate(String kamerNaam, Date date) throws KamerNotFoundException, KamerNaamNotFoundException;
}
