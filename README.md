# SmartGestion - Système de Gestion Longrich

## Description

SmartGestion est une application de bureau complète développée en Java avec Spring Boot pour le backend et Swing pour l'interface utilisateur. Elle est spécialement conçue pour la gestion d'une boutique de produits Longrich, incluant la gestion des clients, produits, stocks, commandes, factures et fournisseurs.

## Fonctionnalités

### Gestion des Utilisateurs
- Authentification et autorisation
- Rôles : ADMIN, USER, MANAGER
- Gestion des profils utilisateurs

### Gestion des Clients
- Types de clients : Partenaire, Particulier, Grossiste, Semi-grossiste
- Informations complètes des clients
- Recherche et filtrage avancés

### Gestion des Produits
- Catalogue complet des produits Longrich
- Familles de produits
- Gestion des prix (achat, revente, PV)
- Suivi des dates de péremption

### Gestion du Stock
- Suivi en temps réel des quantités
- Alertes de stock faible
- Gestion des emplacements

### Gestion des Commandes
- Création et suivi des commandes
- Statuts multiples (En attente, Confirmée, Livrée, etc.)
- Calcul automatique des montants

### Gestion des Factures
- Génération automatique de factures
- Suivi des paiements
- Export PDF

### Tableau de Bord
- Statistiques en temps réel
- Graphiques de performance
- Activités récentes

## Technologies Utilisées

### Backend
- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Spring Security**
- **MySQL** (base de données principale)
- **H2** (base de données de test)

### Frontend
- **Java Swing**
- **FlatLaf** (Look and Feel moderne)

### Outils
- **Maven** (gestion des dépendances)
- **Lombok** (réduction du code boilerplate)
- **MapStruct** (mapping d'objets)

## Installation et Configuration

### Prérequis
- Java 17 ou supérieur
- Maven 3.6 ou supérieur
- MySQL 8.0 ou supérieur

### Configuration de la Base de Données

1. Créer une base de données MySQL :
```sql
CREATE DATABASE smartG CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'smartG'@'localhost' IDENTIFIED BY 'SmartG@2025';
GRANT ALL PRIVILEGES ON smartG.* TO 'smartG'@'localhost';
FLUSH PRIVILEGES;
```

2. Modifier le fichier `application.yml` si nécessaire pour adapter la configuration à votre environnement.

### Compilation et Exécution

1. Cloner le projet :
```bash
git clone <url-du-projet>
cd smart-gestion
```

2. Compiler le projet :
```bash
mvn clean compile
```

3. Lancer l'application :
```bash
mvn spring-boot:run
```

Ou créer un JAR exécutable :
```bash
mvn clean package
java -jar target/smart-gestion-1.0.0.jar
```

## Structure du Projet

```
src/main/java/com/longrich/smartgestion/
├── config/          # Configuration Spring
├── entity/          # Entités JPA
├── dto/             # Data Transfer Objects
├── repository/      # Repositories Spring Data
├── service/         # Services métier
├── enums/           # Énumérations
├── ui/
│   ├── main/        # Fenêtre principale
│   ├── components/  # Composants UI réutilisables
│   └── panels/      # Panneaux de l'interface
└── SmartGestionApplication.java
```

## Utilisation

### Connexion
- **Utilisateur par défaut** : `admin`
- **Mot de passe par défaut** : `admin123`

### Navigation
L'interface est organisée avec :
- Une **sidebar** pour la navigation entre les modules
- Une **navbar** avec recherche, notifications et profil utilisateur
- Un **contenu principal** avec les formulaires et tableaux

### Modules Principaux
1. **Tableau de bord** : Vue d'ensemble des activités
2. **Clients** : Gestion complète de la clientèle
3. **Produits** : Catalogue et gestion des produits
4. **Stock** : Suivi des inventaires
5. **Commandes** : Gestion des commandes clients
6. **Factures** : Facturation et suivi des paiements
7. **Fournisseurs** : Gestion des partenaires
8. **Ventes** : Suivi des performances commerciales

## Spécificités Longrich

L'application intègre les spécificités du marketing de réseau Longrich :
- **Système de PV** (Points de Vente) pour chaque produit
- **Types de clients** adaptés au réseau (Partenaires, Grossistes, etc.)
- **Tarification différenciée** selon le type de client
- **Suivi des performances** par partenaire

## Développement

### Ajout d'un Nouveau Module

1. Créer l'entité dans `entity/`
2. Créer le DTO dans `dto/`
3. Créer le repository dans `repository/`
4. Créer le service dans `service/`
5. Créer le panel UI dans `ui/panels/`
6. Ajouter la navigation dans `Sidebar.java`

### Tests

Lancer les tests :
```bash
mvn test
```

## Contribution

1. Fork le projet
2. Créer une branche pour votre fonctionnalité
3. Commiter vos changements
4. Pousser vers la branche
5. Créer une Pull Request

## Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

## Support

Pour toute question ou problème, veuillez créer une issue sur le repository GitHub.
