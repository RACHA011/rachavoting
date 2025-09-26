package com.racha.rachavoting.controller;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.racha.rachavoting.mapper.VoterMapper;
import com.racha.rachavoting.model.Election;
import com.racha.rachavoting.model.RegisteredEmail;
import com.racha.rachavoting.model.Voter;
import com.racha.rachavoting.payload.EmailDTO;
import com.racha.rachavoting.payload.ErrorResponse;
import com.racha.rachavoting.payload.constants.TokenResponse;
import com.racha.rachavoting.payload.constants.VotingTokenValidation;
import com.racha.rachavoting.payload.vote.CastVoteDto;
import com.racha.rachavoting.payload.vote.VarifyOtpDTO;
import com.racha.rachavoting.payload.vote.VoteDto;
import com.racha.rachavoting.payload.vote.VotingDto;
import com.racha.rachavoting.payload.voter.VoterCreateDTO;
import com.racha.rachavoting.payload.voter.VoterViewDTO;
import com.racha.rachavoting.services.ElectionService;
import com.racha.rachavoting.services.RegisteredEmailService;
import com.racha.rachavoting.services.TokenService;
import com.racha.rachavoting.services.VoterService;
import com.racha.rachavoting.services.components.DistrictService;
import com.racha.rachavoting.services.components.OtpService;
import com.racha.rachavoting.util.Hasher;
import com.racha.rachavoting.util.constants.Rondomkey;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

// TODO: Make a voters portal

/**
 * Controller responsible for voter registration and voting-related operations.
 * Handles OTP verification, voter registration, and email management.
 */
@Controller
@RequestMapping("/vote")
@Slf4j
public class VoteController {

    private static final String SESSION_TOKEN_COOKIE = "SESSION_TOKEN";
    private static final String REG_TOKEN_COOKIE = "REG_TOKEN";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final int MIN_TOKEN_LENGTH = 32;
    private static final int MAX_NATIONAL_ID_LENGTH = 100;

    @Autowired
    private VoterService voterService;

    @Autowired
    private DistrictService districtService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ElectionService electionService;

    @Autowired
    private RegisteredEmailService registeredEmailService;

    @Autowired
    private VoterMapper voterMapper;

    @Value("${app.otp.expiration}")
    private long otpExpirationMinutes;

    @Value("${app.link.url}")
    private String link;

    // ==================== VIEW ENDPOINTS ====================

    @GetMapping("/register-to-vote")
    public String showRegisterPage() {
        return "register";
    }

    @GetMapping("/register")
    public String showVoterRegistrationForm(
            @CookieValue(value = REG_TOKEN_COOKIE, required = false) String regToken,
            Model model) {

        if (regToken == null || regToken.isEmpty()) {
            model.addAttribute("error", "Registration not allowed or expired. Please try again.");
            return "register";
        }

        Map<String, List<String>> districtsByProvince = districtService.getDistrictsGroupedByProvince();
        model.addAttribute("districtsByProvince", districtsByProvince);
        return "register-to-vote";
    }

    @GetMapping("/register-success")
    public String showRegistrationSuccess(Model model,
            RedirectAttributes redirectAttrs) {

        // Check if coming from registration flow
        // String referer = request.getHeader("Referer");
        // if (referer != null && !referer.contains("/vote/register")) {
        // System.out.println("Invalid access attempt to registration success page");
        // redirectAttrs.addFlashAttribute("error", "Error accesing the page.");
        // return "redirect:/";
        // }

        // Check if success attributes exist
        if (!model.containsAttribute("success")) {
            redirectAttrs.addFlashAttribute("error", "Invalid access attempt");
            return "redirect:/";
        }

        return "register-success";
    }

    // ==================== OTP ENDPOINTS ====================

    /**
     * Sends an OTP to the user's email for voting registration verification.
     */
    @PostMapping("/send-otp")
    @ResponseBody
    public ResponseEntity<?> sendRegistrationOtp(@Valid @RequestBody EmailDTO emailDTO, HttpServletResponse response) {
        // Input validation
        String email = emailDTO.getEmail();
        if (!isValidEmail(email)) {
            return ResponseEntity.badRequest().body("Invalid email format.");
        }

        // Check if email is already registered
        if (isEmailAlreadyRegistered(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("This email is already registered for voting.");
        }

        // Generate OTP and session token
        String sessionToken = otpService.generateOtp(email);
        ResponseCookie cookie = createSessionCookie(sessionToken, response);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("OTP sent");
    }

    /**
     * Verifies the OTP provided by the user and generates a registration token.
     */
    @PostMapping("/varify-otp")
    @ResponseBody
    public ResponseEntity<?> verifyOtp(
            @Valid @RequestBody VarifyOtpDTO otpDTO,
            @CookieValue(value = SESSION_TOKEN_COOKIE, required = true) String sessionToken,
            HttpServletResponse response) {

        try {
            if (otpDTO == null || otpDTO.getOtp() == null || otpDTO.getOtp().isEmpty()) {
                return ResponseEntity.badRequest().body("OTP is required.");
            } else if (sessionToken == null || sessionToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized Access: session expired");
            }

            // Validate session token and extract email
            String email = otpService.validateAndExtractEmail(sessionToken);
            if (!isValidEmail(email)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email not valid.");
            }

            // Check if email is already registered
            if (isEmailAlreadyRegistered(email)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("This email is already registered for voting.");
            }

            // Verify OTP
            if (!otpService.validateOtp(sessionToken, otpDTO.getOtp())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("OTP is not valid or expired");
            }

            // Generate registration token
            String regToken = tokenService.generateRegistrationToken();
            if (!tokenService.storeToken(regToken, sessionToken)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to create registration session");
            }

            ResponseCookie cookie = createRegistrationCookie(regToken, response);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body("OTP valid");

        } catch (Exception e) {
            log.error("OTP verification failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid session token");
        }
    }

    // ==================== REGISTRATION ENDPOINTS ====================

    /**
     * Processes voter registration with full validation and persistence.
     */
    @PostMapping("/register")
    @Transactional
    public String processVoterRegistration(
            @Valid @ModelAttribute("voter") VoterCreateDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttrs,
            HttpSession session,
            @CookieValue(value = REG_TOKEN_COOKIE, required = false) String regToken, HttpServletResponse response) {

        try {

            // Check for duplicate submission
            if (session.getAttribute("processingRegistration") != null) {
                redirectAttrs.addFlashAttribute("error", "Registration already in progress");
                return "redirect:/vote/register";
            }

            // Validate form data
            if (bindingResult.hasErrors()) {
                redirectAttrs.addFlashAttribute("error", "Please correct the form errors and try again.");
                return "redirect:/vote/register";
            }

            // Mark registration as processing
            session.setAttribute("processingRegistration", true);
            session.setMaxInactiveInterval(30); // if more than 30 seconds pass, remove the session attribute

            // Validate registration token and extract email
            String email = validateRegistrationToken(regToken);
            if (email == null) {
                redirectAttrs.addFlashAttribute("error", "Invalid registration token. Please try again.");
                return "redirect:/vote/register-to-vote";
            }

            // Validate email and check for duplicates
            if (!isValidEmail(email) || isEmailAlreadyRegistered(email)) {
                redirectAttrs.addFlashAttribute("error", "Email validation failed.");
                return "redirect:/vote/register";
            }

            // Get current year elections
            List<Election> currentElections = getCurrentYearElections();
            if (currentElections.isEmpty()) {
                redirectAttrs.addFlashAttribute("error", "No elections available for registration.");
                return "redirect:/vote/register";
            }

            // Validate district selection
            if (!isValidDistrictSelection(dto.getProvince(), dto.getDistrict())) {
                redirectAttrs.addFlashAttribute("error", "Invalid district for the selected province.");
                return "redirect:/vote/register";
            }

            // generate a rondom id and its hashed value
            Rondomkey rondomkey = generateRondomkey();

            // Create and save voter
            String hashedNationalId = hashNationalId(dto.getNationalId());

            Voter voter = createAndSaveVoter(dto, hashedNationalId, currentElections, rondomkey);

            if (voter == null) {
                redirectAttrs.addFlashAttribute("error", "Failed to create voter. Please try again later.");
                return "redirect:/vote/register";
            }

            VoterViewDTO voterView = voterMapper.toPublicView(voter);

            // Save registered email
            RegisteredEmail registeredEmail = saveRegisteredEmail(email, currentElections);

            // clear cookies after a successfull registration
            clearCookie(SESSION_TOKEN_COOKIE, "/vote", response);
            clearCookie(REG_TOKEN_COOKIE, "/vote/register", response);

            // Send confirmation email
            sendConfirmationEmail(registeredEmail.getEncryptedEmail(), currentElections, voter.getRegestrationNo(),
                    registeredEmail.getUnsubscribeToken(), rondomkey.getRondomId());

            session.removeAttribute("processingRegistration");

            redirectAttrs.addFlashAttribute("success", "Registration complete!");

            redirectAttrs.addFlashAttribute("voter", voterView);

            return "redirect:/vote/register-success";

        } catch (Exception e) {

            log.error("Voter registration failed", e);
            redirectAttrs.addFlashAttribute("error", "Registration failed. Please try again.");
            return "redirect:/vote/register";
        }
    }

    // ==================== UNSUBSCRIBE ENDPOINT ====================

    // find a new way to access the unsubscribe with out the use for email as param
    // or direct encrypted token
    /**
     * Handles email unsubscription requests via encrypted token.
     */
    @GetMapping("/unsubscribe/{token}")
    public ResponseEntity<Map<String, Object>> processUnsubscribe(@PathVariable String token) {
        try {
            // TODO: make an algorithm for token generation
            // Validate token format
            if (token == null || token.length() < MIN_TOKEN_LENGTH) {
                return createErrorResponse(HttpStatus.BAD_REQUEST,
                        "Invalid Token",
                        "The unsubscribe link appears to be incomplete or malformed");
            }

            // // Decrypt email from token
            // String email = decryptEmailFromToken(token);
            // System.out.println(email);
            // if (!isValidEmail(email)) {
            // return createErrorResponse(HttpStatus.BAD_REQUEST,
            // "Invalid Email",
            // "The system couldn't identify a valid email address");
            // }

            // Process unsubscribe
            RegisteredEmail registeredEmail = registeredEmailService.getByToken(token);

            if (registeredEmail == null) {
                return createErrorResponse(HttpStatus.NOT_FOUND,
                        "Email Not Found",
                        "This email address is not registered in our system");
            }

            if (!registeredEmail.isSubscribed()) {
                return createSuccessResponse(HttpStatus.OK,
                        "Already Unsubscribed",
                        "This email address was already unsubscribed");
            }

            // Update subscription status
            registeredEmail.setSubscribed(false);
            registeredEmailService.save(registeredEmail);

            return createSuccessResponse(HttpStatus.OK,
                    "Unsubscribe Successful",
                    "You have been successfully unsubscribed");

        } catch (Exception e) {
            log.error("Unsubscribe processing failed for token: {}", token, e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "System Error",
                    "An unexpected error occurred. Please try again later.");
        }
    }

    @GetMapping("/vote")
    public String vote() {
        return "vote/vote";
    }

    // ==================== Voting Proccess ==================== //

    /**
     * Validates the voter's info using the provided VoteDto.
     * 
     * @param voteDto The DTO containing vote information.
     * @return ResponseEntity with validation result.
     */
    @PostMapping(value = "/vote/validate", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> validateVote(@Valid @RequestBody VoteDto voteDto) {
        Rondomkey rondomkey = new Rondomkey();
        Optional<Voter> optionalVoter = voterService.getByRegestrationNo(voteDto.getRegNo());

        if (optionalVoter.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UnAuthorized: Invalid registration number");
        }
        Voter voter = optionalVoter.get();

        String hashedRondomId = voter.getHashedId();

        if (rondomkey.verify(voteDto.getSecretKey(), hashedRondomId)) {
            try {
                if (Hasher.verify(voteDto.getIdNo(), voter.getHashedNationalId())) {
                    if (!voter.isVerified()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Voter is not verified to vote");
                    }
                    // Todo: Check if the voter has already voted in any of the elections

                    VotingDto votingDto = voterMapper.toVotingDto(voter);
                    TokenResponse tokenResponse1 = tokenService.generateVotingToken(voter.getUniqueId(),
                            new ArrayList<>(voter.getElections()).get(0).getPublicAccessKey() + "");
                    TokenResponse tokenResponse2 = tokenService.generateVotingToken(voter.getUniqueId(),
                            new ArrayList<>(voter.getElections()).get(1).getPublicAccessKey() + "");

                    // For now we`ll be sending raw tokens
                    List<String> tokens = new ArrayList<>();
                    tokens.add(tokenResponse1.getVotingToken());
                    tokens.add(tokenResponse2.getVotingToken());

                    // Return a map containing both the votingDto and tokens
                    Map<String, Object> responseBody = new HashMap<>();
                    responseBody.put("votingDto", votingDto);
                    responseBody.put("tokens", tokens);

                    return ResponseEntity.status(HttpStatus.OK).body(responseBody);
                }
            } catch (NoSuchAlgorithmException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UnAuthorized: Invalid security key");
            } catch (InvalidKeySpecException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UnAuthorized: Invalid National Id");
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
    }

    // New cast vote endpoint using the secure token
@PostMapping(value = "/vote/cast", produces = "application/json")
@ResponseBody
public ResponseEntity<?> castVote(
        @RequestHeader("X-Voting-Token") String votingToken,
        @Valid @RequestBody CastVoteDto castVoteDto) {
    
    try {
        // Validate the voting token
        VotingTokenValidation validation = tokenService.validateVotingToken(votingToken);
        
        // Additional validation: ensure election matches
        if (!validation.getElectionId().equals(castVoteDto.getElectionId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Token not valid for this election");
        }
        
        // Check if party exists and is valid for this election
        if (!isValidPartyForElection(castVoteDto.getPartyId(), castVoteDto.getElectionId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid party for this election");
        }
        
        // Cast the vote
        VoteResponse voteResponse = votingService.castVote(votingToken, castVoteDto.getPartyId());
        
        return ResponseEntity.status(HttpStatus.OK).body(voteResponse);
        
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body("Invalid or expired voting token");
    } catch (IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body("Vote already cast with this token");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Vote casting failed: " + e.getMessage());
    }
}


    // ==================== PRIVATE HELPER METHODS ==================== //

    private Rondomkey generateRondomkey() {
        Rondomkey rondomkey = new Rondomkey();
        return rondomkey.generateRondomkey();
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    private boolean isEmailAlreadyRegistered(String email) {
        List<String> existingEmails = registeredEmailService.getAllRegisteredEmails();
        return existingEmails.contains(email);
    }

    private ResponseCookie createSessionCookie(String token, HttpServletResponse response) {
        // Clear any existing cookie first
        clearCookie(SESSION_TOKEN_COOKIE, "/vote", response);

        // Create new cookie
        return ResponseCookie.from(SESSION_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(false) // Set to true in production
                .path("/vote")
                .maxAge(Duration.ofMinutes(5))
                .sameSite("Strict")
                .build();
    }

    private ResponseCookie createRegistrationCookie(String token, HttpServletResponse response) {
        // Clear any existing cookie first
        clearCookie(REG_TOKEN_COOKIE, "/vote/register", response);

        // Create new cookie
        return ResponseCookie.from(REG_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(false) // Set to true in production
                .path("/vote/register")
                .maxAge(Duration.ofMinutes(15))
                .sameSite("Strict")
                .build();
    }

    private void clearCookie(String name, String path, HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from(name, "")
                .maxAge(0)
                .path(path)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }

    private String validateRegistrationToken(String regToken) {
        if (regToken == null || regToken.isEmpty()) {
            return null;
        }

        try {
            String email = tokenService.validateRegistrationToken(regToken);
            if (email == null || email.isEmpty()) {
                throw new IllegalArgumentException("Invalid registration token");
            }
            return email;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid registration token validation attempt", e);
            return null;
        }
    }

    private List<Election> getCurrentYearElections() {
        return electionService.getElectionsByYearWithInitializedCollections(String.valueOf(Year.now().getValue()));
    }

    private boolean isValidDistrictSelection(String province, String district) {
        Map<String, List<String>> districtsByProvince = districtService.getDistrictsGroupedByProvince();
        List<String> validDistricts = districtsByProvince.get(province);
        return validDistricts != null && validDistricts.contains(district);
    }

    private String hashNationalId(String nationalId) {
        try {
            String hashedId = Hasher.hash(nationalId);
            if (hashedId.length() >= MAX_NATIONAL_ID_LENGTH) {
                throw new IllegalArgumentException("Invalid National ID");
            }
            return hashedId;
        } catch (java.security.NoSuchAlgorithmException | java.security.spec.InvalidKeySpecException e) {
            log.error("Failed to hash national ID", e);
            throw new IllegalArgumentException("Failed to hash national ID", e);
        }
    }

    private Voter createAndSaveVoter(VoterCreateDTO dto, String hashedNationalId, List<Election> elections,
            Rondomkey rondomkey) {
        String regno = voterService.generateRegistrationNo();
        Voter voter = voterMapper.createVote(dto);
        voter.setHashedNationalId(hashedNationalId);
        voter.setVerified(true);

        // Create new HashSet and manage relationships
        Set<Election> electionSet = new HashSet<>();
        elections.forEach(election -> {
            electionSet.add(election);
            if (election.getVoters() == null) {
                election.setVoters(new HashSet<>());
            }
            election.getVoters().add(voter);
        });

        voter.setElections(electionSet);
        voter.setRegestrationNo(regno);
        voter.setHashedId(rondomkey.getHashedId());

        return voterService.save(voter);
    }

    private RegisteredEmail saveRegisteredEmail(String email, List<Election> elections) {
        RegisteredEmail registeredEmail = new RegisteredEmail();
        String encryptEmail = registeredEmailService.encryptEmail(email);
        String token = UUID.randomUUID().toString();
        registeredEmail.setUnsubscribeToken(token);
        registeredEmail.setEncryptedEmail(encryptEmail);
        registeredEmail.setYear(String.valueOf(LocalDate.now().getYear()));
        registeredEmail.setElectionId(elections.stream()
                .map(Election::getPublicAccessKey)
                .toList());
        return registeredEmailService.save(registeredEmail);
    }

    private void sendConfirmationEmail(String encryptedEmail, List<Election> elections, String regNo,
            String unsubscribeToken, String secretKey) {
        try {
            Election firstElection = elections.get(0);
            voterService.sendVotingRegistrationConfirmation(
                    registeredEmailService.decryptEmail(encryptedEmail),
                    "National elections",
                    firstElection.getYear(),
                    encryptedEmail, regNo, secretKey);
            log.info("Registration confirmation email sent");
        } catch (Exception e) {
            log.warn("Failed to send registration confirmation email", e);
        }
    }

    // private String decryptEmailFromToken(String token) {
    // try {
    // return registeredEmailService.decryptEmail(token);
    // } catch (RegisteredEmailService.EmailDecryptionException e) {
    // log.warn("Failed to decrypt unsubscribe token", e);
    // throw new IllegalArgumentException("Invalid or expired token", e);
    // }
    // }

    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String title, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.value());
        response.put("error", title);
        response.put("message", message);
        response.put("timestamp", Instant.now());
        return ResponseEntity.status(status).body(response);
    }

    private ResponseEntity<Map<String, Object>> createSuccessResponse(HttpStatus status, String title, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.value());
        response.put("success", title);
        response.put("message", message);
        response.put("timestamp", Instant.now());
        return ResponseEntity.status(status).body(response);
    }

    // ==================== Error Handler ==================== //

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }

}