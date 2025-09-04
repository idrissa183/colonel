package com.longrich.smartgestion.mapper;

import com.longrich.smartgestion.dto.BonusAttribueDTO;
import com.longrich.smartgestion.entity.BonusAttribue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BonusAttribueMapper {

    @Mapping(source = "produitPromotionnel.id", target = "produitPromotionnelId")
    @Mapping(source = "commande.id", target = "commandeId")
    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "client.nomComplet", target = "clientNom")
    @Mapping(source = "utilisateur.id", target = "utilisateurId")
    @Mapping(source = "produitPromotionnel.produitBonus.libelle", target = "produitBonusNom")
    BonusAttribueDTO toDTO(BonusAttribue entity);

    @Mapping(target = "produitPromotionnel", ignore = true)
    @Mapping(target = "commande", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "utilisateur", ignore = true)
    BonusAttribue toEntity(BonusAttribueDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "produitPromotionnel", ignore = true)
    @Mapping(target = "commande", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "utilisateur", ignore = true)
    void updateEntityFromDTO(BonusAttribueDTO dto, @MappingTarget BonusAttribue entity);
}