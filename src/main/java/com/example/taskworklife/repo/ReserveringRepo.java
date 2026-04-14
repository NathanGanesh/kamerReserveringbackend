package com.example.taskworklife.repo;

import com.example.taskworklife.models.Reservering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReserveringRepo extends JpaRepository<Reservering, Long> {
    List<Reservering> findAllByUser_Email(String email);

    List<Reservering> findAllByKamer_Id(Long kamerId);
}
