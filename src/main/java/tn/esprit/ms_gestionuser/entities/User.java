package tn.esprit.ms_gestionuser.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "utilisateurs")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idUser;

    // === Lien Keycloak ===
    @Column(unique = true)
    String keycloakId;

    // === Champs communs ===
    String nom;

    String prenom;

    @Column(unique = true, nullable = false)
    String email;

    String telephone;

    @Enumerated(EnumType.STRING)
    RoleType role;

    LocalDateTime createdAt;

    boolean isActif = true;

    // === Champs spécifiques DONOR ===
    String donorCompanyName;
    String taxIdNumber;
    String address;

    // === Champs spécifiques RECEIVER (Association) ===
    String associationName;
    Double reputationScore;
    String documentUrl;

    // === Champs spécifiques TRANSPORTER ===
    String transporterCompanyName;
    String vehicleType;
    Double capacity;

    // === Champs spécifiques AUDITOR ===
    String certificationNumber;
    String agencyName;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
