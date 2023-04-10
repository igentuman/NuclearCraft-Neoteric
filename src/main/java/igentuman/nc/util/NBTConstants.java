package igentuman.nc.util;

/**
 * Class for storing constants that are used in various NBT related storage, to reduce the chances of typos
 */
public final class NBTConstants {

    private NBTConstants() {
    }

    //Ones that also are used for interacting with forge/vanilla
    public static final String BASE = "Base";
    public static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    public static final String COUNT = "Count";
    public static final String CUSTOM_NAME = "CustomName";
    public static final String ENCHANTMENTS = "Enchantments";
    public static final String ID = "id";
    public static final String X = "x";
    public static final String Y = "y";
    public static final String Z = "z";

    //Server to Client specific sync NBT tags
    public static final String CLIENT_NEXT = "clientNext";
    public static final String CLIENT_PREVIOUS = "clientPrevious";
    public static final String CURRENT_ACCEPTORS = "acceptors";
    public static final String CURRENT_CONNECTIONS = "connections";
    public static final String HAS_STRUCTURE = "hasStructure";
    public static final String INVENTORY_ID = "inventoryID";
    public static final String LOWER_VOLUME = "lowerVolume";
    public static final String NETWORK = "network";
    public static final String RENDERING = "rendering";
    public static final String RENDER_LOCATION = "renderLocation";
    public static final String SCALE = "scale";
    public static final String SCALE_ALT = "scaleAlt";
    public static final String TAG = "tag";
    public static final String VALVE = "valve";
    public static final String VOLUME = "volume";

    //Generic constants
    public static final String ACTIVE_STATE = "activeState";
    public static final String ALLOW_DEFAULT = "allowDefault";
    public static final String AGE = "age";
    public static final String AMOUNT = "amount";
    public static final String BOXED_CHEMICAL = "boxedChemical";
    public static final String CACHE = "cache";
    public static final String CHANCE = "chance";
    public static final String CHEMICAL_TYPE = "chemicalType";
    public static final String CHUNK_SET = "chunkSet";
    public static final String COLOR = "color";
    public static final String COMPONENT_CONFIG = "componentConfig";
    public static final String COMPONENT_EJECTOR = "componentEjector";
    public static final String COMPONENT_UPGRADE = "componentUpgrade";
    public static final String CONFIG = "config";
    public static final String CONNECTION = "connection";
    public static final String CONTAINER = "Container";
    public static final String CONTROL_TYPE = "controlType";
    public static final String CURRENT_REDSTONE = "currentRedstone";
    public static final String DATA_NAME = "dataName";
    public static final String DATA_TYPE = "dataType";
    public static final String DIMENSION = "dimension";
    public static final String DRAINING = "draining";
    public static final String DUMP_LEFT = "dumpLeft";
    public static final String DUMP_MODE = "dumping";
    public static final String DUMP_RIGHT = "dumpRight";
    public static final String EDIT_MODE = "editMode";
    public static final String EJECT = "eject";
    public static final String ENERGY_CONTAINERS = "EnergyContainers";
    public static final String ENERGY_STORED = "energy";
    public static final String FILLING = "filling";
    public static final String FILTER = "filter";
    public static final String FLUID_STORED = "fluid";
    public static final String FLUID_TANKS = "FluidTanks";
    public static final String FROM_RECIPE = "fromRecipe";
    public static final String FUZZY_MODE = "fuzzyMode";
    public static final String HEAT_CAPACITORS = "HeatCapacitors";
    public static final String HEAT_CAPACITY = "heatCapacity";
    public static final String HOME_LOCATION = "homeLocation";
    public static final String IDLE_DIR = "idleDir";
    public static final String INDEX = "index";
    public static final String ITEM = "Item";
    public static final String ITEMS = "Items";
    public static final String LOCK_STACK = "lockStack";
    public static final String MAGNITUDE = "magnitude";
    public static final String MAIN = "main";
    public static final String MAX = "max";
    public static final String NC_DATA = "ncData";
    public static final String MELTDOWNS = "meltdowns";
    public static final String MIN = "min";
    public static final String MODE = "mode";
    public static final String MODID = "modID";
    public static final String MODULES = "modules";
    public static final String NAME = "name";
    public static final String ORIGINAL_LOCATION = "originalLocation";
    public static final String PATH_TYPE = "pathType";
    public static final String POSITION = "position";
    public static final String PROGRESS = "progress";
    public static final String RADIATION = "radiation";
    public static final String RADIATION_LIST = "radList";
    public static final String RADIUS = "radius";
    public static final String RECEIVED_COORDS = "receivedCoords";
    public static final String REDSTONE = "redstone";
    public static final String SIDE = "side";
    public static final String SIZE_MODE = "sizeMode";
    public static final String SIZE_OVERRIDE = "SizeOverride";
    public static final String SLOT = "Slot";
    public static final String SORTING = "sorting";
    public static final String STATE = "state";
    public static final String STORED = "stored";
    public static final String STRICT_INPUT = "strictInput";
    public static final String TAG_NAME = "tagName";
    public static final String TANK = "Tank";
    public static final String TEMPERATURE = "temperature";
    public static final String TIME = "time";
    public static final String TYPE = "type";
    public static final String UPDATE_DELAY = "updateDelay";
    public static final String UPGRADES = "upgrades";
    public static final String USED_SO_FAR = "usedSoFar";
    public static final String WORLD_GEN_VERSION = "mekWorldGenVersion";
}