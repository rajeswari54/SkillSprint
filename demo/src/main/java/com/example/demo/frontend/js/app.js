/* ==========================================================================
   app.js — bootstraps the SPA: auth screen <-> app shell, nav, router
   ========================================================================== */

const NAV_ITEMS = [
  { name: "dashboard", label: "Dashboard", icon: "bi-grid", roles: ["LEARNER", "LINGUIST", "ADMIN"] },
  { name: "decks", label: "Decks", icon: "bi-collection", roles: ["LEARNER", "LINGUIST", "ADMIN"] },
  { name: "due", label: "Study Due", icon: "bi-clock-history", roles: ["LEARNER", "LINGUIST", "ADMIN"] },
  { name: "results", label: "My Results", icon: "bi-check2-circle", roles: ["LEARNER"] },
  { name: "pending", label: "Grade Answers", icon: "bi-pencil-square", roles: ["LINGUIST", "ADMIN"] },
  { name: "admin", label: "Manage Roles", icon: "bi-gear", roles: ["ADMIN"] },
];

const viewBody = () => document.getElementById("view-body");

function sidebarContentHtml() {
  const navHtml = NAV_ITEMS
    .filter((item) => item.roles.includes(Session.role))
    .map((item) => `
      <button class="nav-link d-flex align-items-center gap-2 w-100 text-start border-0 bg-transparent" data-route="${item.name}">
        <i class="bi ${item.icon}"></i> ${item.label}
      </button>`)
    .join("");

  return `
    <div class="d-flex align-items-center gap-2 fw-bold fs-5 mb-4 px-1">
      <span class="brand-dot brand-dot-sm"></span> SkillSprint
    </div>
    <nav class="nav nav-pills flex-column mb-auto gap-1">${navHtml}</nav>
    <div class="mt-4">
      <div class="user-chip d-flex align-items-center gap-2 mb-2">
        <span class="d-inline-flex align-items-center justify-content-center rounded-circle bg-danger text-white fw-bold" style="width:34px;height:34px;flex-shrink:0;">${Session.username ? Session.username.charAt(0).toUpperCase() : "?"}</span>
        <div>
          <div class="small fw-semibold">${escapeHtml(Session.username || "—")}</div>
          <div class="small text-white-50 text-uppercase" style="font-size:.7rem;">${roleLabel(Session.role)}</div>
        </div>
      </div>
      <button class="btn btn-outline-light btn-sm w-100 mb-2" id="settings-btn-app-DUPID"><i class="bi bi-gear"></i> API settings</button>
      <button class="btn btn-outline-light btn-sm w-100" id="logout-btn-DUPID"><i class="bi bi-box-arrow-right"></i> Log out</button>
    </div>
  `;
}

function buildNav() {
  const html = sidebarContentHtml();

  const desktop = document.getElementById("sidebar-inner");
  desktop.innerHTML = html.replace(/DUPID/g, "desktop");

  const mobile = document.getElementById("sidebar-inner-mobile");
  mobile.innerHTML = html.replace(/DUPID/g, "mobile");

  document.querySelectorAll("[data-route]").forEach((btn) => {
    btn.addEventListener("click", () => {
      navigate(btn.dataset.route);
      const offcanvasEl = document.getElementById("sidebar-offcanvas");
      const instance = bootstrap.Offcanvas.getInstance(offcanvasEl);
      if (instance) instance.hide();
    });
  });

  document.getElementById("settings-btn-app-desktop").addEventListener("click", openSettings);
  document.getElementById("settings-btn-app-mobile").addEventListener("click", openSettings);
  document.getElementById("logout-btn-desktop").addEventListener("click", doLogout);
  document.getElementById("logout-btn-mobile").addEventListener("click", doLogout);
}

function doLogout() {
  Session.clear();
  toast("Logged out.", "info");
  showAuth();
}

function setActiveNav(name) {
  document.querySelectorAll("[data-route]").forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.route === name);
  });
}

function navigate(name, params = {}) {
  Route.set(name, params);
  location.hash = params.id ? `${name}/${params.id}` : name;
  render();
}

function render() {
  setActiveNav(Route.name === "deck-detail" ? "decks" : Route.name);
  const root = viewBody();
  switch (Route.name) {
    case "dashboard": return Views.dashboard(root);
    case "decks": return Views.decks(root);
    case "deck-detail": return Views.deckDetail(root, Route.params);
    case "due": return Views.due(root);
    case "results": return Views.results(root);
    case "pending": return Views.pending(root);
    case "admin": return Views.admin(root);
    default: return Views.dashboard(root);
  }
}

function parseHash() {
  const raw = location.hash.replace(/^#\/?/, "");
  if (!raw) return { name: "dashboard", params: {} };
  const [name, id] = raw.split("/");
  return { name, params: id ? { id } : {} };
}

/* -------------------------------------------------------------------- */
/* Auth <-> App shell toggling                                          */
/* -------------------------------------------------------------------- */
function showApp() {
  document.getElementById("auth-screen").classList.add("d-none");
  document.getElementById("app-shell").classList.remove("d-none");

  buildNav();
  const { name, params } = parseHash();
  const allowed = NAV_ITEMS.find((i) => i.name === name)?.roles.includes(Session.role);
  if (name === "deck-detail" || allowed) {
    Route.set(name, params);
  } else {
    Route.set("dashboard");
  }
  render();
}

function showAuth() {
  document.getElementById("app-shell").classList.add("d-none");
  document.getElementById("auth-screen").classList.remove("d-none");
  location.hash = "";
}

/* -------------------------------------------------------------------- */
/* Wiring                                                                */
/* -------------------------------------------------------------------- */
function wireAuthScreen() {
  document.getElementById("login-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    const errEl = document.getElementById("login-error");
    errEl.textContent = "";
    const btn = e.target.querySelector("button[type=submit]");
    btn.disabled = true;
    try {
      const res = await Api.login(fd.get("username").trim(), fd.get("password"));
      Session.save(res);
      toast(`Welcome back, ${res.username}.`, "success");
      showApp();
    } catch (err) {
      errEl.textContent = err.message;
    } finally {
      btn.disabled = false;
    }
  });

  document.getElementById("register-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    const errEl = document.getElementById("register-error");
    errEl.textContent = "";
    const btn = e.target.querySelector("button[type=submit]");
    btn.disabled = true;
    try {
      const res = await Api.register(fd.get("username").trim(), fd.get("password"));
      Session.save(res);
      toast(`Account created. Welcome, ${res.username}.`, "success");
      showApp();
    } catch (err) {
      errEl.textContent = err.message;
    } finally {
      btn.disabled = false;
    }
  });
}

let settingsModal;
function openSettings() {
  document.getElementById("api-base-input").value = getApiBase();
  settingsModal.show();
}

function wireSettings() {
  settingsModal = new bootstrap.Modal(document.getElementById("settings-modal"));
  document.getElementById("open-settings").addEventListener("click", openSettings);
  document.getElementById("settings-save").addEventListener("click", () => {
    setApiBase(document.getElementById("api-base-input").value);
    toast("API endpoint saved.", "success");
    settingsModal.hide();
  });
}

window.addEventListener("hashchange", () => {
  if (!Session.isLoggedIn()) return;
  const { name, params } = parseHash();
  Route.set(name, params);
  render();
});

/* -------------------------------------------------------------------- */
/* Init                                                                  */
/* -------------------------------------------------------------------- */
function init() {
  wireAuthScreen();
  wireSettings();

  if (Session.isLoggedIn()) {
    showApp();
  } else {
    showAuth();
  }
}

document.addEventListener("DOMContentLoaded", init);
