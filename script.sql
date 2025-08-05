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
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIo0jshRLrMO2q', 'admin@smartgestion.com', 'Administrateur', 'Système', 'ADMIN', true, NOW(), NOW(), true);

-- Quelques produits Longrich de base
INSERT IGNORE INTO produits (code_barre, libelle, description, prix_achat, prix_revente, pv, famille_id, active, stock_minimum, created_at, updated_at) VALUES 
('LR001', 'ANTIMOUSTIQUE (195ml)', 'Spray antimoustique efficace', 3600.00, 4500.00, 3.5, 1, true, 10, NOW(), NOW(), true),
('LR002', 'ANTI TRANSPIRANT (50ml)', 'Déodorant anti-transpirant longue durée', 3300.00, 4000.00, 3.5, 1, true, 15, NOW(), NOW(), true),
('LR003', 'CALCIUM', 'Complément alimentaire au calcium', 11000.00, 13000.00, 11.0, 3, true, 5, NOW(), NOW(), true),
('LR004', 'GEL DE DOUCHE THE BLANC (300ml)', 'Gel de douche au thé blanc', 6600.00, 7500.00, 6.0, 2, true, 20, NOW(), NOW(), true),
('LR005', 'PATE DENTIFRICE THE BLANC (100g)', 'Dentifrice au thé blanc', 2000.00, 2500.00, 1.2, 2, true, 25, NOW(), NOW(), true);
























-- ====================================
-- INSERTION DES FAMILLES DE PRODUITS
-- ====================================

INSERT INTO famille_produit (libelle_famille, description, created_at, updated_at, active) VALUES
('Soins Quotidiens', 'Produits de soins quotidiens pour le corps et l''hygiène personnelle', NOW(), NOW(), true),
('Produits Artémisia', 'Gamme de produits à base d''artémisia avec propriétés antibactériennes', NOW(), NOW(), true),
('Soins Bébé', 'Produits spécialement formulés pour les bébés et enfants', NOW(), NOW(), true),
('Produits Sanitaires', 'Serviettes hygiéniques magnétiques et produits d''hygiène féminine', NOW(), NOW(), true),
('Produits Énergétiques', 'Accessoires énergétiques et ustensiles de cuisine', NOW(), NOW(), true),
('Produits de Santé', 'Compléments alimentaires et produits de bien-être', NOW(), NOW(), true),
('NutriV-Rich', 'Gamme d''aliments préparés et thés nutritionnels', NOW(), NOW(), true);

-- ====================================
-- INSERTION DES PRODUITS DAILY CARE
-- ====================================

INSERT INTO produits (libelle, description, prix_achat, prix_revente, pv, famille_produit_id, stock_minimum, seuil_alerte, created_at, updated_at, active) VALUES
-- Dentifrice
('Dentifrice Longrich 100g', 'Dentifrice aux herbes naturelles, traite les maux de dents, renforce les gencives', 1500, 1800, 1.2, 1, 10, 5, NOW(), NOW(), true),
('Dentifrice Longrich 200g', 'Dentifrice aux herbes naturelles, format familial', 2800, 3600, 3.5, 1, 5, 3, NOW(), NOW(), true),

-- Anti-moustique
('Anti-Moustique Longrich', 'Spray répulsif anti-moustiques 8h de protection, 5% DEET', 3200, 4500, 4.0, 1, 15, 8, NOW(), NOW(), true),

-- Savons
('Savon Charbon de Bambou', 'Savon naturel au charbon de bambou, traite l''acné et les irritations', 4000, 5000, 5.0, 1, 20, 10, NOW(), NOW(), true),

-- Gel douche
('Gel Douche Corporel 300ml', 'Gel douche hydratant aux extraits d''herbes, tous types de peau', 4800, 6000, 6.0, 1, 12, 6, NOW(), NOW(), true),

-- Rafraîchisseur d'haleine
('Rafraîchisseur d''Haleine', 'Spray buccal rafraîchissant à la menthe et thé vert', 1760, 2250, 2.2, 1, 25, 12, NOW(), NOW(), true),

-- Crème corporelle SOD
('Crème Corporelle SOD Milk', 'Crème corporelle au placenta de mouton, élimine vergetures et cicatrices', 3360, 4500, 4.2, 1, 8, 4, NOW(), NOW(), true),

-- Shampooings
('Shampooing 2-en-1 Longrich', 'Shampooing et après-shampooing, traite les pellicules', 4800, 6000, 6.0, 1, 10, 5, NOW(), NOW(), true),
('Shampooing Anti-Pelliculaire Thé Blanc', 'Shampooing au thé blanc, anti-pelliculaire et fortifiant', 2560, 3250, 3.2, 1, 12, 6, NOW(), NOW(), true),

-- Crème pour les mains
('Crème Mains Éclaircissante', 'Crème hydratante et éclaircissante pour les mains', 2800, 3750, 3.5, 1, 15, 8, NOW(), NOW(), true),

-- Déodorant
('Déodorant Roll-On', 'Déodorant anti-transpirant, protection 24h', 2800, 3750, 3.5, 1, 20, 10, NOW(), NOW(), true),

-- Evergreen
('Essence Revitalisante Evergreen', 'Crème anti-âge premium, réparatrice et revitalisante', 64000, 80000, 80.0, 1, 3, 2, NOW(), NOW(), true),
('Masque Facial Evergreen', 'Masque nutritif à la grenade, anti-âge et hydratant', 16000, 20000, 20.0, 1, 8, 4, NOW(), NOW(), true),

-- Lotion corporelle
('Lotion Corporelle Régénérante', 'Lotion au collagène de serpent, hydrate et répare', 2400, 3000, 3.0, 1, 12, 6, NOW(), NOW(), true),

-- Huile de serpent
('Huile de Serpent 80ml', 'Huile traditionnelle chinoise, anti-inflammatoire et cicatrisante', 1600, 2000, 2.0, 1, 15, 8, NOW(), NOW(), true);

-- ====================================
-- INSERTION DES PRODUITS ARTEMISIA
-- ====================================

INSERT INTO produits (libelle, description, prix_achat, prix_revente, pv, famille_produit_id, stock_minimum, seuil_alerte, created_at, updated_at, active) VALUES
-- Gel douche Artémisia
('Gel Douche Artémisia', 'Gel douche antibactérien à l''artémisia, propriétés antimicrobiennes', 1600, 2250, 2.0, 2, 12, 6, NOW(), NOW(), true),

-- Après-shampooing
('Après-Shampooing Réparateur Artémisia', 'Conditionner nourrissant et réparateur à l''artémisia', 1600, 2250, 2.0, 2, 10, 5, NOW(), NOW(), true),

-- Shampooing
('Shampooing Réparateur Artémisia', 'Shampooing nourrissant et réparateur aux extraits d''artémisia', 1600, 2250, 2.0, 2, 10, 5, NOW(), NOW(), true),

-- Déodorant
('Déodorant Artémisia Homme', 'Déodorant roll-on antibactérien pour homme', 1200, 1625, 1.5, 2, 18, 9, NOW(), NOW(), true),
('Déodorant Artémisia Femme', 'Déodorant roll-on antibactérien pour femme', 1200, 1625, 1.5, 2, 18, 9, NOW(), NOW(), true),

-- Savon pour les mains
('Savon Mains Artémisia', 'Savon liquide antibactérien et hydratant pour les mains', 1200, 1625, 1.5, 2, 15, 8, NOW(), NOW(), true),

-- Lessive
('Lessive Antibactérienne Artémisia', 'Lessive concentrée antibactérienne, tue germes et bactéries', 2400, 3250, 3.0, 2, 8, 4, NOW(), NOW(), true),

-- Savon pour sous-vêtements
('Savon Sous-Vêtements Artémisia', 'Savon antibactérien spécial lingerie, propriétés antiseptiques', 400, 625, 0.5, 2, 25, 12, NOW(), NOW(), true),

-- Bain de bouche
('Bain de Bouche Artémisia', 'Bain de bouche antibactérien, protège gencives et dents', 1200, 1625, 1.5, 2, 15, 8, NOW(), NOW(), true),

-- Savon nourrissant
('Savon Nourrissant Artémisia', 'Savon antibactérien nourrissant, stimule le système immunitaire', 400, 625, 0.5, 2, 20, 10, NOW(), NOW(), true),

-- Dentifrice
('Dentifrice Artémisia 120g', 'Dentifrice multi-effets aux extraits d''herbes premium', 400, 625, 0.5, 2, 15, 8, NOW(), NOW(), true),
('Dentifrice Artémisia 200g', 'Dentifrice multi-effets aux extraits d''herbes, format familial', 800, 1250, 1.0, 2, 10, 5, NOW(), NOW(), true);

-- ====================================
-- INSERTION DES PRODUITS BABY CARE
-- ====================================

INSERT INTO produits (libelle, description, prix_achat, prix_revente, pv, famille_produit_id, stock_minimum, seuil_alerte, created_at, updated_at, active) VALUES
-- Gel douche et shampooing bébé
('Gel Douche et Shampooing Bébé', 'Formule douce 2-en-1 à l''extrait de maïs, sans irritation', 4800, 6000, 6.0, 3, 8, 4, NOW(), NOW(), true),

-- Crème hydratante bébé
('Crème Hydratante Bébé', 'Crème douce à l''huile de maïs et extrait de serpent, peau délicate', 2400, 3000, 3.0, 3, 10, 5, NOW(), NOW(), true),

-- Poudre anti-démangeaisons
('Poudre Anti-Démangeaisons Bébé', 'Poudre naturelle au maïs, prévient irritations et démangeaisons', 4800, 6000, 6.0, 3, 8, 4, NOW(), NOW(), true),

-- Couches énergétiques
('Couches Énergétiques Taille S (28pcs)', 'Couches 5D antibactériennes, respirantes et absorbantes', 1600, 2000, 2.0, 3, 20, 10, NOW(), NOW(), true),
('Couches Énergétiques Taille M (26pcs)', 'Couches 5D antibactériennes, respirantes et absorbantes', 1600, 2000, 2.0, 3, 20, 10, NOW(), NOW(), true),
('Couches Énergétiques Taille L (24pcs)', 'Couches 5D antibactériennes, respirantes et absorbantes', 1600, 2000, 2.0, 3, 20, 10, NOW(), NOW(), true),
('Couches Énergétiques Taille XL (22pcs)', 'Couches 5D antibactériennes, respirantes et absorbantes', 1600, 2000, 2.0, 3, 20, 10, NOW(), NOW(), true);

-- ====================================
-- INSERTION DES PRODUITS SANITAIRES
-- ====================================

INSERT INTO produits (libelle, description, prix_achat, prix_revente, pv, famille_produit_id, stock_minimum, seuil_alerte, created_at, updated_at, active) VALUES
-- Serviettes hygiéniques
('Superbklean Pack Mixte (4-en-1)', 'Pack complet serviettes magnétiques : flux abondant, normal, léger, protège-slips', 40000, 62000, 50.0, 4, 5, 3, NOW(), NOW(), true),
('Superbklean Flux Abondant (18 packs)', 'Serviettes magnétiques 8 couches, flux abondant avec anions', 40000, 62000, 50.0, 4, 5, 3, NOW(), NOW(), true),
('Superbklean Protège-Slips Unisexe', 'Protège-slips magnétiques unisexe, anti-bactériens 99%', 40000, 62000, 50.0, 4, 5, 3, NOW(), NOW(), true);

-- ====================================
-- INSERTION DES PRODUITS ÉNERGÉTIQUES
-- ====================================

INSERT INTO produits (libelle, description, prix_achat, prix_revente, pv, famille_produit_id, stock_minimum, seuil_alerte, created_at, updated_at, active) VALUES
-- Marmites énergétiques
('Marmite Énergétique 24cm', 'Marmite en acier inoxydable avec boule énergétique, conserve nutriments', 136000, 170000, 170.0, 5, 2, 1, NOW(), NOW(), true),
('Marmite Énergétique 28cm', 'Marmite en acier inoxydable avec boule énergétique, format familial', 144000, 180000, 180.0, 5, 2, 1, NOW(), NOW(), true),

-- Gobelet alcalin
('Gobelet Alcalin Pi 400ml', 'Gobelet énergétique, transforme l''eau en alcaline, élimine toxines', 36000, 45000, 45.0, 5, 5, 3, NOW(), NOW(), true),

-- Chaussures énergétiques
('Chaussures Énergétiques Femme', 'Chaussures magnétiques stimulent points d''acupuncture', 192000, 240000, 240.0, 5, 3, 2, NOW(), NOW(), true),
('Chaussures Énergétiques Homme', 'Chaussures magnétiques stimulent points d''acupuncture', 240000, 300000, 300.0, 5, 3, 2, NOW(), NOW(), true),

-- Colliers énergétiques
('Collier Énergétique Femme', 'Collier magnétique tourmaline, améliore circulation sanguine', 120000, 150000, 150.0, 5, 4, 2, NOW(), NOW(), true),
('Collier Énergétique Homme', 'Collier magnétique tourmaline, améliore circulation sanguine', 120000, 150000, 150.0, 5, 4, 2, NOW(), NOW(), true);

-- ====================================
-- INSERTION DES PRODUITS DE SANTÉ
-- ====================================

INSERT INTO produits (libelle, description, prix_achat, prix_revente, pv, famille_produit_id, stock_minimum, seuil_alerte, created_at, updated_at, active) VALUES
-- Cordyceps
('Cordyceps Militaris (60 capsules)', 'Capsules de cordyceps, stimule immunité et performances physiques', 56000, 70000, 70.0, 6, 5, 3, NOW(), NOW(), true),
('Café Cordyceps Militaris', 'Café décaféiné au cordyceps, énergisant naturel', 1200, 1625, 1.5, 6, 15, 8, NOW(), NOW(), true),

-- Huile d'argousier
('Huile d''Argousier (120 capsules)', 'Huile riche en omégas 3,6,7,9 et vitamines, protège organes', 26400, 33000, 33.0, 6, 6, 3, NOW(), NOW(), true),

-- Arthro SupReviver
('Arthro SupReviver (60 comprimés)', 'Complément pour arthrite, rhumatismes et problèmes articulaires', 18000, 22500, 22.5, 6, 8, 4, NOW(), NOW(), true),

-- Calcium/Zinc/Fer
('Calcium/Zinc/Fer (100 comprimés)', 'Comprimés à croquer, renforce os et système immunitaire', 11000, 13750, 13.75, 6, 10, 5, NOW(), NOW(), true),

-- Vin de santé
('Vin de Santé Vintage 500ml', 'Liqueur de santé aux herbes, renforce immunité et libido', 8000, 10000, 10.0, 6, 6, 3, NOW(), NOW(), true),

-- Mengqian (femmes)
('Mengqian (160 capsules)', 'Complément fertilité féminine, équilibre hormonal', 22400, 28000, 28.0, 6, 5, 3, NOW(), NOW(), true),

-- Libao (hommes)
('Libao (160 capsules)', 'Complément fertilité masculine, améliore performances', 22400, 28000, 28.0, 6, 5, 3, NOW(), NOW(), true),

-- Gingembre noir
('Gingembre Noir + Cordyceps (40 comprimés)', 'Complément énergie et performances masculines, effet 72h', 24000, 30000, 30.0, 6, 5, 3, NOW(), NOW(), true),

-- Boisson vitaminée
('Boisson Vitaminée Énergisante', 'Boisson énergétique au cordyceps, gingembre noir et vitamine C', 800, 1000, 1.0, 6, 25, 12, NOW(), NOW(), true);

-- ====================================
-- INSERTION DES PRODUITS NUTRIVRICH
-- ====================================

INSERT INTO produits (libelle, description, prix_achat, prix_revente, pv, famille_produit_id, stock_minimum, seuil_alerte, created_at, updated_at, active) VALUES
-- NutriV-Rich
('NutriV-Rich Bleu (30 sachets)', 'Smoothie nutritionnel 45 fruits/légumes, pour tous y compris ulcéreux', 72000, 90000, 90.0, 7, 4, 2, NOW(), NOW(), true),
('NutriV-Rich Rose (30 sachets)', 'Smoothie nutritionnel diabétiques/hypertendus, aide perte de poids', 72000, 90000, 90.0, 7, 4, 2, NOW(), NOW(), true),

-- Thés
('Thé Rose Minceur', 'Thé amincissant, accélère métabolisme et coupe appétit', 4000, 5000, 5.0, 7, 12, 6, NOW(), NOW(), true),
('Thé Vert Détox', 'Thé vert détoxifiant, traite constipation et améliore digestion', 4000, 5000, 5.0, 7, 12, 6, NOW(), NOW(), true),
('Thé Brun Réducteur', 'Thé réducteur de graisses, élimine graisse du foie', 4000, 5000, 5.0, 7, 12, 6, NOW(), NOW(), true),

-- Thé noir et trémelle
('Thé Noir et Trémelle (15 sachets)', 'Mélange amincissant sans effet yo-yo, hydrate et rajeunit la peau', 6400, 8000, 8.0, 7, 10, 5, NOW(), NOW(), true);

-- ====================================
-- MISE À JOUR DES SÉQUENCES (si nécessaire selon SGBD)
-- ====================================

-- Pour PostgreSQL :
-- SELECT setval('famille_produit_id_seq', (SELECT MAX(id) FROM famille_produit));
-- SELECT setval('produits_id_seq', (SELECT MAX(id) FROM produits));

-- ====================================
-- REQUÊTES DE VÉRIFICATION
-- ====================================

-- Vérification des familles insérées
SELECT COUNT(*) as nb_familles FROM famille_produit;

-- Vérification des produits par famille
SELECT 
    fp.libelle_famille,
    COUNT(p.id) as nb_produits,
    AVG(p.pv) as pv_moyen,
    MIN(p.prix_revente) as prix_min,
    MAX(p.prix_revente) as prix_max
FROM famille_produit fp
LEFT JOIN produits p ON fp.id = p.famille_produit_id
GROUP BY fp.id, fp.libelle_famille
ORDER BY nb_produits DESC;

-- Produits avec PV le plus élevé
SELECT 
    p.libelle,
    fp.libelle_famille,
    p.prix_revente,
    p.pv
FROM produits p
JOIN famille_produit fp ON p.famille_produit_id = fp.id
ORDER BY p.pv DESC
LIMIT 10;