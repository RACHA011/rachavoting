package com.racha.rachavoting.services.components;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racha.rachavoting.model.components.Province;
import com.racha.rachavoting.repository.components.ProvinceRepository;

@Service
@Transactional
public class ProvinceService {
    @Autowired
    private ProvinceRepository provinceRepository;

    public Province save(Province province) {
        return provinceRepository.save(province);
    }

    public List<Province> getAll() {
        return provinceRepository.findAll();
    }

    public Optional<Province> findByName(String name) {
        return provinceRepository.findByName(name);
    }
}
