// manage party
if (document.getElementById("manage-party-container")) {
  function previewImage() {
    const fileInput = document.getElementById("imageUpload");
    const file = fileInput.files[0];
    const preview = document.getElementById("imagePreview");
    const placeholder = document.getElementById("svgPlaceholder");
    const form = document.getElementById("save-icon-form");
    const idInput = document.getElementById("id");
    const uniqueId = document.getElementById("uniqueId");

    if (file) {
      const reader = new FileReader();
      reader.onload = function (e) {
        // Update preview image
        if (!preview) {
          const newPreview = document.createElement("img");
          newPreview.id = "imagePreview";
          // newPreview.className = "w-full h-full object-cover";
          newPreview.alt = "Party Logo";
          newPreview.src = e.target.result;
          placeholder.parentNode.insertBefore(newPreview, placeholder);
          placeholder.style.display = "none";
        } else {
          preview.src = e.target.result;
          preview.style.display = "block";
          if (placeholder) placeholder.style.display = "none";
        }

        // Submit via AJAX
        submitImage(file, idInput.value);
      };
      reader.readAsDataURL(file);
    }
  }

  function submitImage(file, id) {
    const formData = new FormData();
    formData.append("id", id);
    formData.append("type", "party");
    formData.append("icon", file);

    // Add CSRF token if using Spring Security
    const csrfToken = document.querySelector("meta[name='_csrf']")?.content;
    const csrfHeader = document.querySelector(
      "meta[name='_csrf_header']"
    )?.content;

    const headers = {};
    if (csrfToken && csrfHeader) {
      headers[csrfHeader] = csrfToken;
    }

    fetch("/save-icon", {
      method: "POST",
      body: formData,
      headers: headers,
    })
      .then((response) => {
        if (!response.ok) throw new Error("Upload failed");
        return response.text();
      })
      .then((data) => {
        console.log("Success:", data);
        uniqueId.value = data;
        // Optional: Show success message
        showAlert("Logo updated successfully", "success");
      })
      .catch((error) => {
        console.error("Error:", error);
        showAlert("Error updating logo", "error");
      });
  }

  // Helper function for showing alerts
  function showAlert(message, type) {
    const alertDiv = document.createElement("div");
    alertDiv.className = `fixed top-4 right-4 p-4 rounded-md ${
      type === "success" ? "bg-green-500" : "bg-red-500"
    } text-white`;
    alertDiv.textContent = message;
    document.body.appendChild(alertDiv);

    setTimeout(() => {
      alertDiv.remove();
    }, 3000);
  }

  document
    .getElementById("status_checkbox")
    .addEventListener("click", function () {
      document.getElementById("status").value = this.checked;
    });

  document
    .getElementById("imageUpload")
    .addEventListener("change", function () {
      previewImage();
    });

  document
    .getElementById("uploadbutton")
    .addEventListener("click", function () {
      document.getElementById("imageUpload").click();
    });
}
// manage account
if(document.getElementById("manage-account-container")) {

}
