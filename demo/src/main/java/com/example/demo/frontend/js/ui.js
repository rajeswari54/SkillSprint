/* ==========================================================================
   ui.js — small reusable helpers shared by every view
   ========================================================================== */

function escapeHtml(str) {
  if (str === null || str === undefined) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function toast(message, type = "info") {
  const bg = { success: "success", error: "danger", info: "dark" }[type] || "dark";
  const stack = document.getElementById("toast-stack");
  const el = document.createElement("div");
  el.className = `toast align-items-center text-bg-${bg} border-0`;
  el.setAttribute("role", "alert");
  el.innerHTML = `
    <div class="d-flex">
      <div class="toast-body">${escapeHtml(message)}</div>
      <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
    </div>`;
  stack.appendChild(el);
  const bsToast = new bootstrap.Toast(el, { delay: 3800 });
  el.addEventListener("hidden.bs.toast", () => el.remove());
  bsToast.show();
}

function roleBadgeClass(role) {
  if (role === "ADMIN") return "badge-role-admin";
  if (role === "LINGUIST") return "badge-role-linguist";
  return "badge-role-learner";
}

function roleLabel(role) {
  if (role === "ADMIN") return "Admin";
  if (role === "LINGUIST") return "Mentor";
  return "Learner";
}

function formatDateTime(value) {
  if (!value) return "—";
  const d = new Date(value);
  if (isNaN(d.getTime())) return String(value);
  return d.toLocaleString(undefined, {
    year: "numeric", month: "short", day: "numeric",
    hour: "2-digit", minute: "2-digit",
  });
}

function nowIso() {
  return new Date().toISOString();
}

/** Build a Bootstrap card with a "Show answer" collapse toggle. */
function buildFlashcard(front, back, { revealBack = true } = {}) {
  const tpl = document.getElementById("tpl-flashcard");
  const node = tpl.content.cloneNode(true);
  const cardEl = node.querySelector(".flashcard");
  cardEl.querySelector(".front-content").textContent = front;
  cardEl.querySelector(".back-content").textContent = back || "";

  const footer = cardEl.querySelector(".reveal-footer");
  const collapseEl = cardEl.querySelector(".back-collapse");
  const btn = cardEl.querySelector(".reveal-btn");

  if (revealBack) {
    const collapse = new bootstrap.Collapse(collapseEl, { toggle: false });
    btn.addEventListener("click", () => {
      collapse.toggle();
      const showing = collapseEl.classList.contains("show");
      btn.innerHTML = showing
        ? '<i class="bi bi-eye-slash"></i> Hide answer'
        : '<i class="bi bi-eye"></i> Show answer';
    });
  } else {
    footer.remove();
  }
  return cardEl;
}

function setLoading(container, message = "Loading…") {
  container.innerHTML = `
    <div class="d-flex align-items-center gap-2 text-secondary py-3">
      <div class="spinner-border spinner-border-sm" role="status"></div>
      <span>${escapeHtml(message)}</span>
    </div>`;
}

function emptyState(container, title, body) {
  container.innerHTML = `
    <div class="text-center border border-dashed rounded-3 py-5 px-3 text-secondary">
      <h5 class="text-dark">${escapeHtml(title)}</h5>
      <p class="mb-0">${escapeHtml(body)}</p>
    </div>`;
}

/** Wrap an async view-render function with a friendly error toast + inline message. */
async function safely(container, fn) {
  try {
    await fn();
  } catch (err) {
    console.error(err);
    toast(err.message || "Something went wrong.", "error");
    emptyState(container, "Couldn't load this", err.message || "Something went wrong.");
  }
}
