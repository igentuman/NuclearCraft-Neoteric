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
		String preffix = "";
		if(value < 1 && value > 0) {
			preffix = "0";
		}
		if(value > -1 && value < 0) {
			preffix = "0";
		}
		DecimalFormat df = new DecimalFormat("#.0");
		if (value == (int) value) {
			return String.valueOf((int)value);
		}
		return  preffix+df.format(value);
	}

	public static String scaledFormat(double value)
	{
		if(value >= 1000000) {
			return numberFormat(value/1000000)+"M";
		}
		if(value >= 1000) {
			return numberFormat(value/1000)+"k";
		}
		return numberFormat(value);
	}

	public static String convertToName(String key)
	{
		StringBuilder result = new StringBuilder();
		String[] parts = key.split("_|/");
		for(String l: parts) {
			if(l.isEmpty()) continue;
			if(result.length() == 0) {
				result = new StringBuilder(l.substring(0, 1).toUpperCase() + l.substring(1));
			} else {
				result.append(" ").append(l.substring(0, 1).toUpperCase()).append(l.substring(1));
			}
		}
		return applySpeccialRules(result.toString());
	}

	public static String applySpeccialRules(String val)
	{
		val = val.replace("Rtg", "RTG");
		val = val.replace("Du", "DU");
		val = val.replace("Tbu", "TBU");
		val = val.replace("Bssco", "BSSCO");
		val = val.replace("Rf", "RF");
		return val;
	}
}
