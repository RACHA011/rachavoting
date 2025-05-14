package com.racha.rachavoting.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.racha.rachavoting.payload.voter.VoterCreateDTO;
import com.racha.rachavoting.services.ElectionService;
import com.racha.rachavoting.services.components.DistrictService;
import com.racha.rachavoting.services.components.ProvinceService;
import com.racha.rachavoting.util.Hasher;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/vote")
public class VoteController {

    @Autowired
    private ElectionService electionService;

    @Autowired
    private DistrictService districtService;

    @GetMapping("/register")
    public String vote(Model model) {
        Map<String, List<String>> districtsByProvince = districtService.getDistrictsGroupedByProvince();

        model.addAttribute("districtsByProvince", districtsByProvince);
        return "register-to-vote";
    }

    @PostMapping("/register")
    // @RateLimiting(maxRequests = 5, timeWindow = 60)
    public String registerForVoting(
            @Valid @ModelAttribute("voter") VoterCreateDTO dto,
            RedirectAttributes redirectAttrs, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                redirectAttrs.addFlashAttribute("error", bindingResult.getAllErrors());
                return "redirect:/vote/register";
            }
            // Hash the ID
            String hashedId;

            do {
                hashedId = Hasher.hash(dto.getNationalId());
            } while (hashedId.length() >= 100);
            Map<String, List<String>> districtsByProvince = districtService.getDistrictsGroupedByProvince();

            List<String> districts = districtsByProvince.get(dto.getProvince());
            if (districts == null || !districts.contains(dto.getDistrict())) {
                redirectAttrs.addFlashAttribute("error", "Invalid district or Province for the selected province.");
                return "redirect:/vote/register";
            }

            
            System.out.println("DTO: " + dto);

            // Save to database
            // voterRepository.save(new Voter(hashedId, ...));

            redirectAttrs.addFlashAttribute("success", "Registration complete!");
            return "redirect:/vote/register-success";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Registration failed");
            return "redirect:/register";
        }

    }

    @GetMapping("/register-success")
    public String getMethodName(Model model) {

        return "register-success";
    }

}
