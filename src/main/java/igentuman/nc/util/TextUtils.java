package igentuman.nc.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.text.DecimalFormat;
import java.util.Locale;

import static igentuman.nc.block.entity.NuclearCraftBE.isGTEUCapEnabled;
import static igentuman.nc.compat.gregtech.GTUtils.convert2EU;
import static igentuman.nc.util.ModUtil.isGtLoaded;

public class TextUtils
{
	public static MutableComponent __(String text, Object... pArgs)
	{
		return Component.translatable(text, pArgs);
	}

	public static String capitalize(String text)
	{
		if(text == null) {
			return text;
		}
		if(text.isEmpty()) {
			return text;
		}
		if(text.length() == 1) {
			return text.toUpperCase();
		}
		return text.substring(0, 1).toUpperCase() + text.substring(1);
	}

	public static MutableComponent applyFormat(Component component, ChatFormatting... color)
	{
		Style style = component.getStyle();
		for(ChatFormatting format : color)
			style = style.applyFormat(format);
		return component.copy().setStyle(style);
	}

    public static String energyGenLine()
    {
        if(ModUtil.isGtLoaded() && isGTEUCapEnabled()) {
            return "tooltip.nc.eu_per_tick";
        }
        return "tooltip.nc.forge_energy_per_tick";
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

	public static int energy2Display(int energy) {
		if(isGtLoaded() && isGTEUCapEnabled()) {
			return convert2EU(energy);
		}
		return energy;
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
		return TextUtils.numberFormat(val/1000)+" B";
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
