const close = document.getElementById("close");
const alert = document.getElementById("alert");
const close3 = document.getElementById("close-3");
const alert3 = document.getElementById("alert-3");
// Real-time search with debounce
const searchInput = document.getElementById("searchCandidate");
if (searchInput) {
  let timer;
  searchInput.addEventListener("input", function (e) {
    clearTimeout(timer);
    timer = setTimeout(() => {
      if (e.target.value.length > 2 || e.target.value.length === 0) {
        this.form.submit();
      }
    }, 500);
  });
}

// Initialize filter selects
const initSelects = () => {
  const electionSelect = document.getElementById("filterElection");
  const statusSelect = document.getElementById("filterStatus");

  if (electionSelect) {
    electionSelect.addEventListener("change", function () {
      // Add filter logic here
      console.log("Election filter changed:", this.value);
    });
  }

  if (statusSelect) {
    statusSelect.addEventListener("change", function () {
      // Add filter logic here
      console.log("Status filter changed:", this.value);
    });
  }
};

initSelects();

close.addEventListener("click", () => {
  alert.style.display = "none";
});
close3.addEventListener("click", () => {
  alert3.style.display = "none";
});
