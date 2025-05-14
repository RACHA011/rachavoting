package com.racha.rachavoting.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.racha.rachavoting.model.Candidate;
import com.racha.rachavoting.model.Logo;
import com.racha.rachavoting.model.Party;
import com.racha.rachavoting.payload.candidate.CandidateCreateDTO;
import com.racha.rachavoting.payload.party.PartyCreateDTO;
import com.racha.rachavoting.services.CandidateService;
import com.racha.rachavoting.services.LogoService;
import com.racha.rachavoting.services.PartyService;
import com.racha.rachavoting.util.logo.LogoStatus;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/candidate")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private LogoService logoService;

    @Autowired
    private PartyService partyService;

    /**
     * Handles the registration of a new candidate.
     *
     * @param candidateCreateDTO The data transfer object containing candidate
     *                           registration details.
     * @return Redirects to the login page after successful registration.
     */
    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("candidateCreateDTO") CandidateCreateDTO dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check password match first
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");

        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("tab", "register");
            return "auth/login"; // Return to form with errors
        }

        try {

            if (candidateService.existsByEmail(dto.getEmail())) {
                bindingResult.rejectValue("email", "error.email", "Email already exists");
                model.addAttribute("tab", "register");
                return "auth/login";
            }

            Candidate candidate = new Candidate();
            candidate.setFullName(dto.getFullName());
            candidate.setEmail(dto.getEmail());
            candidate.setPassword(dto.getPassword());
            // candidate.setPartyId(dto.getPartyId());

            candidateService.createCandidateAccount(candidate);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/candidate/login?tab=login";
        } catch (DataIntegrityViolationException e) {
            bindingResult.rejectValue("email", "error.email", "Email already exists");
            model.addAttribute("tab", "register");
            return "auth/login";
        } catch (Exception e) {
            bindingResult.reject("error.general", "Registration failed. Please try again.");
            model.addAttribute("tab", "register");
            return "auth/login";
        }
    }

    @GetMapping("/login")
    public String login(@RequestParam(name = "tab", defaultValue = "login") String tab,
            Model model) {
        model.addAttribute("tab", tab);
        if ("register".equals(tab)) {
            if (!model.containsAttribute("candidateCreateDTO")) {
                model.addAttribute("candidateCreateDTO", new CandidateCreateDTO());
            }
        }
        return "auth/login";
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('CANDIDATE') and isAuthenticated()")
    public String dashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        return "auth/dashboard";
    }

    @GetMapping("/dashboard/vote")
    @PreAuthorize("hasRole('CANDIDATE') and isAuthenticated()")
    public String vote(Model model) {
        model.addAttribute("activePage", "vote");
        return "auth/dashboard";
    }

    @GetMapping("/dashboard/settings")
    @PreAuthorize("hasRole('CANDIDATE') and isAuthenticated()")
    public String settings(Model model) {
        model.addAttribute("activePage", "settings");
        model.addAttribute("settingPage", "settings");

        return "auth/dashboard";
    }

    @GetMapping("/dashboard/settings/manage-party")
    @PreAuthorize("hasRole('CANDIDATE') and isAuthenticated() and (hasAuthority('PRESIDENT') or hasAuthority('DEPUTY_PRESIDENT'))")
    public String settingsManageParty(Model model, Principal principal) {
        model.addAttribute("activePage", "settings");
        model.addAttribute("settingPage", "manage-party");

        Party party;
        PartyCreateDTO dto;
        try {
            String email = principal.getName();
            Candidate candidate = candidateService.getCandidateByEmail(email);
            if (candidate.getParty() != null) {
                party = candidate.getParty();
            } else {
                party = new Party(); // new instance for creation
            }

            dto = new PartyCreateDTO();
            dto.setId(party.getId());
            dto.setName(party.getName());
            dto.setAbbreviation(party.getAbbreviation());
            dto.setStatus(party.isStatus());
            dto.setLeader(party.getLeader());
            dto.setSlogan(party.getSlogan());
            dto.setDescription(party.getDescription());
            dto.setRegistered(party.isRegistered());
            dto.setFoundingDate(party.getFoundingDate());
            dto.setLogoUrl("http://localhost:8080" + party.getLogoUrl());
            dto.setWebsite(party.getWebsite());
            if (party.getColors().isEmpty()) {
                party.getColors().add("#4f46e5");
                party.getColors().add("#e11d48");
                party.getColors().add("#10b981");
            }
            dto.setRegistrationNo(party.getRegistrationNo());
            dto.setHeadquarters(party.getHeadquarters());

            dto.setColors(party.getColors());
            dto.setFacebook(party.getFacebook());
            dto.setInstagram(party.getInstagram());
            dto.setX(party.getX());
            dto.setYoutube(party.getYoutube());

            model.addAttribute("party", dto); // change it to a dto
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        return "auth/dashboard";
    }

    @PostMapping("/dashboard/settings/manage-party")
    @PreAuthorize("hasRole('CANDIDATE') and isAuthenticated() and hasAuthority('PRESIDENT')")
    public String registerParty(@Valid @ModelAttribute("party") PartyCreateDTO dto,
            BindingResult result,
            RedirectAttributes redirectAttributes, Principal principal) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Error creating Party: " + result.getAllErrors());

            return "redirect:/candidate/dashboard/settings/manage-party"; // return to form with errors
        }
        // if i deside to use this as a party creater and updater update this block
        if (dto.getId() != null) {
            // if you already have a party, theres no need to create a new party
            Party party = partyService.getPartyById(dto.getId());
            if (party != null) {
                redirectAttributes.addFlashAttribute("error",
                        "Error creating Party: cant create more than one one party");
                return "redirect:/candidate/dashboard/settings/manage-party";
            }
        }

        try {

            Party party = new Party();
            party.setId(dto.getId());
            party.setName(dto.getName());
            party.setAbbreviation(dto.getAbbreviation());
            party.setLeader(dto.getLeader());
            party.setSlogan(dto.getSlogan());
            party.setDescription(dto.getDescription());
            party.setRegistered(dto.isRegistered());
            party.setFoundingDate(dto.getFoundingDate());
            party.setLogoUrl(dto.getLogoUrl());
            party.setWebsite(dto.getWebsite());
            party.setStatus(dto.isStatus());

            party.setRegistrationNo(dto.getRegistrationNo());
            party.setHeadquarters(dto.getHeadquarters());

            party.setColors(dto.getColors());
            party.setFacebook(dto.getFacebook());
            party.setInstagram(dto.getInstagram());
            party.setX(dto.getX());
            party.setYoutube(dto.getYoutube());

            String email = principal.getName();

            Candidate candidate = candidateService.getCandidateByEmail(email);

            party.setLeader(candidate.getFullName());

            // use the unique id to get the icon data
            Logo logo = null;
            if (dto.getUniqueId() != null || !dto.getUniqueId().equals("")) {

                logo = logoService.findByUniqueId(dto.getUniqueId());
                if (logo != null) {
                    party.setLogoUrl("/get-icon?id=" + dto.getUniqueId());
                }
            }

            party = partyService.saveParty(party);

            candidate.setParty(party);
            candidateService.save(candidate);

            // use the party id to set the link it with the logo
            if (logo != null) {
                logo.setLogoTypeId(party.getId());
                logo.setStatus(LogoStatus.PERMANENT);
                logoService.save(logo);
            }

            redirectAttributes.addFlashAttribute("success", "Party created successfully!");
            return "redirect:/candidate/dashboard/settings/manage-party";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating party: " + e.getMessage());
            return "redirect:/candidate/dashboard/settings/manage-party";
        }
    }

    @PostMapping("/dashboard/settings/manage-party/update")
    @PreAuthorize("hasRole('CANDIDATE') and isAuthenticated() and (hasAuthority('PRESIDENT') or hasAuthority('DEPUTY_PRESIDENT'))")
    public String updateParty(@Valid @ModelAttribute("party") PartyCreateDTO dto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Error updating Party");

            return "redirect:/candidate/dashboard/settings/manage-party";
        }

        try {
            System.out.println(dto.getId());
            Party party = partyService.getPartyById(dto.getId());

            if (party == null) {
                redirectAttributes.addFlashAttribute("error", "Party not found");
                return "redirect:/candidate/dashboard/settings/manage-party";
            }
            party.setId(dto.getId());
            party.setName(dto.getName());
            party.setAbbreviation(dto.getAbbreviation());
            party.setSlogan(dto.getSlogan());
            party.setDescription(dto.getDescription());
            party.setRegistered(dto.isRegistered());
            party.setFoundingDate(dto.getFoundingDate());
            party.setWebsite(dto.getWebsite());
            party.setStatus(dto.isStatus());

            party.setRegistrationNo(dto.getRegistrationNo());
            party.setHeadquarters(dto.getHeadquarters());

            

            party.setColors(dto.getColors());
            party.setFacebook(dto.getFacebook());
            party.setInstagram(dto.getInstagram());
            party.setX(dto.getX());
            party.setYoutube(dto.getYoutube());

            Logo logo = null;
            if (dto.getUniqueId() != null || !dto.getUniqueId().equals("")) {

                logo = logoService.findByUniqueId(dto.getUniqueId());
                if (logo != null) {
                    party.setLogoUrl("/get-icon?id=" + dto.getUniqueId());
                }
            }

            partyService.saveParty(party);

            if (logo != null) {
                logo.setLogoTypeId(party.getId());
                logo.setStatus(LogoStatus.PERMANENT);
                logoService.save(logo);
            }

            redirectAttributes.addFlashAttribute("success", "Party updated successfully!");
            return "redirect:/candidate/dashboard/settings/manage-party";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating party: " + e.getMessage());
            return "redirect:/candidate/dashboard/settings/manage-party";
        }
    }

    // manange account same as manage party
    @GetMapping("/dashboard/settings/manage-account")
    @PreAuthorize("hasRole('CANDIDATE') and isAuthenticated()")
    public String settingsManageAccount(Model model, Principal principal) {
        model.addAttribute("activePage", "settings");
        model.addAttribute("settingPage", "manage-account");

        return "auth/dashboard";
    }

}
