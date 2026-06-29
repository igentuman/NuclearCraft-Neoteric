package igentuman.nc.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static igentuman.nc.Main.rl;

public class TextUtils
{
	public static MutableComponent __(String text, Object... pArgs)
	{
		return Component.translatable(text, pArgs);
	}

	public static MutableComponent applyFormat(Component component, ChatFormatting... color)
	{
		Style style = component.getStyle();
		for(ChatFormatting format : color)
			style = style.applyFormat(format);
		return component.copy().setStyle(style);
	}

	public static List<String> getBlockNames(String rawLine) {

		List<String> names = new ArrayList<>();
		String[] conditionParts = rawLine.split("=|-|>|<|\\^");
		String[] blocks = conditionParts[0].split("\\|");

		for(String code: blocks) {
			String id = code;
			if(!id.contains(":")) {
				ResourceLocation res = rl(id);
				Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(res);
				block.ifPresent(b -> names.add(b.getName().getString()));
			} else {
				names.add(convertToName(id.split(":")[1]));
			}
		}
		return names;
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

	public static String numberFormat(String value)
	{
		return numberFormat(Double.parseDouble(value));
	}

	public static String roundFormat(double value)
	{
		if(Math.round(value) == 0) {
			return "0";
		}
		String preffix = "";
		if(value < 1 && value > 0) {
			preffix = "0";
		}
		if(value > -1 && value < 0) {
			preffix = "0";
		}
		DecimalFormat df = new DecimalFormat("#.0");
		if(preffix.isEmpty()) {
			df = new DecimalFormat("#");
		}

		if (value == (int) value) {
			return String.valueOf((int)value);
		}
		String formatted = preffix+df.format(value);
		if (formatted.equals("NaN")) {
			return "0";
		}
		return  preffix+df.format(value);
	}

	public static String scaledFormat(double value)
	{
		if(value >= 1000000000000D) {
			return numberFormat(value/1000000000000D)+" T";
		}
		if(value >= 1000000000) {
			return numberFormat(value/1000000000)+" G";
		}
		if(value >= 1000000) {
			return numberFormat(value/1000000)+" M";
		}
		if(value >= 1000) {
			return numberFormat(value/1000)+" k";
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

	public static String formatLiquid(int val)
	{
		if (val < 1000) {
			return val + " mB";
		}
		return TextUtils.numberFormat((double) val/1000)+" B";
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

	public static String formatMass(long mass)
	{
		if(mass >= 1000000000000L) {
			return TextUtils.numberFormat(mass/1000000000000d)+" TT";
		}
		if(mass >= 1000000000) {
			return TextUtils.numberFormat(mass/1000000000d)+" GT";
		}
		if(mass >= 1000000) {
			return TextUtils.numberFormat(mass/1000000d)+" MT";
		}
		if(mass >= 1000) {
			return TextUtils.numberFormat(mass/1000d)+" kT";
		}
		return TextUtils.numberFormat(mass)+" T";
	}

	public static String formatEnergy(long energy)
	{
		if(energy >= 1000000000000L) {
			return TextUtils.numberFormat(energy/1000000000000d)+" TFE";
		}
		if(energy >= 1000000000) {
			return TextUtils.numberFormat(energy/1000000000d)+" GFE";
		}
		if(energy >= 1000000) {
			return TextUtils.numberFormat(energy/1000000d)+" MFE";
		}
		if(energy >= 1000) {
			return TextUtils.numberFormat(energy/1000d)+" kFE";
		}
		return TextUtils.numberFormat(energy)+" FE";
	}

	public static String formatEnergy(int energy)
	{
		if(energy >= 1000000000) {
			return TextUtils.numberFormat(energy/1000000000d)+" GFE";
		}
		if(energy >= 1000000) {
			return TextUtils.numberFormat(energy/1000000d)+" MFE";
		}
		if(energy >= 1000) {
			return TextUtils.numberFormat(energy/1000d)+" kFE";
		}
		return TextUtils.numberFormat(energy)+" FE";
	}

	public static String formatRads(long radiation) {
		if(radiation >= 1000000) {
			return String.format(Locale.US,"%.2f", (float)radiation/1000000)+" Rad";
		}
		if(radiation >= 1000) {
			return String.format(Locale.US,"%.2f", (float)radiation/1000)+" mRad";
		}
		return String.format(Locale.US,"%.2f", (float)radiation)+" uRad";
	}
}
