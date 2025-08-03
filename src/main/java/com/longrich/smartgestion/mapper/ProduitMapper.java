package com.longrich.smartgestion.mapper;

import com.longrich.smartgestion.dto.ProduitDTO;
import com.longrich.smartgestion.entity.Produit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProduitMapper {

    @Mapping(target = "familleProduitId", source = "familleProduit.id")
    @Mapping(target = "familleProduitLibelle", source = "familleProduit.libelleFamille")
    @Mapping(target = "marge", expression = "java(produit.getMarge())")
    @Mapping(target = "pourcentageMarge", expression = "java(produit.getPourcentageMarge())")
    @Mapping(target = "productDisplay", expression = "java(produit.getProductDisplay())")
    @Mapping(target = "stockCritique", expression = "java(produit.isStockCritique())")
    @Mapping(target = "currentStock", expression = "java(produit.getCurrentStock())")
    ProduitDTO toDTO(Produit produit);

    @Mapping(target = "familleProduit", ignore = true)
    @Mapping(target = "stocks", ignore = true)
    @Mapping(target = "lignesCommande", ignore = true)
    Produit toEntity(ProduitDTO produitDTO);

    List<ProduitDTO> toDTOList(List<Produit> produits);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "familleProduit", ignore = true)
    @Mapping(target = "stocks", ignore = true)
    @Mapping(target = "lignesCommande", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(ProduitDTO produitDTO, @MappingTarget Produit produit);
}