if (document.getElementById("vote-content")) {
  // Party selection functionality
  const partyRadios = document.querySelectorAll('input[name="party"]');
  const continueBtn = document.getElementById("continue-btn");

  partyRadios.forEach((radio) => {
    radio.addEventListener("change", function () {
      continueBtn.disabled = !document.querySelector(
        'input[name="party"]:checked'
      );
    });
  });

  // Continue to confirmation step
  continueBtn.addEventListener("click", function () {
    const selectedPartyId = document.querySelector(
      'input[name="party"]:checked'
    ).value;
    const selectedParty = getPartyInfo(selectedPartyId);

    // Hide step 1, show step 2 with selected party
    document.getElementById("step1").classList.add("hidden");

    const step2Content = `
                <div class="mb-6">
                    <div class="progress-bar">
                        <div class="progress-fill w-100"></div>
                    </div>
                    <div class="flex justify-between text-xs text-gray-500 mt-2">
                        <span>Select Party</span>
                        <span>Confirm Vote</span>
                    </div>
                </div>
                
                <div class="bg-white rounded-lg shadow border">
                    <div class="p-6 border-b">
                        <h3 class="font-bold text-lg">Confirm Your Vote</h3>
                        <p class="text-gray-500">Please review and confirm your selection</p>
                    </div>
                    
                    <div class="p-6">
                        <div class="bg-amber-50 text-amber-800 border border-amber-200 rounded-md p-4 mb-6">
                            <div class="flex items-start gap-3">
                              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-4 w-4 mt-0.5 flex-shrink-0 lucide lucide-triangle-alert-icon lucide-triangle-alert"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3"/><path d="M12 9v4"/><path d="M12 17h.01"/></svg>
                                
                                <div>
                                    <h4 class="font-medium">Important</h4>
                                    <p class="text-sm mt-1">Once submitted, your vote cannot be changed. Please ensure your selection is correct.</p>
                                </div>
                            </div>
                        </div>
                        
                        <div class="border rounded-lg p-6 flex flex-col items-center">
                            <div class="p-4 rounded-full ${selectedParty.color.replace(
                              "bg-",
                              "bg-"
                            )} bg-opacity-10 mb-4">
                                <img src="${selectedParty.logo}" alt="${
      selectedParty.name
    }" class="w-16 h-16 object-contain">
                            </div>
                            <h3 class="text-xl font-bold">${
                              selectedParty.name
                            }</h3>
                            <p class="text-gray-500">${
                              selectedParty.shortName
                            }</p>
                        </div>
                        
                        <div id="confirmation-status" class="mt-6 flex flex-col items-center text-center ">
                            <!-- Will be filled when submitting -->
                        </div>
                    </div>
                    
                    <div class="p-4 border-t flex justify-between">
                        <button type="button" id="back-btn" class="border border-gray-300 rounded-md px-4 py-2 hover:bg-gray-50 flex items-center">
                          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-1 h-4 w-4 lucide lucide-arrow-left-icon lucide-arrow-left"><path d="m12 19-7-7 7-7"/><path d="M19 12H5"/></svg>
                            Back
                        </button>
                        <button id="submit-vote-btn" type="button" class="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-md">
                            Submit Vote
                        </button>
                    </div>
                </div>
            `;

    document.getElementById("step2").innerHTML = step2Content;
    document.getElementById("step2").classList.remove("hidden");

    // Set up back button
    document.getElementById("back-btn").addEventListener("click", function () {
      document.getElementById("step2").classList.add("hidden");
      document.getElementById("step1").classList.remove("hidden");
    });

    // Set up submit button
    document
      .getElementById("submit-vote-btn")
      .addEventListener("click", function () {
        const statusDiv = document.getElementById("confirmation-status");
        statusDiv.innerHTML = `
                    <div class="animate-pulse mb-4">
                        <div class="w-12 h-12 rounded-full border-4 border-blue-600 border-t-transparent animate-spin"></div>
                    </div>
                    <h4 class="text-lg font-medium">Recording your vote...</h4>
                    <p class="text-sm text-gray-500 mt-1">Please wait while we securely record your vote</p>
                `;
        statusDiv.classList.remove("hidden");

        // Disable buttons during submission
        document.getElementById("back-btn").disabled = true;
        document.getElementById("submit-vote-btn").disabled = true;

        // Simulate API call
        setTimeout(function () {
          statusDiv.innerHTML = `
                        <div class="animate-pulse mb-4">
                          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="w-12 h-12 text-green-600 lucide lucide-circle-check-big-icon lucide-circle-check-big"><path d="M21.801 10A10 10 0 1 1 17 3.335"/><path d="m9 11 3 3L22 4"/></svg>
                           
                        </div>
                        <h4 class="text-lg font-medium">Vote successful!</h4>
                        <p class="text-sm text-gray-500 mt-1">Your vote has been securely recorded</p>
                    `;

          // Show return to dashboard button
          document.getElementById("submit-vote-btn").classList.add("hidden");
          document.getElementById("back-btn").classList.add("hidden");

          const returnBtn = document.createElement("button");
          returnBtn.className =
            "mx-auto bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md";
          returnBtn.textContent = "Return to Dashboard";
          returnBtn.type = "button";
          returnBtn.addEventListener("click", function () {
            // Show vote complete screen
            showVoteCompleteScreen(selectedParty);
          });

          document
            .querySelector("#step2 div.bg-white > div.p-4.border-t")
            .appendChild(returnBtn);

          // Reinitialize icons
          lucide.createIcons();
        }, 2500);
      });
  });

  // Party data
  function getPartyInfo(id) {
    const parties = [
      {
        id: "anc",
        name: "African National Congress",
        shortName: "ANC",
        logo: "/image/ANC.png",
        color: "bg-green-600",
      },
      {
        id: "da",
        name: "Democratic Alliance",
        shortName: "DA",
        logo: "/image/DA.png",
        color: "bg-blue-600",
      },
      {
        id: "eff",
        name: "Economic Freedom Fighters",
        shortName: "EFF",
        logo: "/image/EFF.png",
        color: "bg-red-600",
      },
      // Add more parties as needed
    ];
    return parties.find((party) => party.id === id);
  }

  // Show vote complete screen
  function showVoteCompleteScreen(party) {
    document.getElementById("voting-process").innerHTML = `
                <div class="bg-white rounded-lg shadow border">
                    <div class="p-6 border-b text-center">
                        <div class="flex justify-center mb-4">
                            <div class="p-3 bg-green-100 rounded-full">
                              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-10 w-10 text-green-600 lucide lucide-circle-check-big-icon lucide-circle-check-big"><path d="M21.801 10A10 10 0 1 1 17 3.335"/><path d="m9 11 3 3L22 4"/></svg>
                               
                            </div>
                        </div>
                        <h3 class="text-2xl font-bold">Vote Successfully Cast</h3>
                        <p class="text-gray-500">Thank you for participating in the democratic process</p>
                    </div>
                    
                    <div class="p-6">
                        <div class="space-y-4">
                            <div class="border rounded-lg p-4">
                                <div class="text-sm text-gray-500 mb-1">Voting Event</div>
                                <div class="font-medium">National General Election 2024</div>
                            </div>
                            
                            <div class="border rounded-lg p-4">
                                <div class="text-sm text-gray-500 mb-1">Date and Time</div>
                                <div class="font-medium">${new Date().toLocaleDateString(
                                  "en-ZA",
                                  {
                                    year: "numeric",
                                    month: "long",
                                    day: "numeric",
                                    hour: "2-digit",
                                    minute: "2-digit",
                                  }
                                )}</div>
                            </div>
                            
                            <div class="border rounded-lg p-4">
                                <div class="text-sm text-gray-500 mb-1">Receipt Number</div>
                                <div class="font-medium">SA-NE2024-${Math.floor(
                                  Math.random() * 1000000
                                )
                                  .toString()
                                  .padStart(6, "0")}</div>
                            </div>
                            
                            <div class="bg-blue-50 border border-blue-200 rounded-md p-4">
                                <div class="flex items-start gap-3">
                                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-4 w-4 text-blue-600 mt-0.5 flex-shrink-0 lucide lucide-info-icon lucide-info"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/></svg>
                                   
                                    <div>
                                        <h4 class="font-medium text-blue-800">Ballot Secrecy Preserved</h4>
                                        <p class="text-sm text-blue-700 mt-1">Your vote is completely anonymous. This receipt only confirms that you participated, not who you voted for.</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="p-4 border-t flex flex-col gap-2">
                        <a th:href="@{/candidate/dashboard}" class="w-full bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-center">
                            Return to Dashboard
                        </a>
                        <button type="button" class="w-full border border-gray-300 rounded-md px-4 py-2 hover:bg-gray-50">
                            View Official Election Information
                        </button>
                    </div>
                </div>
            `;
  }
}
