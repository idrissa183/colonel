package com.longrich.smartgestion.mapper;

import com.longrich.smartgestion.dto.LigneCommandeDTO;
import com.longrich.smartgestion.entity.LigneCommande;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LigneCommandeMapper {

    @Mapping(target = "commandeId", source = "commande.id")
    @Mapping(target = "produitId", source = "produit.id")
    @Mapping(target = "produitLibelle", source = "produit.libelle")
    @Mapping(target = "produitCode", source = "produit.codeBarre")
    @Mapping(target = "quantiteRestante", expression = "java(ligneCommande.getQuantiteRestante())")
    @Mapping(target = "livreCompletement", expression = "java(ligneCommande.isLivreCompletement())")
    @Mapping(target = "livrePartiellement", expression = "java(ligneCommande.isLivrePartiellement())")
    LigneCommandeDTO toDTO(LigneCommande ligneCommande);

    @Mapping(target = "commande", ignore = true)
    @Mapping(target = "produit", ignore = true)
    LigneCommande toEntity(LigneCommandeDTO ligneCommandeDTO);

    List<LigneCommandeDTO> toDTOList(List<LigneCommande> lignesCommande);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "commande", ignore = true)
    @Mapping(target = "produit", ignore = true)
    void updateEntity(LigneCommandeDTO ligneCommandeDTO, @MappingTarget LigneCommande ligneCommande);
}