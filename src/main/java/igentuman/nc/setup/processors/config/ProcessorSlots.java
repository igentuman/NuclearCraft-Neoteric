package igentuman.nc.setup.processors.config;

import java.util.ArrayList;
import java.util.List;

public class ProcessorSlots {

    public static int[] inputSlotPos = new int[] {31, 30};
    public static int[] outputSlotPos = new int[] {115, 30};
    public static int margin = 20;

    private int input_items;
    private int input_fluids;
    private int output_items;
    private int output_fluids;

    //order for slots: input fluids, input items, output fluids, output items;
    private List<int[]> slotPositions;

    public int getInputItems() {
        return input_items;
    }

    public ProcessorSlots setInputItems(int input_items) {
        this.input_items = input_items;
        return this;
    }

    public int getInputFluids() {
        return input_fluids;
    }

    public ProcessorSlots setInputFluids(int input_fluids) {
        this.input_fluids = input_fluids;
        return this;
    }

    public int getOutputItems() {
        return output_items;
    }

    public ProcessorSlots setOutputItems(int output_items) {
        this.output_items = output_items;
        return this;
    }

    public int getOutputFluids() {
        return output_fluids;
    }

    public ProcessorSlots setOutputFluids(int cnt) {
        this.output_fluids = cnt;
        return this;
    }

    //if odd - output in one line, if even output as grid
    public List<int[]> getSlotPositions() {
        if(slotPositions == null) {
            slotPositions = new ArrayList<>();
            int x = inputSlotPos[0];
            int y = inputSlotPos[1];
            if((input_items+input_fluids) % 2 == 0) {
                for (int i = 0; i < input_fluids+input_items; i++) {
                    int itemX = x + (i / 2) * margin;
                    int itemY = y + (i % 2) * margin;
                    slotPositions.add(new int[]{itemX, itemY});
                }
            } else {
                for (int i = 0; i < input_fluids+input_items; i++) {
                    if(input_fluids+input_items == 1) {
                        x += margin;
                    }
                    int itemX = x + margin*i;
                    int itemY = y + margin / 2;
                    slotPositions.add(new int[]{itemX, itemY});
                }
            }
            x = outputSlotPos[0];
            y = outputSlotPos[1];
            if((output_items+output_fluids) % 2 == 0) {
                for (int i = 0; i < output_fluids+output_items; i++) {
                    int itemX = x + (i / 2) * margin;
                    int itemY = y + (i % 2) * margin;
                    slotPositions.add(new int[]{itemX, itemY});
                }
            } else {
                for (int i = 0; i < output_fluids+output_items; i++) {
                    int itemX = x + margin*i;
                    int itemY = y + margin / 2;
                    slotPositions.add(new int[]{itemX, itemY});
                }
            }
        }
        return slotPositions;
    }

    public int slotsCount()
    {
        return input_items+input_fluids+output_items+output_fluids;
    }

    //order for slots: input fluids, input items, output fluids, output items;
    public ProcessorSlots setSlotPositions(List<int[]> slotPositions) {
        this.slotPositions = slotPositions;
        return this;
    }

    public String getSlotType(int id)
    {
        if(id < input_fluids) {
            return "fluid_in";
        }
        if(id < input_fluids+input_items) {
            return "item_in";
        }
        if(id < input_fluids+input_items+output_fluids) {
            return "fluid_out";
        }
        return "item_out";
    }

    public int[] getSlotPos(int id)
    {
        return getSlotPositions().get(id);
    }
}
