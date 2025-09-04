package com.longrich.smartgestion.mapper;

import com.longrich.smartgestion.dto.ProduitPromotionnelDTO;
import com.longrich.smartgestion.entity.ProduitPromotionnel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProduitPromotionnelMapper {

    @Mapping(source = "ventePromotionnelle.id", target = "ventePromotionnelleId")
    @Mapping(source = "produit.id", target = "produitId")
    @Mapping(source = "produit.libelle", target = "produitNom")
    @Mapping(source = "produitBonus.id", target = "produitBonusId")
    @Mapping(source = "produitBonus.libelle", target = "produitBonusNom")
    ProduitPromotionnelDTO toDTO(ProduitPromotionnel entity);

    @Mapping(target = "ventePromotionnelle", ignore = true)
    @Mapping(target = "produit", ignore = true)
    @Mapping(target = "produitBonus", ignore = true)
    ProduitPromotionnel toEntity(ProduitPromotionnelDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ventePromotionnelle", ignore = true)
    @Mapping(target = "produit", ignore = true)
    @Mapping(target = "produitBonus", ignore = true)
    void updateEntityFromDTO(ProduitPromotionnelDTO dto, @MappingTarget ProduitPromotionnel entity);
}