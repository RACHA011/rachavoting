document.getElementById("goback").addEventListener("click", function (event) {
  event.preventDefault();
  window.history.back();
});

document.getElementById("home").addEventListener("click", function (event) {
  event.preventDefault();
  window.location.href = "/";
});
