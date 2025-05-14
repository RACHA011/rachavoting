package com.racha.rachavoting.services.components;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racha.rachavoting.model.components.District;
import com.racha.rachavoting.model.components.Province;
import com.racha.rachavoting.repository.components.DistrictRepository;

@Service
@Transactional
public class DistrictService {

    @Autowired
    private DistrictRepository districtRepository;

    public District save(District district) {
        return districtRepository.save(district);
    }

    @Transactional(readOnly = true)
    public Map<String, List<String>> getDistrictsGroupedByProvince() {
        List<District> districts = districtRepository.findAllWithProvinces();
        
        return districts.stream()
            .collect(Collectors.groupingBy(
                district -> district.getProvince().getName(),
                Collectors.mapping(District::getName, Collectors.toList()))
            );
    }

    public boolean existsByNameAndProvince(String name, Province province) {
        return districtRepository.existsByNameAndProvince(name, province);
    }
}
