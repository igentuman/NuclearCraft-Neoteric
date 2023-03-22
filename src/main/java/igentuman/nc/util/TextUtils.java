package igentuman.nc.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.text.DecimalFormat;

public class TextUtils
{
	public static MutableComponent applyFormat(Component component, ChatFormatting... color)
	{
		Style style = component.getStyle();
		for(ChatFormatting format : color)
			style = style.applyFormat(format);
		return component.copy().setStyle(style);
	}

	public static String numberFormat(double value)
	{
		DecimalFormat df = new DecimalFormat("#.0");
		if (value == (int) value) {
			return String.valueOf((int)value);
		}
		return  df.format(value);
	}
}
