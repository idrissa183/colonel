package com.longrich.smartgestion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceDTO {
    
    private Long id;
    private String nom;
    private String region;
    private String chefLieu;
}