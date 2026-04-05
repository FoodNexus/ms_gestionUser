package tn.esprit.ms_gestionuser.services;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import tn.esprit.ms_gestionuser.entities.RoleType;
import tn.esprit.ms_gestionuser.entities.User;
import tn.esprit.ms_gestionuser.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final Keycloak keycloak;

    @Value("${keycloak.admin.realm}")
    private String realm;


    /**
     * Récupère ou crée automatiquement un utilisateur à partir du token Keycloak.
     * Si l'utilisateur n'existe pas encore en base, il est créé avec les infos du token.
     */
    public User getOrCreateUser(Jwt jwt) {
        String keycloakId = jwt.getSubject();

        Optional<User> existingUser = userRepository.findByKeycloakId(keycloakId);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Créer l'utilisateur localement à partir des claims Keycloak
        User newUser = new User();
        newUser.setKeycloakId(keycloakId);
        newUser.setEmail(jwt.getClaimAsString("email"));
        newUser.setNom(jwt.getClaimAsString("family_name"));
        newUser.setPrenom(jwt.getClaimAsString("given_name"));
        newUser.setActif(true);

        // Déterminer le rôle depuis les claims Keycloak
        newUser.setRole(extractRoleFromJwt(jwt));

        return userRepository.save(newUser);
    }

    /**
     * Récupère un utilisateur par son ID Keycloak.
     */
    public User getUserByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec le keycloakId : " + keycloakId));
    }

    /**
     * Récupère tous les utilisateurs.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Met à jour le profil d'un utilisateur (champs métier uniquement).
     */
    public User updateUserProfile(String keycloakId, User updatedUser) {
        User currentUser = getUserByKeycloakId(keycloakId);

        if (updatedUser.getNom() != null) currentUser.setNom(updatedUser.getNom());
        if (updatedUser.getPrenom() != null) currentUser.setPrenom(updatedUser.getPrenom());
        if (updatedUser.getTelephone() != null) currentUser.setTelephone(updatedUser.getTelephone());
        if (updatedUser.getRole() != null) currentUser.setRole(updatedUser.getRole());

        // Champs DONOR
        if (updatedUser.getDonorCompanyName() != null) currentUser.setDonorCompanyName(updatedUser.getDonorCompanyName());
        if (updatedUser.getTaxIdNumber() != null) currentUser.setTaxIdNumber(updatedUser.getTaxIdNumber());
        if (updatedUser.getAddress() != null) currentUser.setAddress(updatedUser.getAddress());

        // Champs RECEIVER
        if (updatedUser.getAssociationName() != null) currentUser.setAssociationName(updatedUser.getAssociationName());
        if (updatedUser.getReputationScore() != null) currentUser.setReputationScore(updatedUser.getReputationScore());
        if (updatedUser.getDocumentUrl() != null) currentUser.setDocumentUrl(updatedUser.getDocumentUrl());

        // Champs TRANSPORTER
        if (updatedUser.getTransporterCompanyName() != null) currentUser.setTransporterCompanyName(updatedUser.getTransporterCompanyName());
        if (updatedUser.getVehicleType() != null) currentUser.setVehicleType(updatedUser.getVehicleType());
        if (updatedUser.getCapacity() != null) currentUser.setCapacity(updatedUser.getCapacity());

        // Champs AUDITOR
        if (updatedUser.getCertificationNumber() != null) currentUser.setCertificationNumber(updatedUser.getCertificationNumber());
        if (updatedUser.getAgencyName() != null) currentUser.setAgencyName(updatedUser.getAgencyName());

        currentUser = userRepository.save(currentUser);
        syncUserToKeycloak(currentUser);
        return currentUser;
    }

    /**
     * Supprime un utilisateur par son ID.
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur avec l'ID " + id + " introuvable !");
        }
        userRepository.deleteById(id);
    }

    /**
     * Met à jour le profil complet d'un utilisateur par son ID de base de données (sans Keycloak).
     */
    public User updateUserById(Long id, User updatedUser) {
        User currentUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID : " + id));

        if (updatedUser.getNom() != null) currentUser.setNom(updatedUser.getNom());
        if (updatedUser.getPrenom() != null) currentUser.setPrenom(updatedUser.getPrenom());
        if (updatedUser.getTelephone() != null) currentUser.setTelephone(updatedUser.getTelephone());
        if (updatedUser.getAddress() != null) currentUser.setAddress(updatedUser.getAddress());
        if (updatedUser.getRole() != null) currentUser.setRole(updatedUser.getRole());

        // Champs DONOR
        if (updatedUser.getDonorCompanyName() != null) currentUser.setDonorCompanyName(updatedUser.getDonorCompanyName());
        if (updatedUser.getTaxIdNumber() != null) currentUser.setTaxIdNumber(updatedUser.getTaxIdNumber());

        // Champs RECEIVER
        if (updatedUser.getAssociationName() != null) currentUser.setAssociationName(updatedUser.getAssociationName());
        if (updatedUser.getReputationScore() != null) currentUser.setReputationScore(updatedUser.getReputationScore());
        if (updatedUser.getDocumentUrl() != null) currentUser.setDocumentUrl(updatedUser.getDocumentUrl());

        // Champs TRANSPORTER
        if (updatedUser.getTransporterCompanyName() != null) currentUser.setTransporterCompanyName(updatedUser.getTransporterCompanyName());
        if (updatedUser.getVehicleType() != null) currentUser.setVehicleType(updatedUser.getVehicleType());
        if (updatedUser.getCapacity() != null) currentUser.setCapacity(updatedUser.getCapacity());

        // Champs AUDITOR
        if (updatedUser.getCertificationNumber() != null) currentUser.setCertificationNumber(updatedUser.getCertificationNumber());
        if (updatedUser.getAgencyName() != null) currentUser.setAgencyName(updatedUser.getAgencyName());

        currentUser = userRepository.save(currentUser);
        syncUserToKeycloak(currentUser);
        return currentUser;
    }

    /**
     * Bloque ou débloque un utilisateur (change isActif).
     */
    public void toggleUserStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID : " + id));
        user.setActif(active);
        userRepository.save(user);
    }


    /**
     * Extrait le rôle principal depuis les claims Keycloak (realm_access.roles).
     */
    @SuppressWarnings("unchecked")
    private RoleType extractRoleFromJwt(Jwt jwt) {
        try {
            java.util.Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null) {
                java.util.List<String> roles = (java.util.List<String>) realmAccess.get("roles");
                if (roles != null) {
                    for (String role : roles) {
                        try {
                            return RoleType.valueOf(role.toUpperCase());
                        } catch (IllegalArgumentException ignored) {
                            // Ce rôle Keycloak ne correspond pas à un RoleType, on continue
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        // Rôle par défaut si aucun rôle métier trouvé
        return RoleType.PENDING;
    }

    /**
     * Change le mot de passe de l'utilisateur dans Keycloak.
     */
    public void changePassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID : " + id));

        System.out.println("Attempting password change for: " + user.getEmail() + " (KID: " + user.getKeycloakId() + ")");

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(newPassword);
        credential.setTemporary(false);

        try {
            keycloak.realm(realm).users().get(user.getKeycloakId()).resetPassword(credential);
            System.out.println("✅ Mot de passe Keycloak mis à jour pour : " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ ÉCHEC changement mot de passe Keycloak : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Keycloak Error: " + e.getMessage());
        }
    }

    /**
     * Synchronise les informations de base (Nom/Prénom) vers Keycloak.
     */
    private void syncUserToKeycloak(User user) {
        try {
            UserRepresentation userRep = keycloak.realm(realm).users().get(user.getKeycloakId()).toRepresentation();
            userRep.setFirstName(user.getPrenom());
            userRep.setLastName(user.getNom());
            keycloak.realm(realm).users().get(user.getKeycloakId()).update(userRep);
            System.out.println("✅ Synchronisation Keycloak réussie pour : " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ ÉCHEC de synchronisation Keycloak pour " + user.getEmail() + " : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
