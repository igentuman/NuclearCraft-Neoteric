package igentuman.nc.util;

import java.text.DecimalFormat;

public class Units
{

	public static String getSIFormat(double number,String unit)
	{
		 return getSIFormat(number,0,unit);
	}
	
	public static String scaleToSI(double number,int power)
	{
	
		String prefix = getSIPrefix(number,power);
		
		if(prefix.equals("q"))
		{
			number *= Math.pow(10, 30+power);
		}
		if(prefix.equals("r"))
		{
			number *= Math.pow(10, 27+power);
		}
		if(prefix.equals("y"))
		{
			number *= Math.pow(10, 24+power);
		}
		if(prefix.equals("z"))
		{
			number *= Math.pow(10, 21+power);
		}
		if(prefix.equals("a"))
		{
			number *= Math.pow(10, 18+power);
		}
		if(prefix.equals("f"))
		{
			number *= Math.pow(10, 15+power);
		}
		if(prefix.equals("p"))
		{
			number *= Math.pow(10, 12+power);
		}
		if(prefix.equals("n"))
		{
			number *= Math.pow(10, 9+power);
		}
		if(prefix.equals("u"))
		{
			number *= Math.pow(10, 6+power);
		}
		if(prefix.equals("m"))
		{
			number *= Math.pow(10, 3+power);
		}
		if(prefix.equals(""))
		{
			number *= Math.pow(10, 0+power);
		}
		if(prefix.equals("k"))
		{
			number *= Math.pow(10, -3+power);
		}
		if(prefix.equals("M"))
		{
			number *= Math.pow(10, -6+power);
		}
		if(prefix.equals("G"))
		{
			number *= Math.pow(10, -9+power);
		}
		if(prefix.equals("T"))
		{
			number *= Math.pow(10, -12+power);
		}
		
		if(prefix.equals("P"))
		{
			number *= Math.pow(10, -15+power);
		}
		if(prefix.equals("E"))
		{
			number *= Math.pow(10, -18+power);
		}
		if(prefix.equals("Z"))
		{
			number *= Math.pow(10, -21+power);
		}
		if(prefix.equals("Y"))
		{
			number *= Math.pow(10, -24+power);
		}
		if(prefix.equals("R"))
		{
			number *= Math.pow(10, -27+power);
		}
		if(prefix.equals("Q"))
		{
			number *= Math.pow(10, -30+power);
		}
		
		DecimalFormat df = new DecimalFormat("#.###");
		
		return df.format(number);
	}
	
	public static String getSIFormat(double number,int power,String unit)
	{
		
		
		String prefix = getSIPrefix(number,power);
		
		
		if(prefix.equals("q"))
		{
			number *= Math.pow(10, 30+power);
		}
		if(prefix.equals("r"))
		{
			number *= Math.pow(10, 27+power);
		}
		if(prefix.equals("y"))
		{
			number *= Math.pow(10, 24+power);
		}
		if(prefix.equals("z"))
		{
			number *= Math.pow(10, 21+power);
		}
		if(prefix.equals("a"))
		{
			number *= Math.pow(10, 18+power);
		}
		if(prefix.equals("f"))
		{
			number *= Math.pow(10, 15+power);
		}
		if(prefix.equals("p"))
		{
			number *= Math.pow(10, 12+power);
		}
		if(prefix.equals("n"))
		{
			number *= Math.pow(10, 9+power);
		}
		if(prefix.equals("u"))
		{
			number *= Math.pow(10, 6+power);
		}
		if(prefix.equals("m"))
		{
			number *= Math.pow(10, 3+power);
		}
		if(prefix.equals(""))
		{
			number *= Math.pow(10, 0+power);
		}
		if(prefix.equals("k"))
		{
			number *= Math.pow(10, -3+power);
		}
		if(prefix.equals("M"))
		{
			number *= Math.pow(10, -6+power);
		}
		if(prefix.equals("G"))
		{
			number *= Math.pow(10, -9+power);
		}
		if(prefix.equals("T"))
		{
			number *= Math.pow(10, -12+power);
		}
		
		if(prefix.equals("P"))
		{
			number *= Math.pow(10, -15+power);
		}
		if(prefix.equals("E"))
		{
			number *= Math.pow(10, -18+power);
		}
		if(prefix.equals("Z"))
		{
			number *= Math.pow(10, -21+power);
		}
		if(prefix.equals("Y"))
		{
			number *= Math.pow(10, -24+power);
		}
		if(prefix.equals("R"))
		{
			number *= Math.pow(10, -27+power);
		}
		if(prefix.equals("Q"))
		{
			number *= Math.pow(10, -30+power);
		}
		
		DecimalFormat df = new DecimalFormat("#.###");
		
		return df.format(number)+ " "+ prefix+unit;
	}
	
	public static String getParticleEnergy(long number)
	{
		if(number == 0)
		{
			return "< 1"+ " "+"keV";
		}
		return getSIFormat(number, 3, "eV");
	}
	
	public static String getSIPrefix(double number,int power)
	{
		int index =(int) Math.log10(Math.abs(number)) + power;
		
		switch(index)
		{
		case -30:
		case -29:
		case -28:
			return "q";		
		case -27:
		case -26:
		case -25:
			return "r";
		case -24:
		case -23:
		case -22:
			return "y";		
		case -21:
		case -20:
		case -19:
			return "z";		
		case -18:
		case -17:
		case -16:
			return "a";		
		case -15:
		case -14:
		case -13:
			return "f";
		case -12:
		case -11:
		case -10:
			return "p";
		case -9:
		case -8:
		case -7:
			return "n";
		case -6:
		case -5:
		case -4:
			return "u";
		case -3:
		case -2:
		case -1:
			return "m";
		case 0:
		case 1:
		case 2:
			return "";
		case 3:
		case 4:
		case 5:
			return "k";
		case 6:
		case 7:
		case 8:
			return "M";
		case 9:
		case 10:
		case 11:
			return "G";
		case 12:
		case 13:
		case 14:
			return "T";
		case 15:
		case 16:
		case 17:
			return "P";
		case 18:
		case 19:
		case 20:
			return "E";	
		case 21:
		case 22:
		case 23:
			return "Z";
		case 24:
		case 25:
		case 26:
			return "Y";
		case 27:
		case 28:
		case 29:
			return "R";
		case 30:
		case 31:
		case 32:
			return "R";	
		}
	
		return "";
	}

}
