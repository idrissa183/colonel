# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SmartGestion is a Spring Boot desktop application for managing Longrich product sales, inventory, and customer relationships. It combines a modern Spring Boot backend with Java Swing UI for a desktop retail management system.

**Technology Stack:** Java 17, Spring Boot 3.5.4, Spring Data JPA, MySQL 8.0, Java Swing with FlatLaf, Maven

## Development Commands

**Build and Run:**
```bash
# Recommended: Use Maven wrapper
./mvnw clean compile
./mvnw spring-boot:run
./mvnw clean package
./mvnw test

# Alternative: Direct Maven (if available)
mvn clean compile
mvn spring-boot:run
mvn clean package
mvn test
```

**Database Setup:**
```sql
CREATE DATABASE smartG CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'smartG'@'localhost' IDENTIFIED BY 'SmartG@2025';
GRANT ALL PRIVILEGES ON smartG.* TO 'smartG'@'localhost';
```

## Architecture Overview

**Main Package:** `com.longrich.smartgestion`

**Key Architectural Layers:**
- `entity/` - JPA entities with Longrich-specific business models (Client, Produit, Stock, Commande, Facture)
- `repository/` - Spring Data repositories with custom queries
- `service/` - Business logic layer (mostly incomplete implementations)
- `ui/` - Swing desktop interface with CardLayout navigation
- `dto/` - Data transfer objects
- `config/` - Spring configuration

**Entry Point:** `SmartGestionApplication.java` (Spring Boot main class)

**UI Architecture:**
- `MainFrame` - Main window with CardLayout for panel switching
- `Sidebar` - Navigation component for different modules
- `Navbar` - Top navigation with search and user context
- Feature panels in `ui/panels/` for each business module

## Business Domain

**Core Entities:**
- **Client** - Customer types: Partner, Individual, Wholesale, Semi-wholesale
- **Produit** - Products with PV (Points de Vente) for network marketing
- **Stock** - Inventory with expiration tracking
- **Commande/Facture** - Order processing and invoicing
- **User** - Role-based access (ADMIN, USER, MANAGER)

**Longrich-Specific Features:**
- PV system for MLM compensation
- Differentiated pricing by client type
- Network marketing relationship tracking

## Database Configuration

**Production:** MySQL database `smartG` with credentials in `application.yml`
**Testing:** H2 in-memory database
**Initialization:** Auto DDL updates enabled, manual scripts: `script.sql`, `produit.sql`

**Default Login:** admin / admin123

## Development Notes

**Current State:**
- ✅ Entity models with full JPA configuration including new entities (SalleVente, FamilleProduit, MouvementStock, SuiviPV)
- ✅ Enhanced entities with PV tracking, client progression, stock movement, and command delivery tracking
- ✅ Repository layer with custom queries for all entities
- ✅ Comprehensive DTOs for all entities
- ✅ MapStruct mappers for entity-DTO conversion
- ✅ Complete service layer implementations with business logic
- ✅ Spring Security with JWT authentication and role-based access
- ⚠️ Some compilation issues with MapStruct mappers and BaseEntity inheritance
- ❌ Exception handling system needed
- ❌ Enhanced UI components needed

**Key Patterns:**
- Repository pattern for data access
- Service layer with full business logic implementation
- DTO pattern for layer communication with MapStruct mappers
- JWT-based authentication and authorization
- JPA auditing for entity tracking
- Optimistic locking with @Version

**Prerequisites:** Java 17+, Maven 3.6+, MySQL 8.0+, desktop environment for Swing GUI