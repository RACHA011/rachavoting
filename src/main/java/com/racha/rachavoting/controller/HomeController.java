package com.racha.rachavoting.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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

}
