# SkillSprint — Frontend (Bootstrap + Axios edition)

Same app as before, rebuilt with **Bootstrap 5** for styling and **Axios**
for HTTP calls (both loaded via CDN — no npm/build step needed).

## What changed from the plain version

- All custom CSS components (cards, buttons, forms, nav) replaced with
  Bootstrap 5 classes. `css/custom.css` only carries brand color overrides.
- `js/api.js` now uses an Axios instance with request/response
  interceptors instead of hand-rolled `fetch`. Errors are normalized into
  a single readable message, including a specific explanation when the
  backend returns Spring Boot's "No static resource" fallback (this means
  the path you're calling has no matching `@GetMapping`/`@PostMapping` in
  the backend — see the message for exactly what to check).
- The login/register flip-card became a Bootstrap tab component.
  Flashcards use a Bootstrap `collapse` "Show answer" toggle instead of a
  3D flip.
- Sidebar becomes a Bootstrap `offcanvas` on mobile, a fixed sidebar on
  desktop.
- Toasts use Bootstrap's native `Toast` component.

## Running it

1. **Start your Spring Boot backend** (`mvn spring-boot:run`) — confirm
   it's up on `http://localhost:8080`.

2. **Allow CORS from this frontend's origin.** In `CorsConfig.java`, add
   whatever port you serve this frontend on (see step 3), e.g.:
   ```java
   .allowedOrigins("http://localhost:3000", "http://localhost:5500")
   ```
   Restart the backend after saving.

3. **Serve the frontend** (don't open `index.html` by double-click):
   ```bash
   cd skillsprint-frontend
   python3 -m http.server 5500
   ```
   Then open `http://localhost:5500`.

4. **Point the frontend at your API** if it isn't
   `http://localhost:8080/api` — click **"API endpoint settings"** on the
   login screen (or the sidebar once logged in).

## If you see "No static resource ..." errors

This means the exact path the frontend called has **no matching
controller mapping** in your backend — it's not a permissions or CORS
issue. The error message from this app will name the path it tried to
call. Open the matching controller file and confirm:

- The `@GetMapping`/`@PostMapping`/etc. value matches exactly (including
  trailing slashes — Spring treats `/cards/` and `/cards` as different
  routes by default).
- `application.properties` doesn't set a `server.servlet.context-path`
  that would change what URL your controllers actually live at.

If your backend's real paths differ from what's assumed here, open
`js/api.js` — every endpoint is a single line in the `Api` object, so
updating a path is a one-line change.

## Everything else

Login/register flow, roles (Learner/Mentor/Admin), deck & card
management, studying, grading, and results all work exactly as in the
previous version — see the in-app screens for each role. New accounts
always start as `LEARNER`; promote a user to `LINGUIST`/`ADMIN` either
via the in-app **Manage Roles** screen (once you have an admin account)
or directly in MySQL:
```sql
UPDATE system_user SET role='ADMIN' WHERE username='yourname';
```
then log out and back in so the new JWT carries the updated role.
