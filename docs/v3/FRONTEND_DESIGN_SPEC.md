V07 Calm Space — Journal-Led Frontend Design Specification

Audience: UI/UX designers, frontend engineers, and AI coding agents
Design direction: Visually pleasing journal interface — warm, quiet, tactile, editorial, personal, and modern.
Core principle: The app should feel like opening a beautifully designed personal journal, not a terminal, developer dashboard, analytics console, or enterprise productivity tool.

1. Design Personality

V07 is a personal journal and life companion.

The interface should communicate:

calm

personal

reflective

warm

spacious

tactile

quietly premium

easy to read

human rather than technical

1.1 What the UI must NOT feel like

Avoid the visual language of:

terminals

command-line interfaces

developer tools

admin dashboards

monitoring software

enterprise SaaS

database explorers

futuristic HUDs

overly geometric "tech" interfaces

generic AI-generated dashboard designs

Do not use:

dense grids of equal cards

excessive borders

excessive uppercase text

monospace typography for normal UI

technical-looking badges everywhere

unnecessary status labels

data-dashboard terminology

decorative code-like elements

excessive pills

visual noise created by too many components

2. Typography

2.1 Primary Typeface

Use a readable journal/editorial sans-serif.

Preferred:

font-family:
  "Plus Jakarta Sans",
  -apple-system,
  BlinkMacSystemFont,
  "Segoe UI",
  sans-serif;

The type should feel soft and natural rather than technological.

Do not introduce a "designer font" merely to make the interface look artistic.

The typography should feel special through:

hierarchy

spacing

weight

line height

composition

—not through an unusual typeface.

2.2 Typography Rules

Screen title

Large, confident, human.

Example:

Journal

Not:

JOURNAL

Subheading

A short, simple phrase that gives the screen personality or context.

Example:

A quiet place for your thoughts

Not:

THOUGHTS & REFLECTION MODULE

Section heading

Simple and conversational.

Examples:

Today

A little progress

Your thoughts

Coming up

Recent moments

Avoid:

DAILY SUMMARY METRICS

CHRONOLOGICAL EVENT STREAM

ACTIVE PLUGINS

SYSTEM STATUS

Body text

Natural sentence case.

Use comfortable line height and enough width for easy reading.

Labels

Use sentence case wherever possible.

Prefer:

Screen time

over:

SCREEN TIME

Prefer:

Focus today

over:

FOCUS TELEMETRY

Monospace

Monospace is not part of the normal visual language.

Only use a monospace font when displaying genuinely technical content such as:

file paths

code

raw JSON

database records

developer diagnostics

Timers, dates, normal metrics, headings, and ordinary UI text should use the primary typeface.

3. Writing & UI Copy

The application should speak like a thoughtful journal, not a software system.

3.1 Title + Subheading Pattern

Every major screen can use:

Simple title

A short phrase that explains the feeling or purpose.

Examples:

Journal

Journal
Somewhere to put your thoughts.

Tracker

Your day
A quiet look at how your time unfolded.

Tools

Your tools
Small things that help you through the day.

Profile

Your space
Make V07 feel like yours.

Focus

Focus
A little uninterrupted time for yourself.

3.2 Copy Rules

Use:

short phrases

conversational wording

sentence case

gentle descriptions

meaningful microcopy

Avoid:

robotic commands

system terminology

unnecessary technical nouns

marketing-heavy copy

fake wellness language

excessive exclamation marks

Prefer:

Your plate is clear.
Take a breath, read something, or leave the rest for tomorrow.

Instead of:

ALL TASKS COMPLETED — SYSTEM IDLE

4. Color System

The palette should feel like paper, ink, leaves, and small warm accents.

4.1 Light Theme

--canvas: #F6F7F4;
--surface: #FFFFFF;
--surface-soft: #EEF2EE;

--text-primary: #202824;
--text-secondary: #66716B;
--text-tertiary: #8A948E;

--accent: #397A68;
--accent-soft: #E3F0EB;

--warm: #A86F61;
--warm-soft: #F4E9E5;

--border: #E1E7E2;
--divider: #EDF1EE;

--danger: #B84A4A;
--danger-soft: #F8E8E7;

Color principles

The interface should be mostly neutral.

Use the accent color sparingly for:

primary actions

selected states

progress

meaningful highlights

important interactive elements

Do not turn every card, badge, icon, or statistic into a colored object.

5. Dark Mode

Dark mode should feel like a quiet journal at night, not a developer terminal.

It must NOT use:

pure black backgrounds

neon green

glowing borders everywhere

cyberpunk gradients

bright green-on-black terminal aesthetics

excessive shadows

high-saturation accents

5.1 Dark Theme Tokens

--dark-canvas: #171B19;
--dark-surface: #202522;
--dark-surface-soft: #272D29;

--dark-text-primary: #E9EEE9;
--dark-text-secondary: #AAB5AE;
--dark-text-tertiary: #7D8982;

--dark-accent: #75B7A1;
--dark-accent-soft: #263D35;

--dark-warm: #D09A8C;
--dark-warm-soft: #3A2B28;

--dark-border: #343C37;
--dark-divider: #2B322E;

--dark-danger: #E07B78;
--dark-danger-soft: #402827;

5.2 Dark Mode Rules

Background

Use a very dark green-gray rather than black.

background: #171B19;

The purpose is to create the feeling of:

paper and ink after sunset

rather than:

terminal window

Surfaces

Cards should be only slightly lighter than the background.

Avoid huge contrast between cards.

background: #202522;

Text

Primary text should be soft off-white.

Do not use pure white everywhere.

color: #E9EEE9;

Accent

Use a muted, desaturated green.

color: #75B7A1;

Avoid bright neon teal.

Shadows

Dark mode should rely more on:

tonal separation

subtle borders

spacing

and less on large black shadows.

Images / illustrations

Where appropriate, slightly reduce image brightness and saturation rather than applying a heavy dark overlay.

6. Spacing & Layout

The interface should breathe.

Use a generous spacing system:

4
8
12
16
20
24
32
40
48
64

Prefer whitespace over borders.

A section should feel separated because there is space between it and the next section, not because every section has a box around it.

6.1 Screen Width

For mobile:

max-width: 480px;
margin-inline: auto;
padding-inline: 20px;

On larger screens, retain a comfortable reading width instead of stretching content across the entire viewport.

7. Cards & Surfaces

Cards should feel like pieces of paper sitting gently on the page.

7.1 Card Style

Default:

background: var(--surface);
border-radius: 20px;
border: 1px solid var(--border);

However, not every component needs a card.

Use three levels:

Level 1 — Open content

No border.

Used for:

journal entries

headings

simple lists

reading content

Level 2 — Soft surface

Very subtle background.

Used for:

secondary information

grouped controls

lightweight widgets

Level 3 — Raised card

White/light surface with subtle shadow.

Used for:

important actions

focus sessions

featured content

Do not stack cards inside cards unless necessary.

8. Header

The header should feel like the top of a journal page.

Avoid a large app-dashboard toolbar.

Example:

Good morning, Sarah

A little space for today.

The logo should be small and quiet.

Profile and notifications should remain visually secondary.

Do not surround every header item with a circle.

Use icon buttons only where the action needs emphasis.

9. Bottom Navigation

Keep the floating navigation, but make it quieter.

Home     Tools     Journal     Day     You

Use familiar icons.

Active state:

subtle tinted background

accent icon

optional short label

Avoid:

large glowing pills

excessive animation

heavy shadows

dramatic expansion

The navigation should disappear into the experience rather than dominate it.

10. Dashboard

The home screen should feel like a journal opening page, not a dashboard.

Suggested structure

Greeting

Good morning

What would make today feel a little better?

Today

A small collection of useful information.

For example:

2h 15m

screen time today

50 min

focused time

4

habits completed

These should not necessarily be three separate cards.

A single calm composition is preferable.

Focus

Take a little time for yourself

25:00

[Start]

Tasks

A few things for today

Review morning pages

Water the plants

Read for 10 minutes

Reading

Currently reading

Quiet: The Power of Introverts

Chapter 4 · 42% complete

11. Journal

The journal should be the visual heart of the application.

Header

Journal

Somewhere to put your thoughts.

Search

Use a quiet search field:

Search your thoughts...

Do not make the search bar look like a command palette.

Entries

Prefer an editorial reading layout over boxed database records.

Example:

TODAY

09:30 AM

Morning clarity after the green tea.

Finished the chapter on solitude. The quiet
hours of early dawn bring an unmatched
spaciousness.

#calm   #morning

The entry can use:

generous vertical spacing

subtle date separators

very light dividers

small timestamps

readable body text

Avoid putting every journal entry inside a heavy bordered card.

12. Journal Composer

The composer should feel like writing on paper.

Placeholder

What's on your mind?

Mood

Use a small set of expressive options:

Calm

Focused

Inspired

Reflective

Avoid technical selection controls.

Save

Prefer:

Save

rather than:

SAVE REFLECTION

The interaction should feel immediate and quiet.

13. Tools

Rename the concept internally from "plugin catalog" to Tools in the visible UI.

Header

Your tools

Small things that help you through the day.

Tool cards

Example:

Reading

Keep track of what you're reading.

Open

Habits

Notice the things you keep coming back to.

Open

Focus

Create a little uninterrupted time.

Open

Gita

Read, reflect, and return to a verse.

Open

Avoid:

OPEN PLUGIN →

Use:

Open

or simply an arrow icon.

14. Tracker

The tracker should feel like looking back through a personal diary.

Visible UI terminology:

Your day

Earlier

This morning

This afternoon

This evening

Focus

Reading

Movement

Rest

Avoid:

Telemetry

Events

Event stream

Metrics dashboard

Raw database

System activity

Diagnostic

Example

Your day

A quiet look at how your time unfolded.

Today

2h 15m
screen time

50 min
focused

4
habits

Earlier

9:30 AM
Morning light
Completed

9:05 AM
25 minutes of focus
Completed

8:35 AM
Reading
30 minutes

15. Data / Developer Views

Technical screens may exist, but they should be visually separated from the normal product experience.

Examples:

raw JSON

SQLite records

storage paths

logs

diagnostics

These are the only areas where monospace is appropriate.

Use a clear label:

Advanced

For inspecting V07's stored data.

Do not expose developer terminology throughout the normal app.

16. Profile & Settings

Use human language.

Header

Your space

Make V07 feel like yours.

Sections

Your profile

Storage

Notifications

Appearance

Sync

Advanced

Danger zone

Avoid:

PROFILE & SETTINGS

as a giant uppercase heading.

17. Buttons

Buttons should feel tactile but restrained.

Primary:

background: var(--accent);
color: white;
border-radius: 14px;

Secondary:

background: var(--surface-soft);
color: var(--text-primary);
border-radius: 14px;

Avoid turning every action into a pill.

Use pill shapes mainly for:

filters

small status indicators

compact tags

18. Icons

Use clean, simple vector icons.

Material Symbols Outlined or another consistent vector icon set is acceptable.

Icons should support the content rather than become decoration.

Avoid emoji as primary UI icons.

Emoji may appear inside journal content or mood selection where appropriate.

19. Motion

Motion should feel like paper moving gently.

Use:

200–300ms
ease-out

Prefer:

fade

small translation

gentle scale

subtle opacity changes

Avoid:

bouncing UI

excessive spring animations

glowing effects

dramatic morphing

constant motion

20. Accessibility & Ergonomics

Every interactive control should have at least:

40 × 40px

Prefer:

44 × 44px

Touch interactions should be comfortable on Android.

Respect:

env(safe-area-inset-top)
env(safe-area-inset-bottom)

Text should remain readable when system font scaling is increased.

Do not sacrifice readability to preserve a rigid visual layout.

21. Responsive Behavior

The design should remain editorial at every width.

Do not convert the mobile interface into a dense desktop dashboard.

Mobile

single-column

generous spacing

large readable text

simple navigation

Tablet

slightly wider reading column

optional two-column tool layout

journal remains focused

Desktop

centered content

generous margins

optional side navigation

journal content remains comfortably narrow

22. AI / Frontend Implementation Guardrails

These rules are especially important when an AI coding agent implements the design.

NEVER

introduce a monospace font for normal UI

convert headings to uppercase automatically

add borders to every component

create a card for every metric

use technical terminology in visible UI

add neon colors

create terminal-like panels

use ASCII-inspired visual elements

make every element a pill

turn the home screen into an analytics dashboard

add decorative gradients without purpose

use "SYSTEM", "MODULE", "TELEMETRY", "EVENT STREAM", or "PLUGIN" as visible user-facing language unless technically necessary

ALWAYS

prioritize whitespace

use Plus Jakarta Sans for normal UI

use sentence case

keep headings simple

use short conversational subheadings

make journal content the visual priority

use subtle surfaces instead of constant borders

keep colors muted

make dark mode feel like a quiet nighttime journal

use monospace only for genuinely technical data

preserve a calm reading rhythm

let typography and spacing create hierarchy

23. Final Visual Test

Before considering a screen complete, ask:

Does it look like a journal?

If yes, continue.

Does it look like a dashboard?

Remove cards, labels, borders, and metrics.

Does it look like a terminal?

Remove monospace, uppercase text, technical terminology, and code-like structures.

Does it feel crowded?

Increase whitespace before adding another component.

Does the heading sound robotic?

Rewrite it as a simple phrase.

Does dark mode look like a developer terminal?

Reduce contrast, remove neon colors, use warm dark green-gray surfaces, and soften the accent.

24. The Core V07 Rule

V07 should feel like a beautiful personal journal that happens to understand your data — not a data dashboard that happens to contain a journal.