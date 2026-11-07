---
kind: business_term
name: Business Glossary
category: business_term
scope:
    - '**'
---

### go-url-expander
- Definition：The upstream web service that this app calls at `/expand?url=...` to resolve shortened URLs (e.g. Twitter/X short links) into their final destination. Its default endpoint is `https://go-url-expander.brunoshiroma.com`.

### url_expander
- Definition：The name of this Kotlin Multiplatform application (Android + iOS) whose purpose is to take a shortened URL, query the go-url-expander service, and present the expanded URL with copy/share/open actions.
