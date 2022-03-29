package com.example.taskworklife.service.reservaties;

import com.example.taskworklife.exception.kamer.KamerIsNietGevonden;
import com.example.taskworklife.exception.kamer.KamerNaamLengteIsTeKlein;
import com.example.taskworklife.exception.kamer.KamerNaamNotFoundException;
import com.example.taskworklife.models.Reservering;
import com.example.taskworklife.models.user.UserPrincipal;
import org.springframework.data.domain.Page;

public interface ReservatiesService {
    Page<Reservering> getAllReservatiesByUser(UserPrincipal naam, String email, Integer pageSize, Integer pageNo) throws KamerIsNietGevonden, KamerNaamNotFoundException, KamerNaamLengteIsTeKlein;
}