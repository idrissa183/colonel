package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.CommandeFournisseur;
import com.longrich.smartgestion.entity.LigneCommandeFournisseur;
import com.longrich.smartgestion.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LigneCommandeFournisseurRepository extends JpaRepository<LigneCommandeFournisseur, Long> {

    List<LigneCommandeFournisseur> findByCommandeFournisseur(CommandeFournisseur commandeFournisseur);

    List<LigneCommandeFournisseur> findByProduit(Produit produit);

    List<LigneCommandeFournisseur> findByCommandeFournisseurAndProduit(CommandeFournisseur commandeFournisseur, Produit produit);

    @Query("SELECT l FROM LigneCommandeFournisseur l WHERE l.active = true AND l.commandeFournisseur.id = :commandeId")
    List<LigneCommandeFournisseur> findActiveByCommandeId(@Param("commandeId") Long commandeId);

    @Query("SELECT SUM(l.sousTotal) FROM LigneCommandeFournisseur l WHERE l.active = true AND l.commandeFournisseur = :commande")
    BigDecimal sumSousTotalByCommande(@Param("commande") CommandeFournisseur commande);

    @Query("SELECT SUM(l.quantite) FROM LigneCommandeFournisseur l WHERE l.active = true AND l.produit = :produit")
    Integer sumQuantiteByProduit(@Param("produit") Produit produit);

    @Query("SELECT l FROM LigneCommandeFournisseur l WHERE l.active = true AND " +
           "l.produit.id = :produitId AND l.commandeFournisseur.statut IN ('CONFIRMEE', 'EN_COURS')")
    List<LigneCommandeFournisseur> findByProduitIdInCommandesActives(@Param("produitId") Long produitId);

    void deleteByCommandeFournisseur(CommandeFournisseur commandeFournisseur);
}