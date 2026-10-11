# NuclearCraft Fission Reactor
![Keep it below meltdown.](block:nuclearcraft:fission_reactor_controller)

A serial port module can read the state of a formed fission reactor. Point it at the reactor controller or at any fission reactor port. The protocol supports both read and write operations.

When writing to the serial interface, four values are legal:
- `0` selects the stored energy level.
- `1` selects the stored heat level. This is the default mode.
- `2` selects the depletion progress of the current fuel.
- `3` selects the amount of fuel in the fuel slot, relative to a full stack.

Other values are ignored and keep the current mode.

When reading from the serial interface, a value in the range of [0, 100] is returned. It is the percentage of the selected quantity. 0 is returned while the reactor is not formed.
