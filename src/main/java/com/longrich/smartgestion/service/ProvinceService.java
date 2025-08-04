package com.longrich.smartgestion.service;

import com.longrich.smartgestion.entity.Province;
import com.longrich.smartgestion.repository.ProvinceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProvinceService {
    
    private final ProvinceRepository provinceRepository;
    
    public List<Province> findAll() {
        log.debug("Récupération de toutes les provinces");
        return provinceRepository.findAllOrderByNom();
    }
    
    public Optional<Province> findById(Long id) {
        log.debug("Recherche de la province avec l'ID: {}", id);
        return provinceRepository.findById(id);
    }
    
    public Optional<Province> findByNom(String nom) {
        log.debug("Recherche de la province avec le nom: {}", nom);
        return provinceRepository.findByNom(nom);
    }
    
    public List<Province> findByRegion(String region) {
        log.debug("Recherche des provinces de la région: {}", region);
        return provinceRepository.findByRegion(region);
    }
    
    public List<String> findAllRegions() {
        log.debug("Récupération de toutes les régions");
        return provinceRepository.findDistinctRegions();
    }
    
    public Province save(Province province) {
        log.debug("Sauvegarde de la province: {}", province.getNom());
        return provinceRepository.save(province);
    }
    
    public void deleteById(Long id) {
        log.debug("Suppression de la province avec l'ID: {}", id);
        provinceRepository.deleteById(id);
    }
    
    public boolean existsById(Long id) {
        return provinceRepository.existsById(id);
    }
    
    public long count() {
        return provinceRepository.count();
    }
}