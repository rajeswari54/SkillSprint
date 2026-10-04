/* ==========================================================================
   views.js — one render function per screen, built with Bootstrap markup.
   Every render function receives the #view-body element and fills it in.
   ========================================================================== */

const Views = {};

function setHeader(eyebrow, title, actionsHtml = "") {
  document.getElementById("view-eyebrow").textContent = eyebrow;
  document.getElementById("view-title").textContent = title;
  document.getElementById("header-actions").innerHTML = actionsHtml;
}

/* -------------------------------------------------------------------- */
/* Dashboard                                                             */
/* -------------------------------------------------------------------- */
Views.dashboard = async function (root) {
  setHeader("Overview", `Welcome back, ${Session.username}`);
  setLoading(root);

  await safely(root, async () => {
    const decks = await Api.getDecks();
    let dueCount = null;
    try { dueCount = (await Api.getDueCards()).length; } catch { /* optional */ }

    const canAuthor = Session.is("LINGUIST", "ADMIN");

    root.innerHTML = `
      <div class="row row-cols-1 row-cols-md-3 g-3">
        <div class="col">
          <div class="card shadow-sm h-100"><div class="card-body">
            <div class="display-6 fw-bold text-primary">${decks.length}</div>
            <div class="text-secondary fw-semibold small">Decks available</div>
          </div></div>
        </div>
        <div class="col">
          <div class="card shadow-sm h-100"><div class="card-body">
            <div class="display-6 fw-bold text-primary">${dueCount === null ? "—" : dueCount}</div>
            <div class="text-secondary fw-semibold small">Cards due for review</div>
          </div></div>
        </div>
        <div class="col">
          <div class="card shadow-sm h-100"><div class="card-body">
            <div class="display-6 fw-bold text-primary">${roleLabel(Session.role)}</div>
            <div class="text-secondary fw-semibold small">Your role</div>
          </div></div>
        </div>
      </div>

      <div class="card shadow-sm mt-4">
        <div class="card-body">
          <h5 class="card-title">Where to start</h5>
          <ul class="mb-0 text-secondary">
            <li>Open <strong>Decks</strong> to browse study material${canAuthor ? " or create a new deck" : ""}.</li>
            <li>Check <strong>Study Due</strong> for cards scheduled for review today.</li>
            ${Session.is("LEARNER") ? "<li>Answer a card, then check <strong>My Results</strong> once a mentor grades it.</li>" : ""}
            ${canAuthor ? "<li>Use <strong>Grade Answers</strong> to review and score pending student submissions.</li>" : ""}
            ${Session.is("ADMIN") ? "<li>Promote or demote users from <strong>Manage Roles</strong>.</li>" : ""}
          </ul>
        </div>
      </div>
    `;
  });
};

/* -------------------------------------------------------------------- */
/* Decks list                                                            */
/* -------------------------------------------------------------------- */
Views.decks = async function (root) {
  const canAuthor = Session.is("LINGUIST", "ADMIN");
  setHeader("Study material", "Decks");
  setLoading(root);

  await safely(root, async () => {
    const decks = await Api.getDecks();

    const createPanel = canAuthor ? `
      <div class="card shadow-sm">
        <div class="card-body">
          <h5 class="card-title">Create a new deck</h5>
          <form id="create-deck-form" class="row g-3 mt-1">
            <div class="col-md-6">
              <label class="form-label">Title</label>
              <input type="text" class="form-control" name="title" required placeholder="e.g. Spanish Basics">
            </div>
            <div class="col-md-6">
              <label class="form-label">Mentor name</label>
              <input type="text" class="form-control" name="mentorName" required placeholder="Your display name">
            </div>
            <div class="col-md-3">
              <label class="form-label">Capacity</label>
              <input type="number" class="form-control" name="capacity" min="1" required placeholder="30">
            </div>
            <div class="col-md-9">
              <label class="form-label">Description</label>
              <input type="text" class="form-control" name="description" required placeholder="What this deck covers">
            </div>
            <div class="col-12">
              <button type="submit" class="btn btn-primary">Create deck</button>
            </div>
          </form>
        </div>
      </div>` : "";

    const list = decks.length ? `
      <div class="row row-cols-1 row-cols-md-2 g-3" id="deck-grid">
        ${decks.map(deckCardHtml).join("")}
      </div>` : "";

    root.innerHTML = createPanel + `<div id="deck-list-wrap" class="mt-4">${list}</div>`;

    if (!decks.length) {
      emptyState(root.querySelector("#deck-list-wrap"), "No decks yet",
        canAuthor ? "Create the first deck above to get started." : "Ask a mentor to create a deck to get started.");
    }

    if (canAuthor) {
      root.querySelector("#create-deck-form").addEventListener("submit", async (e) => {
        e.preventDefault();
        const fd = new FormData(e.target);
        const payload = {
          title: fd.get("title").trim(),
          description: fd.get("description").trim(),
          capacity: Number(fd.get("capacity")),
          mentorName: fd.get("mentorName").trim(),
        };
        const btn = e.target.querySelector("button[type=submit]");
        btn.disabled = true;
        try {
          await Api.createDeck(payload);
          toast("Deck created.", "success");
          Views.decks(root);
        } catch (err) {
          toast(err.message, "error");
          btn.disabled = false;
        }
      });
    }

    root.querySelectorAll(".deck-card").forEach((card) => {
      card.addEventListener("click", (e) => {
        if (e.target.closest(".deck-delete-btn")) return;
        navigate("deck-detail", { id: card.dataset.id });
      });
    });

    root.querySelectorAll(".deck-delete-btn").forEach((btn) => {
      btn.addEventListener("click", async (e) => {
        e.stopPropagation();
        const id = btn.dataset.id;
        if (!confirm("Delete this deck permanently? This cannot be undone.")) return;
        try {
          await Api.deleteDeck(id);
          toast("Deck deleted.", "success");
          Views.decks(root);
        } catch (err) {
          toast(err.message, "error");
        }
      });
    });
  });
};

function deckCardHtml(deck) {
  const id = deck.id ?? deck.deckId;
  return `
    <div class="col">
      <div class="card deck-card h-100 shadow-sm position-relative" data-id="${id}">
        ${Session.is("ADMIN") ? `<button class="btn btn-outline-danger btn-sm deck-delete-btn position-absolute top-0 end-0 m-3" data-id="${id}"><i class="bi bi-trash"></i></button>` : ""}
        <div class="card-body">
          <h5 class="card-title">${escapeHtml(deck.title)}</h5>
          <p class="card-text text-secondary">${escapeHtml(deck.description || "")}</p>
          <div class="d-flex gap-3 small text-secondary font-monospace">
            <span>Mentor: ${escapeHtml(deck.mentorName || "—")}</span>
            <span>Capacity: ${escapeHtml(deck.capacity ?? "—")}</span>
          </div>
        </div>
      </div>
    </div>`;
}

/* -------------------------------------------------------------------- */
/* Deck detail — cards, add card, submit answer                         */
/* -------------------------------------------------------------------- */
Views.deckDetail = async function (root, { id }) {
  const canSeeAnswers = Session.is("LINGUIST", "ADMIN");
  const backBtn = `<button class="btn btn-outline-secondary btn-sm" id="back-to-decks"><i class="bi bi-arrow-left"></i> Back to decks</button>`;
  setHeader("Deck", "Loading…", backBtn);
  setLoading(root);

  await safely(root, async () => {
    const deck = await Api.getDeck(id);
    // Backend only exposes one cards endpoint (see js/api.js note) — role
    // only controls whether we *show* the answer, not what the API returns.
    const cards = await Api.getCards(id);

    setHeader("Deck", deck.title, backBtn);

    const addCardPanel = canSeeAnswers ? `
      <div class="card shadow-sm">
        <div class="card-body">
          <h5 class="card-title">Add a card</h5>
          <form id="add-card-form">
            <div class="mb-3">
              <label class="form-label">Question (front)</label>
              <textarea class="form-control" name="frontContent" rows="2" required placeholder="What is…"></textarea>
            </div>
            <div class="mb-3">
              <label class="form-label">Answer (back)</label>
              <textarea class="form-control" name="backContent" rows="2" required placeholder="The answer students should recall"></textarea>
            </div>
            <button type="submit" class="btn btn-primary">Add card</button>
          </form>
        </div>
      </div>` : "";

    root.innerHTML = `
      <div class="card shadow-sm">
        <div class="card-body">
          <p class="text-secondary mb-2">${escapeHtml(deck.description || "")}</p>
          <div class="d-flex gap-3 small text-secondary font-monospace">
            <span>Mentor: ${escapeHtml(deck.mentorName || "—")}</span>
            <span>Capacity: ${escapeHtml(deck.capacity ?? "—")}</span>
          </div>
        </div>
      </div>
      ${addCardPanel}
      <div>
        <h5 class="mb-3">Cards${canSeeAnswers ? "" : ""}</h5>
        <div class="row row-cols-1 row-cols-md-2 g-3" id="cards-grid"></div>
      </div>
    `;

    document.getElementById("back-to-decks").addEventListener("click", () => navigate("decks"));

    const grid = root.querySelector("#cards-grid");
    if (!cards.length) {
      emptyState(grid, "No cards yet", canSeeAnswers ? "Add the first card above." : "This deck doesn't have any cards yet.");
    } else {
      cards.forEach((c) => {
        const col = document.createElement("div");
        col.className = "col d-flex flex-column gap-2";

        const cardEl = buildFlashcard(c.frontContent, c.backContent, { revealBack: canSeeAnswers });
        col.appendChild(cardEl);

        if (Session.is("LEARNER")) {
          const form = document.createElement("form");
          form.className = "card shadow-sm";
          form.innerHTML = `
            <div class="card-body">
              <label class="form-label small fw-semibold text-secondary">Your answer</label>
              <textarea class="form-control form-control-sm" name="answerText" rows="2" placeholder="Type your answer…" required></textarea>
              <button type="submit" class="btn btn-primary btn-sm mt-2">Submit answer</button>
            </div>
          `;
          form.addEventListener("submit", async (e) => {
            e.preventDefault();
            const text = new FormData(e.target).get("answerText").trim();
            const btn = e.target.querySelector("button");
            btn.disabled = true;
            try {
              await Api.submitAnswer({ cardId: c.id ?? c.cardId, deckId: Number(id), answerText: text });
              toast("Answer submitted. A mentor will grade it soon.", "success");
              e.target.reset();
            } catch (err) {
              toast(err.message, "error");
            } finally {
              btn.disabled = false;
            }
          });
          col.appendChild(form);
        }

        grid.appendChild(col);
      });
    }

    if (canSeeAnswers) {
      root.querySelector("#add-card-form").addEventListener("submit", async (e) => {
        e.preventDefault();
        const fd = new FormData(e.target);
        const btn = e.target.querySelector("button[type=submit]");
        btn.disabled = true;
        try {
          await Api.addCard(id, {
            frontContent: fd.get("frontContent").trim(),
            backContent: fd.get("backContent").trim(),
          });
          toast("Card added.", "success");
          Views.deckDetail(root, { id });
        } catch (err) {
          toast(err.message, "error");
          btn.disabled = false;
        }
      });
    }
  });
};

/* -------------------------------------------------------------------- */
/* Study Due                                                             */
/* -------------------------------------------------------------------- */
Views.due = async function (root) {
  const canLog = Session.is("LINGUIST", "ADMIN");
  setHeader("Spaced repetition", "Study Due");
  setLoading(root);

  await safely(root, async () => {
    const due = await Api.getDueCards();

    const logPanel = canLog ? `
      <div class="card shadow-sm">
        <div class="card-body">
          <h5 class="card-title">Log a completed study session</h5>
          <p class="text-secondary small">Record a score for a student's session directly. This updates their SM-2 schedule immediately.</p>
          <form id="log-session-form" class="row g-3">
            <div class="col-md-6">
              <label class="form-label">Student username</label>
              <input type="text" class="form-control" name="studentUsername" required>
            </div>
            <div class="col-md-6">
              <label class="form-label">Deck ID</label>
              <input type="number" class="form-control" name="deckId" required min="1">
            </div>
            <div class="col-md-6">
              <label class="form-label">Card ID</label>
              <input type="number" class="form-control" name="cardId" required min="1">
            </div>
            <div class="col-md-6">
              <label class="form-label">Score (0–5)</label>
              <input type="number" class="form-control" name="score" required min="0" max="5">
            </div>
            <div class="col-12">
              <button type="submit" class="btn btn-primary">Log session</button>
            </div>
          </form>
        </div>
      </div>` : "";

    let rows = "";
    if (due.length) {
      rows = `
        <div class="card shadow-sm mt-4">
          <div class="card-body">
            <h5 class="card-title">Due now</h5>
            <div class="table-responsive">
              <table class="table align-middle">
                <thead><tr><th>Card</th><th>Deck</th><th>Next review</th><th>Interval</th></tr></thead>
                <tbody>
                  ${due.map((d) => `
                    <tr>
                      <td>#${escapeHtml(d.cardId ?? d.id ?? "—")}</td>
                      <td>#${escapeHtml(d.deckId ?? "—")}</td>
                      <td>${formatDateTime(d.nextReviewDate)}</td>
                      <td>${d.intervalDays !== undefined ? escapeHtml(d.intervalDays) + " day(s)" : "—"}</td>
                    </tr>`).join("")}
                </tbody>
              </table>
            </div>
          </div>
        </div>`;
    }

    root.innerHTML = logPanel + `<div id="due-list-wrap">${rows}</div>`;
    if (!due.length) {
      emptyState(root.querySelector("#due-list-wrap"), "Nothing due right now",
        "Cards will appear here as their review date arrives. Head to Decks to study ahead.");
    }

    if (canLog) {
      root.querySelector("#log-session-form").addEventListener("submit", async (e) => {
        e.preventDefault();
        const fd = new FormData(e.target);
        const btn = e.target.querySelector("button[type=submit]");
        btn.disabled = true;
        const nowTime = nowIso();
        try {
          await Api.completeSession({
            studentUsername: fd.get("studentUsername").trim(),
            deckId: Number(fd.get("deckId")),
            cardId: Number(fd.get("cardId")),
            startTime: nowTime,
            endTime: nowTime,
            score: Number(fd.get("score")),
          });
          toast("Session logged.", "success");
          e.target.reset();
        } catch (err) {
          toast(err.message, "error");
        } finally {
          btn.disabled = false;
        }
      });
    }
  });
};

/* -------------------------------------------------------------------- */
/* My Results (LEARNER)                                                  */
/* -------------------------------------------------------------------- */
Views.results = async function (root) {
  setHeader("Your progress", "My Results");
  setLoading(root);

  await safely(root, async () => {
    const results = await Api.getMyResults();

    if (!results.length) {
      emptyState(root, "No answers yet", "Submit an answer from a deck to see your results here.");
      return;
    }

    root.innerHTML = `
      <div class="card shadow-sm">
        <div class="card-body">
          <div class="table-responsive">
            <table class="table align-middle">
              <thead><tr><th>Card</th><th>Your answer</th><th>Correct answer</th><th>Score</th><th>Status</th><th>Submitted</th></tr></thead>
              <tbody>
                ${results.map((r) => `
                  <tr>
                    <td>#${escapeHtml(r.cardId ?? "—")}</td>
                    <td>${escapeHtml(r.answerText ?? r.studentAnswer ?? "—")}</td>
                    <td>${escapeHtml(r.correctAnswer ?? "Pending evaluation")}</td>
                    <td>${r.score !== null && r.score !== undefined ? `<span class="badge bg-secondary">${escapeHtml(r.score)}</span>` : "—"}</td>
                    <td><span class="badge ${r.status === "EVALUATED" ? "text-bg-success" : "text-bg-warning"}">${escapeHtml(r.status || "PENDING")}</span></td>
                    <td>${formatDateTime(r.submittedAt)}</td>
                  </tr>`).join("")}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    `;
  });
};

/* -------------------------------------------------------------------- */
/* Grade Answers / Pending (LINGUIST, ADMIN)                             */
/* -------------------------------------------------------------------- */
Views.pending = async function (root) {
  setHeader("Review submissions", "Grade Answers");
  setLoading(root);

  await safely(root, async () => {
    const decks = await Api.getDecks();

    root.innerHTML = `
      <div class="card shadow-sm">
        <div class="card-body">
          <h5 class="card-title">Choose a deck</h5>
          <label class="form-label">Deck</label>
          <select class="form-select" id="deck-select" style="max-width:340px;">
            <option value="">Select a deck…</option>
            ${decks.map((d) => `<option value="${d.id ?? d.deckId}">${escapeHtml(d.title)}</option>`).join("")}
          </select>
        </div>
      </div>
      <div id="pending-body" class="mt-4"></div>
    `;

    const body = root.querySelector("#pending-body");
    root.querySelector("#deck-select").addEventListener("change", async (e) => {
      const deckId = e.target.value;
      if (!deckId) { body.innerHTML = ""; return; }
      setLoading(body, "Loading pending answers…");
      await safely(body, async () => {
        const pending = await Api.getPendingAnswers(deckId);
        renderPendingTable(body, pending, deckId);
      });
    });
  });
};

function renderPendingTable(body, pending, deckId) {
  if (!pending.length) {
    emptyState(body, "All caught up", "No pending answers for this deck right now.");
    return;
  }

  body.innerHTML = `
    <div class="card shadow-sm">
      <div class="card-body">
        <div class="table-responsive">
          <table class="table align-middle">
            <thead><tr><th>Student</th><th>Question</th><th>Their answer</th><th>Correct answer</th><th>Score</th></tr></thead>
            <tbody id="pending-tbody">
              ${pending.map((p) => `
                <tr data-answer-id="${p.answerId ?? p.id}">
                  <td>${escapeHtml(p.username ?? p.studentUsername ?? "—")}</td>
                  <td>${escapeHtml(p.frontContent ?? "—")}</td>
                  <td>${escapeHtml(p.studentAnswer ?? p.answerText ?? "—")}</td>
                  <td>${escapeHtml(p.correctAnswer ?? p.backContent ?? "—")}</td>
                  <td>
                    <div class="btn-group btn-group-sm" role="group">
                      ${[0,1,2,3,4,5].map((s) => `<button type="button" class="btn btn-outline-secondary score-opt" data-score="${s}">${s}</button>`).join("")}
                    </div>
                  </td>
                </tr>`).join("")}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `;

  body.querySelectorAll("tr[data-answer-id]").forEach((row) => {
    const answerId = row.dataset.answerId;
    row.querySelectorAll(".score-opt").forEach((btn) => {
      btn.addEventListener("click", async () => {
        row.querySelectorAll(".score-opt").forEach((b) => b.classList.remove("selected"));
        btn.classList.add("selected");
        row.querySelectorAll(".score-opt").forEach((b) => b.disabled = true);
        try {
          await Api.evaluateAnswer(Number(answerId), Number(btn.dataset.score));
          toast("Score recorded.", "success");
          row.style.transition = "opacity .3s ease";
          row.style.opacity = "0.3";
          setTimeout(() => row.remove(), 300);
        } catch (err) {
          toast(err.message, "error");
          row.querySelectorAll(".score-opt").forEach((b) => b.disabled = false);
        }
      });
    });
  });
}

/* -------------------------------------------------------------------- */
/* Manage Roles (ADMIN)                                                  */
/* -------------------------------------------------------------------- */
Views.admin = async function (root) {
  setHeader("Administration", "Manage Roles");

  root.innerHTML = `
    <div class="card shadow-sm" style="max-width:480px;">
      <div class="card-body">
        <h5 class="card-title">Promote or demote a user</h5>
        <form id="role-form">
          <div class="mb-3">
            <label class="form-label">Username</label>
            <input type="text" class="form-control" name="username" required placeholder="e.g. bob">
          </div>
          <div class="mb-3">
            <label class="form-label">New role</label>
            <select class="form-select" name="role" required>
              <option value="LEARNER">Learner</option>
              <option value="LINGUIST">Mentor (Linguist)</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>
          <button type="submit" class="btn btn-primary">Update role</button>
          <div class="text-danger small mt-2" id="role-error"></div>
        </form>
      </div>
    </div>
  `;

  root.querySelector("#role-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    const errEl = root.querySelector("#role-error");
    errEl.textContent = "";
    const btn = e.target.querySelector("button[type=submit]");
    btn.disabled = true;
    try {
      await Api.updateRole(fd.get("username").trim(), fd.get("role"));
      toast("Role updated.", "success");
      e.target.reset();
    } catch (err) {
      errEl.textContent = err.message;
    } finally {
      btn.disabled = false;
    }
  });
};
