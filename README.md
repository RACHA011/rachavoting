# Racha Voting App

Racha Voting App is a full-featured role-based voting system developed using **Java**, **Thymeleaf**, and **PostgreSQL**. The application allows for secure and flexible election management, supporting both individual political participation and future integration with organizations and businesses.

The app is being actively developed and is designed with scalability and future-proofing in mind, including planned support for **blockchain-based voting** to ensure transparency and immutability of results.

---

## Tech Stack

* **Backend:** Java (Spring Boot)
* **Frontend:** Thymeleaf
* **Database:** PostgreSQL
* **Authentication:** Role-Based (with two types of auth supported)
* **Future Support:** Blockchain-based voting system

---

## User Roles & Authorities

### 1. **Admin**

* Has full access to the system.
* Can:

  * Create elections.
  * Assign candidate roles (e.g., promote a user to **President**).
  * View detailed election data and statistics.
  * Update information about any user or election.
* Has authority: `ADMIN`

### 2. **Candidate (User / Party Member)**

* Represents party members (and in the future, organizations and businesses).
* Can:

  * Participate in elections.
  * Presidents can create a political party.
  * Vice Presidents can update information about the party.
* Will support additional use cases in the future (like business voting).
* Has authority: `CANDIDATE` (subroles defined by party position enums)

---

## Party Management

* Political party members are organized by specific roles.
* Each role is part of a broader category (e.g., Party Leadership).
* Example party positions (defined using enums):

  * **President**
  * **Deputy President**
  * **Chairperson**
* These are categorized using enums like `PartyPosition` and `PartyPositionCategory`.

### Future Enhancements

* Candidates will be linked directly to the party they belong to.
* Party roles will be generalized to work for various organizational structures.
* More granular authority control will be implemented per party position.

---

## Voting & Elections

* Admins can create and manage elections with detailed information.
* Users (candidates) can participate in these elections.
* Voting functionality is currently being implemented using PostgreSQL.
* Blockchain voting is planned for future versions to improve transparency and integrity.

---

## Authentication System

* Two types of authentication are supported (details based on your future setup).
* Each user is authenticated and authorized based on their role (`ADMIN`, `CANDIDATE`, etc.).
* Flexible structure allows easy integration of more auth types and role expansions in the future.

---

## Roadmap

### In Progress:

* Implementation of voting functionality using PostgreSQL.
* Linking party members to their respective parties and roles.

### Planned:

* Blockchain integration for secure, verifiable elections.
* Expanding candidate system to support:

  * Business voting.
  * Organization voting.
* Enhancing party management with role delegation and visibility.
* Improved admin analytics dashboard.

---

## 📌 Status

* ✅ Backend structure set up
* ✅ Role-based authentication working
* ✅ Admin dashboard implemented
* ✅ Party role enums (e.g., `PartyPosition`) defined
* 🔧 Voting logic in progress (PostgreSQL-based)
* 🧠 Blockchain voting architecture under consideration

---
the reason for this app is to test my self more and to learn more about security and data protection