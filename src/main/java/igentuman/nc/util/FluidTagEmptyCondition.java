/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package igentuman.nc.util;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class FluidTagEmptyCondition implements ICondition
{
    private static final ResourceLocation NAME = new ResourceLocation("forge", "fluid_tag_empty");
    private final TagKey<Fluid> tag;

    public FluidTagEmptyCondition(String location)
    {
        this(ResourceLocation.tryParse(location));
    }

    public FluidTagEmptyCondition(String namespace, String path)
    {
        this(new ResourceLocation(namespace, path));
    }

    public FluidTagEmptyCondition(ResourceLocation tag)
    {
        this.tag = TagKey.create(Registries.FLUID, tag);
    }

    @Override
    public ResourceLocation getID()
    {
        return NAME;
    }

    @Override
    public boolean test(IContext context)
    {
        return context.getTag(tag).isEmpty();
    }

    @Override
    public String toString()
    {
        return "tag_empty(\"" + tag.location() + "\")";
    }

    public static class Serializer implements IConditionSerializer<FluidTagEmptyCondition>
    {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, FluidTagEmptyCondition value)
        {
            json.addProperty("tag", value.tag.location().toString());
        }

        @Override
        public FluidTagEmptyCondition read(JsonObject json)
        {
            return new FluidTagEmptyCondition(ResourceLocation.tryParse(GsonHelper.getAsString(json, "tag")));
        }

        @Override
        public ResourceLocation getID()
        {
            return FluidTagEmptyCondition.NAME;
        }
    }
}
