---
navigation:
  title: Universal Pipe
  parent: machines.md
  icon: pipe
item_ids:
  - nuclearcraft:pipe
  - nuclearcraft:pipe_connector
---

# Universal Pipe

<Row>
  <BlockImage id="pipe" />
  <BlockImage id="pipe_connector" />
</Row>

The **Universal Pipe** is a single conduit that carries items, fluids, and energy through one network at the same time. Pipes only move the network around; a **Pipe Connector** is what actually talks to an adjacent block.

## Networks

Connect pipes together and they merge into one network. Every connector on that network shares the same items, fluids, and energy. Place a connector against any inventory, tank, or energy machine to interface with it, then open its GUI to configure it.

## Transport Media

A fresh connector transports nothing. Tick **Items**, **Fluids**, or **Energy** to permit each medium independently — all three stay inert until authorized. (The safety interlock nobody reads.)

## Direction

Cycle the transfer direction:

- **Disabled** — no transfer.
- **Pull** — draw out of the attached block.
- **Push** — feed into the attached block.
- **Auto** — let the block decide.

## Redstone

Set **Redstone: Ignored** to always run, or **Redstone: Required** to run only while a signal is present.
