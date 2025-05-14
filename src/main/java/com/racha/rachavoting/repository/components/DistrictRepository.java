package com.racha.rachavoting.repository.components;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.racha.rachavoting.model.components.District;
import com.racha.rachavoting.model.components.Province;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {
    boolean existsByNameAndProvince(String name, Province province);

    @Query("SELECT d FROM District d JOIN FETCH d.province")
    List<District> findAllWithProvinces();
}