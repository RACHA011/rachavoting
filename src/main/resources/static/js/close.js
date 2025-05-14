const close = document.getElementById("close");
const alert = document.getElementById("alert");
const close3 = document.getElementById("close-3");
const alert3 = document.getElementById("alert-3");
const close2 = document.getElementById("close-2");
const alert2 = document.getElementById("alert-2");

close.addEventListener("click", () => {
  alert.style.display = "none";
});
close3.addEventListener("click", () => {
  alert3.style.display = "none";
});
close2.addEventListener("click", () => {
  alert2.style.display = "none";
});
