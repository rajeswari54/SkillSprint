/* ==========================================================================
   state.js — session state backed by localStorage so a refresh stays logged in
   ========================================================================== */

const Session = {
  get token() { return localStorage.getItem(CONFIG.STORAGE_KEYS.token); },
  get username() { return localStorage.getItem(CONFIG.STORAGE_KEYS.username); },
  get role() { return localStorage.getItem(CONFIG.STORAGE_KEYS.role); },

  isLoggedIn() { return !!this.token; },

  save({ token, username, role }) {
    localStorage.setItem(CONFIG.STORAGE_KEYS.token, token);
    localStorage.setItem(CONFIG.STORAGE_KEYS.username, username);
    localStorage.setItem(CONFIG.STORAGE_KEYS.role, role);
  },

  clear() {
    localStorage.removeItem(CONFIG.STORAGE_KEYS.token);
    localStorage.removeItem(CONFIG.STORAGE_KEYS.username);
    localStorage.removeItem(CONFIG.STORAGE_KEYS.role);
  },

  is(...roles) { return roles.includes(this.role); },
};

const Route = {
  name: "dashboard",
  params: {},
  set(name, params = {}) { this.name = name; this.params = params; },
};
