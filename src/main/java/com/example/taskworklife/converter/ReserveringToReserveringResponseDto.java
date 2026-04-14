package com.example.taskworklife.converter;

import com.example.taskworklife.dto.reservering.ReserveringResponseDto;
import com.example.taskworklife.models.Reservering;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class ReserveringToReserveringResponseDto implements Converter<Reservering, ReserveringResponseDto> {
    @Nullable
    @Override
    public ReserveringResponseDto convert(Reservering source) {
        ReserveringResponseDto dto = new ReserveringResponseDto();
        dto.setId(source.getId());
        dto.setKamerNaam(source.getKamer() == null ? null : source.getKamer().getNaam());
        dto.setUserEmail(source.getUser() == null ? null : source.getUser().getEmail());
        dto.setStartTijd(source.getStart());
        dto.setEindTijd(source.getEnd());
        return dto;
    }
}
