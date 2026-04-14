package com.example.taskworklife.models.user;

import com.example.taskworklife.models.Reservering;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long id;

    @NotNull(message = "{reservering.constraints.username.NotNull.message}")
    private String naam;

    @NotNull
    private String achternaam;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull
    @Size(min = 8, max = 255)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "{reservering.constraints.password.Pattern.message}")
    private String wachtwoord;

    @NotNull
    @Pattern(regexp = "\\w+@\\w+\\.\\w+(,\\s*\\w+@\\w+\\.\\w+)*", message = "not valid email")
    private String email;

    private String profileImageUrl;
    private Date laatstIngelodgeDatum;
    private Date laatstIngelodgeDatumDisplay;
    private Date joinDate;
    private String role;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> authorities = new ArrayList<>();

    private boolean isActive;
    private boolean isNotLocked;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Reservering> reserveringArrayList = new ArrayList<>();

    public User addReservering(Reservering reservering) {
        reservering.setUser(this);
        if (!this.reserveringArrayList.contains(reservering)) {
            this.reserveringArrayList.add(reservering);
        }
        return this;
    }

    public User removeReservering(Reservering reservering) {
        this.reserveringArrayList.remove(reservering);
        reservering.setUser(null);
        return this;
    }
}
