package com.racha.rachavoting.services;

import java.security.SecureRandom;
import java.time.Year;
import java.util.HashSet;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racha.rachavoting.model.Voter;
import com.racha.rachavoting.model.components.RegistrationSequence;
import com.racha.rachavoting.repository.VoterRepository;
import com.racha.rachavoting.util.email.EmailDetails;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class VoterService {

    private static final String ALLOWED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private VoterRepository voterRepository; // Assuming you have a VoterRepository

    @Autowired
    private EmailService emailService;

    @Value("${app.link.url}")
    private String link;

    @Autowired
    private RegistrationSequenceService registrationSequenceService;

    @Transactional
    public Voter save(Voter voter) {
        // Ensure proper relationship management
        if (voter.getElections() != null) {
            voter.getElections().forEach(election -> {
                if (election.getVoters() == null) {
                    election.setVoters(new HashSet<>());
                }
                election.getVoters().add(voter);
            });
        }
        return voterRepository.save(voter);
    }

    public Optional<Voter> getByRegestrationNo(String regNo) {
        return voterRepository.findByRegestrationNo(regNo);
    }

    public Optional<Voter> getByUniqueId(String uniqueId) {
        return voterRepository.findByUniqueId(uniqueId);
    }

    public boolean existsByHashedNationalId(String hashedNationalId) {
        return voterRepository.existsByHashedNationalId(hashedNationalId);
    }

    public boolean sendVotingRegistrationConfirmation(String email, String electionName, String electionYear,
            String unsubscribeToken, String regNo, String secretKey) {
        try {
            String unsubscribeLink = link + "/vote/unsubscribe/" + unsubscribeToken;
            String voterPortalLink = link + "/vote/voter-portal";

            String emailContent = String.format(
                    """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <style>
                                    body {
                                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                                        line-height: 1.6;
                                        color: #333;
                                        max-width: 600px;
                                        margin: 0 auto;
                                        padding: 20px;
                                    }
                                    .header {
                                        background-color: #007A4D; /* SA green */
                                        color: white;
                                        padding: 20px;
                                        text-align: center;
                                        border-radius: 5px 5px 0 0;
                                    }
                                    .content {
                                        padding: 25px;
                                        background-color: #f9f9f9;
                                    }
                                    .confirmation-box {
                                        background-color: #002395; /* SA blue */
                                        color: white;
                                        font-size: 20px;
                                        padding: 20px;
                                        text-align: center;
                                        margin: 20px 0;
                                        border-radius: 5px;
                                        border-left: 5px solid #DE3831; /* SA red accent */
                                    }
                                    .footer {
                                        margin-top: 30px;
                                        padding-top: 20px;
                                        border-top: 1px solid #eee;
                                        font-size: 12px;
                                        color: #777;
                                        text-align: center;
                                    }
                                    .button {
                                        display: inline-block;
                                        padding: 10px 20px;
                                        background-color: #DE3831; /* SA red */
                                        color: white !important;
                                        text-decoration: none;
                                        border-radius: 5px;
                                        font-weight: bold;
                                        margin: 15px 0;
                                    }
                                    .election-details {
                                        background-color: white;
                                        border: 1px solid #ddd;
                                        border-radius: 5px;
                                        padding: 15px;
                                        margin: 20px 0;
                                    }
                                    .credentials-box {
                                        background-color: #fff8e1;
                                        border: 1px solid #ffd54f;
                                        border-radius: 5px;
                                        padding: 15px;
                                        margin: 20px 0;
                                    }
                                    .warning {
                                        color: #d32f2f;
                                        font-weight: bold;
                                    }
                                </style>
                            </head>
                            <body>
                                <div class="header">
                                    <h1>Racha voting Online</h1>
                                    <p>Voter Registration Confirmation</p>
                                </div>

                                <div class="content">
                                    <div class="confirmation-box">
                                        <strong>Your voter registration was successful!</strong>
                                    </div>

                                    <p>Dear Voter,</p>

                                    <p>Thank you for registering to vote in the upcoming election. Your registration has been processed successfully.</p>

                                    <div class="election-details">
                                        <h3>Election Details</h3>
                                        <p><strong>Election:</strong> %s</p>
                                        <p><strong>Year:</strong> %s</p>
                                    </div>

                                    <div class="credentials-box">
                                        <h3>Your Voting Credentials</h3>
                                        <p><strong>Registration Number:</strong> %s</p>
                                        <p><strong>Secret Key:</strong> %s</p>
                                        <p class="warning">IMPORTANT: Keep these credentials secure. You will need them to vote.</p>
                                        <p>Do not share these details with anyone. Racha voting will never ask for your secret key.</p>
                                    </div>

                                    <p>We will notify you when voting opens with instructions on how to cast your ballot.</p>

                                    <p>For any questions about your registration, please contact our support team.</p>

                                    <center>
                                        <a href="%s" class="button">
                                            Access Your Voter Portal
                                        </a>
                                    </center>
                                </div>

                                <div class="footer">
                                    <p>© %d Racha Voting</p>
                                    <p>
                                        <a href="%s" style="color: #00f; text-decoration: none;">Unsubscribe </a> from future communications
                                    </p>
                                </div>
                            </body>
                            </html>
                            """,
                    electionName,
                    electionYear,
                    regNo,
                    secretKey,
                    voterPortalLink,
                    Year.now().getValue(),
                    unsubscribeLink);

            // Plain text alternative
            String plainTextContent = String.format(
                    """
                            Racha Voting ONLINE - REGISTRATION CONFIRMATION

                            Dear Voter,

                            Your voter registration was successful!

                            Election Details:
                            Election: %s
                            Year: %s

                            YOUR VOTING CREDENTIALS:
                            Registration No: %s
                            Secret Key: %s

                            IMPORTANT: Keep these credentials secure. You will need them to vote.
                            Do not share these details with anyone. Racha voting will never ask for your secret key.

                            We will notify you when voting opens with instructions on how to cast your ballot.

                            Access Your Voter Portal: %s

                            For any questions about your registration, please contact our support team.

                            ---
                            To unsubscribe from future communications, visit: %s

                            © %d Racha Voting.
                            """,
                    electionName,
                    electionYear,
                    regNo,
                    secretKey,
                    voterPortalLink,
                    unsubscribeLink,
                    Year.now().getValue());

            EmailDetails emailDetails = new EmailDetails();
            emailDetails.setRecipient(email);
            emailDetails.setMsgBody(emailContent);
            emailDetails.setSubject("Your Voter Registration Confirmation - " + electionYear);
            emailDetails.setHtml(true);
            emailDetails.setPlainTextContent(plainTextContent);

            return emailService.sendEmail(emailDetails);

        } catch (Exception e) {
            log.error("Failed to send registration confirmation to {}", email, e);
            return false;
        }
    }

    public String generateRegistrationNo() {
        RegistrationSequence sequence = registrationSequenceService
                .getCurrentRegistrationSequence(Year.now().getValue(), "VOTER");
        sequence.setCounter(sequence.getCounter() + 1);

        String year = String.valueOf(Year.now().getValue());

        String randomCode = generateRandomCode();

        sequence = registrationSequenceService.save(sequence);

        return String.format("RA-VOTE-%s-%06d-%s", year, sequence.getCounter(), randomCode);
    }

    /**
     * Generates a random 4-character code using alphanumeric characters.
     *
     * @return A random 4-character code.
     */
    private static String generateRandomCode() {
        StringBuilder sb = new StringBuilder(4);

        for (int i = 0; i < 4; i++) {
            int randomIndex = secureRandom.nextInt(ALLOWED_CHARACTERS.length());
            char randomChar = ALLOWED_CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }
        return sb.toString();
    }

    
}
