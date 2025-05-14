
// Tab switching functionality
document.querySelectorAll(".tab-button").forEach((button) => {
  button.addEventListener("click", function () {
    // Remove active class from all buttons
    document.querySelectorAll(".tab-button").forEach((btn) => {
      btn.classList.remove(
        "active",
        "text-blue-600",
        "border-b-2",
        "border-blue-600"
      );
      btn.classList.add("text-gray-500", "hover:text-gray-700");
    });

    // Add active class to clicked button
    this.classList.add(
      "active",
      "text-blue-600",
      "border-b-2",
      "border-blue-600"
    );
    this.classList.remove("text-gray-500", "hover:text-gray-700");

    // Show/hide content
    if (this.textContent.trim() === "Upcoming") {
      document.getElementById("upcoming-content").classList.remove("hidden");
      document.getElementById("past-content").classList.add("hidden");
    } else {
      document.getElementById("past-content").classList.remove("hidden");
      document.getElementById("upcoming-content").classList.add("hidden");
    }
  });
});
