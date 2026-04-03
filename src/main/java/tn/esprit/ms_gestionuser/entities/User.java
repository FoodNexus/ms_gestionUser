package tn.esprit.ms_gestionuser.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    // NOUVEAU : C'est le lien avec le compte Keycloak (l'UUID)
    @Column(unique = true, nullable = false)
    String keycloakId;

    String nom;

    String prenom;

    @Column(unique = true, nullable = false)
    String email;

    // L'attribut password a été supprimé ici !

    String telephone;

    @Enumerated(EnumType.STRING)
    RoleType role;

    boolean isActif = true;
}