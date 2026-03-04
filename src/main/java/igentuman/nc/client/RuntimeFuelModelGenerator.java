package igentuman.nc.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import igentuman.nc.setup.registration.FissionFuel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;

/**
 * Runtime Model Generator for Custom Fission Fuels
 * 
 * This class generates item model JSON files at runtime for custom fuels
 * registered via KubeJS or other mods. It creates models in the kubejs
 * resource pack directory so they can be loaded without requiring datagen.
 * 
 * Models are generated during client initialization and can be regenerated
 * via command or manually by calling {@link #generateResources()}.
 */
@OnlyIn(Dist.CLIENT)
public class RuntimeFuelModelGenerator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeFuelModelGenerator.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    // Base paths for model and texture generation
    private static final String KUBEJS_ASSETS_PATH = "kubejs/assets/" + MODID;
    private static final String MODELS_PATH = KUBEJS_ASSETS_PATH + "/models/item";
    private static final String LANG_PATH = KUBEJS_ASSETS_PATH + "/lang";
    private static final String TEXTURES_PATH = KUBEJS_ASSETS_PATH + "/textures/item/fuel";
    private static final String BLANK_FUEL_TEXTURE_PATH = "assets/nuclearcraft/textures/item/fuel/blank.png";
    
    private static int modelsGenerated = 0;
    private static int texturesGenerated = 0;
    private static int errorsEncountered = 0;

    public static void generateResources() {
        LOGGER.info("=".repeat(60));
        LOGGER.info("Starting runtime fuel model generation...");
        LOGGER.info("=".repeat(60));
        
        modelsGenerated = 0;
        texturesGenerated = 0;
        errorsEncountered = 0;
        
        try {
            // Create base directories
            createDirectories();
            
            // Generate textures for all registered fuels
            generateFuelTextures();
            
            // Generate models for all registered fuels
            generateFuelModels();
            
            // Create README for texture requirements
            createTextureReadme();

            createLangFile();
            
            // Summary
            LOGGER.info("=".repeat(60));
            LOGGER.info("✓ Generated {} model files", modelsGenerated);
            LOGGER.info("✓ Generated {} texture files", texturesGenerated);
            if (errorsEncountered > 0) {
                LOGGER.warn("⚠ {} error(s) occurred during generation", errorsEncountered);
            }
            LOGGER.info("=".repeat(60));
            LOGGER.info("Next steps:");
            LOGGER.info("  1. Press F3+T in-game to reload resources");
            LOGGER.info("=".repeat(60));
            
        } catch (Exception e) {
            LOGGER.error("Failed to generate fuel models", e);
        }
    }

    /**
     * Create language file with translations for custom fuels
     */
    private static void createLangFile() {
        try {
            Path gameDir = FMLPaths.GAMEDIR.get();
            Path langPath = gameDir.resolve(LANG_PATH).resolve("en_gb.json");
            
            // Get custom fuels
            var customFuels = FissionFuel.getCustomFuels();
            
            if (customFuels.isEmpty()) {
                LOGGER.info("No custom fuels to add to language file");
                return;
            }
            
            // Build translations JSON
            JsonObject translations = new JsonObject();
            
            for (var fuelDef : customFuels) {
                String group = fuelDef.group;
                String name = fuelDef.name;
                
                // Generate translations for base fuel
                addFuelTranslation(translations, List.of("fuel", group, name, ""));
                
                // Generate translations for depleted fuel
                addFuelTranslation(translations, List.of("depleted", group, name, ""));
                
                // Generate translations for variants if not special fuel
                if (!group.matches("xenorium.*|quantite.*")) {
                    addFuelTranslation(translations, List.of("fuel", group, name, "_ox"));
                    addFuelTranslation(translations, List.of("fuel", group, name, "_ni"));
                    addFuelTranslation(translations, List.of("fuel", group, name, "_za"));
                    addFuelTranslation(translations, List.of("fuel", group, name, "_tr"));
                    
                    addFuelTranslation(translations, List.of("depleted", group, name, "_ox"));
                    addFuelTranslation(translations, List.of("depleted", group, name, "_ni"));
                    addFuelTranslation(translations, List.of("depleted", group, name, "_za"));
                    addFuelTranslation(translations, List.of("depleted", group, name, "_tr"));
                }
            }
            
            // Write language file
            String json = GSON.toJson(translations);
            Files.writeString(langPath, json,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
            
            LOGGER.info("✓ Created language file with {} translations at: {}", 
                       translations.size(), langPath);
            
        } catch (IOException e) {
            LOGGER.error("Failed to create language file", e);
            errorsEncountered++;
        }
    }
    
    /**
     * Add a fuel translation to the JSON object
     * @param translations The JSON object to add to
     * @param fuelKey The fuel key [type, group, name, variant]
     */
    private static void addFuelTranslation(JsonObject translations, List<String> fuelKey) {
        String type = fuelKey.get(0);
        String group = fuelKey.get(1);
        String name = fuelKey.get(2);
        String variant = fuelKey.get(3);
        
        // Build translation key: "item.nuclearcraft.fuel_<group>_<name><variant>"
        String translationKey = "item." + MODID + "." + buildItemId(type, group, name, variant);
        
        // Build display name using the same format as data generator
        String displayName = buildDisplayName(type, group, name, variant);
        
        translations.addProperty(translationKey, displayName);
    }
    
    /**
     * Build display name for a fuel item
     * Format matches data generator: "convertToName(type) convertToName(group) NAME VARIANT"
     * Example: "Fuel Uranium 233 OX", "Depleted Thorium LE NI"
     */
    private static String buildDisplayName(String type, String group, String name, String variant) {
        // Use the same format as NCLanguageProvider.fuel()
        // convertToName(name.get(0))+" "+convertToName(name.get(1))+" "+name.get(2).toUpperCase()+" "+name.get(3).toUpperCase()
        
        StringBuilder displayName = new StringBuilder();
        
        // Add type (fuel/depleted) converted to title case
        displayName.append(convertToName(type));
        displayName.append(" ");
        
        // Add group name (converted to title case)
        displayName.append(convertToName(group));
        displayName.append(" ");
        
        // Add fuel name in uppercase
        displayName.append(name.toUpperCase());
        displayName.append(" ");
        
        // Add variant in uppercase (remove leading underscore if present)
        if (!variant.isEmpty()) {
            String variantCode = variant.startsWith("_") ? variant.substring(1) : variant;
            displayName.append(variantCode.toUpperCase());
        }
        
        return displayName.toString().trim();
    }
    
    /**
     * Convert a string to title case (first letter uppercase, rest lowercase)
     * Handles underscores and hyphens as word separators
     * This matches the behavior of TextUtils.convertToName()
     */
    private static String convertToName(String key) {
        StringBuilder result = new StringBuilder();
        String[] parts = key.split("_|/");
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (result.length() == 0) {
                result = new StringBuilder(part.substring(0, 1).toUpperCase() + part.substring(1));
            } else {
                result.append(" ").append(part.substring(0, 1).toUpperCase()).append(part.substring(1));
            }
        }
        return result.toString();
    }

    /**
     * Generate textures for custom fuel items
     */
    private static void generateFuelTextures() {
        // Get list of custom fuels
        var customFuels = FissionFuel.getCustomFuels();
        
        if (customFuels.isEmpty()) {
            LOGGER.info("No custom fuels registered - skipping texture generation");
            return;
        }
        
        LOGGER.info("Generating textures for {} custom fuel(s)...", customFuels.size());
        
        // Generate textures for each custom fuel
        for (var fuelDef : customFuels) {
            String group = fuelDef.group;
            String name = fuelDef.name;
            
            try {
                // Generate base fuel texture
                generateFuelTexture(group, name, "", false);
                
                // Generate depleted fuel texture
                generateFuelTexture(group, name, "", true);
                
                // Generate variants (oxide, nitride, zirconium alloy, triso) if not special fuel
                if (!group.matches("xenorium.*|quantite.*")) {
                    generateFuelTexture(group, name, "_ox", false);
                    generateFuelTexture(group, name, "_ni", false);
                    generateFuelTexture(group, name, "_za", false);
                    generateFuelTexture(group, name, "_tr", false);
                    
                    generateFuelTexture(group, name, "_ox", true);
                    generateFuelTexture(group, name, "_ni", true);
                    generateFuelTexture(group, name, "_za", true);
                    generateFuelTexture(group, name, "_tr", true);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to generate textures for fuel: {}/{}", group, name, e);
                errorsEncountered++;
            }
        }
    }
    
    /**
     * Generate models for custom fuel items only
     */
    private static void generateFuelModels() {
        // Get list of custom fuels
        var customFuels = FissionFuel.getCustomFuels();
        
        if (customFuels.isEmpty()) {
            LOGGER.info("No custom fuels registered - skipping model generation");
            return;
        }
        
        LOGGER.info("✓ Processed {} custom fuel(s)", customFuels.size());
        
        // Generate models for each custom fuel
        for (var fuelDef : customFuels) {
            String group = fuelDef.group;
            String name = fuelDef.name;
            
            try {
                // Generate base fuel model
                generateFuelModel(List.of("fuel", group, name, ""), false);
                
                // Generate depleted fuel model
                generateFuelModel(List.of("depleted", group, name, ""), true);
                
                // Generate variants (oxide, nitride, zirconium alloy, triso) if not special fuel
                if (!group.matches("xenorium.*|quantite.*")) {
                    generateFuelModel(List.of("fuel", group, name, "_ox"), false);
                    generateFuelModel(List.of("fuel", group, name, "_ni"), false);
                    generateFuelModel(List.of("fuel", group, name, "_za"), false);
                    generateFuelModel(List.of("fuel", group, name, "_tr"), false);
                    
                    generateFuelModel(List.of("depleted", group, name, "_ox"), true);
                    generateFuelModel(List.of("depleted", group, name, "_ni"), true);
                    generateFuelModel(List.of("depleted", group, name, "_za"), true);
                    generateFuelModel(List.of("depleted", group, name, "_tr"), true);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to generate models for fuel: {}/{}", group, name, e);
                errorsEncountered++;
            }
        }
    }
    
    /**
     * Generate a single fuel model
     * @param fuelKey The fuel key [type, group, name, variant]
     * @param isDepleted Whether this is a depleted fuel
     */
    private static void generateFuelModel(List<String> fuelKey, boolean isDepleted) throws IOException {
        // Parse fuel key: [type, group, name, variant]
        // type: "fuel" or "depleted"
        // group: e.g., "uranium", "thorium"
        // name: e.g., "233", "le"
        // variant: "", "_ox", "_ni", "_za", "_tr"
        
        String type = fuelKey.get(0);
        String group = fuelKey.get(1);
        String name = fuelKey.get(2);
        String variant = fuelKey.get(3);
        
        // Build item ID
        String itemId = buildItemId(type, group, name, variant);
        
        // Build texture path
        String texturePath = buildTexturePath(group, name, variant, isDepleted);
        
        // Create model JSON
        JsonObject model = createModelJson(texturePath);
        
        // Write model file
        writeModelFile(itemId, model);
        
        LOGGER.debug("  ✓ Generated model: {}.json", itemId);
        modelsGenerated++;
    }
    
    /**
     * Build the item ID from fuel components
     */
    private static String buildItemId(String type, String group, String name, String variant) {
        StringBuilder id = new StringBuilder();
        
        if (type.equals("depleted")) {
            id.append("depleted_");
        }
        
        id.append("fuel_")
          .append(group)
          .append("_")
          .append(name.replace("-", "_"));
        
        if (!variant.isEmpty()) {
            id.append(variant);
        }
        
        return id.toString();
    }
    
    /**
     * Build the texture path for a fuel
     */
    private static String buildTexturePath(String group, String name, String variant, boolean isDepleted) {
        StringBuilder path = new StringBuilder(MODID);
        path.append(":item/fuel/")
            .append(group);
        
        if (isDepleted) {
            path.append("/depleted");
        }
        
        path.append("/")
            .append(name.replace("-", "_"));
        
        if (!variant.isEmpty()) {
            path.append(variant);
        }
        
        return path.toString();
    }
    
    /**
     * Create a model JSON object
     */
    private static JsonObject createModelJson(String texturePath) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:item/generated");
        
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", texturePath);
        model.add("textures", textures);
        
        return model;
    }
    
    /**
     * Write a model JSON file
     */
    private static void writeModelFile(String itemId, JsonObject model) throws IOException {
        Path gameDir = FMLPaths.GAMEDIR.get();
        Path modelPath = gameDir.resolve(MODELS_PATH).resolve(itemId + ".json");
        
        // Ensure parent directory exists
        Files.createDirectories(modelPath.getParent());
        
        // Write JSON
        String json = GSON.toJson(model);
        Files.writeString(modelPath, json, 
                         StandardOpenOption.CREATE, 
                         StandardOpenOption.TRUNCATE_EXISTING);
    }
    
    /**
     * Create necessary directory structure
     */
    private static void createDirectories() throws IOException {
        Path gameDir = FMLPaths.GAMEDIR.get();
        Path langDir = gameDir.resolve(LANG_PATH);
        Files.createDirectories(langDir);
        Path modelsDir = gameDir.resolve(MODELS_PATH);
        Files.createDirectories(modelsDir);
        
        FissionFuel.getCustomFuels().stream()
            .map(fuelDef -> fuelDef.group) // Get group
            .distinct()
            .forEach(group -> {
                try {
                    Path textureDir = gameDir.resolve(TEXTURES_PATH).resolve(group);
                    Path depletedDir = textureDir.resolve("depleted");
                    Files.createDirectories(textureDir);
                    Files.createDirectories(depletedDir);
                } catch (IOException e) {
                    LOGGER.error("Failed to create texture directory for group: {}", group, e);
                }
            });
        
        LOGGER.info("✓ Created directory structure");
    }
    
    /**
     * Create a README file explaining texture requirements
     */
    private static void createTextureReadme() {
        try {
            Path gameDir = FMLPaths.GAMEDIR.get();
            Path readmePath = gameDir.resolve(TEXTURES_PATH).resolve("readme.txt");
            Files.createDirectories(readmePath.getParent());

            StringBuilder content = new StringBuilder();
            content.append("NuclearCraft Custom Fuel Textures\n");
            content.append("=".repeat(50)).append("\n\n");
            content.append("This directory contains textures for custom fission fuels.\n\n");
            
            content.append("AUTOMATIC TEXTURE GENERATION:\n");
            content.append("  Textures are automatically generated from the blank fuel template\n");
            content.append("  with color tints based on fuel name and type. The system will:\n");
            content.append("  - Generate unique colors for each fuel based on its name\n");
            content.append("  - Apply darker tones for depleted fuels\n");
            content.append("  - Apply variant-specific color shifts (oxide, nitride, etc.)\n");
            content.append("  - Skip generation if a texture already exists\n\n");
            
            content.append("CUSTOM TEXTURES:\n");
            content.append("  You can override auto-generated textures by placing your own\n");
            content.append("  PNG files in the appropriate directories. The generator will\n");
            content.append("  not overwrite existing textures.\n\n");
            
            content.append("Directory Structure:\n");
            content.append("  fuel/\n");
            content.append("    <group>/              (e.g., uranium, thorium, plutonium)\n");
            content.append("      <name>.png          Active fuel texture\n");
            content.append("      <name>_ox.png       Oxide variant\n");
            content.append("      <name>_ni.png       Nitride variant\n");
            content.append("      <name>_za.png       Zirconium Alloy variant\n");
            content.append("      <name>_tr.png       TRISO variant\n");
            content.append("      depleted/\n");
            content.append("        <name>.png        Depleted fuel texture\n");
            content.append("        <name>_ox.png     Depleted oxide\n");
            content.append("        <name>_ni.png     Depleted nitride\n");
            content.append("        <name>_za.png     Depleted zirconium alloy\n");
            content.append("        <name>_tr.png     Depleted TRISO\n\n");
            
            content.append("Texture Specifications:\n");
            content.append("  - Format: PNG with transparency support\n");
            content.append("  - Size: 16x16 pixels (or 32x32 for HD)\n");
            content.append("  - Color coding (auto-applied):\n");
            content.append("    * Base: Bright, vibrant colors (hash-based)\n");
            content.append("    * Oxide (_ox): Lighter with white/gray tones\n");
            content.append("    * Nitride (_ni): Blue/purple tones\n");
            content.append("    * Zirconium Alloy (_za): Metallic silver\n");
            content.append("    * TRISO (_tr): Warmer tones\n");
            content.append("    * Depleted: Darker, grayer versions\n\n");
            
            content.append("Registered Custom Fuel Groups:\n");
            FissionFuel.getCustomFuels().stream()
                .map(fuelDef -> fuelDef.group)
                .distinct()
                .sorted()
                .forEach(group -> content.append("  - ").append(group).append("\n"));
            
            content.append("\nTo regenerate textures:\n");
            content.append("  1. Delete the texture files you want to regenerate\n");
            content.append("  2. Run the command: /nc_fuel_models\n");
            content.append("  3. Press F3+T in-game to reload resources\n");
            
            Files.writeString(readmePath, content.toString(),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
            
            LOGGER.info("✓ Created texture README at: {}", readmePath);
            
        } catch (IOException e) {
            LOGGER.error("Failed to create texture README", e);
        }
    }
    
    /**
     * Check if a texture file exists
     * @param group Fuel group
     * @param name Fuel name
     * @param variant Fuel variant
     * @param isDepleted Whether this is depleted fuel
     * @return true if texture exists
     */
    public static boolean textureExists(String group, String name, String variant, boolean isDepleted) {
        Path gameDir = FMLPaths.GAMEDIR.get();
        StringBuilder texturePath = new StringBuilder(TEXTURES_PATH)
            .append("/")
            .append(group);
        
        if (isDepleted) {
            texturePath.append("/depleted");
        }
        
        texturePath.append("/")
                   .append(name.replace("-", "_"));
        
        if (!variant.isEmpty()) {
            texturePath.append(variant);
        }
        
        texturePath.append(".png");
        
        Path fullPath = gameDir.resolve(texturePath.toString());
        return Files.exists(fullPath);
    }
    
    /**
     * Generate a single fuel texture with color tint
     * @param group Fuel group
     * @param name Fuel name
     * @param variant Fuel variant ("", "_ox", "_ni", "_za", "_tr")
     * @param isDepleted Whether this is depleted fuel
     */
    private static void generateFuelTexture(String group, String name, String variant, boolean isDepleted) throws IOException {
        // Build the output path
        Path gameDir = FMLPaths.GAMEDIR.get();
        StringBuilder texturePath = new StringBuilder(TEXTURES_PATH)
            .append("/")
            .append(group);
        
        if (isDepleted) {
            texturePath.append("/depleted");
        }
        
        texturePath.append("/")
                   .append(name.replace("-", "_"));
        
        if (!variant.isEmpty()) {
            texturePath.append(variant);
        }
        
        texturePath.append(".png");
        
        Path outputPath = gameDir.resolve(texturePath.toString());
        
        // Skip if texture already exists
        if (Files.exists(outputPath)) {
            LOGGER.debug("  ⊘ Skipping existing texture: {}", outputPath.getFileName());
            return;
        }
        
        // Load the blank fuel texture from mod jar
        try (InputStream inputStream = RuntimeFuelModelGenerator.class.getClassLoader()
                .getResourceAsStream(BLANK_FUEL_TEXTURE_PATH)) {
            
            if (inputStream == null) {
                LOGGER.error("Failed to load blank fuel texture from: {}", BLANK_FUEL_TEXTURE_PATH);
                errorsEncountered++;
                return;
            }
            
            // Read the image
            NativeImage blankImage = NativeImage.read(inputStream);
            
            // Generate color tint based on fuel properties
            int tintColor = generateTintColor(name, variant, isDepleted);
            
            // Apply tint to the image
            NativeImage tintedImage = applyTint(blankImage, tintColor);
            
            // Ensure parent directory exists
            Files.createDirectories(outputPath.getParent());
            
            // Write the tinted image
            tintedImage.writeToFile(outputPath);
            
            // Clean up
            blankImage.close();
            tintedImage.close();
            
            LOGGER.debug("  ✓ Generated texture: {}", outputPath.getFileName());
            texturesGenerated++;
            
        } catch (IOException e) {
            LOGGER.error("Failed to generate texture for {}/{}{} (depleted: {})", 
                        group, name, variant, isDepleted, e);
            errorsEncountered++;
        }
    }
    
    /**
     * Generate a color tint based on fuel name, variant, and depletion status
     * Uses a hash-based approach to generate consistent colors
     * 
     * @param name Fuel name
     * @param variant Fuel variant
     * @param isDepleted Whether this is depleted fuel
     * @return ARGB color value
     */
    private static int generateTintColor(String name, String variant, boolean isDepleted) {
        // Build the seed string for color generation
        String seedString;
        if (isDepleted) {
            // For depleted fuel: "depleted" + name + variant
            seedString = "depleted" + name + variant;
        } else {
            // For active fuel: name + variant
            seedString = name + variant;
        }
        
        // Generate hash-based color
        int hash = seedString.hashCode();
        
        // Extract RGB components from hash
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = (hash & 0x0000FF);
        
        // Adjust colors based on fuel type
        if (isDepleted) {
            // Depleted fuels: darker, more gray
            r = (r + 60) / 2;  // Reduce saturation
            g = (g + 60) / 2;
            b = (b + 60) / 2;
        } else {
            // Active fuels: brighter, more vibrant
            r = Math.min(255, r + 80);
            g = Math.min(255, g + 80);
            b = Math.min(255, b + 80);
        }
        
        // Apply variant-specific color shifts
        if (!variant.isEmpty()) {
            switch (variant) {
                case "_ox": // Oxide: add white/gray tones
                    r = Math.min(255, r + 40);
                    g = Math.min(255, g + 40);
                    b = Math.min(255, b + 40);
                    break;
                case "_ni": // Nitride: add blue/purple tones
                    b = Math.min(255, b + 60);
                    r = Math.max(0, r - 20);
                    break;
                case "_za": // Zirconium Alloy: metallic silver
                    int avg = (r + g + b) / 3;
                    r = Math.min(255, avg + 60);
                    g = Math.min(255, avg + 60);
                    b = Math.min(255, avg + 60);
                    break;
                case "_tr": // TRISO: warmer tones
                    r = Math.min(255, r + 40);
                    g = Math.min(255, g + 20);
                    b = Math.max(0, b - 20);
                    break;
            }
        }
        
        // Return ARGB color (full opacity)
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
    
    /**
     * Apply a color tint to a NativeImage
     * 
     * @param source Source image
     * @param tintColor ARGB tint color
     * @return New tinted image
     */
    private static NativeImage applyTint(NativeImage source, int tintColor) {
        int width = source.getWidth();
        int height = source.getHeight();
        
        // Create a new image with the same dimensions
        NativeImage tinted = new NativeImage(width, height, true);
        
        // Extract tint RGB components
        int tintR = (tintColor >> 16) & 0xFF;
        int tintG = (tintColor >> 8) & 0xFF;
        int tintB = tintColor & 0xFF;
        
        // Apply tint to each pixel
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = source.getPixelRGBA(x, y);
                
                // Extract RGBA components (NativeImage uses ABGR format)
                int a = (pixel >> 24) & 0xFF;
                int b = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int r = pixel & 0xFF;
                
                // Apply tint by multiplying with source color
                int newR = (r * tintR) / 255;
                int newG = (g * tintG) / 255;
                int newB = (b * tintB) / 255;
                
                // Reconstruct pixel in ABGR format
                int newPixel = (a << 24) | (newB << 16) | (newG << 8) | newR;
                
                tinted.setPixelRGBA(x, y, newPixel);
            }
        }
        
        return tinted;
    }
    
    /**
     * Get statistics about model generation
     */
    public static String getGenerationStats() {
        return String.format("Generated %d models and %d textures with %d errors", 
                           modelsGenerated, texturesGenerated, errorsEncountered);
    }
}