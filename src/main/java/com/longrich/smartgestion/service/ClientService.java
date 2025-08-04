package com.longrich.smartgestion.service;

import com.longrich.smartgestion.dto.ClientDTO;
import com.longrich.smartgestion.entity.Client;
import com.longrich.smartgestion.enums.TypeClient;
import com.longrich.smartgestion.mapper.ClientMapper;
import com.longrich.smartgestion.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final ProvinceService provinceService;

    public List<ClientDTO> getAllClients() {
        return clientMapper.toDTOList(clientRepository.findAll());
    }

    public List<ClientDTO> getActiveClients() {
        return clientMapper.toDTOList(clientRepository.findByActiveTrue());
    }

    public Optional<ClientDTO> getClientById(Long id) {
        return clientRepository.findById(id)
                .map(clientMapper::toDTO);
    }

    public Optional<ClientDTO> getClientByCode(String code) {
        return clientRepository.findByCode(code)
                .map(clientMapper::toDTO);
    }

    public List<ClientDTO> searchClients(String search) {
        return clientMapper.toDTOList(clientRepository.searchActiveClients(search));
    }

    public List<ClientDTO> getClientsByType(TypeClient typeClient) {
        return clientMapper.toDTOList(clientRepository.findByTypeClient(typeClient));
    }

    public List<String> getAllProvinces() {
        return provinceService.findAll().stream()
                .map(province -> province.getNom())
                .toList();
    }

    public ClientDTO saveClient(ClientDTO clientDTO) {
        if (clientDTO.getId() == null && clientRepository.existsByCode(clientDTO.getCode())) {
            throw new IllegalArgumentException("Un client avec ce code existe déjà");
        }

        // Validation: Total PV obligatoire pour les partenaires
        if (clientDTO.getTypeClient() == TypeClient.PARTENAIRE && 
            (clientDTO.getTotalPv() == null || clientDTO.getTotalPv() <= 0)) {
            throw new IllegalArgumentException("Le total PV est obligatoire pour les partenaires");
        }

        Client client = clientMapper.toEntity(clientDTO);
        
        // Gérer la province
        if (clientDTO.getProvince() != null) {
            provinceService.findByNom(clientDTO.getProvince())
                    .ifPresent(client::setProvince);
        }
        
        // Générer le code partenaire si nécessaire
        if (client.getTypeClient() == TypeClient.PARTENAIRE) {
            client.genererCodePartenaire();
        }
        
        Client savedClient = clientRepository.save(client);
        log.info("Client sauvegardé: {}", savedClient.getCode());
        return clientMapper.toDTO(savedClient);
    }

    public ClientDTO updateClient(Long id, ClientDTO clientDTO) {
        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client non trouvé"));

        // Vérifier si le code est modifié et s'il existe déjà
        if (!existingClient.getCode().equals(clientDTO.getCode()) &&
                clientRepository.existsByCode(clientDTO.getCode())) {
            throw new IllegalArgumentException("Un client avec ce code existe déjà");
        }

        // Validation: Total PV obligatoire pour les partenaires
        if (clientDTO.getTypeClient() == TypeClient.PARTENAIRE && 
            (clientDTO.getTotalPv() == null || clientDTO.getTotalPv() <= 0)) {
            throw new IllegalArgumentException("Le total PV est obligatoire pour les partenaires");
        }

        clientMapper.updateEntity(clientDTO, existingClient);
        
        // Gérer la province
        if (clientDTO.getProvince() != null) {
            provinceService.findByNom(clientDTO.getProvince())
                    .ifPresent(existingClient::setProvince);
        } else {
            existingClient.setProvince(null);
        }
        
        // Générer le code partenaire si passage vers partenaire
        if (existingClient.getTypeClient() == TypeClient.PARTENAIRE && 
            existingClient.getCodePartenaire() == null) {
            existingClient.genererCodePartenaire();
        }
        
        Client updatedClient = clientRepository.save(existingClient);
        log.info("Client mis à jour: {}", updatedClient.getCode());
        return clientMapper.toDTO(updatedClient);
    }

    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client non trouvé"));

        // Soft delete
        client.setActive(false);
        clientRepository.save(client);
        log.info("Client désactivé: {}", client.getCode());
    }

    public boolean existsByCode(String code) {
        return clientRepository.existsByCode(code);
    }

    public List<ClientDTO> getClientsEnAttentePartenaire() {
        return clientMapper.toDTOList(clientRepository.findByTypeClient(TypeClient.EN_ATTENTE_PARTENAIRE));
    }

    public List<ClientDTO> getClientsPouvantDevenirPartenaire() {
        return clientRepository.findByTypeClient(TypeClient.EN_ATTENTE_PARTENAIRE)
                .stream()
                .filter(Client::peutDeveniPartenaire)
                .map(clientMapper::toDTO)
                .toList();
    }

    public ClientDTO promouvoirVersPartenaire(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client non trouvé"));

        if (!client.peutDeveniPartenaire()) {
            throw new IllegalArgumentException("Le client ne peut pas devenir partenaire (PV insuffisants)");
        }

        client.setTypeClient(TypeClient.PARTENAIRE);
        client.setCodeDefinitif(true);
        client.genererCodePartenaire(); // Générer le code partenaire lors de la promotion
        Client updatedClient = clientRepository.save(client);
        log.info("Client promu vers partenaire: {} avec code partenaire: {}", 
                updatedClient.getCode(), updatedClient.getCodePartenaire());
        return clientMapper.toDTO(updatedClient);
    }

    public void ajouterPV(Long clientId, Integer pv) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client non trouvé"));

        client.setTotalPv(client.getTotalPv() + pv);
        clientRepository.save(client);
        log.info("PV ajoutés au client {}: +{} (Total: {})", client.getCode(), pv, client.getTotalPv());
    }
}