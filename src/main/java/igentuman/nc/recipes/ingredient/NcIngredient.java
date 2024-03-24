package igentuman.nc.recipes.ingredient;

import com.google.common.collect.Lists;
import com.google.gson.*;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NcIngredient extends Ingredient {
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

   protected NcIngredient(Stream<? extends Ingredient.Value> pValues) {
      super(Stream.empty());
      this.values = pValues.toArray((p_43933_) -> {
         return new NcIngredient.Value[p_43933_];
      });

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

   private void dissolve() {
      if (this.itemStacks == null) {
         this.itemStacks = Arrays.stream(this.values).flatMap((p_43916_) -> {
            return p_43916_.getItems().stream();
         }).distinct().toArray((p_43910_) -> {
            return new ItemStack[p_43910_];
         });
      }

   }

   public boolean test(@Nullable ItemStack pStack) {
      if (pStack == null) {
         return false;
      } else {
         this.dissolve();
         if (this.itemStacks.length == 0) {
            return pStack.isEmpty();
         } else {
            for(ItemStack itemstack : this.itemStacks) {
               if (itemstack.is(pStack.getItem())) {
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

   private final boolean isVanilla = this.getClass() == NcIngredient.class;
   public static NcIngredient of(TagKey<Item> pTag) {
      return fromValues(Stream.of(new NcIngredient.TagValue(pTag)));
   }

   public static NcIngredient fromValues(Stream<? extends Ingredient.Value> pStream) {
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
         jsonobject.addProperty("item", ForgeRegistries.ITEMS.getKey(this.item.getItem()).toString());
         if(item.hasTag()) {
            if(item.getTag().contains("Damage")) {
               if(item.getTag().getInt("Damage") == 0) {
                  item.getTag().remove("Damage");
               }
            }
            if(!item.getTag().getAllKeys().isEmpty()) {
               jsonobject.addProperty("nbt", item.getTag().toString());
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

         for(Item item : ForgeRegistries.ITEMS.tags().getTag(tag).stream().toList()) {
            list.add(new ItemStack(item));
         }

         if (list.size() == 0) {
            list.add(new ItemStack(net.minecraft.world.level.block.Blocks.BARRIER).setHoverName(net.minecraft.network.chat.Component.literal("Empty Tag: " + this.tag.location())));
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

   public interface Value  extends Ingredient.Value{
      String getName();
   }
}
