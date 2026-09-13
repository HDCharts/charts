---
name: hdc-docs
description: HDCharts documentation and release-note writing style for direct, positive, concise, user-focused technical communication.
---

# HDCharts Documentation

Use for README content, API documentation, migration guides, release notes,
sample explanations, and other user-facing text.

## Writing Rules

- Lead with the capability, behavior, or action the reader needs.
- Use direct, positive descriptions of supported APIs and behavior. Keep
  removed or unsupported APIs to the migration context that requires them.
- Use concrete verbs and name the public API, chart, module, or user outcome.
  Prefer `Use ChartData with Double values.` over a sentence centered on a
  removed or rejected API.
- Make migration guidance actionable: show the supported call shape, required
  boundary conversion, and resulting behavior.
- Keep release notes concise and user-facing. Describe one coherent outcome in
  plain language, omit implementation history, PR identifiers, internal test
  details, and future work.
- Keep one coherent outcome per release-note sentence. Combine API, validation,
  interaction, and documentation changes only when they share one topic.
- Use headings that describe the content directly, such as `Use`, `Behavior`,
  `Selection`, and `Validation`. Keep paragraphs short and examples minimal.
- Keep claims about support, compatibility, platform behavior, test coverage,
  and migration safety grounded in the implementation.
- Use code examples that compile against the current public API. Prefer named
  arguments where they clarify chart data, selection, styles, and formatters.
