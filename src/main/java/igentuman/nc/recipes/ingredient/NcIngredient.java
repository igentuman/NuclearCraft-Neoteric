package igentuman.nc.recipes.ingredient;

import com.google.common.collect.Lists;
import com.google.gson.*;
import igentuman.api.platform.NCItemStacks;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.nbt.CompoundTag;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.registries.BuiltInRegistries;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static igentuman.nc.setup.registration.Registries.ITEM_REGISTRY;
import static igentuman.nc.util.NcUtils.rlFromString;

public class NcIngredient implements Predicate<ItemStack> {

   private static final java.util.concurrent.atomic.AtomicInteger INVALIDATION_COUNTER = new java.util.concurrent.atomic.AtomicInteger();
   public static void invalidateAll() {
      INVALIDATION_COUNTER.incrementAndGet();
   }

   public static final NcIngredient EMPTY = new NcIngredient(Stream.empty());
   private final NcIngredient.Value[] values;
   @Nullable
   private ItemStack[] itemStacks;
   @Nullable
   private IntList stackingIds;
   private int invalidationCounter;

   protected int count = 1;

   private String name;

   protected NcIngredient(Stream<? extends NcIngredient.Value> pValues) {
      this.values = pValues.toArray(NcIngredient.Value[]::new);
   }

    public static NcIngredient of(String name) {
         if(name.contains("#")) {
            TagKey<Item> tag = TagKey.create(ITEM_REGISTRY, rlFromString(name.replace("#","")));
            return of(tag);
         }
         return of(BuiltInRegistries.ITEM.get(rlFromString(name)));
    }

    public String getName() {
      if(name == null) {
         name = values[0].getName();
      }
      return name;
   }

   public static void ping() {
   }

   public ItemStack[] getItems() {
      this.dissolve();
      return this.itemStacks;
   }

   /**
    * Convert this NcIngredient to a vanilla Ingredient for APIs that require it.
    */
   public Ingredient asIngredient() {
      return Ingredient.of(getItems());
   }

   private void dissolve() {
      if (this.itemStacks == null) {
         this.itemStacks = Arrays.stream(this.values).flatMap((p_43916_) -> {
            return p_43916_.getItems().stream();
         }).distinct().toArray((p_43910_) -> {
            return new ItemStack[p_43910_];
         });
      }

   }

   @Override
   public boolean test(@Nullable ItemStack pStack) {
      if (pStack == null) {
         return false;
      } else {
         this.dissolve();
         if (this.itemStacks.length == 0) {
            return pStack.isEmpty();
         } else {
            for(ItemStack itemstack : this.itemStacks) {
               if (itemstack.is(pStack.getItem()) && count <= pStack.getCount()) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public IntList getStackingIds() {
      if (this.stackingIds == null || checkInvalidation()) {
         this.markValid();
         this.dissolve();
         this.stackingIds = new IntArrayList(this.itemStacks.length);

         for(ItemStack itemstack : this.itemStacks) {
            this.stackingIds.add(StackedContents.getStackingIndex(itemstack));
         }

         this.stackingIds.sort(IntComparators.NATURAL_COMPARATOR);
      }

      return this.stackingIds;
   }

   private boolean checkInvalidation() {
      int current = INVALIDATION_COUNTER.get();
      if (this.invalidationCounter != current) {
         this.invalidationCounter = current;
         return true;
      }
      return false;
   }

   private void markValid() {
      this.invalidationCounter = INVALIDATION_COUNTER.get();
   }

   public JsonElement toJson() {
      if (this.values.length == 1) {
         return this.values[0].serialize();
      } else {
         JsonArray jsonarray = new JsonArray();

         for(NcIngredient.Value ingredient$value : this.values) {
            jsonarray.add(ingredient$value.serialize());
         }

         return jsonarray;
      }
   }

   public boolean isEmpty() {
      return this.values.length == 0 && (this.itemStacks == null || this.itemStacks.length == 0) && (this.stackingIds == null || this.stackingIds.isEmpty());
   }


   public boolean isSimple() {
      return true;
   }

   public static NcIngredient of(TagKey<Item> pTag) {
      return fromValues(Stream.of(new NcIngredient.TagValue(pTag)));
   }

   public static NcIngredient fromValues(Stream<? extends NcIngredient.Value> pStream) {
      NcIngredient ingredient = new NcIngredient(pStream);
      return ingredient.isEmpty() ? EMPTY : ingredient;
   }

   public static NcIngredient of() {
      return EMPTY;
   }

   public static NcIngredient of(ItemLike... pItems) {
      return stack(Arrays.stream(pItems).map(ItemStack::new));
   }

   public static NcIngredient stack(ItemStack... pStacks) {
      return stack(Arrays.stream(pStacks));
   }

   public static NcIngredient fromVals(Stream<? extends Value> pStream) {
      NcIngredient ingredient = new NcIngredient(pStream);
      return ingredient.values.length == 0 ? EMPTY : ingredient;
   }

   public static NcIngredient stack(Stream<ItemStack> pStacks) {
      return fromVals(pStacks.filter((p_43944_) -> {
         return !p_43944_.isEmpty();
      }).map(NcIngredient.ItemValue::new));
   }

   public static NcIngredient of(TagKey<Item> pTag, int ... pCounts) {
      return  fromVals(Stream.of(new NcIngredient.TagValue(pTag, pCounts)))
              .withCount(pCounts);
   }

   public NcIngredient withCount(int[] pCounts) {
        if(pCounts.length == 0) {
             return this;
        }
        if(pCounts.length == 1) {
             this.count = pCounts[0];
        }
        return this;
   }

   public static class ItemValue implements NcIngredient.Value {
      private final ItemStack item;
      public String getName() {
         return item.getItem().toString();
      }
      public ItemValue(ItemStack pItem) {
         this.item = pItem;
      }

      public Collection<ItemStack> getItems() {
         return Collections.singleton(this.item);
      }

      public JsonObject serialize() {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("item", BuiltInRegistries.ITEM.getKey(this.item.getItem()).toString());
         if(NCItemStacks.hasCustomData(item)) {
            CompoundTag tag = NCItemStacks.getTag(item);
            if(tag.contains("Damage")) {
               if(tag.getInt("Damage") == 0) {
                  tag.remove("Damage");
               }
            }
            if(!tag.getAllKeys().isEmpty()) {
               jsonobject.addProperty("nbt", tag.toString());
            }
         }
         if(item.getCount()>1) {
            jsonobject.addProperty("count", item.getCount());
         }
         return jsonobject;
      }
   }

   public static class TagValue implements NcIngredient.Value {
      private final TagKey<Item> tag;
      private final int count;

      public TagValue(TagKey<Item> pTag, int...pCount) {
         this.tag = pTag;
         if(pCount.length > 0) {
            count = pCount[0];
         } else {
            count = 1;
         }
      }

      public String getName() {
         return tag.location().getPath().replace("/","_");
      }

      public Collection<ItemStack> getItems() {
         List<ItemStack> list = Lists.newArrayList();

         BuiltInRegistries.ITEM.getTag(tag).ifPresent(holders -> {
            for (net.minecraft.core.Holder<Item> holder : holders) {
               list.add(new ItemStack(holder.value()));
            }
         });

         if (list.isEmpty()) {
            ItemStack barrier = new ItemStack(net.minecraft.world.level.block.Blocks.BARRIER);
            barrier.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Empty Tag: " + this.tag.location()));
            list.add(barrier);
         }
         return list;
      }

      public JsonObject serialize() {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("tag", this.tag.location().toString());
         if(count>1) {
            jsonobject.addProperty("count", this.count);
         }
         return jsonobject;
      }
   }

   public interface Value {
      String getName();
      Collection<ItemStack> getItems();
      JsonObject serialize();
   }
}
