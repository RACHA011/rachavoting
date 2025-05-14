package com.racha.rachavoting.controller;

import java.math.BigInteger;
import java.security.Principal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.racha.rachavoting.model.Authority;
import com.racha.rachavoting.model.Candidate;
import com.racha.rachavoting.model.Election;
import com.racha.rachavoting.model.Vote;
import com.racha.rachavoting.payload.admin.ElectionCreateDTO;
import com.racha.rachavoting.payload.admin.ElectionViewDTO;
import com.racha.rachavoting.payload.candidate.CandidateViewDTO;
import com.racha.rachavoting.services.AuthorityService;
import com.racha.rachavoting.services.CandidateService;
import com.racha.rachavoting.services.ElectionService;
import com.racha.rachavoting.util.party.PartyPosition;
import com.racha.rachavoting.util.party.PartyPositionCategory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Handles admin-related operations such as managing elections.
 * Restricted to users with the ADMIN role.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN') and isAuthenticated()")
public class AdminController {

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private ElectionService electionService;

    @Autowired
    private AuthorityService authorityService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("user", principal.getName());
        model.addAttribute("title", "Admin Dashboard");
        model.addAttribute("pagetitle", "Dashboard");
        return "admin/auth/dashboard";
    }

    @GetMapping("/history")
    public String history(Model model, Principal principal) {
        model.addAttribute("user", principal.getName());
        model.addAttribute("title", "Voting History");
        model.addAttribute("pagetitle", "History");
        return "admin/auth/history";
    }

    @GetMapping("/setting")
    public String setting(Model model, Principal principal) {
        model.addAttribute("user", principal.getName());
        model.addAttribute("title", "Settings");
        model.addAttribute("pagetitle", "Settings");
        return "admin/auth/setting";
    }

    @GetMapping("/voter")
    public String voter(Model model, Principal principal) {
        model.addAttribute("user", principal.getName());
        model.addAttribute("title", "Voter Management");
        model.addAttribute("pagetitle", "Voter Management");
        return "admin/auth/voters";
    }

    @GetMapping("/candidate")
    public String candidate(@RequestParam(value = "search", required = false) String searchTerm,
            @PageableDefault(size = 10) Pageable pageable,
            Model model,
            Principal principal) {

        // Existing model attributes
        model.addAttribute("user", principal.getName());
        model.addAttribute("title", "Candidate Management");
        model.addAttribute("pagetitle", "Candidate Management");
        model.addAttribute("TotalCandidate", candidateService.candidateCount());
        model.addAttribute("ActiveCandidate", candidateService.candidateCountStatusTrue());
        model.addAttribute("elections", electionService.getActiveElection());
        model.addAttribute("Approval", candidateService.getNonVarifiedPresidents().size());

        // Search functionality
        Page<Candidate> pageCandidates = candidateService.searchCandidates(searchTerm, pageable);
        Page<CandidateViewDTO> dtoPage = pageCandidates.map(candidate -> {
            CandidateViewDTO dto = new CandidateViewDTO();
            dto.setId(candidate.getId());
            dto.setFullName(candidate.getFullName());
            dto.setEmail(candidate.getEmail());
            dto.setActive(candidate.isActive());
            dto.setStatus(candidate.getStatus());
            dto.setProfilePicture(candidate.getProfilePicture());
            dto.setValid(candidate.isValid());
            dto.setPresidentialCandidate(candidate.isPresidentialCandidate() && candidate.isValid());// a candidate can
                                                                                                     // only be consider
                                                                                                     // a presidentioal
                                                                                                     // candidate if
                                                                                                     // validated
            return dto;
        });

        model.addAttribute("candidates", dtoPage.getContent());
        model.addAttribute("page", dtoPage);
        model.addAttribute("searchTerm", searchTerm);

        return "admin/auth/candidate";
    }

    @PostMapping("/candidate/search")
    public String searchCandidates(
            @RequestParam("searchTerm") String searchTerm,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addAttribute("search", searchTerm);
        return "redirect:/admin/candidate";
    }

    /**
     * Assigns a role to a candidate.
     * 
     * @param id    The unique identifier of the candidate
     * @param role  The role to assign to the candidate (e.g., "president")
     * @param model The Spring MVC model for adding attributes
     * @return Redirect path after processing
     */
    @PostMapping("/candidate/role/{role}/{id}")
    public String assignRole(
            @PathVariable("id") String id,
            @PathVariable String role,
            RedirectAttributes redirectAttributes) { // Use RedirectAttributes instead of Model

        try {

            Candidate candidate = candidateService.getCandidateById(Long.parseLong(id));
            String normalizedRole = role.toUpperCase(); // Normalize to uppercase

            if ("PRESIDENT".equals(normalizedRole)) {
                // Your existing president logic
                Authority partyLeadershipAuthority = authorityService
                        .findByName(PartyPositionCategory.PARTY_LEADERSHIP.name())
                        .orElseGet(() -> {
                            Authority newAuthority = new Authority();
                            newAuthority.setName(PartyPositionCategory.PARTY_LEADERSHIP.name());
                            return authorityService.save(newAuthority);
                        });

                Authority presidentAuthority = authorityService
                        .findByName(PartyPosition.PRESIDENT.name())
                        .orElseGet(() -> {
                            Authority newAuthority = new Authority();
                            newAuthority.setName(PartyPosition.PRESIDENT.name());
                            return authorityService.save(newAuthority);
                        });

                candidate.getAuthorityIds().add(partyLeadershipAuthority.getId());
                candidate.getAuthorityIds().add(presidentAuthority.getId());
                candidate.setPresidentialCandidate(true);
                candidate.setValid(true);
            }

            candidateService.save(candidate);
            redirectAttributes.addFlashAttribute("success", "Role assigned successfully");

        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid ID format");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/admin/candidate";
    }

    @GetMapping("/election")
    public String election(Model model, Principal principal) {
        model.addAttribute("user", principal.getName());
        model.addAttribute("title", "Election Management");
        model.addAttribute("pagetitle", "Election Management");

        List<Election> elections = electionService.getAllElections()
                .stream()
                .sorted((e1, e2) -> Integer.compare(Integer.parseInt(e2.getYear()), Integer.parseInt(e1.getYear())))
                .toList();

        List<ElectionViewDTO> electionViewDTOs = new ArrayList<>();

        for (Election election : elections) {
            ElectionViewDTO dto = new ElectionViewDTO();
            dto.setTitle(election.getTitle());
            dto.setYear(election.getYear());
            dto.setStartDate(election.getStartDate());
            dto.setEndDate(election.getEndDate());
            dto.setRegistrationDeadline(election.getRegistrationDeadline());
            dto.setActive(election.isActive());
            dto.setDescription(election.getDescription());
            dto.setRegisteredVoters(election.getRegisteredVoters());
            dto.setTotalVotes(election.getTotalVotes());

            electionViewDTOs.add(dto);
        }
        ;

        model.addAttribute("elections", electionViewDTOs);

        return "admin/auth/election";
    }

    /**
     * Displays details of a specific election based on the year.
     *
     * @param year      The year of the election to fetch.
     * @param model     The model to pass data to the view.
     * @param principal The currently authenticated user.
     * @return The view for displaying election details.
     */
    @GetMapping("/election/{year}")
    public String electionByYear(@PathVariable String year, Model model, Principal principal) {
        model.addAttribute("user", principal.getName());
        model.addAttribute("title", "Election Management");
        model.addAttribute("pagetitle", "Election Management");

        Election election = electionService.findElectionByYear(year);
        if (election != null) {
            ElectionViewDTO dto = new ElectionViewDTO();
            dto.setTitle(election.getTitle());
            dto.setYear(election.getYear());
            dto.setStartDate(election.getStartDate());
            dto.setEndDate(election.getEndDate());
            dto.setRegistrationDeadline(election.getRegistrationDeadline());
            dto.setActive(election.isActive());
            dto.setDescription(election.getDescription());
            dto.setRegisteredVoters(election.getRegisteredVoters());
            dto.setTotalVotes(election.getTotalVotes());
            model.addAttribute("election", dto);
        } else {
            model.addAttribute("error", "Election not found for the year: " + year);
        }

        return "admin/auth/componets/electionview";
    }

    /**
     * Handles the creation of a new election.
     *
     * @param dto       The data transfer object containing election details.
     * @param result    The binding result for validation errors.
     * @param model     The model to pass data to the view.
     * @param principal The currently authenticated user.
     * @return The view for creating an election or a redirect to the election list
     *         on success.
     */
    @PostMapping("/election")
    public String handleCreateElection(
            @Valid @ModelAttribute("electionCreateDTO") ElectionCreateDTO dto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Principal principal,
            HttpServletRequest request) {

        // redirectAttributes.addFlashAttribute("user", principal.getName());
        // redirectAttributes.addFlashAttribute("title", "Create New Election");
        // redirectAttributes.addFlashAttribute("pagetitle", "New Election");

        // Validate business rules only if no basic validation errors
        if (!result.hasErrors()) {
            // Check for duplicate year
            // if (electionService.existsByYear(dto.getYear() + "")) {
            // result.rejectValue("year", "duplicate", "Election for this year already
            // exists");
            // }

            // Check registration deadline
            if (dto.getRegistrationDeadline().isAfter(dto.getStartDate())) {
                result.rejectValue("registrationDeadline", "invalid",
                        "Registration must close before voting starts");
                redirectAttributes.addFlashAttribute("error", "Registration must close before voting starts");
            }

            // Check for date overlap
            // if (electionService.hasDateOverlap(dto.getStartDate(), dto.getEndDate())) {
                
            //     result.rejectValue("startDate", "overlap", "Dates overlap with existing election");
            //     redirectAttributes.addFlashAttribute("error", "Dates overlap with existing election");

            // }
        }

        // Return early if there are validation errors
        if (result.hasErrors()) {
            return "redirect:/admin/election";
        }

        // Create election if validation passes
        try {
            Election election = new Election();
            election.setTitle(dto.getTitle());
            election.setYear(dto.getYear() + "");
            election.setStartDate(dto.getStartDate());
            election.setEndDate(dto.getEndDate());
            election.setRegistrationDeadline(dto.getRegistrationDeadline());
            election.setDescription(dto.getDescription());
            election.setActive(false);
            election.setCreatedAt(LocalDateTime.now());
            election.setUpdatedAt(LocalDateTime.now());
            election.setCreatedBy(principal.getName());
            election.setRegisteredVoters(new BigInteger("0"));
            election.setVotes(new ArrayList<Vote>());

            electionService.saveElection(election);

            redirectAttributes.addFlashAttribute("showSuccessModal", true);
            return "redirect:/admin/election";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Creation failed: " + e.getMessage());
            return "redirect:/admin/election";
        }
    }

    /**
     * Formats a BigInteger into a human-readable string with commas.
     *
     * @param number The BigInteger to format.
     * @return A formatted string representation of the number.
     */
    public String formatBigInteger(BigInteger number) {
        return NumberFormat.getNumberInstance().format(number);
    }
}
