package com.racha.rachavoting.components;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.racha.rachavoting.model.components.District;
import com.racha.rachavoting.model.components.Province;
import com.racha.rachavoting.services.components.DistrictService;
import com.racha.rachavoting.services.components.ProvinceService;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private ProvinceService provinceService;

    @Autowired
    private DistrictService districtService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Map<String, List<String>> districtsByProvince = Map.of(
            "Eastern Cape", List.of("Buffalo City", "Nelson Mandela Bay", "Amathole", "Chris Hani", "Joe Gqabi", "OR Tambo", "Sarah Baartman"),
            "Free State", List.of("Mangaung", "Fezile Dabi", "Lejweleputswa", "Thabo Mofutsanyana", "Xhariep"),
            "Gauteng", List.of("City of Johannesburg", "City of Tshwane", "Ekurhuleni", "Sedibeng", "West Rand"),
            "KwaZulu-Natal", List.of("eThekwini", "Amajuba", "Harry Gwala", "iLembe", "King Cetshwayo", "Ugu", "uMgungundlovu", "uMkhanyakude", "uMzinyathi", "uThukela", "Zululand"),
            "Limpopo", List.of("Capricorn", "Mopani", "Sekhukhune", "Vhembe", "Waterberg"),
            "Mpumalanga", List.of("Ehlanzeni", "Gert Sibande", "Nkangala"),
            "Northern Cape", List.of("Frances Baard", "John Taolo Gaetsewe", "Namakwa", "Pixley ka Seme", "ZF Mgcawu"),
            "North West", List.of("Bojanala Platinum", "Dr Kenneth Kaunda", "Dr Ruth Segomotsi Mompati", "Ngaka Modiri Molema"),
            "Western Cape", List.of("City of Cape Town", "Cape Winelands", "Central Karoo", "Garden Route", "Overberg", "West Coast")
        );

        for (var entry : districtsByProvince.entrySet()) {
            String provinceName = entry.getKey();
            List<String> districtNames = entry.getValue();

            Province province = provinceService.findByName(provinceName)
                .orElseGet(() -> provinceService.save(new Province(provinceName)));

            for (String districtName : districtNames) {
                boolean exists = districtService.existsByNameAndProvince(districtName, province);
                if (!exists) {
                    District district = new District();
                    district.setName(districtName);
                    district.setProvince(province);
                    districtService.save(district);
                }
            }
        }
    }
}
