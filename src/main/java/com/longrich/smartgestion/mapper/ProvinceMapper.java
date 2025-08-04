package com.longrich.smartgestion.mapper;

import com.longrich.smartgestion.dto.ProvinceDTO;
import com.longrich.smartgestion.entity.Province;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProvinceMapper {
    
    ProvinceDTO toDTO(Province province);
    
    Province toEntity(ProvinceDTO provinceDTO);
    
    List<ProvinceDTO> toDTOList(List<Province> provinces);
    
    List<Province> toEntityList(List<ProvinceDTO> provinceDTOs);
}