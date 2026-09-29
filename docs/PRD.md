# Product Requirements Document (PRD)
## CarLot Desktop Inventory Application

| Field | Value |
| --- | --- |
| **Product name** | CarLot |
| **Document version** | 1.0 |
| **Status** | Draft — for alignment before implementation |
| **Source of truth** | Existing JavaDoc API (`Car`, `CarLot`, `CarLotMain`) dated 2021-07-16 |
| **Goal** | Ship a native desktop executable with a clear, standard inventory UI/UX that implements the documented CarLot domain |

---

## 1. Executive summary

CarLot is a used-car lot inventory tool. Today the documented product is a **console app** (`CarLotMain`) over an in-memory inventory (`CarLot`) of vehicles (`Car`), with optional persistence to `carlot.txt`.

This PRD defines a **desktop executable** that keeps the same domain rules and operations, but replaces the console with a **professional inventory UI**: browse/search inventory, add cars, sell cars, view insights (MPG/mileage/profit), and save/load inventory — without requiring the user to type menu numbers or remember identifiers by hand.

**Success looks like:** a lot attendant can open the app, manage inventory in under a minute per task, and trust that sold status, profit, and disk persistence behave exactly as the documented API specifies.

---

## 2. Current codebase reality

The workspace contains **generated JavaDoc only** (no `.java` sources). The documented system is:

| Class | Role |
| --- | --- |
| `Car` | Single vehicle for sale |
| `CarLot` | Inventory collection + operations + disk I/O |
| `CarLotMain` | Console UI entry point (`main`) |

**Implication for build:** we will implement (or re-implement) `Car` / `CarLot` to match the documented contracts, then replace `CarLotMain` with a graphical shell packaged as an executable.

---

## 3. Problem statement

Console menus are slow, error-prone, and hard to scan when inventory grows. Staff need:

- Instant visibility of which cars are **available vs sold**
- Forms with validation instead of free-text console prompts
- One-click access to “best MPG”, averages, and total profit
- Reliable save/load of `carlot.txt` without remembering file commands

---

## 4. Goals and non-goals

### Goals

1. Deliver a **double-clickable desktop executable** (macOS primary; Windows/Linux nice-to-have).
2. Implement **100% of documented CarLot operations** behind the UI.
3. Provide a **standard inventory UX**: table + detail + primary actions + confirmation for destructive/irreversible flows (sell).
4. Persist inventory to **`carlot.txt`** (same constant as docs: `CARLOT_INVENTORY_LOCATION`).
5. Make validation and errors visible and recoverable (no silent failures).

### Non-goals (v1)

- Multi-user / multi-lot / cloud sync
- Customer-facing website or mobile app
- Photos, VIN decoding, financing, CRM
- Accounting export beyond on-screen profit totals
- Editing sold cars’ historical sale data (unless we later add an explicit “unsell” — out of scope for v1)

---

## 5. Users and jobs-to-be-done

| Persona | Primary job |
| --- | --- |
| **Lot attendant** | Add cars, look up by ID, mark as sold with actual price |
| **Lot manager** | Scan inventory, compare MPG/mileage/price, see total profit |
| **Owner / student operator** | Open app, load last save, work session, save and quit |

**JTBD:** When I acquire or sell a vehicle, I want to update the lot inventory quickly so the list always reflects what we can sell and what we’ve earned.

---

## 6. Domain model (must match docs)

### 6.1 `Car` fields

| Field | Type | Rules / notes |
| --- | --- | --- |
| `id` | String | Unique identifier; **must not contain spaces** |
| `mileage` | int | Mileage when added to inventory |
| `mpg` | int | Miles per gallon |
| `cost` | double | Amount paid to acquire the car |
| `salesPrice` | double | Asking price (may differ from final sale) |
| `sold` | boolean | Available vs sold |
| `priceSold` | double | Actual sale price (set on sell) |
| `profit` | double | `priceSold - cost` (set on sell) |

**Comparators (for sorting / insights):**

- `compareMPG(other)` — negative / 0 / positive
- `compareMileage(other)` — negative / 0 / positive
- `compareSalesprice(other)` — same pattern for asking price

**Sell behavior (`sellCar(priceSold)`):** mark sold, store `priceSold`, compute `profit = priceSold - cost`.

### 6.2 `CarLot` operations

| Operation | Behavior |
| --- | --- |
| `addCar(id, mileage, mpg, cost, salesPrice)` | Append new car to inventory |
| `findCarByIdentifier(id)` | Return matching `Car` or `null` |
| `sellCar(id, priceSold)` | Sell by ID; throw if ID missing |
| `getInventory()` / `setInventory(...)` | Access / replace list |
| `getCarsInOrderOfEntry()` | Copy in entry order |
| `getCardsSortedByMPG()` | Copy sorted **highest → lowest MPG** |
| `getCarWithBestMPG()` | Single car with highest MPG |
| `getCarWithHighestMileage()` | Single car with highest mileage |
| `getAverageMpg()` | Average MPG across inventory |
| `getTotalProfit()` | Sum of profit for **sold** cars |
| `saveToDisk()` | Write inventory to `carlot.txt` |
| `loadFromDisk()` | Read inventory from `carlot.txt` |

**Persistence file:** `carlot.txt` (constant `CARLOT_INVENTORY_LOCATION`).

---

## 7. Product vision — executable + UI

### 7.1 Delivery form

- **Primary:** macOS app bundle / `.app` or single runnable binary the user can open without a terminal.
- **Secondary:** cross-platform jar + installer, or Electron/Tauri wrapper if we choose a web UI stack.
- App should launch to a **ready inventory screen** (auto-load `carlot.txt` if present; empty state if not).

### 7.2 Recommended architecture (decision pending — see §12)

Keep domain logic separate from UI:

```
┌─────────────────────────────────────┐
│  UI layer (desktop window)          │
│  Inventory table · forms · dialogs  │
└─────────────────┬───────────────────┘
                  │
┌─────────────────▼───────────────────┐
│  Domain: Car + CarLot               │
│  (match JavaDoc contracts)          │
└─────────────────┬───────────────────┘
                  │
┌─────────────────▼───────────────────┐
│  Persistence: carlot.txt            │
└─────────────────────────────────────┘
```

UI never owns business rules for profit or sell; it calls `CarLot` / `Car`.

---

## 8. Functional requirements

### FR-1 — Inventory list (home)

- Show all cars in a sortable table.
- Columns (minimum): ID, Mileage, MPG, Cost, Asking price, Status (Available / Sold), Price sold, Profit.
- Default order: **order of entry**.
- Optional sort: by MPG (desc), mileage, sales price, status.
- Row selection opens detail / enables Sell.
- Empty state: short message + primary **Add car** CTA.
- Visual distinction for sold vs available (badge or muted row).

### FR-2 — Add car

- Form fields: ID, Mileage, MPG, Cost, Sales price.
- Validate before submit:
  - ID required, no spaces, unique in inventory
  - Mileage ≥ 0, MPG > 0
  - Cost ≥ 0, Sales price ≥ 0
- On success: add to inventory, select new row, toast/snackbar confirmation.
- On failure: inline field errors; do not clear valid fields.

### FR-3 — Find / search

- Search box filters table by ID (substring match OK for UX; exact match still available via “Find”).
- Exact find (`findCarByIdentifier`) used for Sell confirmation when user types an ID.

### FR-4 — Sell car

- Available only for cars with `sold == false`.
- Dialog: show ID, asking price, cost; input **Price sold**.
- Confirm action (irreversible in v1).
- On success: update status, price sold, profit; refresh totals.
- If ID not found: clear error message (maps to `IllegalArgumentException`).

### FR-5 — Insights panel / dashboard strip

Always-visible or one-click panel showing:

| Insight | Source API |
| --- | --- |
| Average MPG | `getAverageMpg()` |
| Best MPG car | `getCarWithBestMPG()` |
| Highest mileage car | `getCarWithHighestMileage()` |
| Total profit (sold) | `getTotalProfit()` |
| Inventory count | derived from list |
| Available count | derived |

Include a **View sorted by MPG** mode that uses `getCardsSortedByMPG()`.

### FR-6 — Persistence

- **Load on startup** from `carlot.txt` if file exists; if missing, start empty (no crash).
- **Save** via menu/toolbar and optionally on quit (prompt if dirty).
- Surface I/O errors (`FileNotFoundException` / permission) in a dialog with path shown.
- File format: defined in implementation plan; must round-trip all `Car` fields including sold state.

### FR-7 — Application chrome

- Window title: **CarLot**
- Menu / toolbar: Add · Sell · Save · Load · Quit (or equivalent)
- Keyboard: common shortcuts (e.g. ⌘N Add, ⌘S Save, ⌘F Focus search) on macOS
- Quit confirms if unsaved changes

---

## 9. UI / UX requirements (standard, high quality)

### 9.1 Design principles

1. **Inventory-first** — the table is the product; secondary panels support it.
2. **Progressive disclosure** — insights and sell flow appear when needed, not as clutter.
3. **Direct manipulation** — select a row → act; avoid “type menu option 3”.
4. **Forgiving input** — validate early; money fields accept currency-style input; show formatted currency in display.
5. **Status clarity** — Available vs Sold is always obvious at a glance.
6. **Trust** — confirm irreversible sell; show profit calculation preview before confirm (`priceSold − cost`).

### 9.2 Layout (v1)

```
┌──────────────────────────────────────────────────────────┐
│ CarLot                          [Search…]  [Add] [Save]  │
├──────────────┬───────────────────────────────────────────┤
│ Insights     │  Inventory table                          │
│ Avg MPG      │  ID │ Mi │ MPG │ Cost │ Ask │ Status …   │
│ Best MPG     │  ───────────────────────────────────────  │
│ Hi mileage   │  …                                        │
│ Profit $     │                                           │
│              ├───────────────────────────────────────────┤
│              │  Detail / actions for selected car        │
│              │  [Sell…]                                  │
└──────────────┴───────────────────────────────────────────┘
```

- Desktop width target: ≥ 1100px comfortable; usable down to ~900px.
- Do **not** mimic a marketing landing page; this is a **utility / ops app**.
- Prefer a calm, high-contrast UI: clear type hierarchy, consistent spacing, readable numbers (tabular figures for money/mileage).
- Avoid decorative clutter: no emoji, no neon glow, no purple-gradient “AI default” look.

### 9.3 Interaction patterns

| Pattern | Usage |
| --- | --- |
| Primary button | Add car, Confirm sell |
| Secondary button | Cancel, Load |
| Destructive / irreversible | Sell confirm (distinct styling) |
| Modal dialog | Add form, Sell form, Error |
| Toast | Soft success after add/save |
| Empty state | Illustration optional; text + CTA required |
| Loading | Brief busy cursor on disk I/O |

### 9.4 Accessibility

- Keyboard navigation through table and forms
- Labels on every input; errors associated with fields
- Contrast meeting WCAG AA for text and status badges
- Focus visible; dialogs trap focus appropriately

### 9.5 Content / copy tone

Plain operational English:

- “Add car to inventory”
- “Mark as sold”
- “No inventory file found — starting empty.”
- “Could not save to carlot.txt”

---

## 10. Non-functional requirements

| Area | Requirement |
| --- | --- |
| Performance | Inventory of ≤ 5,000 cars remains snappy (filter/sort < 100ms perceived) |
| Reliability | Crash on bad file → recoverable empty/partial load with error; never corrupt silently |
| Portability | Runs without user-installed JDK if we ship a bundled runtime (or clear install notes) |
| Data | Local only; no network required for core features |
| Maintainability | Domain tests cover Car/CarLot independently of UI |

---

## 11. User flows

### Flow A — First launch

1. Open executable  
2. No `carlot.txt` → empty inventory + insights zeros  
3. User clicks **Add car**, fills form, saves  
4. User clicks **Save** → `carlot.txt` created  

### Flow B — Daily sell

1. Launch → auto-load  
2. Search or select car  
3. **Sell** → enter price sold → preview profit → confirm  
4. Status updates; total profit updates  
5. Save (or save on quit)  

### Flow C — Manager review

1. Open insights strip  
2. Sort by MPG / view best MPG & highest mileage  
3. Scan available inventory asking prices  

---

## 12. Technical options (to decide before coding)

| Option | Pros | Cons |
| --- | --- | --- |
| **A. Java + JavaFX** | Matches original domain language; solid desktop; jpackage → `.app` / `.exe` | Need to implement missing sources |
| **B. Java Swing** | Simple, classic | Dated look unless heavily themed |
| **C. Python + CustomTkinter / PyQt + PyInstaller** | Fast UI iteration | Re-port domain from JavaDoc |
| **D. Electron / Tauri + web UI** | Excellent UI polish | Heavier / more moving parts for a small inventory app |

**Recommendation for this project:** **Option A (Java + JavaFX + jpackage)** — preserves the documented API, keeps `Car`/`CarLot` as first-class Java types, and produces a real executable with a modern-enough standard UI toolkit.

---

## 13. Acceptance criteria (v1)

- [ ] Executable launches without terminal usage
- [ ] User can add a car with validated fields; duplicate/spaced IDs rejected
- [ ] Inventory table shows all documented fields relevant to operators
- [ ] User can sell an available car; profit = priceSold − cost; sold cars cannot be sold again
- [ ] Selling unknown ID shows an error (no crash)
- [ ] Insights match `CarLot` APIs (avg MPG, best MPG, highest mileage, total profit)
- [ ] Sort by MPG highest→lowest matches `getCardsSortedByMPG()`
- [ ] Save/load round-trips inventory through `carlot.txt`
- [ ] Unsaved-change prompt on quit
- [ ] Empty and error states are clear
- [ ] Keyboard + basic a11y checks pass for primary flows

---

## 14. Milestones

| Phase | Deliverable |
| --- | --- |
| **0 — Align** | Approve this PRD + choose tech stack (default JavaFX) |
| **1 — Domain** | Implement `Car` + `CarLot` + unit tests from JavaDoc |
| **2 — Persistence** | `saveToDisk` / `loadFromDisk` + file format + tests |
| **3 — UI shell** | Window, table, empty state, insights strip |
| **4 — Flows** | Add + Sell + Search + validation + confirmations |
| **5 — Package** | `jpackage` (or chosen packager) → macOS executable |
| **6 — Polish** | Shortcuts, dirty-state, copy, visual pass |

---

## 15. Open questions

1. Confirm **tech stack** (JavaFX vs other).
2. Should **sold cars remain in the table** forever (as docs imply) or be filterable/hideable? *(Recommend: remain + filter Available / Sold / All.)*
3. Exact **text file format** for `carlot.txt` (CSV vs delimited lines) — not specified in JavaDoc.
4. Should Save be **automatic after every mutation**, or manual only? *(Recommend: manual + prompt on quit; optional auto-save later.)*
5. Target OS for first executable: **macOS only**, or Windows too?

---

## 16. Appendix — API surface checklist

### Car
- [ ] Constructors: default; `(id, mileage, mpg, cost, salesPrice)`
- [ ] `sellCar(priceSold)`
- [ ] `compareMPG` / `compareMileage` / `compareSalesprice`
- [ ] Getters/setters for all fields
- [ ] `toString()` human-readable

### CarLot
- [ ] `addCar` / `findCarByIdentifier` / `sellCar`
- [ ] Order / sort / best MPG / highest mileage / average / total profit
- [ ] `saveToDisk` / `loadFromDisk`
- [ ] `CARLOT_INVENTORY_LOCATION = "carlot.txt"`

### UI (replaces CarLotMain console)
- [ ] All console features exposed as GUI actions
- [ ] Packaged executable entry point

---

*End of PRD v1.0 — ready for review and implementation planning.*
