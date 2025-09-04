package com.longrich.smartgestion.mapper;

import com.longrich.smartgestion.dto.VentePromotionnelleDTO;
import com.longrich.smartgestion.entity.VentePromotionnelle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VentePromotionnelleMapper {

    @Mapping(target = "produitsPromotionnels", ignore = true)
    VentePromotionnelleDTO toDTO(VentePromotionnelle entity);

    @Mapping(target = "produitsPromotionnels", ignore = true)
    VentePromotionnelle toEntity(VentePromotionnelleDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "produitsPromotionnels", ignore = true)
    void updateEntityFromDTO(VentePromotionnelleDTO dto, @MappingTarget VentePromotionnelle entity);
}