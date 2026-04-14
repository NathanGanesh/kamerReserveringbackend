package com.example.taskworklife.models;

import com.example.taskworklife.models.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Setter
public class Reservering {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;

    private LocalDateTime start;
    private LocalDateTime end;

    @JsonBackReference
    @ManyToOne
    private Kamer kamer;

    @JsonBackReference
    @ManyToOne
    private User user;
}
