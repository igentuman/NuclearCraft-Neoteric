package igentuman.nc.compat.computer;

import dan200.computercraft.api.lua.LuaFunction;
import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.block_entity.IBeamPort;
import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.multiblock.MultiblockHandler;
import li.cil.oc2.api.bus.device.object.Callback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BeamControllerApi<T extends MultiblockControllerBE> extends ControllerApi<T> {

    public BeamControllerApi(T be, ComputerErrors errors) {
        super(be, errors);
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final Map<Integer, Map<String, Object>> getBeamPortsInfo() {
        Map<Integer, Map<String, Object>> result = new HashMap<>();
        List<IBeamPort> ports = beamPorts();
        for (int i = 0; i < ports.size(); i++) {
            IBeamPort port = ports.get(i);
            BlockPos pos = port.getBlockPos();
            Map<String, Object> info = new HashMap<>();
            info.put("mode", port.beamPortMode().getSerializedName());
            info.put("x", pos.getX());
            info.put("y", pos.getY());
            info.put("z", pos.getZ());
            result.put(i + 1, info);
        }
        return result;
    }

    @LuaFunction(mainThread = true)
    @Callback
    public final String setBeamPortMode(int id, String mode) {
        List<IBeamPort> ports = beamPorts();
        if (id < 1 || id > ports.size()) throw error("Beam port id must be between 1 and " + ports.size());
        BeamPortMode target = beamPortMode(mode);
        return ports.get(id - 1).applyBeamPortMode(target, null).getSerializedName();
    }

    private List<IBeamPort> beamPorts() {
        List<IBeamPort> ports = new ArrayList<>();
        if (!(be.getLevel() instanceof ServerLevel level)) return ports;
        MultiblockHandler.MultiblockInstance instance = MultiblockHandler.getInstance(level, be.getBlockPos());
        if (instance == null || !instance.formed) return ports;
        instance.cache.getStructurePositions().stream().sorted().map(BlockPos::of)
                .filter(level::hasChunkAt)
                .map(level::getBlockEntity)
                .forEach(blockEntity -> {
                    if (blockEntity instanceof IBeamPort port && port.controller() == be) ports.add(port);
                });
        return ports;
    }

    private BeamPortMode beamPortMode(String mode) {
        String name = mode.toLowerCase(Locale.ROOT);
        for (BeamPortMode candidate : BeamPortMode.values()) {
            if (candidate.getSerializedName().equals(name)) return candidate;
        }
        throw error("Mode must be one of input, output, disabled");
    }
}
