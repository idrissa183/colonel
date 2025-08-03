package com.longrich.smartgestion.entity;

import com.longrich.smartgestion.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(name = "nom", nullable = false)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "telephone")
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "last_login")
    private java.time.LocalDateTime lastLogin;
}