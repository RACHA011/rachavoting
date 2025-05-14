const menuToggle = document.getElementById("menuToggle");
const sidebar = document.getElementById("sidebar");
const closeErrorModal = document.getElementById("closeErrorModal");

if (menuToggle || sidebar) {
  document.addEventListener("DOMContentLoaded", function () {
    if (menuToggle && sidebar) {
      menuToggle.addEventListener("click", function () {
        sidebar.classList.toggle("sidebar-open");
        sidebar.classList.toggle("sidebar-collapsed");

        // Store state in localStorage
        const isCollapsed = sidebar.classList.contains("sidebar-collapsed");
        localStorage.setItem("sidebarCollapsed", isCollapsed);
      });

      // Check for saved state
      const savedState = localStorage.getItem("sidebarCollapsed");
      if (savedState === "true") {
        sidebar.classList.add("sidebar-collapsed");
        sidebar.classList.remove("sidebar-open");
      }
    }

    // Highlight active menu item
    const currentPath = window.location.pathname;
    const navItems = document.querySelectorAll(".nav-item");

    navItems.forEach((item) => {
      // Remove active class from all items first
      item.classList.remove("active");

      // Get the href attribute (handles both regular href and Thymeleaf href)
      const href =
        item.getAttribute("href") ||
        item.getAttribute("th:href")?.replace("@{", "").replace("}", "");

      // Check if current path matches this href
      if (href && currentPath.includes(href)) {
        item.classList.add("active");
      }
    });
  });
}

if (closeErrorModal) {
  document.addEventListener("DOMContentLoaded", function () {
    const confirmErrorModal = document.getElementById("confirmErrorModal");
    const errorContainer = document.getElementById("errorContainer");

    function hideModal() {
      if (errorContainer) {
        errorContainer.style.display = "none";
      }
    }

    if (closeErrorModal) {
      closeErrorModal.addEventListener("click", hideModal);
    }

    if (confirmErrorModal) {
      confirmErrorModal.addEventListener("click", hideModal);
    }
  });
}

// Modal Functionality
const addElectionBtn = document.getElementById("addElectionBtn");
const addElectionModal = document.getElementById("addElectionModal");
const cancelElectionBtn = document.getElementById("cancelElectionBtn");
const electionForm = document.getElementById("electionForm");

// Open the "Add Election" Modal
addElectionBtn.addEventListener("click", () => {
  addElectionModal.classList.remove("hidden");
});

// Close the "Add Election" Modal
cancelElectionBtn.addEventListener("click", () => {
  addElectionModal.classList.add("hidden");
});

// Handle Election Form Submission
electionForm.addEventListener("submit", (e) => {
  // e.preventDefault();

  // Simulate API call to save the election
  console.log("New election submitted");

  // Close the modal and reset the form
  addElectionModal.classList.add("hidden");
  electionForm.reset();
});

// Format Dates for Display
function formatDate(dateString) {
  const date = new Date(dateString);
  return date.toLocaleDateString("en-ZA", {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}
document
  .getElementById("electionCreateForm")
  .addEventListener("submit", function (e) {
    // Clear previous errors
    document.querySelectorAll(".is-invalid").forEach((el) => {
      el.classList.remove("is-invalid");
    });

    // Client-side validation
    let isValid = true;

    // Validate year
    const year = document.querySelector('input[name="year"]');
    if (!year.value || year.value < 2000 || year.value > 2100) {
      year.classList.add("is-invalid");
      isValid = false;
    }

    // Validate dates
    const startDate = new Date(
      document.querySelector('input[name="startDate"]').value
    );
    const endDate = new Date(
      document.querySelector('input[name="endDate"]').value
    );
    const regDeadline = new Date(
      document.querySelector('input[name="registrationDeadline"]').value
    );

    if (startDate > endDate) {
      // Add error highlighting
      isValid = false;
    }

    if (regDeadline >= startDate) {
      // Add error highlighting
      isValid = false;
    }
    console.log("Start Date:", startDate);
    console.log("End Date:", endDate);
    console.log("Registration Deadline:", regDeadline);
    console.log("is valid:", isValid);

    if (!isValid) {
      e.preventDefault();
      // You can add more specific error messages here
    }
  });
// success modal
const successModal = document.getElementById("successModal");
if (successModal) {
  // Show the modal if it exists in the DOM (th:if rendered it)
  successModal.classList.add("show");

  const closeSuccessModal = document.getElementById("closeSuccessModal");
  const confirmSuccessModal = document.getElementById("confirmSuccessModal");

  const hideSuccessModal = () => {
    successModal.classList.remove("show");
    // Optionally remove the modal from DOM after animation
    setTimeout(() => {
      successModal.remove();
    }, 300);
  };

  if (closeSuccessModal)
    closeSuccessModal.addEventListener("click", hideSuccessModal);
  if (confirmSuccessModal)
    confirmSuccessModal.addEventListener("click", hideSuccessModal);

  // Also close when clicking outside the modal
  successModal.addEventListener("click", function (e) {
    if (e.target === successModal) {
      hideSuccessModal();
    }
  });

  // Close with Escape key
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape" && successModal.classList.contains("show")) {
      hideSuccessModal();
    }
  });
}
