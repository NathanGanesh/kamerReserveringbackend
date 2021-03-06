package com.example.taskworklife.bootstrap;

import com.example.taskworklife.models.FileAttachment;
import com.example.taskworklife.models.Kamer;
import com.example.taskworklife.models.Reservering;
import com.example.taskworklife.models.user.User;
import com.example.taskworklife.repo.FileAttachmentRepo;
import com.example.taskworklife.repo.KamerRepo;
import com.example.taskworklife.repo.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
@Profile({"test", "dev", "default"})
public class KamerBootstrap implements ApplicationListener<ContextRefreshedEvent> {
    private final KamerRepo kamerRepo;
    private final FileAttachmentRepo fileAttachmentRepo;
    private final UserRepo userRepo;

    @Autowired
    public KamerBootstrap(KamerRepo kamerRepo, FileAttachmentRepo fileAttachmentRepo, UserRepo userRepo) {
        this.kamerRepo = kamerRepo;
        this.fileAttachmentRepo = fileAttachmentRepo;
        this.userRepo = userRepo;
    }

    private List<Kamer> getKamers() {
        List<Kamer> kamers = new ArrayList<>();
        User userByEmail = userRepo.findUserByEmail("admin@gmail.com");
        Kamer kamer = new Kamer();
        //kamer 1
        kamer.setNaam("Kamer1");
        //kamer 1 reservering
        List<Reservering> reserveringListKamer1 = new ArrayList<>();
        Reservering reservering1Kamer1 = new Reservering();
        reservering1Kamer1.setStart(LocalDateTime.of(LocalDate.of(2021, Month.OCTOBER, 20), LocalTime.of(7, 0)));
        reservering1Kamer1.setEnd(LocalDateTime.of(LocalDate.of(2021, Month.OCTOBER, 20), LocalTime.of(8, 0)));

        Reservering reservering1Kamer2 = new Reservering();
        reservering1Kamer2.setStart(LocalDateTime.of(LocalDate.of(2021, Month.OCTOBER, 20), LocalTime.of(8, 0)));
        reservering1Kamer2.setEnd(LocalDateTime.of(LocalDate.of(2021, Month.OCTOBER, 20), LocalTime.of(9, 0)));

        kamer.setStartTijd(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0)));
        kamer.setSluitTijd(LocalDateTime.of(LocalDate.now(), LocalTime.of(20, 0)));
        reservering1Kamer1.setKamer(kamer);
        reservering1Kamer2.setKamer(kamer);
        reserveringListKamer1.add(reservering1Kamer1);
        reserveringListKamer1.add(reservering1Kamer2);
        kamer.setReservering(reserveringListKamer1);

        Kamer kamer2 = new Kamer();
        kamer2.setNaam("kamer2");
        kamer2.setStartTijd(LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0)));
        kamer2.setSluitTijd(LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 0)));
        //kamer 1 reservering
        List<Reservering> reserveringListKamer2 = new ArrayList<>();

        Reservering reservering2Kamer1 = new Reservering();
        reservering2Kamer1.setStart(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)));
        reservering2Kamer1.setEnd(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0)));

//        reservering2Kamer1.setUser(userByEmail);

        Reservering reservering2Kamer2 = new Reservering();
        reservering2Kamer2.setStart(LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0)));
        reservering2Kamer2.setEnd(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)));

        Reservering reservering2Kamer3 = new Reservering();
        reservering2Kamer3.setStart(LocalDateTime.of(LocalDate.now(), LocalTime.of(16, 0)));
        reservering2Kamer3.setEnd(LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 0)));

//        reservering2Kamer2.setUser(userByEmail);


        reservering2Kamer1.setKamer(kamer2);
        reservering2Kamer2.setKamer(kamer2);
        reservering2Kamer3.setKamer(kamer2);
        reservering2Kamer1.setUser(userByEmail);
        reservering2Kamer2.setUser(userByEmail);
        reservering2Kamer3.setUser(userByEmail);

        reserveringListKamer2.add(reservering2Kamer1);
        reserveringListKamer2.add(reservering2Kamer2);
        reserveringListKamer2.add(reservering2Kamer3);

        kamer2.setReservering(reserveringListKamer2);


        FileAttachment fileAttachment = new FileAttachment();

        fileAttachment.setName("Monkey_chathead.png");

        fileAttachment.setFileType("image/png");

        fileAttachment.setDate(new Date());

        kamer2.addFileAttachment(fileAttachment);
        kamers.add(kamer2);
        kamers.add(kamer);


        return kamers;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        kamerRepo.saveAll(getKamers());
    }
}
