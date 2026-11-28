# Project decisions

**English** · [Português (Brasil)](DECISIONS.pt-BR.md)

I use this log to explain the decisions that define the current NextHub MVP. I group my choices by topic and mark earlier decisions as superseded when I change them.

## 23 November 2026 — Opportunity-board references

- **My decision:** I used InfoJobs and Indeed as references for familiar search, filtering, and opportunity-card patterns without reproducing either product.
- **Alternatives I considered:** I could have created every interaction from scratch or copied one portal's layout.
- **Why I chose it:** recognizable patterns make the board easier to understand while leaving room for NextHub's own identity and scope.

## 24 November 2026 — MVP scope

- **My decision:** I prioritized a demonstrable student and company flow: profiles, opportunities, search, explainable recommendations, favorites, applications, and résumé access.
- **Alternatives I considered:** I could have built a static listings page or a full recruitment and selection platform.
- **Why I chose it:** an end-to-end local product demonstrates the problem without requiring unnecessary integrations or a full hiring workflow.

## 25 November 2026 — Official application architecture (superseded 26 November)

- **My decision at that time:** I treated `frontend/` (React 18 and Vite) and `backend/` (Node.js and Express) as the active application. I considered the earlier Java/Spring and React/Vite scaffolds in `Back-end/` and `Front-end/` superseded and removed them from the working project.
- **Alternatives I considered:** I could have kept both application trees or made the Java/Spring scaffold the official backend.
- **Why I chose it at that time:** the Node/Express implementation contained the authentication, opportunity, profile, application, favorite, and résumé flows, plus the demonstration data. Keeping the Java/Vite starters would leave competing, non-integrated applications and a CI workflow testing the wrong source.

## 25 November 2026 — Local JSON persistence

- **My decision at that time:** I retained the existing JSON demonstration and runtime data in `backend/data/` and stored uploaded PDFs in the ignored `backend/uploads/` directory.
- **Alternatives I considered:** I could have introduced a SQL database or an external data service.
- **Why I chose it at that time:** the committed local data made the academic demo repeatable and available offline; I considered a database unnecessary for the demonstration. I also required backups before manual data maintenance.

## 25 November 2026 — Session authentication and résumé boundaries

- **My decision at that time:** I kept bcrypt-hashed passwords and role-checked Express sessions; I restricted résumé access to the student owner and companies with applications to their own opportunities.
- **Alternatives I considered:** I could have stored plaintext credentials, exposed uploads as static files, or introduced an external identity provider.
- **Why I chose it:** this fits a local MVP and prevents direct public access to résumés. A production deployment still needs durable shared session storage and managed file storage.

## 24 November 2026 — Explainable recommendations

- **My decision:** I calculate recommendations deterministically from the student's interests, education, and preferred format, and show a short explanation for each match.
- **Alternatives I considered:** I could have used random ordering, keyword-only recommendations, or an opaque external AI ranking service.
- **Why I chose it:** the result is predictable and easy to explain during the presentation, without external APIs or service costs.

## 24 November 2026 — Saved opportunities and application navigation

- **My decision:** I show favorites as a tab within the student's Applications area rather than as a separate navigation destination.
- **Alternatives I considered:** I could have separated Saved and Applications or hidden favorites from application tracking.
- **Why I chose it:** this keeps saved and submitted opportunities together while maintaining separate views.

## 24 November 2026 — Responsive navigation and theme

- **My decision:** I kept role-specific navigation, a desktop sidebar, a mobile bottom bar, and light/dark theme controls in Settings.
- **Alternatives I considered:** I could have used identical navigation for both roles or added a permanent theme control to the mobile bar.
- **Why I chose it:** role-specific actions reduce clutter, and the mobile bar stays focused on navigation.

## 24 November 2026 — Out-of-scope features

- **My decision:** I left gamification, rankings, automatic web scraping, external institutional integrations, and a full company selection process out of this MVP.
- **Alternatives I considered:** I could have expanded the product beyond the one-week demonstrable flow.
- **Why I chose it:** those features add complexity without being necessary to validate the opportunity-board concept.

## 26 November 2026 — Java and SQLite backend migration

- **My decision:** I replaced the Node/Express API with the official Java/Spring Boot API, chose Spring Data JPA/Hibernate for persistence, and selected SQLite as the local persistent database.
- **Alternatives I considered:** I could have kept Node as a competing API or retained data only in JSON files.
- **Why I chose it:** this matches the requested architecture, centralizes persistence in SQLite, and keeps one REST API responsible for NextHub flows. This supersedes my 25 November backend and persistence decisions.

## 26 November 2026 — Visual adjustments and résumé access

- **My decision:** I recorded my visual style adjustments and specified that the company's candidate area should provide “View résumé” buttons.
- **Alternatives I considered:** I could have made résumés accessible only from another screen or omitted direct access from the candidate list.
- **Why I chose it:** direct access makes it easier to review a résumé while evaluating candidates.
- **Status:** I recorded this as a UI decision at the time, not as confirmation that the feature was in the code; I had reverted the related changes.

## 27 November 2026 — Profile, notification, and application behavior

- **My decision:** I persist student course entries with the profile in SQLite and expose them through the profile API.
- **Alternatives I considered:** I could have dropped course data from profile updates or introduced a separate course table.
- **Why I chose it:** course entries are a small part of the student's profile in this MVP, so the API validates and stores the list as JSON with the user record.

- **My decision:** I derive notifications from application records and save read state per user in the browser's `localStorage`; I show the unread dot only when unread notifications exist.
- **Alternatives I considered:** I could have added a separate server-side notification table or shown the dot permanently.
- **Why I chose it:** this keeps the local MVP simple. Read state stays in the browser and is not synchronized across devices.

- **My decision:** I let a student withdraw an application through `DELETE /api/applications/{id}`, restricted to that application's owner.
- **Alternatives I considered:** I could have let companies delete student applications or kept applications immutable.
- **Why I chose it:** students control their own submissions while the API enforces application ownership.

- **My decision:** I keep the mobile navigation bar fixed while scrolling and include device safe-area padding.
- **Alternatives I considered:** I could have let the bar scroll with page content or ignored mobile safe areas.
- **Why I chose it:** this keeps navigation accessible on phones without letting system UI obscure it.

## 27 November 2026 — Opportunity actions and motion

- **My decision:** I keep only the “View” action in the company's opportunity list and place “Edit” and “Close opportunity” in the details.
- **Alternatives I considered:** I could have kept every action in the list row or split the buttons between the list and details.
- **Why I chose it:** this simplifies the list and prevents actions from wrapping or becoming cramped on small screens.

- **My decision:** I use restrained animations for interface feedback and transitions while honoring the system's reduced-motion preference.
- **Alternatives I considered:** I could have removed interface animation or added continuous, pronounced motion.
- **Why I chose it:** I want to provide visual feedback without distraction or disregard for accessibility preferences.

## 27 November 2026 — Card, dialog, tab, and candidate-review layout

- **My decision:** I keep favorite-card columns consistent when only a few results are shown, animate the active tab indicator, and keep opportunity details centered and scrollable within the viewport.
- **Alternatives I considered:** I could have allowed sparse favorite cards to expand and spread across the row, kept a static tab underline, or let long dialogs exceed the viewport.
- **Why I chose it:** a consistent grid and clear tab movement make navigation easier, while the dialog remains usable on smaller screens.
- **My decision:** I place the candidate status beside the profile action, keep both controls the same size, align the status to the right, and put résumé access inside the candidate profile.
- **Alternatives I considered:** I could have kept a separate résumé button in the candidate row or left status on a different line.
- **Why I chose it:** the candidate row stays organized while résumé access remains available from the profile.

## Free-tier hosting preparation

- **My decision:** I kept SQLite and local files as development defaults and added a `prod` profile with PostgreSQL/Flyway, JDBC sessions, and résumés in a private Supabase Storage bucket. I prepared the frontend for Vercel and the Java API for Render with Docker, without publishing any services.
- **Alternatives I considered:** I could have replaced the local architecture, used public résumé storage, or published before validating services and credentials.
- **Why I chose it:** this preserves the existing local workflow and documents a reproducible hosting path while keeping secrets and documents out of the frontend. Free plans have inactivity suspension, resource limits, and no production-grade reliability guarantees; I therefore treat this deployment as a demo and require paid services and verified backups for dependable use.
