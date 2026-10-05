/**
 * Blood Bank Management System - Main Application Controller
 * Handles UI interactions, routing, charts, modals, toasts, and business logic
 */

// Blood Transfusion Compatibility Rules (Red Blood Cells)
const BLOOD_COMPATIBILITY = {
  "A+": {
    canGiveTo: ["A+", "AB+"],
    canReceiveFrom: ["A+", "A-", "O+", "O-"]
  },
  "A-": {
    canGiveTo: ["A+", "A-", "AB+", "AB-"],
    canReceiveFrom: ["A-", "O-"]
  },
  "B+": {
    canGiveTo: ["B+", "AB+"],
    canReceiveFrom: ["B+", "B-", "O+", "O-"]
  },
  "B-": {
    canGiveTo: ["B+", "B-", "AB+", "AB-"],
    canReceiveFrom: ["B-", "O-"]
  },
  "AB+": {
    canGiveTo: ["AB+"],
    canReceiveFrom: ["A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"] // Universal recipient
  },
  "AB-": {
    canGiveTo: ["AB+", "AB-"],
    canReceiveFrom: ["AB-", "A-", "B-", "O-"]
  },
  "O+": {
    canGiveTo: ["O+", "A+", "B+", "AB+"],
    canReceiveFrom: ["O+", "O-"]
  },
  "O-": {
    canGiveTo: ["A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"], // Universal donor
    canReceiveFrom: ["O-"]
  }
};

class BloodBankApp {
  constructor() {
    this.currentView = "dashboard";
    this.currentUser = null;
    this.stockChart = null;
    this.distributionChart = null;
    this.selectedDonorForEdit = null;
    this.selectedSearchGroup = "O+";
    this.currentReportTab = "donors";
    
    this.init();
  }

  init() {
    this.checkAuth();
    this.setupEventListeners();
    this.handleRoute();
  }

  // ==========================================
  // Authentication & Session
  // ==========================================
  checkAuth() {
    const savedUser = sessionStorage.getItem("bbms_user");
    if (savedUser) {
      try {
        this.currentUser = JSON.parse(savedUser);
        this.updateUserUI();
        document.getElementById("loginOverlay").style.display = "none";
      } catch (e) {
        this.showLogin();
      }
    } else {
      this.showLogin();
    }
  }

  showLogin() {
    this.currentUser = null;
    sessionStorage.removeItem("bbms_user");
    document.getElementById("loginOverlay").style.display = "flex";
  }

  updateUserUI() {
    if (this.currentUser) {
      document.getElementById("navUserName").textContent = this.currentUser.name || this.currentUser.username;
      document.getElementById("navUserRole").textContent = "Administrator";
    }
  }

  handleLogin(e) {
    e.preventDefault();
    const userField = document.getElementById("loginUsername");
    const passField = document.getElementById("loginPassword");
    const username = userField.value.trim();
    const password = passField.value;

    const user = window.db.authenticate(username, password);
    if (user) {
      this.currentUser = user;
      sessionStorage.setItem("bbms_user", JSON.stringify(user));
      this.updateUserUI();
      document.getElementById("loginOverlay").style.display = "none";
      userField.value = "";
      passField.value = "";
      this.showToast("Welcome Back!", `Logged in as ${user.username}`, "success");
      this.renderCurrentView();
    } else {
      this.showToast("Authentication Failed", "Invalid username or password. Try demo login: admin / admin123", "error");
    }
  }

  handleLogout() {
    this.showConfirm(
      "Confirm Logout",
      "Are you sure you want to sign out from the Blood Bank Management System?",
      () => {
        sessionStorage.removeItem("bbms_user");
        this.currentUser = null;
        this.showLogin();
        this.showToast("Signed Out", "You have been logged out safely.", "info");
      }
    );
  }

  // ==========================================
  // Navigation & Routing
  // ==========================================
  setupEventListeners() {
    // Hash routing
    window.addEventListener("hashchange", () => this.handleRoute());

    // Login form
    const loginForm = document.getElementById("loginForm");
    if (loginForm) {
      loginForm.addEventListener("submit", (e) => this.handleLogin(e));
    }

    // Auto-fill demo credentials button
    const autofillBtn = document.getElementById("autofillDemoBtn");
    if (autofillBtn) {
      autofillBtn.addEventListener("click", () => {
        document.getElementById("loginUsername").value = "admin";
        document.getElementById("loginPassword").value = "admin123";
      });
    }

    // Logout
    const logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) {
      logoutBtn.addEventListener("click", () => this.handleLogout());
    }

    // Mobile sidebar toggle
    const mobileMenuBtn = document.getElementById("mobileMenuBtn");
    const sidebar = document.getElementById("sidebar");
    if (mobileMenuBtn && sidebar) {
      mobileMenuBtn.addEventListener("click", () => {
        sidebar.classList.toggle("mobile-open");
      });
    }

    // Close mobile sidebar when clicking a nav link
    document.querySelectorAll(".nav-item").forEach(link => {
      link.addEventListener("click", () => {
        if (sidebar && sidebar.classList.contains("mobile-open")) {
          sidebar.classList.remove("mobile-open");
        }
      });
    });

    // Reset Data button
    const resetDataBtn = document.getElementById("resetDataBtn");
    if (resetDataBtn) {
      resetDataBtn.addEventListener("click", () => {
        this.showConfirm(
          "Reset Database to Sample Data?",
          "This will reset all donors, blood stock, donations, and requests to default seed data from schema.sql.",
          () => {
            window.db.resetToDefaults();
            this.showToast("Database Reset", "System successfully restored to default seed records.", "success");
            this.renderCurrentView();
          }
        );
      });
    }

    // Modal background clicks
    document.querySelectorAll(".modal-backdrop").forEach(backdrop => {
      backdrop.addEventListener("click", (e) => {
        if (e.target === backdrop) {
          this.closeAllModals();
        }
      });
    });

    // Modal close buttons
    document.querySelectorAll(".modal-close-btn, .btn-modal-cancel").forEach(btn => {
      btn.addEventListener("click", () => this.closeAllModals());
    });

    // Setup forms
    this.setupDonorForms();
    this.setupStockForms();
    this.setupDonationForms();
    this.setupRequestForms();
    this.setupSearchBlood();
    this.setupReports();
  }

  handleRoute() {
    const hash = window.location.hash.replace("#", "") || "dashboard";
    const validViews = ["dashboard", "donors", "stock", "donations", "requests", "search", "reports"];
    const target = validViews.includes(hash) ? hash : "dashboard";
    this.navigateTo(target, false);
  }

  navigateTo(viewId, updateHash = true) {
    this.currentView = viewId;
    if (updateHash) {
      window.location.hash = viewId;
    }

    // Update active nav link
    document.querySelectorAll(".nav-item").forEach(el => {
      if (el.getAttribute("data-view") === viewId) {
        el.classList.add("active");
      } else {
        el.classList.remove("active");
      }
    });

    // Update view panels
    document.querySelectorAll(".view-section").forEach(sec => {
      if (sec.id === `view-${viewId}`) {
        sec.classList.add("active");
      } else {
        sec.classList.remove("active");
      }
    });

    // Update page title
    const titles = {
      dashboard: "Admin Dashboard",
      donors: "Donor Management",
      stock: "Blood Stock Overview",
      donations: "Donation Management",
      requests: "Blood Requests & Approvals",
      search: "Search Blood Availability",
      reports: "Reports & Analytics"
    };
    document.getElementById("pageHeaderTitle").textContent = titles[viewId] || "Dashboard";

    // Render data for current view
    this.renderCurrentView();
  }

  renderCurrentView() {
    switch (this.currentView) {
      case "dashboard":
        this.renderDashboard();
        break;
      case "donors":
        this.renderDonors();
        break;
      case "stock":
        this.renderStock();
        break;
      case "donations":
        this.renderDonations();
        break;
      case "requests":
        this.renderRequests();
        break;
      case "search":
        this.renderSearchBlood();
        break;
      case "reports":
        this.renderReports();
        break;
    }
  }

  // ==========================================
  // Dashboard Controller
  // ==========================================
  renderDashboard() {
    const stats = window.db.getDashboardStats();

    // Stats counts
    document.getElementById("statDonorsCount").textContent = stats.totalDonors;
    document.getElementById("statUnitsCount").textContent = stats.totalBloodUnits;
    document.getElementById("statRequestsCount").textContent = stats.totalRequests;
    document.getElementById("statDonationsCount").textContent = stats.totalDonations;

    // Alert Banner if low or out of stock
    const alertBanner = document.getElementById("dashboardAlertBanner");
    const alertText = document.getElementById("dashboardAlertText");
    const alerts = [];
    if (stats.outOfStockCount > 0) {
      alerts.push(`<strong>${stats.outOfStockCount}</strong> blood group(s) are completely out of stock!`);
    }
    if (stats.lowStockCount > 0) {
      alerts.push(`<strong>${stats.lowStockCount}</strong> blood group(s) are running low (< 5 units).`);
    }
    if (stats.pendingRequests > 0) {
      alerts.push(`<strong>${stats.pendingRequests}</strong> blood request(s) are awaiting admin review.`);
    }

    if (alerts.length > 0) {
      alertText.innerHTML = alerts.join(" &bull; ");
      alertBanner.style.display = "flex";
    } else {
      alertBanner.style.display = "none";
    }

    // Blood stock cards
    this.renderStockCards("dashboardStockGrid");

    // Charts
    this.renderDashboardCharts();

    // Recent Donations
    this.renderRecentDonations();

    // Recent Requests
    this.renderRecentRequests();
  }

  renderStockCards(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    const stockList = window.db.getAllStock();

    container.innerHTML = stockList.map(item => {
      let badgeClass = "available";
      if (item.status === "Low Stock") badgeClass = "low-stock";
      else if (item.status === "Not Available") badgeClass = "not-available";

      return `
        <div class="stock-card" onclick="window.app.quickSearchStock('${item.bloodGroup}')">
          <div class="stock-group-badge">${item.bloodGroup}</div>
          <div class="stock-units">${item.unitsAvailable}</div>
          <div class="stock-unit-label">Units Available</div>
          <span class="status-badge ${badgeClass}">${item.status}</span>
        </div>
      `;
    }).join("");
  }

  renderDashboardCharts() {
    const stockList = window.db.getAllStock();
    const labels = stockList.map(s => s.bloodGroup);
    const data = stockList.map(s => s.unitsAvailable);

    // Color palette based on levels
    const bgColors = stockList.map(s => {
      if (s.unitsAvailable === 0) return "#dc2626";
      if (s.unitsAvailable < 5) return "#d97706";
      return "#b22234";
    });

    // Check if Chart.js is available
    if (typeof Chart !== "undefined") {
      const barCtx = document.getElementById("stockBarChart");
      if (barCtx) {
        if (this.stockChart) this.stockChart.destroy();
        this.stockChart = new Chart(barCtx, {
          type: "bar",
          data: {
            labels: labels,
            datasets: [{
              label: "Available Units",
              data: data,
              backgroundColor: bgColors,
              borderRadius: 6
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: { display: false }
            },
            scales: {
              y: {
                beginAtZero: true,
                ticks: { stepSize: 2 }
              }
            }
          }
        });
      }

      const doughnutCtx = document.getElementById("stockDoughnutChart");
      if (doughnutCtx) {
        if (this.distributionChart) this.distributionChart.destroy();
        const palette = ["#b22234", "#e11d48", "#f43f5e", "#fb7185", "#38bdf8", "#0284c7", "#059669", "#10b981"];
        this.distributionChart = new Chart(doughnutCtx, {
          type: "doughnut",
          data: {
            labels: labels,
            datasets: [{
              data: data,
              backgroundColor: palette
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: { position: "right" }
            }
          }
        });
      }
    }
  }

  renderRecentDonations() {
    const list = window.db.getAllDonations().slice(0, 5);
    const tbody = document.getElementById("dashboardRecentDonations");
    if (!tbody) return;

    if (list.length === 0) {
      tbody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--text-muted);">No donations recorded yet.</td></tr>`;
      return;
    }

    tbody.innerHTML = list.map(d => `
      <tr>
        <td><strong>${this.escapeHtml(d.donorName)}</strong></td>
        <td><span class="blood-pill">${d.bloodGroup}</span></td>
        <td><strong>${d.unitsDonated}</strong> unit(s)</td>
        <td>${d.donationDate}</td>
      </tr>
    `).join("");
  }

  renderRecentRequests() {
    const list = window.db.getAllRequests().slice(0, 5);
    const tbody = document.getElementById("dashboardRecentRequests");
    if (!tbody) return;

    if (list.length === 0) {
      tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">No requests recorded yet.</td></tr>`;
      return;
    }

    tbody.innerHTML = list.map(r => {
      let badgeClass = "status-" + r.status.toLowerCase();
      return `
        <tr>
          <td><strong>${this.escapeHtml(r.patientName)}</strong></td>
          <td>${this.escapeHtml(r.hospitalName)}</td>
          <td><span class="blood-pill">${r.bloodGroup}</span> (${r.unitsRequired} u)</td>
          <td><span class="status-badge ${badgeClass}">${r.status}</span></td>
          <td>${r.requestDate}</td>
        </tr>
      `;
    }).join("");
  }

  // ==========================================
  // Donor Management Controller
  // ==========================================
  setupDonorForms() {
    // Search & Filter
    const searchInput = document.getElementById("donorSearchInput");
    const groupFilter = document.getElementById("donorGroupFilter");

    if (searchInput) {
      searchInput.addEventListener("input", () => this.renderDonors());
    }
    if (groupFilter) {
      groupFilter.addEventListener("change", () => this.renderDonors());
    }

    // Open Add Donor Modal
    const openAddBtn = document.getElementById("openAddDonorModalBtn");
    if (openAddBtn) {
      openAddBtn.addEventListener("click", () => this.openDonorModal());
    }

    // Donor Form Submit
    const form = document.getElementById("donorForm");
    if (form) {
      form.addEventListener("submit", (e) => this.handleDonorFormSubmit(e));
    }
  }

  renderDonors() {
    const tbody = document.getElementById("donorsTableBody");
    if (!tbody) return;

    const search = (document.getElementById("donorSearchInput")?.value || "").toLowerCase().trim();
    const groupFilter = document.getElementById("donorGroupFilter")?.value || "All";

    let donors = window.db.getAllDonors();

    // Filters
    if (groupFilter !== "All") {
      donors = donors.filter(d => d.bloodGroup === groupFilter);
    }
    if (search) {
      donors = donors.filter(d =>
        d.fullName.toLowerCase().includes(search) ||
        d.phoneNumber.includes(search) ||
        (d.email && d.email.toLowerCase().includes(search)) ||
        (d.address && d.address.toLowerCase().includes(search))
      );
    }

    if (donors.length === 0) {
      tbody.innerHTML = `<tr><td colspan="9" style="text-align: center; padding: 2rem; color: var(--text-muted);">No donors found matching criteria.</td></tr>`;
      return;
    }

    tbody.innerHTML = donors.map(d => `
      <tr>
        <td>#${d.donorId}</td>
        <td><strong>${this.escapeHtml(d.fullName)}</strong></td>
        <td>${d.age} / ${d.gender}</td>
        <td><span class="blood-pill">${d.bloodGroup}</span></td>
        <td><a href="tel:${d.phoneNumber}" style="color: var(--primary); text-decoration: none;">${d.phoneNumber}</a></td>
        <td>${d.email ? this.escapeHtml(d.email) : "<span style='color: var(--text-light);'>-</span>"}</td>
        <td>${d.address ? this.escapeHtml(d.address) : "<span style='color: var(--text-light);'>-</span>"}</td>
        <td>${d.lastDonationDate || "<span style='color: var(--text-muted);'>Never</span>"}</td>
        <td>
          <div style="display: flex; gap: 0.35rem;">
            <button class="btn btn-sm btn-secondary" onclick="window.app.openDonorModal(${d.donorId})" title="Edit Donor">
              <i class="fa-solid fa-pen-to-square"></i>
            </button>
            <button class="btn btn-sm btn-success" onclick="window.app.quickDonateForDonor(${d.donorId})" title="Record Donation">
              <i class="fa-solid fa-hand-holding-droplet"></i>
            </button>
            <button class="btn btn-sm btn-danger" onclick="window.app.confirmDeleteDonor(${d.donorId})" title="Delete Donor">
              <i class="fa-solid fa-trash"></i>
            </button>
          </div>
        </td>
      </tr>
    `).join("");
  }

  openDonorModal(donorId = null) {
    const modal = document.getElementById("donorModal");
    const title = document.getElementById("donorModalTitle");
    const form = document.getElementById("donorForm");
    form.reset();

    if (donorId) {
      const donor = window.db.getDonorById(donorId);
      if (!donor) return;
      this.selectedDonorForEdit = donor;
      title.innerHTML = `<i class="fa-solid fa-user-pen"></i> Edit Donor #${donor.donorId}`;
      document.getElementById("donorIdField").value = donor.donorId;
      document.getElementById("donorFullName").value = donor.fullName;
      document.getElementById("donorAge").value = donor.age;
      document.getElementById("donorGender").value = donor.gender;
      document.getElementById("donorBloodGroup").value = donor.bloodGroup;
      document.getElementById("donorPhone").value = donor.phoneNumber;
      document.getElementById("donorEmail").value = donor.email || "";
      document.getElementById("donorAddress").value = donor.address || "";
      document.getElementById("donorLastDonation").value = donor.lastDonationDate || "";
    } else {
      this.selectedDonorForEdit = null;
      title.innerHTML = `<i class="fa-solid fa-user-plus"></i> Add New Donor`;
      document.getElementById("donorIdField").value = "";
    }

    modal.classList.add("open");
  }

  handleDonorFormSubmit(e) {
    e.preventDefault();
    try {
      const id = document.getElementById("donorIdField").value;
      const fullName = document.getElementById("donorFullName").value;
      const age = document.getElementById("donorAge").value;
      const gender = document.getElementById("donorGender").value;
      const bloodGroup = document.getElementById("donorBloodGroup").value;
      const phone = document.getElementById("donorPhone").value;
      const email = document.getElementById("donorEmail").value;
      const address = document.getElementById("donorAddress").value;
      const lastDonation = document.getElementById("donorLastDonation").value;

      // Validation
      window.ValidationUtil.requireNonEmpty(fullName, "Full Name");
      const parsedAge = window.ValidationUtil.validateAge(age);
      window.ValidationUtil.requireNonEmpty(gender, "Gender");
      window.ValidationUtil.validateBloodGroup(bloodGroup);
      const validatedPhone = window.ValidationUtil.validatePhoneNumber(phone);
      const validatedEmail = window.ValidationUtil.validateEmail(email);

      if (lastDonation) {
        window.ValidationUtil.validateDateNotFuture(lastDonation);
      }

      const donorPayload = {
        fullName: fullName.trim(),
        age: parsedAge,
        gender: gender.trim(),
        bloodGroup: bloodGroup.trim(),
        phoneNumber: validatedPhone,
        email: validatedEmail || null,
        address: address.trim() || null,
        lastDonationDate: lastDonation ? lastDonation : null
      };

      if (id) {
        donorPayload.donorId = Number(id);
        window.db.updateDonor(donorPayload);
        this.showToast("Donor Updated", `Donor ${donorPayload.fullName} updated successfully.`, "success");
      } else {
        const added = window.db.addDonor(donorPayload);
        this.showToast("Donor Added", `Donor ${added.fullName} (ID #${added.donorId}) registered successfully.`, "success");
      }

      this.closeAllModals();
      this.renderDonors();
      this.renderDashboard();
    } catch (err) {
      this.showToast("Validation Error", err.message, "error");
    }
  }

  confirmDeleteDonor(donorId) {
    const donor = window.db.getDonorById(donorId);
    if (!donor) return;

    this.showConfirm(
      "Confirm Donor Deletion",
      `Are you sure you want to delete donor "${donor.fullName}" (Blood Group: ${donor.bloodGroup})? This action cannot be undone.`,
      () => {
        window.db.deleteDonor(donorId);
        this.showToast("Donor Deleted", `Donor ${donor.fullName} removed.`, "info");
        this.renderDonors();
        this.renderDashboard();
      }
    );
  }

  quickDonateForDonor(donorId) {
    this.navigateTo("donations");
    this.openDonationModal(donorId);
  }

  // ==========================================
  // Blood Stock Management Controller
  // ==========================================
  setupStockForms() {
    const openAdjustBtn = document.getElementById("openAdjustStockModalBtn");
    if (openAdjustBtn) {
      openAdjustBtn.addEventListener("click", () => this.openStockModal());
    }

    const form = document.getElementById("stockAdjustForm");
    if (form) {
      form.addEventListener("submit", (e) => this.handleStockAdjustSubmit(e));
    }
  }

  renderStock() {
    this.renderStockCards("stockOverviewGrid");

    const tbody = document.getElementById("stockTableBody");
    if (!tbody) return;

    const stockList = window.db.getAllStock();
    tbody.innerHTML = stockList.map(s => {
      let badgeClass = "available";
      if (s.status === "Low Stock") badgeClass = "low-stock";
      else if (s.status === "Not Available") badgeClass = "not-available";

      // Capacity bar calculation (taking 20 units as normal ceiling benchmark)
      const pct = Math.min(100, Math.round((s.unitsAvailable / 20) * 100));

      return `
        <tr>
          <td><span class="blood-pill">${s.bloodGroup}</span></td>
          <td><strong>${s.unitsAvailable}</strong> unit(s)</td>
          <td style="width: 35%;">
            <div style="background: #e2e8f0; height: 10px; border-radius: 999px; overflow: hidden;">
              <div style="background: ${s.unitsAvailable === 0 ? '#dc2626' : s.unitsAvailable < 5 ? '#d97706' : '#16a34a'}; width: ${pct}%; height: 100%;"></div>
            </div>
          </td>
          <td><span class="status-badge ${badgeClass}">${s.status}</span></td>
          <td>
            <button class="btn btn-sm btn-primary" onclick="window.app.openStockModal('${s.bloodGroup}')">
              <i class="fa-solid fa-sliders"></i> Adjust
            </button>
          </td>
        </tr>
      `;
    }).join("");
  }

  openStockModal(defaultGroup = "A+") {
    const modal = document.getElementById("stockModal");
    const groupSelect = document.getElementById("stockAdjustGroup");
    groupSelect.value = defaultGroup;
    document.getElementById("stockAdjustUnits").value = "1";
    document.getElementById("stockAdjustAction").value = "add";
    modal.classList.add("open");
  }

  handleStockAdjustSubmit(e) {
    e.preventDefault();
    try {
      const group = document.getElementById("stockAdjustGroup").value;
      const action = document.getElementById("stockAdjustAction").value;
      const units = document.getElementById("stockAdjustUnits").value;

      window.ValidationUtil.validateBloodGroup(group);
      const parsedUnits = window.ValidationUtil.validatePositiveUnits(units);

      if (action === "add") {
        const newTotal = window.db.increaseStock(group, parsedUnits);
        this.showToast("Stock Replenished", `Added ${parsedUnits} unit(s) of ${group}. New total: ${newTotal} units.`, "success");
      } else {
        const newTotal = window.db.decreaseStock(group, parsedUnits);
        this.showToast("Stock Deducted", `Deducted ${parsedUnits} unit(s) of ${group}. New total: ${newTotal} units.`, "info");
      }

      this.closeAllModals();
      this.renderStock();
      this.renderDashboard();
    } catch (err) {
      this.showToast("Stock Error", err.message, "error");
    }
  }

  // ==========================================
  // Donation Management Controller
  // ==========================================
  setupDonationForms() {
    const openAddBtn = document.getElementById("openRecordDonationModalBtn");
    if (openAddBtn) {
      openAddBtn.addEventListener("click", () => this.openDonationModal());
    }

    const donorSelect = document.getElementById("donationDonorSelect");
    if (donorSelect) {
      donorSelect.addEventListener("change", () => {
        const donorId = donorSelect.value;
        const donor = window.db.getDonorById(donorId);
        document.getElementById("donationBloodGroup").value = donor ? donor.bloodGroup : "";
      });
    }

    const form = document.getElementById("donationForm");
    if (form) {
      form.addEventListener("submit", (e) => this.handleDonationSubmit(e));
    }

    const searchInput = document.getElementById("donationSearchInput");
    if (searchInput) {
      searchInput.addEventListener("input", () => this.renderDonations());
    }
  }

  renderDonations() {
    const tbody = document.getElementById("donationsTableBody");
    if (!tbody) return;

    const search = (document.getElementById("donationSearchInput")?.value || "").toLowerCase().trim();
    let donations = window.db.getAllDonations();

    if (search) {
      donations = donations.filter(d =>
        d.donorName.toLowerCase().includes(search) ||
        d.bloodGroup.toLowerCase().includes(search) ||
        d.donationDate.includes(search)
      );
    }

    if (donations.length === 0) {
      tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 2rem; color: var(--text-muted);">No donations recorded yet.</td></tr>`;
      return;
    }

    tbody.innerHTML = donations.map(d => `
      <tr>
        <td>#${d.donationId}</td>
        <td><strong>${this.escapeHtml(d.donorName)}</strong></td>
        <td><span class="blood-pill">${d.bloodGroup}</span></td>
        <td><strong>${d.unitsDonated}</strong> unit(s)</td>
        <td>${d.donationDate}</td>
      </tr>
    `).join("");
  }

  openDonationModal(presetDonorId = null) {
    const modal = document.getElementById("donationModal");
    const donorSelect = document.getElementById("donationDonorSelect");
    const donors = window.db.getAllDonors();

    donorSelect.innerHTML = `<option value="">-- Choose Donor --</option>` +
      donors.map(d => `<option value="${d.donorId}" ${presetDonorId && Number(presetDonorId) === d.donorId ? "selected" : ""}>${this.escapeHtml(d.fullName)} (${d.bloodGroup}) - Phone: ${d.phoneNumber}</option>`).join("");

    const dateField = document.getElementById("donationDate");
    const today = new Date().toISOString().split("T")[0];
    dateField.value = today;
    dateField.max = today; // Enforce no future dates

    document.getElementById("donationUnits").value = "1";

    if (presetDonorId) {
      const donor = window.db.getDonorById(presetDonorId);
      document.getElementById("donationBloodGroup").value = donor ? donor.bloodGroup : "";
    } else {
      document.getElementById("donationBloodGroup").value = "";
    }

    modal.classList.add("open");
  }

  handleDonationSubmit(e) {
    e.preventDefault();
    try {
      const donorId = document.getElementById("donationDonorSelect").value;
      const bloodGroup = document.getElementById("donationBloodGroup").value;
      const date = document.getElementById("donationDate").value;
      const units = document.getElementById("donationUnits").value;

      if (!donorId) throw new Error("A registered donor must be selected.");
      window.ValidationUtil.validateBloodGroup(bloodGroup);
      window.ValidationUtil.validateDateNotFuture(date);
      const parsedUnits = window.ValidationUtil.validatePositiveUnits(units);

      const donation = window.db.recordDonation({
        donorId,
        bloodGroup,
        donationDate: date,
        unitsDonated: parsedUnits
      });

      this.showToast(
        "Donation Recorded",
        `Recorded ${parsedUnits} unit(s) from ${donation.donorName}. Blood stock for ${bloodGroup} automatically increased!`,
        "success"
      );

      this.closeAllModals();
      this.renderDonations();
      this.renderStock();
      this.renderDashboard();
    } catch (err) {
      this.showToast("Donation Error", err.message, "error");
    }
  }

  // ==========================================
  // Blood Requests Controller
  // ==========================================
  setupRequestForms() {
    const openAddBtn = document.getElementById("openSubmitRequestModalBtn");
    if (openAddBtn) {
      openAddBtn.addEventListener("click", () => this.openRequestModal());
    }

    const form = document.getElementById("requestForm");
    if (form) {
      form.addEventListener("submit", (e) => this.handleRequestSubmit(e));
    }

    const filter = document.getElementById("requestStatusFilter");
    if (filter) {
      filter.addEventListener("change", () => this.renderRequests());
    }
  }

  renderRequests() {
    const tbody = document.getElementById("requestsTableBody");
    if (!tbody) return;

    const statusFilter = document.getElementById("requestStatusFilter")?.value || "All";
    let requests = window.db.getAllRequests();

    if (statusFilter !== "All") {
      requests = requests.filter(r => r.status === statusFilter);
    }

    if (requests.length === 0) {
      tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; padding: 2rem; color: var(--text-muted);">No blood requests found.</td></tr>`;
      return;
    }

    tbody.innerHTML = requests.map(r => {
      let badgeClass = "status-" + r.status.toLowerCase();
      const isPending = r.status === "Pending";
      const isApproved = r.status === "Approved";

      return `
        <tr>
          <td>#${r.requestId}</td>
          <td><strong>${this.escapeHtml(r.patientName)}</strong></td>
          <td>${this.escapeHtml(r.hospitalName)}</td>
          <td><a href="tel:${r.contactNumber}" style="color: var(--primary); text-decoration: none;">${r.contactNumber}</a></td>
          <td><span class="blood-pill">${r.bloodGroup}</span></td>
          <td><strong>${r.unitsRequired}</strong> unit(s)</td>
          <td>${r.requestDate}</td>
          <td><span class="status-badge ${badgeClass}">${r.status}</span></td>
          <td>
            <div style="display: flex; gap: 0.35rem;">
              ${isPending ? `
                <button class="btn btn-sm btn-primary" onclick="window.app.processRequestStatus(${r.requestId}, 'Approved')" title="Approve Request (checks stock & deducts)">
                  <i class="fa-solid fa-check"></i> Approve
                </button>
                <button class="btn btn-sm btn-danger" onclick="window.app.processRequestStatus(${r.requestId}, 'Rejected')" title="Reject Request">
                  <i class="fa-solid fa-xmark"></i> Reject
                </button>
              ` : ''}
              ${isApproved ? `
                <button class="btn btn-sm btn-success" onclick="window.app.processRequestStatus(${r.requestId}, 'Completed')" title="Mark as Completed">
                  <i class="fa-solid fa-circle-check"></i> Complete
                </button>
              ` : ''}
            </div>
          </td>
        </tr>
      `;
    }).join("");
  }

  openRequestModal() {
    const modal = document.getElementById("requestModal");
    const form = document.getElementById("requestForm");
    form.reset();

    const today = new Date().toISOString().split("T")[0];
    document.getElementById("requestDate").value = today;
    document.getElementById("requestUnits").value = "1";

    modal.classList.add("open");
  }

  handleRequestSubmit(e) {
    e.preventDefault();
    try {
      const patient = document.getElementById("requestPatientName").value;
      const hospital = document.getElementById("requestHospitalName").value;
      const phone = document.getElementById("requestContactPhone").value;
      const group = document.getElementById("requestBloodGroup").value;
      const units = document.getElementById("requestUnits").value;
      const date = document.getElementById("requestDate").value;

      window.ValidationUtil.requireNonEmpty(patient, "Patient Name");
      window.ValidationUtil.requireNonEmpty(hospital, "Hospital Name");
      const validPhone = window.ValidationUtil.validatePhoneNumber(phone);
      window.ValidationUtil.validateBloodGroup(group);
      const validUnits = window.ValidationUtil.validatePositiveUnits(units);
      window.ValidationUtil.requireNonEmpty(date, "Request Date");

      const created = window.db.addRequest({
        patientName: patient.trim(),
        hospitalName: hospital.trim(),
        contactNumber: validPhone,
        bloodGroup: group,
        unitsRequired: validUnits,
        requestDate: date
      });

      this.showToast(
        "Request Created",
        `Request #${created.requestId} for ${created.patientName} (${created.unitsRequired} units of ${created.bloodGroup}) recorded as Pending.`,
        "success"
      );

      this.closeAllModals();
      this.renderRequests();
      this.renderDashboard();
    } catch (err) {
      this.showToast("Request Error", err.message, "error");
    }
  }

  processRequestStatus(requestId, targetStatus) {
    const req = window.db.getRequestById(requestId);
    if (!req) return;

    if (targetStatus === "Approved") {
      // Check stock before approving
      const available = window.db.getStockByGroup(req.bloodGroup).unitsAvailable;
      if (available < req.unitsRequired) {
        this.showToast(
          "Insufficient Stock",
          `Cannot approve request #${requestId}: only ${available} unit(s) of ${req.bloodGroup} available, but ${req.unitsRequired} required.`,
          "error"
        );
        return;
      }

      this.showConfirm(
        `Approve Request #${requestId}?`,
        `Approving this request will immediately deduct ${req.unitsRequired} unit(s) of ${req.bloodGroup} from blood stock. Confirm approval?`,
        () => {
          try {
            window.db.updateRequestStatus(requestId, "Approved");
            this.showToast("Request Approved", `Request #${requestId} approved and blood stock deducted.`, "success");
            this.renderRequests();
            this.renderStock();
            this.renderDashboard();
          } catch (e) {
            this.showToast("Approval Failed", e.message, "error");
          }
        }
      );
    } else if (targetStatus === "Completed") {
      this.showConfirm(
        `Complete Request #${requestId}?`,
        `Mark blood request #${requestId} as fulfilled and completed?`,
        () => {
          try {
            window.db.updateRequestStatus(requestId, "Completed");
            this.showToast("Request Completed", `Request #${requestId} marked as Completed.`, "success");
            this.renderRequests();
            this.renderDashboard();
          } catch (e) {
            this.showToast("Error", e.message, "error");
          }
        }
      );
    } else if (targetStatus === "Rejected") {
      this.showConfirm(
        `Reject Request #${requestId}?`,
        `Are you sure you want to reject blood request #${requestId}? Blood stock will not be deducted.`,
        () => {
          try {
            window.db.updateRequestStatus(requestId, "Rejected");
            this.showToast("Request Rejected", `Request #${requestId} rejected.`, "info");
            this.renderRequests();
            this.renderDashboard();
          } catch (e) {
            this.showToast("Error", e.message, "error");
          }
        }
      );
    }
  }

  // ==========================================
  // Search Blood & Compatibility Matrix
  // ==========================================
  setupSearchBlood() {
    const selector = document.getElementById("searchBloodGroupSelector");
    if (selector) {
      selector.addEventListener("change", (e) => {
        this.selectedSearchGroup = e.target.value;
        this.renderSearchBlood();
      });
    }
  }

  quickSearchStock(group) {
    this.selectedSearchGroup = group;
    this.navigateTo("search");
    const sel = document.getElementById("searchBloodGroupSelector");
    if (sel) sel.value = group;
  }

  renderSearchBlood() {
    const group = this.selectedSearchGroup;
    const stock = window.db.getStockByGroup(group);

    document.getElementById("searchResultGroup").textContent = group;
    document.getElementById("searchResultUnits").textContent = `${stock.unitsAvailable} Units`;

    const statusBadge = document.getElementById("searchResultStatus");
    statusBadge.textContent = stock.status;
    statusBadge.className = "status-badge";
    if (stock.status === "Available") statusBadge.classList.add("available");
    else if (stock.status === "Low Stock") statusBadge.classList.add("low-stock");
    else statusBadge.classList.add("not-available");

    // Compatibility rules
    const compat = BLOOD_COMPATIBILITY[group] || { canGiveTo: [], canReceiveFrom: [] };

    document.getElementById("searchCanGiveTo").innerHTML = compat.canGiveTo.map(g => `<span class="blood-pill">${g}</span>`).join(" ");
    document.getElementById("searchCanReceiveFrom").innerHTML = compat.canReceiveFrom.map(g => `<span class="blood-pill">${g}</span>`).join(" ");

    // Matching donors
    const donors = window.db.getAllDonors().filter(d => d.bloodGroup === group);
    const tbody = document.getElementById("searchDonorsTableBody");
    if (!tbody) return;

    if (donors.length === 0) {
      tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; padding: 1.5rem; color: var(--text-muted);">No donors currently registered with ${group} blood group.</td></tr>`;
      return;
    }

    const today = new Date();
    tbody.innerHTML = donors.map(d => {
      // 90-day cooldown check for eligibility
      let isEligible = true;
      let daysAgoText = "Never donated";
      if (d.lastDonationDate) {
        const lastDate = new Date(d.lastDonationDate);
        const diffDays = Math.floor((today - lastDate) / (1000 * 60 * 60 * 24));
        if (diffDays < 90) {
          isEligible = false;
          daysAgoText = `${diffDays} days ago (Cooling period)`;
        } else {
          daysAgoText = `${diffDays} days ago`;
        }
      }

      return `
        <tr>
          <td><strong>${this.escapeHtml(d.fullName)}</strong></td>
          <td>${d.age} / ${d.gender}</td>
          <td><a href="tel:${d.phoneNumber}" style="color: var(--primary); text-decoration: none;">${d.phoneNumber}</a></td>
          <td>${d.address || "-"}</td>
          <td>${daysAgoText}</td>
          <td>
            ${isEligible ? `
              <span class="status-badge available"><i class="fa-solid fa-circle-check"></i> Eligible</span>
              <button class="btn btn-sm btn-primary" style="margin-left: 0.5rem;" onclick="window.app.quickDonateForDonor(${d.donorId})">
                Record Donation
              </button>
            ` : `
              <span class="status-badge low-stock"><i class="fa-solid fa-clock"></i> Ineligible (< 90d)</span>
            `}
          </td>
        </tr>
      `;
    }).join("");
  }

  // ==========================================
  // Reports & Analytics Controller
  // ==========================================
  setupReports() {
    document.querySelectorAll(".report-tab-btn").forEach(btn => {
      btn.addEventListener("click", (e) => {
        document.querySelectorAll(".report-tab-btn").forEach(b => b.classList.remove("active"));
        e.target.classList.add("active");
        this.currentReportTab = e.target.getAttribute("data-tab");
        this.renderReports();
      });
    });

    const groupFilter = document.getElementById("reportGroupFilter");
    if (groupFilter) {
      groupFilter.addEventListener("change", () => this.renderReports());
    }

    const exportBtn = document.getElementById("exportReportCsvBtn");
    if (exportBtn) {
      exportBtn.addEventListener("click", () => this.exportCurrentReportCSV());
    }

    const printBtn = document.getElementById("printReportBtn");
    if (printBtn) {
      printBtn.addEventListener("click", () => window.print());
    }
  }

  renderReports() {
    const donorsReport = document.getElementById("reportSectionDonors");
    const stockReport = document.getElementById("reportSectionStock");
    const donationsReport = document.getElementById("reportSectionDonations");
    const requestsReport = document.getElementById("reportSectionRequests");
    const filterRow = document.getElementById("reportDonorFilterRow");

    [donorsReport, stockReport, donationsReport, requestsReport].forEach(el => {
      if (el) el.style.display = "none";
    });

    if (this.currentReportTab === "donors") {
      if (filterRow) filterRow.style.display = "flex";
      if (donorsReport) donorsReport.style.display = "block";
      this.renderReportDonorsTable();
    } else {
      if (filterRow) filterRow.style.display = "none";
      if (this.currentReportTab === "stock") {
        if (stockReport) stockReport.style.display = "block";
        this.renderReportStockTable();
      } else if (this.currentReportTab === "donations") {
        if (donationsReport) donationsReport.style.display = "block";
        this.renderReportDonationsTable();
      } else if (this.currentReportTab === "requests") {
        if (requestsReport) requestsReport.style.display = "block";
        this.renderReportRequestsTable();
      }
    }
  }

  renderReportDonorsTable() {
    const tbody = document.getElementById("reportDonorsTableBody");
    if (!tbody) return;
    const filter = document.getElementById("reportGroupFilter")?.value || "All";
    let donors = window.db.getAllDonors();
    if (filter !== "All") donors = donors.filter(d => d.bloodGroup === filter);

    tbody.innerHTML = donors.map(d => `
      <tr>
        <td>#${d.donorId}</td>
        <td>${this.escapeHtml(d.fullName)}</td>
        <td>${d.age}</td>
        <td>${d.gender}</td>
        <td><span class="blood-pill">${d.bloodGroup}</span></td>
        <td>${d.phoneNumber}</td>
        <td>${d.email || "-"}</td>
        <td>${d.lastDonationDate || "Never"}</td>
      </tr>
    `).join("");
  }

  renderReportStockTable() {
    const tbody = document.getElementById("reportStockTableBody");
    if (!tbody) return;
    const stock = window.db.getAllStock();
    tbody.innerHTML = stock.map(s => `
      <tr>
        <td><span class="blood-pill">${s.bloodGroup}</span></td>
        <td><strong>${s.unitsAvailable}</strong></td>
        <td><span class="status-badge ${s.status === 'Available' ? 'available' : s.status === 'Low Stock' ? 'low-stock' : 'not-available'}">${s.status}</span></td>
      </tr>
    `).join("");
  }

  renderReportDonationsTable() {
    const tbody = document.getElementById("reportDonationsTableBody");
    if (!tbody) return;
    const donations = window.db.getAllDonations();
    tbody.innerHTML = donations.map(d => `
      <tr>
        <td>#${d.donationId}</td>
        <td>${this.escapeHtml(d.donorName)}</td>
        <td><span class="blood-pill">${d.bloodGroup}</span></td>
        <td>${d.unitsDonated}</td>
        <td>${d.donationDate}</td>
      </tr>
    `).join("");
  }

  renderReportRequestsTable() {
    const tbody = document.getElementById("reportRequestsTableBody");
    if (!tbody) return;
    const requests = window.db.getAllRequests();
    tbody.innerHTML = requests.map(r => `
      <tr>
        <td>#${r.requestId}</td>
        <td>${this.escapeHtml(r.patientName)}</td>
        <td>${this.escapeHtml(r.hospitalName)}</td>
        <td><span class="blood-pill">${r.bloodGroup}</span></td>
        <td>${r.unitsRequired}</td>
        <td>${r.requestDate}</td>
        <td><span class="status-badge status-${r.status.toLowerCase()}">${r.status}</span></td>
      </tr>
    `).join("");
  }

  exportCurrentReportCSV() {
    let filename = `blood_bank_${this.currentReportTab}_report.csv`;
    let csv = "";

    if (this.currentReportTab === "donors") {
      const donors = window.db.getAllDonors();
      csv = "ID,Full Name,Age,Gender,Blood Group,Phone,Email,Address,Last Donation Date\n";
      csv += donors.map(d => `"${d.donorId}","${d.fullName}","${d.age}","${d.gender}","${d.bloodGroup}","${d.phoneNumber}","${d.email || ''}","${d.address || ''}","${d.lastDonationDate || ''}"`).join("\n");
    } else if (this.currentReportTab === "stock") {
      const stock = window.db.getAllStock();
      csv = "Blood Group,Units Available,Status\n";
      csv += stock.map(s => `"${s.bloodGroup}","${s.unitsAvailable}","${s.status}"`).join("\n");
    } else if (this.currentReportTab === "donations") {
      const donations = window.db.getAllDonations();
      csv = "Donation ID,Donor ID,Donor Name,Blood Group,Donation Date,Units Donated\n";
      csv += donations.map(d => `"${d.donationId}","${d.donorId}","${d.donorName}","${d.bloodGroup}","${d.donationDate}","${d.unitsDonated}"`).join("\n");
    } else if (this.currentReportTab === "requests") {
      const requests = window.db.getAllRequests();
      csv = "Request ID,Patient Name,Hospital Name,Contact,Blood Group,Units Required,Date,Status\n";
      csv += requests.map(r => `"${r.requestId}","${r.patientName}","${r.hospitalName}","${r.contactNumber}","${r.bloodGroup}","${r.unitsRequired}","${r.requestDate}","${r.status}"`).join("\n");
    }

    const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.setAttribute("download", filename);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    this.showToast("Export Complete", `Report downloaded as ${filename}`, "success");
  }

  // ==========================================
  // Modals, Toast & Alerts Helpers
  // ==========================================
  closeAllModals() {
    document.querySelectorAll(".modal-backdrop").forEach(m => m.classList.remove("open"));
  }

  showToast(title, message, type = "info") {
    const container = document.getElementById("toastContainer");
    if (!container) return;

    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;

    let iconClass = "fa-circle-info";
    if (type === "success") iconClass = "fa-circle-check";
    else if (type === "error") iconClass = "fa-circle-exclamation";
    else if (type === "warning") iconClass = "fa-triangle-exclamation";

    toast.innerHTML = `
      <i class="fa-solid ${iconClass} toast-icon"></i>
      <div class="toast-content">
        <div class="toast-title">${this.escapeHtml(title)}</div>
        <div class="toast-message">${this.escapeHtml(message)}</div>
      </div>
      <button class="toast-close" onclick="this.parentElement.remove()">&times;</button>
    `;

    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = "0";
      toast.style.transform = "translateX(100%)";
      setTimeout(() => toast.remove(), 250);
    }, 4500);
  }

  showConfirm(title, message, onConfirm) {
    const modal = document.getElementById("confirmModal");
    if (!modal) {
      if (confirm(`${title}\n\n${message}`)) {
        onConfirm();
      }
      return;
    }

    document.getElementById("confirmModalTitle").textContent = title;
    document.getElementById("confirmModalMessage").textContent = message;

    const actionBtn = document.getElementById("confirmModalActionBtn");
    const newBtn = actionBtn.cloneNode(true);
    actionBtn.parentNode.replaceChild(newBtn, actionBtn);

    newBtn.addEventListener("click", () => {
      modal.classList.remove("open");
      onConfirm();
    });

    modal.classList.add("open");
  }

  escapeHtml(str) {
    if (!str) return "";
    return String(str)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }
}

// Instantiate on load
document.addEventListener("DOMContentLoaded", () => {
  window.app = new BloodBankApp();
});
