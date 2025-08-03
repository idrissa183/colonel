-- Insertion des données de base

-- Types de clients
INSERT IGNORE INTO typeclient (libelle) VALUES 
('Partenaire'),
('Particulier'),
('Grossiste'),
('Semi-grossiste');

-- Provinces du Burkina Faso
INSERT IGNORE INTO provinces (province) VALUES 
('Bam'),
('Banwa'),
('Bazèga'),
('Bougouriba'),
('Boulgou'),
('Boulkiemdé'),
('Comoé'),
('Ganzourgou'),
('Gnagna'),
('Gourma'),
('Houet'),
('Ioba'),
('Kadiogo'),
('Kénédougou'),
('Komondjari'),
('Kompienga'),
('Kossi'),
('Koulpélogo'),
('Kouritenga'),
('Kourwéogo'),
('Léraba'),
('Loroum'),
('Mouhoun'),
('Namentenga'),
('Nahouri'),
('Nayala'),
('Noumbiel'),
('Oubritenga'),
('Oudalan'),
('Passoré'),
('Poni'),
('Sanguié'),
('Sanmatenga'),
('Séno'),
('Sissili'),
('Soum'),
('Sourou'),
('Tapoa'),
('Tuy'),
('Yagha'),
('Yatenga'),
('Ziro'),
('Zondoma'),
('Zoundwéogo');

-- Familles de produits Longrich
INSERT IGNORE INTO familles_produit (libelle, description, active) VALUES 
('Beauté', 'Produits de beauté et cosmétiques', true),
('Soins', 'Produits de soins corporels', true),
('Santé', 'Compléments alimentaires et produits de santé', true),
('Hygiène', 'Produits d\'hygiène personnelle', true),
('Accessoires', 'Accessoires et équipements', true);

-- Utilisateur administrateur par défaut
INSERT IGNORE INTO users (username, password, email, nom, prenom, role, active, created_at, updated_at) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIo0jshRLrMO2q', 'admin@smartgestion.com', 'Administrateur', 'Système', 'ADMIN', true, NOW(), NOW());

-- Quelques produits Longrich de base
INSERT IGNORE INTO produits (code_barre, libelle, description, prix_achat, prix_revente, pv, famille_id, active, stock_minimum, created_at, updated_at) VALUES 
('LR001', 'ANTIMOUSTIQUE (195ml)', 'Spray antimoustique efficace', 3600.00, 4500.00, 3.5, 1, true, 10, NOW(), NOW()),
('LR002', 'ANTI TRANSPIRANT (50ml)', 'Déodorant anti-transpirant longue durée', 3300.00, 4000.00, 3.5, 1, true, 15, NOW(), NOW()),
('LR003', 'CALCIUM', 'Complément alimentaire au calcium', 11000.00, 13000.00, 11.0, 3, true, 5, NOW(), NOW()),
('LR004', 'GEL DE DOUCHE THE BLANC (300ml)', 'Gel de douche au thé blanc', 6600.00, 7500.00, 6.0, 2, true, 20, NOW(), NOW()),
('LR005', 'PATE DENTIFRICE THE BLANC (100g)', 'Dentifrice au thé blanc', 2000.00, 2500.00, 1.2, 2, true, 25, NOW(), NOW());