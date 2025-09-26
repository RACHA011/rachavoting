package com.racha.rachavoting.controller;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.racha.rachavoting.mapper.ElectionMapper;
import com.racha.rachavoting.model.Election;
import com.racha.rachavoting.model.Logo;
import com.racha.rachavoting.payload.election.ElectionPublicDTO;
import com.racha.rachavoting.services.ElectionService;
import com.racha.rachavoting.services.LogoService;
import com.racha.rachavoting.util.logo.LogoType;

@Controller
public class HomeController {

    @Autowired
    private ElectionService electionService;

    @Autowired
    private ElectionMapper electionMapper;

    @Autowired
    private LogoService logoService;

    @Autowired
    private SecureRandom secureRandom;

    @GetMapping("/")
    public String home(Model model) {
        List<Election> elections = electionService.getIsActiveTrueorCurrentyear();

        if (!elections.isEmpty()) {
            List<ElectionPublicDTO> electionsPublicDTO = elections.stream()
                    .map(electionMapper::toPublicDTO)
                    .collect(Collectors.toList());

            model.addAttribute("elections", electionsPublicDTO);
        } else {
            model.addAttribute("elections", new ArrayList<ElectionPublicDTO>());
        }
        return "home";
    }

    @SuppressWarnings("null")
    @PostMapping(path = "/save-icon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<?> saveIcon(
            @RequestParam String id,
            @RequestParam String type,
            @RequestPart MultipartFile icon) {

        try {

            // 1. Validate file
            if (icon.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            if (icon.getContentType() == null || !icon.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().body("Only image files are allowed");
            }

            // 2. initialize logo
            Logo logo = new Logo();

            byte[] data = icon.getBytes();
            logo.setData(data);
            if ("candidate".equalsIgnoreCase(type)) {
                logo.setLogoType(LogoType.CANDIDATE);
            } else if ("party".equalsIgnoreCase(type)) {
                logo.setLogoType(LogoType.PARTY);
            } else if ("organization".equalsIgnoreCase(type)) {
                logo.setLogoType(LogoType.ORGANIZATION);
            } else {
                logo.setLogoType(LogoType.UNKNOWN);
            }

            if (id != null && !id.isEmpty()) {
                try {
                    // first mark the last picture as tmporary or delete it
                    List<Logo> logos = logoService.findByLogoTypeId(Long.parseLong(id));

                    if (!logos.isEmpty()) {
                        logos.forEach(temp -> {
                            logoService.delete(temp);
                        });
                    }

                    logo.setLogoTypeId(Long.parseLong(id));
                } catch (Exception e) {
                    return ResponseEntity.status(500).body("Error processing id");
                }
            }
            logo.setName(icon.getOriginalFilename());
            logo.setContentType(icon.getContentType());

            // 3. Save logo
            logo = logoService.save(logo);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
            String timestampPart = logo.getCreatedAt().format(formatter);

            // Add randomness to ensure uniqueness
            String randomPart = UUID.randomUUID().toString().substring(0, 6);

            logo.setUniqueId("LID-" + timestampPart + "-" + randomPart);
            logo = logoService.save(logo);

            // we will return the time at which the logo was created
            return ResponseEntity.ok(logo.getUniqueId());

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing logo: " + e.getMessage());
        }
    }

    @GetMapping(path = "/get-icon")
    @ResponseBody
    public ResponseEntity<?> getIcon(@RequestParam String id) {
        try {

            Logo logo;
            try {
                logo = logoService.findById(Long.parseLong(id));
            } catch (NumberFormatException e) {
                logo = logoService.findByUniqueId(id);
            }
            if (logo == null) {
                return ResponseEntity.notFound().build();
            }
            // return the logo
            // LogoPublicDTO logoPublicDTO = new LogoMapper().topublicDto(logo);
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(logo.getContentType())) // e.g., image/png
                    .body(logo.getData());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(500).body("Id invalid");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing logo: " + e.getMessage());
        }
    }

    @PostMapping("/generate-id")
    public ResponseEntity<?> getId(
            @RequestParam(value = "dob", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob) {

        try {
            // 1. Generate date of birth
            LocalDate dateOfBirth = (dob != null) ? dob : generateRandomDateWithoutInvalidLeapDays();

            // 2. Generate gender
            String gender = (getSecureBit() == 1) ? "male" : "female";

            // 3. Generate valid ID in one attempt
            String idNumber = generateValidID(dateOfBirth, gender);

            return ResponseEntity.ok(idNumber);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generating ID: " + e.getMessage());
        }
    }

    // Optimized ID generation that creates valid IDs in one attempt
    private String generateValidID(LocalDate dob, String gender) {
        // 1. Generate date part (YYMMDD)
        String datePart = dob.format(DateTimeFormatter.ofPattern("yyMMdd"));

        // 2. Generate sequence number based on gender
        int sequence;
        if ("female".equalsIgnoreCase(gender)) {
            sequence = 1000 + secureRandom.nextInt(4000); // 1000-4999
        } else {
            sequence = 5000 + secureRandom.nextInt(5000); // 5000-9999
        }
        String sequencePart = String.format("%04d", sequence);

        // 3. Citizenship (0 = SA citizen)
        String citizenshipPart = "0";

        // 4. Race digit (historical, use 8 for test data)
        String racePart = "8";

        // 5. Combine first 12 digits
        String first12 = datePart + sequencePart + citizenshipPart + racePart;

        // 6. Calculate checksum
        int checksum = calculateLuhnChecksum(first12);

        return first12 + checksum;
    }

    // Pre-calculated Luhn checksum
    private static int calculateLuhnChecksum(String number) {
        int sum = 0;
        boolean even = false;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));

            if (even) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }

            sum += digit;
            even = !even;
        }

        return (10 - (sum % 10)) % 10;
    }

    private LocalDate generateRandomDateSecure() {
        int minYear = 1950;
        int maxYear = 2006;

        // Generate random year between 1950-2006
        int year = minYear + secureRandom.nextInt(maxYear - minYear + 1);

        // Generate random month (1-12)
        int month = 1 + secureRandom.nextInt(12);

        // Generate random day (properly handling month lengths)
        int maxDay = Month.of(month).maxLength();
        int day = 1 + secureRandom.nextInt(maxDay);

        return LocalDate.of(year, month, day);
    }

    // avoids February 29th for non-leap years
    private LocalDate generateRandomDateWithoutInvalidLeapDays() {
        LocalDate date;
        do {
            date = generateRandomDateSecure();
        } while (date.getMonth() == Month.FEBRUARY &&
                date.getDayOfMonth() == 29 &&
                !date.isLeapYear());

        return date;
    }

    private int getSecureBit() {
        return secureRandom.nextInt(2); // returns 0 or 1
    }
}
