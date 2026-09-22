package igentuman.nc.registration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public record RecipeFamilyEntry(
        ResourceLocation id,
        DeferredHolder<RecipeType<?>, RecipeType<?>> type,
        DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> serializer,
        RecipeSchemaKind schemaKind,
        String ownerEntryId,
        List<String> consumerEntryIds,
        List<String> catalystEntryIds
) {
}
