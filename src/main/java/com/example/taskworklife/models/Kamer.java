package com.example.taskworklife.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "kamer")
@RequiredArgsConstructor
public class Kamer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;

    private String naam;
    private LocalDateTime sluitTijd;
    private LocalDateTime startTijd;

    @OneToMany(mappedBy = "kamer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Reservering> reservering = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "kamer")
    @JsonManagedReference
    private List<FileAttachment> attachments = new ArrayList<>();

    public Kamer addFileAttachment(FileAttachment fileAttachment) {
        fileAttachment.setKamer(this);
        this.attachments.add(fileAttachment);
        return this;
    }

    public Kamer addReservering(Reservering reservering) {
        reservering.setKamer(this);
        if (!this.reservering.contains(reservering)) {
            this.reservering.add(reservering);
        }
        return this;
    }

    public Kamer removeReservering(Reservering reservering) {
        this.reservering.remove(reservering);
        reservering.setKamer(null);
        return this;
    }
}
