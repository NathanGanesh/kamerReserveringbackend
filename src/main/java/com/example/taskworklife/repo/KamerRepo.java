package com.example.taskworklife.repo;


import com.example.taskworklife.models.Kamer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface KamerRepo extends JpaRepository<Kamer, Long> {
        Kamer findByNaam(String kamerNaam);

//        @Query(value="from Kamer t INNER JOIN Reservering ON Kamer.id = Reservering.kamer.id where :dateToFilter BETWEEN cast(Reservering.start as Date)as start AND cast(Reservering.END as Date)")
//        Kamer findByNaamAndGetAllReserveringenOnSpeicifedDay(@Param("dateToFilter") String dateToFilter);

        @Query(value = "SELECT p.end as end , p.start as start FROM Kamer b,Reservering p WHERE (b.id = p.kamer.id) AND (b.naam = :naam AND(:date between cast(p.start as date)   AND cast(p.end as date)) )")
        Optional<List<Object>> findByNaamAndGetAllReserveringenOnSpecificDay(@Param("date") Date date, @Param("naam") String naam);

}
