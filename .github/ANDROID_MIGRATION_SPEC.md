# Native Android Library Manager

## Problem Statement

The Personal AI Library Manager is a local Streamlit application. It is not a practical native mobile experience: it has no Android-native offline data store, camera/photo-picker workflow, Android app lock, local loan notifications, portable Android backup format, or responsive phone/tablet navigation. The owner needs a private, offline-first Android application that imports the existing catalogue and preserves its books, tags, collections, wishlist, loans, cover images, and historical timestamps.

## Solution

Build a native Kotlin Android application using Jetpack Compose and Room under the existing repository's Android module. The application is single-device and fully offline, with no internet permission, analytics, remote diagnostics, or Android Auto Backup. It is released as a privately installed, permanently signed APK using application ID `com.venkateshgowda.personallibrary` and supports Android 10/API 29 and later.

The app uses five primary destinations: Dashboard, Library, Search, Loans, and More. It retains the `Venkatesh Gowdas Personal Library` title and VG identity, with an optional editable library profile in Settings. It supports light, dark, and system-default themes; English (India); device-localized date and number formatting; and INR as the default currency.

## User Stories

1. As a library owner, I want to install a private signed APK, so that I can use my catalogue without publishing it to an app store.
2. As a library owner, I want to use the app without an internet connection, so that my data remains local and usable anywhere.
3. As a first-time user, I want onboarding that offers import or a new catalogue, so that I can choose the correct starting point.
4. As an existing Streamlit user, I want to import a legacy archive into an empty Android catalogue, so that I retain my library history.
5. As an importer, I want strict archive validation before any data changes, so that corrupted or malicious files cannot damage my catalogue.
6. As an importer, I want missing legacy covers reported as warnings, so that valid books are still recovered.
7. As an importer, I want legacy IDs mapped to Android IDs while preserving valid timestamps, so that relationships and history remain accurate.
8. As an importer, I want legacy timestamps interpreted as Asia/Kolkata time, so that their historical meaning is retained.
9. As an importer, I want legacy prices interpreted as INR, so that my investment total remains accurate.
10. As a library owner, I want an app lock based on biometric authentication or device credential, so that no separate password can be lost.
11. As a library owner, I want a five-minute default lock timeout with configurable alternatives, so that the app balances privacy and convenience.
12. As a library owner, I want recent-app previews hidden while the app is backgrounded, so that personal data is not exposed in the app switcher.
13. As a library owner, I want to see total books and investment on the dashboard, so that I can understand my collection at a glance.
14. As a library owner, I want to see active, overdue, and due-soon loans, so that I can follow up on books lent out.
15. As a library owner, I want to see wishlist estimated cost on the dashboard, so that I can plan future purchases.
16. As a library owner, I want recent books and compact category and reading-status summaries, so that I can review the current catalogue.
17. As a library owner, I want to add books with title, author, category, price, dates, publisher, ISBN, book barcode, language, rating, reading status, favourite status, review, notes, tags, and collections, so that records retain existing detail.
18. As a library owner, I want title and author to be required and price to default to INR 0, so that records are compatible with my current catalogue.
19. As a library owner, I want ISBNs normalized and unique when supplied, so that duplicate editions are prevented.
20. As a library owner, I want a warning for a similar title and author when ISBN is blank, so that I can avoid accidental duplicates while allowing multiple copies or editions.
21. As a library owner, I want to scan an ISBN or a library inventory barcode with my camera, so that the corresponding identifier is entered without manual typing while the app remains offline.
21. As a library owner, I want to edit book details, so that the catalogue stays current.
22. As a library owner, I want permanent book deletion confirmed and to remove related images, loans, tags, and collection links, so that storage and data remain consistent.
23. As a library owner, I want to select cover images from the Android photo picker or capture them with the camera, so that cover photos are convenient to add.
24. As a library owner, I want images resized to 1600 px and capped at 2 MB, so that backups remain manageable.
25. As a library owner, I want up to five newly added images per book, so that book records remain focused.
26. As an importer, I want all valid legacy images retained in their original order, so that migration is not lossy.
27. As a library owner, I want to search titles, authors, ISBNs, categories, tags, and collections, so that I can quickly find a book.
28. As a library owner, I want filters for reading status, favourite, category, tag, and collection and useful sort orders, so that I can browse large catalogues.
29. As a library owner, I want hybrid fuzzy search for title and author on queries of at least three characters, so that typos still find relevant books.
30. As a library owner, I want exact and partial matches before fuzzy matches and configurable 60-90 similarity thresholds, so that search results remain predictable.
31. As a library owner, I want to organize books using tags and collections, so that my catalogue can reflect my own system.
32. As a library owner, I want tag and collection deletion to remove only assignments after confirmation, so that books are never deleted accidentally.
33. As a library owner, I want to rename tags and collections and edit collection descriptions, so that organization can evolve.
34. As a library owner, I want to maintain a wishlist with priority, expected price, date, status, and notes, so that planned purchases are organized.
35. As a library owner, I want to review a prefilled book form before converting a wishlist item to a purchase, so that the resulting catalogue record is accurate.
36. As a lender, I want to record borrower, contact, borrowed date, expected return date, and notes, so that loan history is complete.
37. As a lender, I want one active loan per book record and derived Lent, Overdue, and Returned statuses, so that loan state cannot contradict its dates.
38. As a lender, I want date validation for loan and return dates, so that historical records remain valid.
39. As a lender, I want configurable local reminders three days before due date, on the due date, and weekly while overdue, so that I remember follow-ups.
40. As a privacy-conscious lender, I want notifications to be private by default, so that lock screens do not reveal borrower or book details.
41. As a library owner, I want to create one encrypted portable backup archive through the Android file picker, so that I can explicitly keep my data safe.
42. As a library owner, I want AES-256 backup encryption with an unrecoverable user passphrase, so that shared archive files remain private.
43. As a library owner, I want backup restore to preview counts, create a safety backup, and replace the catalogue only after confirmation, so that recovery is predictable.
44. As a library owner, I want archive limits and path traversal protection, so that imports and restores cannot exhaust storage or write outside app storage.
45. As a phone user, I want ergonomic portrait and landscape layouts, so that the app works naturally in everyday use.
46. As a tablet user, I want optimized responsive layouts and keyboard navigation, so that the app is useful on large screens.
47. As an accessibility user, I want TalkBack labels, logical focus order, 48 dp touch targets, high contrast, and 200% font scaling support, so that the app remains usable.
48. As a library owner, I want milestone-two reports for collection, spending, ratings, loans, and wishlist summaries, so that I can analyze the catalogue.
49. As a library owner, I want milestone-two CSV and XLSX export for books and wishlist data, so that I can save or share records through Android's system picker.

## Implementation Decisions

- Keep the existing Python Streamlit application intact as the legacy-data source.
- Add an independent Android Gradle project using Kotlin, Jetpack Compose, Room, DataStore, AndroidX Biometric, WorkManager, and local Android storage.
- Use Room-owned Android IDs. Import logic maps legacy identifiers before creating relational records.
- Store money in integer paise and associate values with INR on legacy import.
- Store date-only values without timezone conversion. Convert valid legacy timestamp values from Asia/Kolkata local time to UTC instants for Android storage.
- Model book records as title/edition records with at most one active loan. Physical-copy tracking is deferred.
- Require title, author, and non-negative price. Allow INR 0 for unknown or free prices. Normalize ISBN-10/ISBN-13 input and enforce uniqueness only for non-empty ISBN values.
- Provide an offline CameraX and ML Kit barcode scanner in Add Book. Accept EAN-13, EAN-8, UPC-A, UPC-E, Code 128, and ITF. A valid ISBN-10/ISBN-13 is normalized into the ISBN field and checked against local records; a non-ISBN inventory value is stored in the separate optional Book barcode field. The scanner must provide runtime camera permission handling, flashlight control, and manual-entry fallback. It must not retrieve online book metadata.
- Provide local partial matching across catalogue metadata and hybrid fuzzy matching for title and author. Fuzzy matching is enabled at three characters and defaults to score 70.
- Limit newly added book images to five, validate JPEG/PNG/WebP, resize to a 1600 px maximum dimension, and store at no more than 2 MB per image. Legacy imports retain all valid images.
- Require legacy archives to contain root-level `library.db` and `book_covers/`. Map legacy database references from `uploads/book_covers/` into the root-level covers directory.
- Permit legacy import only while the Android catalogue is empty. Do not provide merging.
- Validate ZIP archives in private temporary storage. Reject corrupt databases, unsupported schemas, path traversal entries, duplicate ISBNs, unreadable images, archives over 500 MB compressed, data over 1 GB extracted, entries over 20 MB, and archives with more than 10,000 entries.
- Android-created backups are versioned encrypted ZIP archives containing a manifest, JSON catalogue/settings data, and cover files. Restore replaces existing data only after confirmation and a safety backup.
- Do not use Android Auto Backup, network APIs, analytics, external diagnostics, or `INTERNET` permission.
- Apply biometric/device credential app locking on cold launch and after a configurable background timeout. Default timeout is five minutes; alternatives are immediate, 15 minutes, and never.
- Hide content in the app switcher while backgrounded but permit normal screenshots in the unlocked app.
- Use a five-item primary navigation model: Dashboard, Library, Search, Loans, and More. Put tags, collections, wishlist, and add-book operations in Library; put reports, exports, backup/restore, and settings in More.
- Provide light, dark, and system theme choices. Default language is English (India), with device-localized dates and numbers.
- Use a release signing configuration loaded only from untracked local credentials and a permanent locally generated keystore. Never commit keys or passwords.

## Testing Decisions

- The primary seam is the repository/import-export boundary: tests must verify observable records, relationships, validation failures, and archive output rather than Room or Compose internals.
- Unit tests cover book validation, ISBN normalization and uniqueness, duplicate warnings, price conversion, fuzzy ranking, loan-state derivation, date validation, reminder scheduling, legacy mapping, archive validation, backup encryption, and restore safety behavior.
- Room integration tests cover transactional imports, relationship mapping, replacement restore, image-record cleanup, and derived dashboard metrics.
- Compose UI tests cover onboarding, five-destination navigation, book entry, search and filtering, wishlist conversion, loan return, confirmation dialogs, settings, and empty states.
- Responsive UI tests cover phone portrait, phone landscape, and tablet layouts, including font scaling and keyboard navigation.
- Manual release validation runs on at least one Android 10+ phone and one Android 10+ tablet and verifies camera capture, photo picker, biometric/device credential fallback, file-provider sharing, private notification content, legacy import, encrypted backup/restore, and signed APK installation.
- The legacy Streamlit test suite is prior art for validation and service-level behavior; Android tests reproduce its user-visible business rules without coupling to its implementation.

## Out of Scope

- Google Play Store publication.
- Multi-device synchronization, cloud accounts, web synchronization, and all network metadata lookup.
- Online ISBN metadata services, cover downloads, and any network-based barcode lookup.
- PDF export and printing.
- Android backup service integration.
- Separate app passwords and local database encryption.
- Merge imports/restores.
- Trash or undo for deleted books.
- Physical-copy inventory identities and copy quantities.
- Additional application languages.

## Further Notes

- Milestone 1 covers onboarding, security, dashboard, books, images, search including fuzzy matching, tags, collections, wishlist conversion, loans, local reminders, legacy import, encrypted backup/restore, and accessibility-responsive design.
- Milestone 2 covers reports plus CSV/XLSX exports through the Android file picker/share sheet.
- The release gate requires automated unit, Room, and Compose UI tests; manual phone/tablet validation; and a signed APK that completes legacy import, Android restore, image workflows, authentication, and notification checks.
- The desired tracker label is `ready-for-agent`. Publication is pending because GitHub CLI is not installed in this workspace.

## Today's Additions

### Home Dashboard

- Horizontal Home rails, including Continue Reading, Recently Added, and Books by Category, must provide visible left and right chevron controls in addition to swipe scrolling.
- Chevron controls must use smooth animated scrolling, have TalkBack labels, and be disabled when a rail cannot be scrolled in the selected direction.

### Session Menu

- Tapping the app logo in the top app bar must open an account and application menu.
- The menu must provide Switch user, Sign out, and Exit actions.
- Switch user must open sign-in while preserving the previous session until the user cancels. Sign out must clear the active session. Exit must retain a confirmation dialog and close the app task only after confirmation.

### Library Management

- The Libraries management dialog must allow an authorized user to edit a library name, description, and owner field.
- Library names remain unique and a name and owner are required.
- Library deletion must require confirmation and must describe the affected data.
- Deleting a library must remove its books, dependent loan history, image/assignment records, and wishlist items transactionally.
- The final remaining library must not be removable. When an active library is removed, the app must select a remaining library.

### Collaboration, Roles, and Permissions

The application must support library-scoped collaboration. A user account can have a separate membership and role in each library. Permissions are granted through a centrally defined RBAC matrix and must be evaluated from the active library membership, not from a global UI flag.

Supported roles:

| Role | Capabilities |
| --- | --- |
| Owner | Full library access; settings; books, categories, tags, import/export; users; reports; ownership transfer; library deletion. |
| Admin | Manage books, categories, tags, user memberships other than the Owner, membership requests, and reports. Cannot transfer ownership or delete the library. |
| Librarian | Add and edit books, manage loans and book locations, and view reports. Cannot manage users. |
| Member | View/search books, add personal notes and ratings, manage a personal wishlist, and borrow books. Cannot edit library book records. |
| Guest | Read-only access with limited search. Cannot borrow or modify data. |

Required access-control rules:

- Owner is protected from role removal or membership removal until ownership is transferred.
- Users cannot remove themselves accidentally.
- Only users with Manage Users may invite, change roles, remove members, or decide membership requests.
- Only Owner can transfer ownership or delete a library.
- Delete controls must only be available to roles holding the applicable delete permission.
- Role checks must protect both visible controls and destructive confirmation actions.
- Existing legacy Administrator accounts must migrate to Owner; legacy User accounts must migrate to Member.

### User Roles and Permissions Management

- Provide a Material 3 Manage Users experience reachable from More for authorized roles.
- List library members with avatar, display name, email/username, joined date, colour-coded role badge, books added, books borrowed, and activity summary.
- Support member search, role filtering, and sorting by name.
- Support confirmation dialogs for promotion, demotion, role removal, and member removal.
- Include a permissions dashboard that displays the capabilities granted by every role.
- Include membership requests separated into Pending, Approved, and Rejected states, with Admin/Owner approval and rejection actions.
- Include an activity audit log for user added/removed, role changed, book added/deleted, and book borrowed/returned events.
- The Create User form must require username, display name, and a password of at least six characters. Required labels must display `*`, and missing values must produce field-specific validation messages.
- Usernames must be unique without regard to letter case. Attempting to create an existing username must retain the form and show a modal error explaining that the user already exists and a different username is required.

### Invitations

- Authorized users must be able to invite collaborators by email, shareable invite code, shareable invitation link, and QR-code invitation.
- Invitations must be library-scoped, have a pending state, and record the invite action in the audit log.
- Sharing must use Android system sharing or email intents and must not require an internet permission.

### Data Architecture

- Room must model Book data including optional ISBN and barcode fields; User profile data; Library; Role; Permission; LibraryMembership; MembershipRequest; and AuditLog entities.
- Add safe Room migrations for all collaboration fields and tables, retaining existing users and libraries.
- Current schema version is 19. Migration 18 to 19 adds the nullable `barcode` column to `books` and must be registered in both the application database and the loan-reminder worker database builders.
- Use a repository as the authoritative boundary for invitations, request decisions, membership changes, role changes, ownership transfer, and audit logging.
- Provide a lifecycle-aware ViewModel for role-management actions and UI state.
- The module must remain offline-first, preserve light/dark themes, and support responsive phone and tablet layouts.