document.addEventListener('DOMContentLoaded', function() {
    const submitButton = document.getElementById('submit-vote');
    const errorMessage = document.getElementById('error-message');
    const votingForm = document.getElementById('voting-form');
    const thankYou = document.getElementById('thank-you');
    const voteConfirmation = document.getElementById('vote-confirmation');
    
    submitButton.addEventListener('click', function() {
        const selectedCandidate = document.querySelector('input[name="candidate"]:checked');
        
        if (!selectedCandidate) {
            errorMessage.style.display = 'flex';
            return;
        }
        
        errorMessage.style.display = 'none';
        
        // Get the selected candidate's name and party
        const candidateLabel = document.querySelector(`label[for="${selectedCandidate.id}"]`);
        const candidateName = candidateLabel.querySelector('.candidate-name').textContent;
        
        // Update the confirmation message
        voteConfirmation.innerHTML = `
            You have voted for <strong>${candidateName}</strong>.
            Thank you for participating in this democratic process.
        `;
        
        // Hide the voting form and show the thank you message
        votingForm.style.display = 'none';
        thankYou.style.display = 'block';
        
        // In a real app, you would send the vote to a backend here
        console.log('Vote submitted for:', selectedCandidate.value);
    });
    
    // Make the entire radio option clickable
    const radioOptions = document.querySelectorAll('.radio-option');
    radioOptions.forEach(option => {
        option.addEventListener('click', function() {
            const radio = this.querySelector('input[type="radio"]');
            radio.checked = true;
        });
    });
});