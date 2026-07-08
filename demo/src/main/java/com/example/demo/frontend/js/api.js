/* ==========================================================================
   api.js — Axios wrapper, one function per backend endpoint.
   Endpoints follow the SkillSprint controller layer:
     AuthController, RoadmapController, MilestoneController,
     EnrollmentController, AnswerController
   ========================================================================== */

const http = axios.create();

// Attach base URL + JWT fresh on every request, since both can change at runtime.
http.interceptors.request.use((config) => {
  config.baseURL = getApiBase();
  const token = localStorage.getItem(CONFIG.STORAGE_KEYS.token);
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Normalize every failure into a single readable Error.
http.interceptors.response.use(
  (res) => res.data,
  (err) => {
    let message = "Something went wrong.";

    if (err.response) {
      const { status, data } = err.response;
      const path = data && data.path;

      if (status === 404 && data && /No static resource/i.test(data.message || data.error || "")) {
        // This means Spring Boot has NO controller mapping for this path at all —
        // not a bad request, not a permissions issue. Surface that clearly.
        message = `The backend has no endpoint at "${path || err.config.url}". ` +
          `Double-check the @GetMapping/@PostMapping path (and any server.servlet.context-path) ` +
          `in the matching controller — the frontend and backend paths don't line up.`;
      } else if (status === 401) {
        message = "Your session has expired or the credentials were rejected. Please log in again.";
      } else if (status === 403) {
        message = "Your account role doesn't have permission for this action.";
      } else {
        message = (data && (data.message || data.error)) || `Request failed (${status}).`;
      }
    } else if (err.request) {
      message = `Could not reach the backend at ${getApiBase()}. Is Spring Boot running, and is the API base URL correct?`;
    }

    return Promise.reject(new Error(message));
  }
);

const Api = {
  // ---- Auth ----
  register: (username, password) => http.post("/auth/register", { username, password }),
  login: (username, password) => http.post("/auth/login", { username, password }),
  updateRole: (username, role) => http.put("/auth/role", { username, role }),

  // ---- Decks (LearningRoadmap) ----
  getDecks: () => http.get("/decks/"),
  getDeck: (id) => http.get(`/decks/${id}`),
  createDeck: (deck) => http.post("/decks/", deck),
  deleteDeck: (id) => http.delete(`/decks/${id}`),

  // ---- Cards (RoadmapMilestone) ----
  // NOTE: the backend currently exposes a single GET /cards/ endpoint that
  // returns frontContent AND backContent to every authenticated role — there
  // is no separate "full" endpoint and no server-side hiding of the answer
  // for learners yet. The frontend hides the answer from learners visually,
  // but be aware the raw network response is not actually role-restricted.
  getCards: (deckId) => http.get(`/decks/${deckId}/cards/`),
  addCard: (deckId, card) => http.post(`/decks/${deckId}/cards/`, card),

  // ---- Study / SM-2 (RoadmapEnrollment) ----
  getDueCards: () => http.get("/study/due"),
  completeSession: (session) => http.post("/study/complete", session),

  // ---- Answers ----
  submitAnswer: (answer) => http.post("/answers/submit", answer),
  getPendingAnswers: (deckId) => http.get(`/answers/pending/${deckId}`),
  evaluateAnswer: (answerId, score) => http.post("/answers/evaluate", { answerId, score }),
  getMyResults: () => http.get("/answers/my-results"),
};
