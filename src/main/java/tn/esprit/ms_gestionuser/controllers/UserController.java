package tn.esprit.ms_gestionuser.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms_gestionuser.dto.PasswordChangeRequest;
import tn.esprit.ms_gestionuser.entities.User;
import tn.esprit.ms_gestionuser.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Récupère le profil de l'utilisateur connecté.
     * Si c'est la première connexion, l'utilisateur est automatiquement créé en
     * base.
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.getOrCreateUser(jwt);
        return ResponseEntity.ok(user);
    }

    /**
     * Met à jour le profil de l'utilisateur connecté (champs métier uniquement).
     */
    @PutMapping("/me/update")
    public ResponseEntity<User> updateMyProfile(@AuthenticationPrincipal Jwt jwt, @RequestBody User updatedUser) {
        User user = userService.updateUserProfile(jwt.getSubject(), updatedUser);
        return ResponseEntity.ok(user);
    }

    /**
     * Récupère tous les utilisateurs (réservé à l'ADMIN).
     */
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Supprime un utilisateur par son ID (réservé à l'ADMIN).
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("Utilisateur avec l'ID " + id + " a été supprimé avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Update user profile by ID
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        User user = userService.updateUserById(id, updatedUser);
        return ResponseEntity.ok(user);
    }

    /**
     * Toggle user status by Admin
     */
    @PutMapping("/toggle-status/{id}")
    public ResponseEntity<String> toggleUserStatus(@PathVariable Long id, @RequestParam boolean active) {
        userService.toggleUserStatus(id, active);
        return ResponseEntity.ok("Statut de l'utilisateur mis à jour.");
    }

    /**
     * Change le mot de passe de l'utilisateur connecté.
     */
    @PostMapping("/me/change-password")
    public ResponseEntity<String> changeMyPassword(@AuthenticationPrincipal Jwt jwt,
            @RequestBody PasswordChangeRequest payload) {
        String newPassword = payload.getNewPassword();
        if (newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("Le nouveau mot de passe est requis.");
        }
        User user = userService.getOrCreateUser(jwt);
        userService.changePassword(user.getIdUser(), newPassword);
        return ResponseEntity.ok("Mot de passe mis à jour avec succès.");
    }

    /**
     * Endpoint de test pour vérifier que l'authentification fonctionne.
     */
    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth(@AuthenticationPrincipal Jwt jwt) {
        String message = "Bravo ! Vous êtes authentifié via Keycloak ! "
                + "Email: " + jwt.getClaimAsString("email")
                + ", Subject: " + jwt.getSubject();
        return ResponseEntity.ok(message);
    }
}
