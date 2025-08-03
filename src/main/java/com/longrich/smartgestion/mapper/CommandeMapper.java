package com.longrich.smartgestion.mapper;

import com.longrich.smartgestion.dto.CommandeDTO;
import com.longrich.smartgestion.entity.Commande;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {
        LigneCommandeMapper.class }, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CommandeMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientNom", source = "client.nom")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userNom", source = "user.nom")
    @Mapping(target = "commandeNonLivree", expression = "java(commande.isCommandeNonLivree())")
    @Mapping(target = "totalQuantiteCommandee", expression = "java(commande.getTotalQuantiteCommandee())")
    @Mapping(target = "totalQuantiteLivree", expression = "java(commande.getTotalQuantiteLivree())")
    @Mapping(target = "totalQuantiteRestante", expression = "java(commande.getTotalQuantiteRestante())")
    CommandeDTO toDTO(Commande commande);

    @Mapping(target = "client", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "lignes", ignore = true)
    @Mapping(target = "facture", ignore = true)
    Commande toEntity(CommandeDTO commandeDTO);

    List<CommandeDTO> toDTOList(List<Commande> commandes);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "lignes", ignore = true)
    @Mapping(target = "facture", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntity(CommandeDTO commandeDTO, @MappingTarget Commande commande);
}