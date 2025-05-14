document.addEventListener("DOMContentLoaded", function () {
  // Get election data from Thymeleaf
  const electionData = /*[[${election}]]*/ {};

  // Calculate voter turnout
  const registeredVoters = parseFloat(
    document.getElementById("registeredVoters").textContent.replace(/,/g, "")
  );
  const totalVotes = parseFloat(
    document.getElementById("totalVotes").textContent.replace(/,/g, "")
  );

  // Avoid division by zero
  const turnoutPercentage =
    registeredVoters > 0
      ? ((totalVotes / registeredVoters) * 100).toFixed(2)
      : "0.00";
  document.getElementById("voterTurnout").textContent = `${turnoutPercentage}%`;

  // Update time remaining
  updateTimeRemaining();

  // Update progress
  updateProgress();

  // Initialize chart - Make sure Chart.js is loaded
  if (typeof Chart !== "undefined") {
    initializeChart(registeredVoters, totalVotes);
  } else {
    console.error(
      "Chart.js is not loaded. Please include the Chart.js library."
    );
    document.querySelector(".chart-container").innerHTML =
      '<div class="chart-error">Chart visualization unavailable</div>';
  }

  // Set up interval to update time remaining
  setInterval(updateTimeRemaining, 60000); // Update every minute
});

function updateTimeRemaining() {
  // Get end date from the DOM instead of relying on JavaScript variables
  const endDateText = document.getElementById("endDate").textContent.trim();
  const endDateParts = endDateText.match(
    /(\w+)\s+(\d+),(\d+)\s+at\s+(\d+):(\d+)/
  );

  if (!endDateParts) {
    console.error("Could not parse end date:", endDateText);
    return;
  }

  const months = {
    January: 0,
    February: 1,
    March: 2,
    April: 3,
    May: 4,
    June: 5,
    July: 6,
    August: 7,
    September: 8,
    October: 9,
    November: 10,
    December: 11,
  };

  const month = months[endDateParts[1]];
  const day = parseInt(endDateParts[2]);
  const year = parseInt(endDateParts[3]);
  const hour = parseInt(endDateParts[4]);
  const minute = parseInt(endDateParts[5]);

  const end = new Date(year, month, day, hour, minute);
  const now = new Date();

  if (now > end) {
    document.getElementById("timeRemaining").innerHTML = `
      <div class="countdown-label">Election has ended</div>
      <div class="countdown">Results are being processed</div>
    `;
    return;
  }

  const diff = end - now;
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

  document.getElementById("timeRemaining").innerHTML = `
    <div class="countdown-label">Time remaining:</div>
    <div class="countdown">${days} days, ${hours} hours, ${minutes} minutes</div>
  `;
}

function updateProgress() {
  // Get dates from the DOM
  const startDateText = document.getElementById("startDate").textContent.trim();
  const endDateText = document.getElementById("endDate").textContent.trim();

  const parseDate = (dateText) => {
    const dateParts = dateText.match(/(\w+)\s+(\d+),(\d+)\s+at\s+(\d+):(\d+)/);
    if (!dateParts) return null;

    const months = {
      January: 0,
      February: 1,
      March: 2,
      April: 3,
      May: 4,
      June: 5,
      July: 6,
      August: 7,
      September: 8,
      October: 9,
      November: 10,
      December: 11,
    };

    return new Date(
      parseInt(dateParts[3]),
      months[dateParts[1]],
      parseInt(dateParts[2]),
      parseInt(dateParts[4]),
      parseInt(dateParts[5])
    );
  };

  const start = parseDate(startDateText);
  const end = parseDate(endDateText);
  const now = new Date();

  if (!start || !end) {
    console.error("Could not parse dates:", startDateText, endDateText);
    return;
  }

  const totalDuration = end - start;
  const elapsed = now - start;

  let progressPercentage = 0;

  if (now < start) {
    progressPercentage = 0;
  } else if (now > end) {
    progressPercentage = 100;
  } else {
    progressPercentage = Math.floor((elapsed / totalDuration) * 100);
  }

  document.getElementById(
    "progressPercentage"
  ).textContent = `${progressPercentage}%`;
  document.getElementById(
    "progressFill"
  ).className = `progress-fill w-${progressPercentage}`;

  // Update status circles based on progress
  const registrationCircle = document.querySelector(
    ".progress-stat:nth-child(1) .stat-circle"
  );
  const votingCircle = document.querySelector(
    ".progress-stat:nth-child(2) .stat-circle"
  );
  const resultsCircle = document.querySelector(
    ".progress-stat:nth-child(3) .stat-circle"
  );

  const registrationStatus = document.querySelector(
    ".progress-stat:nth-child(1) .stat-value"
  );
  const votingStatus = document.querySelector(
    ".progress-stat:nth-child(2) .stat-value"
  );
  const resultsStatus = document.querySelector(
    ".progress-stat:nth-child(3) .stat-value"
  );

  if (now < start) {
    // Before election starts
    registrationCircle.className = "stat-circle completed";
    votingCircle.className = "stat-circle pending";
    resultsCircle.className = "stat-circle pending";

    registrationStatus.textContent = "Completed";
    votingStatus.textContent = "Upcoming";
    resultsStatus.textContent = "Pending";
  } else if (now > end) {
    // After election ends
    registrationCircle.className = "stat-circle completed";
    votingCircle.className = "stat-circle completed";
    resultsCircle.className = "stat-circle active";

    registrationStatus.textContent = "Completed";
    votingStatus.textContent = "Completed";
    resultsStatus.textContent = "Processing";
  } else {
    // During election
    registrationCircle.className = "stat-circle completed";
    votingCircle.className = "stat-circle active";
    resultsCircle.className = "stat-circle pending";

    registrationStatus.textContent = "Completed";
    votingStatus.textContent = "In Progress";
    resultsStatus.textContent = "Pending";
  }
}

document.getElementById("goback").addEventListener("click", function (event) {
  event.preventDefault();
  window.history.back();
});

function initializeChart(registeredVoters, totalVotes) {
  try {
    const ctx = document.getElementById("voterChart").getContext("2d");
    if (!ctx) {
      console.error("Could not get canvas context");
      return;
    }

    const votesRemaining = registeredVoters - totalVotes;

    // Destroy existing chart if it exists
    if (window.voterChart) {
      window.voterChart = null;
    }

    window.voterChart = new Chart(ctx, {
      type: "doughnut",
      data: {
        labels: ["Votes Cast", "Remaining Voters"],
        datasets: [
          {
            data: [totalVotes, votesRemaining],
            backgroundColor: ["#10b981", "#e5e7eb"],
            borderColor: ["#ffffff", "#ffffff"],
            borderWidth: 2,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: "70%",
        plugins: {
          legend: {
            position: "bottom",
            labels: {
              padding: 20,
              boxWidth: 12,
            },
          },
          tooltip: {
            callbacks: {
              label: function (context) {
                const label = context.label || "";
                const value = context.raw || 0;
                const percentage = Math.round((value / registeredVoters) * 100);
                return `${label}: ${value.toLocaleString()} (${percentage}%)`;
              },
            },
          },
        },
      },
    });
  } catch (error) {
    console.error("Error initializing chart:", error);
    document.querySelector(".chart-container").innerHTML =
      '<div class="chart-error">Error loading chart: ' +
      error.message +
      "</div>";
  }
}
