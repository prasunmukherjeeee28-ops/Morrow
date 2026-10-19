# Security Policy

## Reporting a vulnerability

Please report suspected security vulnerabilities privately to the repository owner rather than posting exploitable details or secrets in a public issue.

Include a concise description, affected component, impact, and reproducible steps where safe. Do not include other people's private data.

## Secret handling

- Keep Groq and other provider API keys in server-side Cloudflare Worker secrets.
- Never embed provider API keys or private credentials in Android source, APKs, README files, screenshots, or commits.
- Treat OAuth ID tokens and app session tokens as sensitive.
- Verify authentication tokens on the server before trusting identity claims.
- Demo authentication and placeholder sign-in buttons are not production authentication.

## Supported versions

Morrow is an actively developing project. Security fixes and supported versions will be documented here as release practices mature.
