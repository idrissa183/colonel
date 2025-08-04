package com.longrich.smartgestion.mapper;

import com.longrich.smartgestion.dto.ClientDTO;
import com.longrich.smartgestion.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClientMapper {

    @Mapping(target = "nomComplet", expression = "java(client.getNomComplet())")
    @Mapping(target = "clientId", expression = "java(client.getClientId())")
    @Mapping(target = "peutDeveniPartenaire", expression = "java(client.peutDeveniPartenaire())")
    @Mapping(target = "province", expression = "java(client.getProvince() != null ? client.getProvince().getNom() : null)")
    ClientDTO toDTO(Client client);

    @Mapping(target = "commandes", ignore = true)
    @Mapping(target = "factures", ignore = true)
    @Mapping(target = "province", ignore = true)
    Client toEntity(ClientDTO clientDTO);

    List<ClientDTO> toDTOList(List<Client> clients);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "commandes", ignore = true)
    @Mapping(target = "factures", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "province", ignore = true)
    void updateEntity(ClientDTO clientDTO, @MappingTarget Client client);
}