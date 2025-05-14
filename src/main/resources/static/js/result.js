document.addEventListener('DOMContentLoaded', function() {
    initializeTabs();
    setTimeout(loadAllResults, 1000);
});

/**
 * Initializes the tab switching functionality
 */
function initializeTabs() {
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabContents = document.querySelectorAll('.tab-content');
    
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const tabId = this.getAttribute('data-tab');
            
            // Toggle active state
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabContents.forEach(content => content.classList.remove('active'));
            
            this.classList.add('active');
            document.getElementById(`${tabId}-tab`).classList.add('active');
        });
    });
}

/**
 * Loads all election result data
 */
function loadAllResults() {
    loadPresidentialResults();
    loadCongressionalResults();
}

/**
 * Loads and displays presidential election results
 */
function loadPresidentialResults() {
    const presidentialResults = [
        { name: "Jane Smith", party: "Progressive Party", votes: 1250000, percentage: 42 },
        { name: "John Doe", party: "Conservative Party", votes: 1100000, percentage: 37 },
        { name: "Alex Johnson", party: "Centrist Alliance", votes: 450000, percentage: 15 },
        { name: "Maria Rodriguez", party: "Green Party", votes: 180000, percentage: 6 }
    ];
    
    // Hide loading spinners
    hideElement('presidential-loading');
    hideElement('pie-chart-loading');
    
    // Show pie chart
    showElement('pie-chart-container');
    
    // Render results
    renderPresidentialResults(presidentialResults);
    createPieChart(presidentialResults);
}

/**
 * Renders the presidential results HTML
 */
function renderPresidentialResults(results) {
    let resultsHTML = '';
    let legendHTML = '';
    
    results.forEach(candidate => {
        const partyColor = getPartyColor(candidate.party);
        
        resultsHTML += `
            <div class="candidate-result">
                <div class="result-header">
                    <div class="candidate-info">
                        <span class="candidate-name">${candidate.name}</span>
                        <span class="candidate-party">${candidate.party}</span>
                    </div>
                    <div class="vote-info">
                        <span class="percentage">${candidate.percentage}%</span>
                        <span class="votes">${candidate.votes.toLocaleString()} votes</span>
                    </div>
                </div>
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${candidate.percentage}%; background-color: ${partyColor};"></div>
                </div>
            </div>
        `;
        
        legendHTML += `
            <div class="legend-item">
                <div class="legend-color" style="background-color: ${partyColor};"></div>
                <span>${candidate.name} (${candidate.percentage}%)</span>
            </div>
        `;
    });
    
    document.getElementById('presidential-results').innerHTML = resultsHTML;
    document.getElementById('presidential-legend').innerHTML = legendHTML;
}

/**
 * Loads and displays congressional election results
 */
function loadCongressionalResults() {
    const congressionalResults = [
        { name: "Progressive Party", seats: 220, percentage: 51 },
        { name: "Conservative Party", seats: 210, percentage: 48 },
        { name: "Other Parties", seats: 5, percentage: 1 }
    ];
    
    // Hide loading spinner
    hideElement('congressional-loading');
    
    // Show results
    showElement('congressional-results');
    
    // Render results
    renderCongressionalBars(congressionalResults);
    renderCongressionalDots();
}

/**
 * Renders the congressional results bars
 */
function renderCongressionalBars(results) {
    let barsHTML = '';
    
    results.forEach(party => {
        const partyColor = getPartyColor(party.name);
        
        barsHTML += `
            <div class="candidate-result">
                <div class="result-header">
                    <div class="candidate-info">
                        <span class="candidate-name">${party.name}</span>
                    </div>
                    <div class="vote-info">
                        <span class="percentage">${party.seats} seats</span>
                        <span class="votes">${party.percentage}%</span>
                    </div>
                </div>
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${party.percentage}%; background-color: ${partyColor};"></div>
                </div>
            </div>
        `;
    });
    
    document.getElementById('congressional-bars').innerHTML = barsHTML;
}

/**
 * Renders the congressional seat visualization
 */
function renderCongressionalDots() {
    let dotsHTML = '';
    let legendHTML = '';
    
    // Generate dots for each seat
    for (let i = 0; i < 435; i++) {
        let color = '#d1d5db';
        if (i < 220) color = '#3b82f6';
        else if (i < 430) color = '#ef4444';
        
        dotsHTML += `<div class="dot" style="background-color: ${color};" title="Seat ${i+1}"></div>`;
    }
    
    // Generate legend
    legendHTML = `
        <div class="legend-item">
            <div class="legend-color" style="background-color: #3b82f6;"></div>
            <span>Progressive Party (220 seats)</span>
        </div>
        <div class="legend-item">
            <div class="legend-color" style="background-color: #ef4444;"></div>
            <span>Conservative Party (210 seats)</span>
        </div>
        <div class="legend-item">
            <div class="legend-color" style="background-color: #d1d5db;"></div>
            <span>Other Parties (5 seats)</span>
        </div>
    `;
    
    document.getElementById('congress-dots').innerHTML = dotsHTML;
    document.getElementById('congress-legend').innerHTML = legendHTML;
}

/**
 * Creates a pie chart visualization
 */
function createPieChart(results) {
    const pieChart = document.querySelector('.pie-chart');
    
    let cumulativePercentage = 0;
    let pieHTML = '';
    
    // Create pie segments
    results.forEach(candidate => {
        const partyColor = getPartyColor(candidate.party);
        const startAngle = cumulativePercentage * 3.6; // Convert percentage to degrees (360 / 100 = 3.6)
        const endAngle = (cumulativePercentage + candidate.percentage) * 3.6;
        
        pieHTML += `
            <div style="
                position: absolute;
                width: 100%;
                height: 100%;
                background-color: ${partyColor};
                clip-path: polygon(50% 50%, 50% 0%, ${endAngle <= 90 ? 50 + 50 * Math.tan(endAngle * Math.PI / 180) : 100}% ${endAngle <= 90 ? 0 : endAngle <= 180 ? 50 - 50 * Math.tan((180 - endAngle) * Math.PI / 180) : 0}%, ${endAngle >= 180 ? 0 : 50 - 50 * Math.tan(startAngle * Math.PI / 180)}% ${startAngle <= 90 ? 0 : startAngle <= 180 ? 50 - 50 * Math.tan((180 - startAngle) * Math.PI / 180) : 100}%);
                transform: rotate(${startAngle}deg);
            "></div>
        `;
        
        cumulativePercentage += candidate.percentage;
    });
    
    // Add center white circle for donut effect
    pieHTML += `
        <div style="
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 30%;
            height: 30%;
            background-color: white;
            border-radius: 50%;
        "></div>
    `;
    
    pieChart.innerHTML = pieHTML;
}

/**
 * Maps party names to their colors
 */
function getPartyColor(party) {
    const partyColors = {
        "Progressive Party": "#3b82f6", // Blue
        "Conservative Party": "#ef4444", // Red
        "Centrist Alliance": "#eab308",  // Yellow
        "Green Party": "#10b981",        // Green
    };
    
    return partyColors[party] || "#6b7280"; // Default to gray
}

/**
 * Helper function to hide an element
 */
function hideElement(id) {
    document.getElementById(id).style.display = 'none';
}

/**
 * Helper function to show an element
 */
function showElement(id) {
    document.getElementById(id).style.display = 'block';
}