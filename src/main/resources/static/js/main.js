// Tab functionality
const tabButtons = document.querySelectorAll(".tab-button");
const tabContents = document.querySelectorAll(".tab-content");

tabButtons.forEach((button) => {
  button.addEventListener("click", () => {
    // Remove active class from all buttons
    tabButtons.forEach((btn) =>
      btn.classList.remove(
        "border-b-2",
        "border-sa-blue",
        "text-sa-blue",
        "tab-active"
      )
    );
    // Add active class to clicked button
    button.classList.add(
      "border-b-2",
      "border-sa-blue",
      "text-sa-blue",
      "tab-active"
    );

    // Hide all tab contents
    tabContents.forEach((content) => content.classList.add("hidden"));
    // Show selected tab content
    const tabId = button.getAttribute("data-tab");
    document.getElementById(tabId).classList.remove("hidden");
  });
});

// // Scroll effect for navbar
window.addEventListener("scroll", function () {
  const navbar = document.getElementById("navbar");
  if (window.scrollY > 10) {
    // navbar.classList.remove("bg-white");
    navbar.classList.add(
      //   "backdrop-blur-sm",
      "border-b"
      //   "shadow-sm"
    );
  } else {
    // navbar.classList.add("bg-white");
    navbar.classList.remove(
      //   "backdrop-blur-sm",
      "border-b"
      //   "shadow-sm"
    );
  }
});

// Mobile menu toggle
const mobileMenuButton = document.getElementById("mobile-menu-button");
const closeMenuButton = document.getElementById("close-menu-button");
const mobileMenu = document.getElementById("mobile-menu");

mobileMenuButton.addEventListener("click", function () {
  mobileMenu.classList.remove("translate-x-full");
});

closeMenuButton.addEventListener("click", function () {
  mobileMenu.classList.add("translate-x-full");
});

// Close menu when clicking on a link
const mobileLinks = mobileMenu.querySelectorAll("a");
mobileLinks.forEach((link) => {
  link.addEventListener("click", function () {
    mobileMenu.classList.add("translate-x-full");
  });
});

// Set current year in footer
document.getElementById("current-year").textContent = new Date().getFullYear();
