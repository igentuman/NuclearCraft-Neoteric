---
navigation:
  title: Computer Integration
  icon: accelerator_port
  position: 5
---

# Computer Integration

Most NuclearCraft multiblocks can be read and driven from **ComputerCraft** (peripherals) or
**OpenComputers v2** (devices). The two APIs mirror each other: method names and return values
match, so a script ports between them by changing only how you acquire the handle.

These pages cover the accelerator family. For the full list across every block - reactors,
turbines, processors, kugelblitz - see the `COMPUTERS.md` reference shipped with the mod.

## Beam ports

The accelerator, ring accelerator, particle chambers and beam diverter all expose two beam-port
methods used to route particles at runtime:

- `getBeamPortsInfo()` → list | nil — one entry per beam port, sorted by position. Each entry is a
  table: `id` (int), `x` / `y` / `z` (int), `mode` (`"input"`, `"output"`, or `"disabled"`), and
  `particle` (a table with `energy` / `focus` / `amount` / `particle`, or nil).
- `setBeamPortMode(id, mode)` → boolean — set port `id` to `"input"`, `"output"`, or `"disabled"`
  (case-insensitive). Returns `false` if the multiblock is not formed or the arguments are invalid.

Both run on the server thread (`mainThread = true` on ComputerCraft, `synchronize = true` on
OpenComputers), so call them sparingly.

<SubPages icons={true} />
