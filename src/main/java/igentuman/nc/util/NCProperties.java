package igentuman.nc.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class NCProperties
{
	public static final DirectionProperty FACING_ALL = DirectionProperty.create("facing", DirectionUtil.VALUES);
	public static final DirectionProperty FACING_HORIZONTAL = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
	public static final DirectionProperty FACING_TOP_DOWN = DirectionProperty.create("facing", Direction.UP, Direction.DOWN);

	public static final BooleanProperty MULTIBLOCKSLAVE = BooleanProperty.create("multiblockslave");
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
	public static final BooleanProperty MIRRORED = BooleanProperty.create("mirrored");

	public static final IntegerProperty INT_16 = IntegerProperty.create("int_16", 0, 15);
	public static final IntegerProperty INT_32 = IntegerProperty.create("int_32", 0, 31);

	public static class VisibilityList
	{
		private final ImmutableSet<String> selected;
		private final boolean showSelected;

		private VisibilityList(Collection<String> selected, boolean show)
		{
			this.selected = ImmutableSet.copyOf(selected);
			this.showSelected = show;
		}

		public static VisibilityList show(String... visible)
		{
			return show(Arrays.asList(visible));
		}

		public static VisibilityList show(Collection<String> visible)
		{
			return new VisibilityList(visible, true);
		}

		public static VisibilityList hide(Collection<String> hidden)
		{
			return new VisibilityList(hidden, false);
		}

		public static VisibilityList showAll()
		{
			return hide(ImmutableSet.of());
		}

		public static VisibilityList hideAll()
		{
			return show(Arrays.asList());
		}

		public boolean isVisible(String group)
		{
			return showSelected==selected.contains(group);
		}
	}

	public static class IEObjState
	{
		private final VisibilityList visibility;

		public IEObjState(VisibilityList visibility)
		{
			this.visibility = visibility;
		}
	}

	public static class Model
	{
		@Deprecated
		public static final ModelProperty<IEObjState> IE_OBJ_STATE = new ModelProperty<>();
		@Deprecated
		public static final ModelProperty<Map<String, String>> TEXTURE_REMAP = new ModelProperty<>();
		public static final ModelProperty<BlockPos> SUBMODEL_OFFSET = new ModelProperty<>();
	}
}