# BoviGestor Agent Guide

Read this file before changing the repository. It is an entry point, not a replacement for the canonical documents below.

## Precedence

Follow system and user instructions first, then explicit task scope, this guide, and the referenced project documents. Do not treat silence as approval.

## Working rules

- Preserve the educational purpose: the developer implements domain exercises unless explicitly delegated.
- Keep the pure domain independent from Spring, JPA, HTTP, and persistence.
- Use feature branches; `develop` and `main` are protected integration branches.
- Do not commit, push, open or merge a pull request, delete branches, or change infrastructure without explicit authorization.
- Report real evidence only: commands, outputs, checks, and SHAs actually observed.
- Execute an explicit, authorized reviewer instruction immediately; do not merely acknowledge or summarize it. See `docs/04-governance/REVIEW_PROTOCOL.md`.

## Canonical references

- Current scope and maturity: `docs/00-overview/PROJECT_CURRENT_STATE.md`
- Known business vocabulary: `docs/00-overview/BUSINESS_DOMAIN.md`
- Architecture and decisions: `docs/01-architecture/`
- Review, Git, and continuity: `docs/04-governance/`

`.agents/skills` is a portable convention. Do not claim automatic discovery by a tool unless that capability is verified in the active environment.
