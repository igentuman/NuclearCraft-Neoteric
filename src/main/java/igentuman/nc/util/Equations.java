package igentuman.nc.util;

import igentuman.nc.content.particles.Particle;
import igentuman.nc.content.particles.ParticleStack;

import static igentuman.nc.handler.config.AcceleratorConfig.ACCELERATOR_CONFIG;

public class Equations
{

	public static double focusLoss(double distance, ParticleStack stack)
	{
		if (stack != null)
		{
			Particle particle = stack.getParticle();
			return ACCELERATOR_CONFIG.BEAM_ATTENUATION_RATE.get()*distance*(1+Math.abs(particle.getCharge())*Math.sqrt(stack.getAmount()/(double)ACCELERATOR_CONFIG.BEAM_SCALING.get()));
		}
		return 0;
	}
	
	public static double travelDistance(ParticleStack stack)
	{
		if (stack != null)
		{
			Particle particle = stack.getParticle();
			return stack.getFocus()/focusLoss( 1, stack);
		}
		return 0;
	}
	
	public static double focusGain(double quadrupoleStrength, ParticleStack stack)
	{
		if (stack != null)
		{
			Particle particle = stack.getParticle();
			return quadrupoleStrength * Math.abs(particle.getCharge());
		}
		return 0;
	}
	
	public static long linacEnergyGain(long cavityVoltage, ParticleStack stack)
	{
		if (stack != null)
		{
			Particle particle = stack.getParticle();
			return (long) (cavityVoltage * Math.abs(particle.getCharge()))/1000;
		}
		return 0;
	}
	
	
	public static long ringEnergyMaxEnergyFromDipole(double dipoleStrength, double radius, double charge, double mass)
	{
		return (long) (Math.pow(charge*dipoleStrength*radius, 2)/(2*mass)*1000000);
	}
	
	public static long ringEnergyMaxEnergyFromRadiation(long cavityVoltage, double radius, double charge, double mass)
	{
		return (long) (mass*Math.pow(3*cavityVoltage*radius/Math.abs(charge),0.25) * 1000000);
	}
	
	public static long ringEnergyMaxEnergy(double dipoleStrength ,long cavityVoltage, double radius, ParticleStack stack)
	{
		if (stack != null)
		{
			Particle particle = stack.getParticle();
			return  Math.min(ringEnergyMaxEnergyFromDipole(dipoleStrength, radius, particle.getCharge(), particle.getMass()), ringEnergyMaxEnergyFromRadiation(cavityVoltage, radius, particle.getCharge(), particle.getMass()));
		}
		return 0;
		
	}
	
	public static long synchrotronRadiationEnergy(double radius, ParticleStack stack)
	{
		if (stack != null)
		{
			Particle particle = stack.getParticle();
			return (long) (Math.pow(stack.getMeanEnergy() / (1000 * particle.getMass()), 3) / (2 * Math.PI *1000000 *radius));
		}
		return 0;
	}
	
	public static long cornerEnergyLoss(ParticleStack stack, double radius)
	{
		if (stack != null)
		{
			Particle particle = stack.getParticle();
			return (long) (stack.getMeanEnergy() * Math.pow(particle.getCharge(), 2) / (6 * Math.pow(particle.getMass(), 4) * Math.pow(radius, 2)));
		}
		return 0;
	}
	
	public static long collisionEnergy(long energy1, long energy2)
	{
		return  (long) (2*Math.sqrt(energy1*energy2));
	}
	
	public static long particleOutputAmount(long inputAmount, long recipeAmount, double crossSection, double efficency)
	{
		return  (long) (inputAmount*recipeAmount*Math.min(crossSection*efficency,1.0));
	}
	
	public static long particleOutputAmountCollisionChamber(long inputAmount1,long inputAmount2, long energy1,long energy2, long recipeAmount, double crossSection, double efficency)
	{
		return  (long) (recipeAmount*Math.min(inputAmount1, inputAmount2)* Math.min(crossSection*efficency*(1-Math.abs(energy1-energy2)/(energy1+energy2)), 1.0));
	}
	
	
	
	public static long particleOutputEnergy(long inputEnergy, long recipeQ,long particleNumber)
	{
		return (inputEnergy+recipeQ)/particleNumber;
	}
	


	
	
	
	
	
}
