/* ==========================================================================
   config.js — where this frontend finds the Spring Boot backend
   ========================================================================== */

const CONFIG = {
  DEFAULT_API_BASE: "http://localhost:8080/api",
  STORAGE_KEYS: {
    apiBase: "skillsprint.apiBase",
    token: "skillsprint.token",
    username: "skillsprint.username",
    role: "skillsprint.role",
  },
};

function getApiBase() {
  return localStorage.getItem(CONFIG.STORAGE_KEYS.apiBase) || CONFIG.DEFAULT_API_BASE;
}

function setApiBase(url) {
  const clean = (url || "").trim().replace(/\/+$/, "");
  localStorage.setItem(CONFIG.STORAGE_KEYS.apiBase, clean || CONFIG.DEFAULT_API_BASE);
}
