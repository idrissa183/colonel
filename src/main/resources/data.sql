-- Insertion des 45 provinces du Burkina Faso avec leurs régions et chefs-lieux
INSERT IGNORE INTO provinces (nom, region, chef_lieu, created_at, updated_at) VALUES
-- Région du Centre
('Kadiogo', 'Centre', 'Ouagadougou', NOW(), NOW()),

-- Région du Boucle du Mouhoun
('Balé', 'Boucle du Mouhoun', 'Boromo', NOW(), NOW()),
('Banwa', 'Boucle du Mouhoun', 'Solenzo', NOW(), NOW()),
('Kossi', 'Boucle du Mouhoun', 'Nouna', NOW(), NOW()),
('Mouhoun', 'Boucle du Mouhoun', 'Dédougou', NOW(), NOW()),
('Nayala', 'Boucle du Mouhoun', 'Toma', NOW(), NOW()),
('Sourou', 'Boucle du Mouhoun', 'Tougan', NOW(), NOW()),

-- Région des Cascades
('Comoé', 'Cascades', 'Banfora', NOW(), NOW()),
('Léraba', 'Cascades', 'Sindou', NOW(), NOW()),

-- Région du Centre-Est
('Boulgou', 'Centre-Est', 'Tenkodogo', NOW(), NOW()),
('Koulpélogo', 'Centre-Est', 'Ouargaye', NOW(), NOW()),
('Kouritenga', 'Centre-Est', 'Koupéla', NOW(), NOW()),

-- Région du Centre-Nord
('Bam', 'Centre-Nord', 'Kongoussi', NOW(), NOW()),
('Namentenga', 'Centre-Nord', 'Boulsa', NOW(), NOW()),
('Sanmatenga', 'Centre-Nord', 'Kaya', NOW(), NOW()),

-- Région du Centre-Ouest
('Boulkiemdé', 'Centre-Ouest', 'Koudougou', NOW(), NOW()),
('Sanguié', 'Centre-Ouest', 'Réo', NOW(), NOW()),
('Sissili', 'Centre-Ouest', 'Léo', NOW(), NOW()),
('Ziro', 'Centre-Ouest', 'Sapouy', NOW(), NOW()),

-- Région du Centre-Sud
('Bazèga', 'Centre-Sud', 'Kombissiri', NOW(), NOW()),
('Nahouri', 'Centre-Sud', 'Pô', NOW(), NOW()),
('Zoundwéogo', 'Centre-Sud', 'Manga', NOW(), NOW()),

-- Région de l'Est
('Gnagna', 'Est', 'Bogandé', NOW(), NOW()),
('Gourma', 'Est', 'Fada N''Gourma', NOW(), NOW()),
('Komandjoari', 'Est', 'Gayéri', NOW(), NOW()),
('Kompienga', 'Est', 'Kompienga', NOW(), NOW()),
('Tapoa', 'Est', 'Diapaga', NOW(), NOW()),

-- Région des Hauts-Bassins
('Houet', 'Hauts-Bassins', 'Bobo-Dioulasso', NOW(), NOW()),
('Kénédougou', 'Hauts-Bassins', 'Orodara', NOW(), NOW()),
('Tuy', 'Hauts-Bassins', 'Houndé', NOW(), NOW()),

-- Région du Nord
('Loroum', 'Nord', 'Titao', NOW(), NOW()),
('Passoré', 'Nord', 'Yako', NOW(), NOW()),
('Yatenga', 'Nord', 'Ouahigouya', NOW(), NOW()),
('Zondoma', 'Nord', 'Gourcy', NOW(), NOW()),

-- Région du Plateau-Central
('Ganzourgou', 'Plateau-Central', 'Zorgho', NOW(), NOW()),
('Kourwéogo', 'Plateau-Central', 'Boussé', NOW(), NOW()),
('Oubritenga', 'Plateau-Central', 'Ziniaré', NOW(), NOW()),

-- Région du Sahel
('Oudalan', 'Sahel', 'Gorom-Gorom', NOW(), NOW()),
('Séno', 'Sahel', 'Dori', NOW(), NOW()),
('Soum', 'Sahel', 'Djibo', NOW(), NOW()),
('Yagha', 'Sahel', 'Sebba', NOW(), NOW()),

-- Région du Sud-Ouest
('Bougouriba', 'Sud-Ouest', 'Diébougou', NOW(), NOW()),
('Ioba', 'Sud-Ouest', 'Dano', NOW(), NOW()),
('Noumbiel', 'Sud-Ouest', 'Batié', NOW(), NOW()),
('Poni', 'Sud-Ouest', 'Gaoua', NOW(), NOW());