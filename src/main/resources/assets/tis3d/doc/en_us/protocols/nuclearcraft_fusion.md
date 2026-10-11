# NuclearCraft Fusion Reactor
![Hotter than the sun.](block:nuclearcraft:fusion_reactor_core)

A serial port module can read the state of a formed fusion reactor. Point it at the fusion reactor core or at any of its proxy blocks. The protocol supports both read and write operations.

When writing to the serial interface, three values are legal:
- `0` selects the stored energy level.
- `1` selects the reactor heat level. This is the default mode.
- `2` selects the plasma efficiency.

Other values are ignored and keep the current mode.

When reading from the serial interface, a value in the range of [0, 100] is returned. It is the percentage of the selected quantity. 0 is returned while the reactor is not formed.
