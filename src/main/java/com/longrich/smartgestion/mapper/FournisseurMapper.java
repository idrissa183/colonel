package com.longrich.smartgestion.mapper;

import com.longrich.smartgestion.dto.FournisseurDTO;
import com.longrich.smartgestion.entity.Fournisseur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FournisseurMapper {

    @Mapping(target = "nomComplet", expression = "java(fournisseur.getNomComplet())")
    @Mapping(target = "prenomObligatoire", expression = "java(fournisseur.prenomObligatoire())")
    @Mapping(target = "estPersonnePhysique", expression = "java(fournisseur.estPersonnePhysique())")
    @Mapping(target = "estPersonneMorale", expression = "java(fournisseur.estPersonneMorale())")
    FournisseurDTO toDTO(Fournisseur fournisseur);

    @Mapping(target = "commandes", ignore = true)
    Fournisseur toEntity(FournisseurDTO fournisseurDTO);

    List<FournisseurDTO> toDTOList(List<Fournisseur> fournisseurs);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "commandes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(FournisseurDTO fournisseurDTO, @MappingTarget Fournisseur fournisseur);
}