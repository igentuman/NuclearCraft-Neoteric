package igentuman.nc.datagen.recipes.recipes;

import lach_01298.qmd.QMDConstants;
import lach_01298.qmd.config.QMDConfig;
import lach_01298.qmd.particle.*;
import lach_01298.qmd.recipe.*;
import lach_01298.qmd.recipe.ingredient.IParticleIngredient;
import nc.recipe.ingredient.*;
import nc.util.FluidStackHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TargetChamberRecipesOld extends QMDRecipeHandler
{

	public TargetChamberRecipesOld()
	{
		super("target_chamber", 1, 1, 1, 1, 1, 3);
		
	}

	@Override
	public void addRecipes()
	{
	
		Map<String,String> SpallationMaterials = new HashMap<String, String>();
		
		SpallationMaterials.put("ingotCalifornium252All","wasteSpallationCalifornium");
		SpallationMaterials.put("ingotCalifornium251All","wasteSpallationCalifornium");
		SpallationMaterials.put("ingotCalifornium250All","wasteSpallationCalifornium");
		SpallationMaterials.put("ingotCalifornium249All","wasteSpallationCalifornium");
		SpallationMaterials.put("ingotBerkelium248All","wasteSpallationBerkelium");
		SpallationMaterials.put("ingotBerkelium247All","wasteSpallationBerkelium");
		SpallationMaterials.put("ingotCurium247All","wasteSpallationCurium");
		SpallationMaterials.put("ingotCurium246All","wasteSpallationCurium");
		SpallationMaterials.put("ingotCurium245All","wasteSpallationCurium");
		SpallationMaterials.put("ingotCurium243All","wasteSpallationCurium");
		SpallationMaterials.put("ingotAmericium243All","wasteSpallationAmericium");
		SpallationMaterials.put("ingotAmericium242All","wasteSpallationAmericium");
		SpallationMaterials.put("ingotAmericium241All","wasteSpallationAmericium");
		SpallationMaterials.put("ingotPlutonium242All","wasteSpallationPlutonium");
		SpallationMaterials.put("ingotPlutonium241All","wasteSpallationPlutonium");
		SpallationMaterials.put("ingotPlutonium239All","wasteSpallationPlutonium");
		SpallationMaterials.put("ingotPlutonium238All","wasteSpallationPlutonium");
		SpallationMaterials.put("ingotNeptunium237All","wasteSpallationNeptunium");
		SpallationMaterials.put("ingotNeptunium236All","wasteSpallationNeptunium");
		SpallationMaterials.put("ingotUranium238All","wasteSpallationUranium");
		SpallationMaterials.put("ingotUranium235All","wasteSpallationUranium");
		SpallationMaterials.put("ingotUranium234","wasteSpallationUranium");
		SpallationMaterials.put("ingotUranium233All","wasteSpallationUranium");
		SpallationMaterials.put("dustProtactinium233","wasteSpallationProtactinium");
		SpallationMaterials.put("dustProtactinium231","wasteSpallationProtactinium");
		SpallationMaterials.put("ingotThorium","wasteSpallationThorium");
		SpallationMaterials.put("dustRadium","wasteSpallationRadium");
		SpallationMaterials.put("dustPolonium","wasteSpallationPolonium");
		SpallationMaterials.put("dustBismuth","wasteSpallationBismuth");
		SpallationMaterials.put("ingotLead","wasteSpallationLead");
		SpallationMaterials.put("ingotMercury","wasteSpallationMercury");
		SpallationMaterials.put("ingotGold","wasteSpallationGold");
		SpallationMaterials.put("ingotPlatinum","wasteSpallationPlatinum");
		SpallationMaterials.put("ingotIridium","wasteSpallationIridium");
		SpallationMaterials.put("ingotIridium192","wasteSpallationIridium");
		SpallationMaterials.put("ingotOsmium","wasteSpallationOsmium");
		SpallationMaterials.put("ingotTungsten","wasteSpallationTungsten");
		SpallationMaterials.put("ingotHafnium","wasteSpallationHafnium");
		
		
		//addRecipe(itemInput, fluidInput, particleInput [particle, amount, minEnergy, minFocus], itemOutput, fluidOutput
		//particleOutput+ [particle, amount], particleOutput0 [particle, amount], particleOutput- [particle, amount], maxEnergy, crossSection, energyRelased, radiation)
		
		// old recipe
		//addRecipe(itemInput, particleInput [particle, amount, minEnergy, minFocus], item output,
		//particleOutput+ [particle, amount], particleOutput0 [particle, amount], particleOutput- [particle, amount], maxEnergy, crossSection, energyRelased, radiation)
		
		
		

		
		
		//proton reactions
		addQMDBalancedRecipe("ingotBoron11",new ParticleStack(Particles.proton, 1, 400), null,
				new ParticleStack(Particles.alpha,3), null, null, 900, 0.2, 8680);
		
		addQMDBalancedRecipe("ingotBeryllium",new ParticleStack(Particles.proton, 1, 1000), "ingotLithium6",
				new ParticleStack(Particles.alpha), null, null, 7500, 0.625, 2640);
		
		addQMDBalancedRecipe("ingotAluminum",  new ParticleStack(Particles.proton, 1, 1500), "itemSilicon",
				null, new ParticleStack(Particles.photon), null, 2000, 0.02, 11100);
		
		addQMDBalancedRecipe(null,fluidStack("tritium", FluidStackHelper.BUCKET_VOLUME),  new ParticleStack(Particles.proton, 1, 2400), null, null,
				new ParticleStack(Particles.helion), new ParticleStack(Particles.neutron), null, 3600, 0.5, -764);
		
		addQMDBalancedRecipe("ingotBoron10", new ParticleStack(Particles.proton, 1, 4000),"ingotBeryllium7",
				new ParticleStack(Particles.alpha), null, null,6000, 0.8, 1700);
		
		addQMDBalancedRecipe(null,fluidStack("fluorine", FluidStackHelper.BUCKET_VOLUME),  new ParticleStack(Particles.proton, 1, 4000), null, fluidStack("oxygen", FluidStackHelper.BUCKET_VOLUME),
				new ParticleStack(Particles.alpha), null, null, 11000, 0.5, 8620);
			
		addQMDBalancedRecipe("ingotCopper", new ParticleStack(Particles.proton, 1, 4500), "ingotZinc",
				null, new ParticleStack(Particles.photon), null, 5900, 0.04, 7200);
		
		addQMDBalancedRecipe("ingotCobalt", new ParticleStack(Particles.proton, 1, 4600), "ingotNickel",
				null, new ParticleStack(Particles.photon), null, 5600, 0.2, 9020);
		
		addQMDBalancedRecipe(null,fluidStack("deuterium", FluidStackHelper.BUCKET_VOLUME),  new ParticleStack(Particles.proton, 1, 6000), null, null,
				new ParticleStack(Particles.helion), new ParticleStack(Particles.photon), null, 10000, 0.02, 5490);
		
		addQMDBalancedRecipe("ingotOsmium", new ParticleStack(Particles.proton, 1, 8300), "ingotIridium192",
				null, new ParticleStack(Particles.neutron), null, 10300, 0.16, -2340);
		
		addQMDBalancedRecipe("ingotManganese", new ParticleStack(Particles.proton, 1, 10000), "ingotIron",
				null, new ParticleStack(Particles.photon), null, 19500, 0.02, 9670);
		
		addQMDBalancedRecipe(null,fluidStack("deuterium", FluidStackHelper.BUCKET_VOLUME),  new ParticleStack(Particles.proton, 1, 11000), null, null,
				new ParticleStack(Particles.proton,2), new ParticleStack(Particles.neutron), null, 20500, 0.16, -2230);
		
		addQMDBalancedRecipe("ingotThorium", new ParticleStack(Particles.proton, 1, 11500), "dustProtactinium231",
				null, new ParticleStack(Particles.neutron,2), null, 16000, 0.625, -7340);
		
		addQMDBalancedRecipe("ingotUranium238", new ParticleStack(Particles.proton, 1, 12000), "ingotNeptunium237",
				null, new ParticleStack(Particles.neutron, 2), null, 16500, 0.32, -6930);
		
		addQMDBalancedRecipe("ingotGold", null, new ParticleStack(Particles.proton, 1, 12500), null, fluidStack("mercury", FluidStackHelper.INGOT_VOLUME),
				null, new ParticleStack(Particles.neutron,2), null, 14000, 0.5, -8680);
		
		addQMDBalancedRecipe("ingotPlutonium242", new ParticleStack(Particles.proton, 1, 12500), "ingotAmericium241",
				null, new ParticleStack(Particles.neutron,2), null, 16500, 0.4, -7580);
		
		addQMDBalancedRecipe("dustBismuth", new ParticleStack(Particles.proton, 1, 14000), "dustPolonium",
				null, new ParticleStack(Particles.photon), null, 19000, 0.02, 4470);
		
		addQMDBalancedRecipe("ingotBoron11", new ParticleStack(Particles.proton, 1, 15500), "dustGraphite",
				null, new ParticleStack(Particles.photon), null, 26000, 0.02, 15000);
		
		addQMDBalancedRecipe("ingotCalcium", new ParticleStack(Particles.proton, 1, 16500), "ingotPotassium",
				new ParticleStack(Particles.proton,2), null, null, 25000, 1, -7810);
		
		addQMDBalancedRecipe(null,fluidStack("nitrogen", FluidStackHelper.BUCKET_VOLUME),  new ParticleStack(Particles.proton, 1, 17000), "ingotBeryllium7", null,
				new ParticleStack(Particles.alpha,2), null, null, 26000, 0.032, -8930);
		
		addQMDBalancedRecipe(null,fluidStack("oxygen", FluidStackHelper.BUCKET_VOLUME),  new ParticleStack(Particles.proton, 1, 19000), "dustGraphite", null,
				new ParticleStack(Particles.alpha), null, new ParticleStack(Particles.proton), 25500, 0.25, -6140);
		
		addQMDBalancedRecipe("itemSilicon",  new ParticleStack(Particles.proton, 1, 19000), "ingotAluminum",
				new ParticleStack(Particles.proton,2), null, null, 28000, 1, -11100);
		
		addQMDBalancedRecipe("ingotUranium238", new ParticleStack(Particles.proton, 1, 19000), "ingotNeptunium236",
				null, new ParticleStack(Particles.neutron,3), null, 23000, 1, -13500);
		
		addQMDBalancedRecipe("ingotSodium", new ParticleStack(Particles.proton, 1, 20000), "ingotSodium22",
				new ParticleStack(Particles.proton), new ParticleStack(Particles.neutron), null, 28000,  0.5, -12400);
		
		addQMDBalancedRecipe("dustBismuth", new ParticleStack(Particles.proton, 1, 20000), "dustLead",
				new ParticleStack(Particles.alpha), null, null, 24000, 0.04, 10900);
		
		addQMDBalancedRecipe("ingotUranium235", new ParticleStack(Particles.proton, 1, 20500), "ingotNeptunium236",
				null, new ParticleStack(Particles.photon), null, 30000, 0.02, 4320);
		
		addQMDBalancedRecipe("ingotGold", new ParticleStack(Particles.proton, 1, 21000), "ingotPlatinum",
				new ParticleStack(Particles.alpha), null, null, 25000, 0.064, 9000);
		
		addQMDBalancedRecipe("ingotMagnesium26", new ParticleStack(Particles.proton, 1 , 23000), "ingotSodium22",
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron), null, 29000, 0.1, -14000);
		
		addQMDBalancedRecipe("dustGraphite", new ParticleStack(Particles.proton, 1, 27000), "ingotBeryllium7",
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron), new ParticleStack(Particles.proton), 35000, 0.08, -15000);
		
		addQMDBalancedRecipe("ingotBoron11", new ParticleStack(Particles.proton, 1, 30000), "ingotBeryllium7",
				new ParticleStack(Particles.triton), new ParticleStack(Particles.neutron,2), null, 33000, 0.05, -30000);
		
		addQMDBalancedRecipe("ingotCalcium", null, new ParticleStack(Particles.proton, 1, 30000), null, fluidStack("argon",FluidStackHelper.BUCKET_VOLUME),
				new ParticleStack(Particles.proton,3), null, null, 43000, 0.20, -13700);
		
		addQMDBalancedRecipe("itemSilicon",  new ParticleStack(Particles.proton, 1, 32000), "ingotMagnesium26",
				new ParticleStack(Particles.proton,3), null, null, 50000, 0.2, -18800);
		
		addQMDBalancedRecipe(null, fluidStack("nitrogen",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.proton, 1, 33000), "dustGraphite", null,
				new ParticleStack(Particles.proton), null, new ParticleStack(Particles.deuteron), 46000, 0.125, -9760);
		
		addQMDBalancedRecipe("ingotMagnesium24", new ParticleStack(Particles.proton, 1 , 38000), "ingotSodium22",
				new ParticleStack(Particles.proton, 2), new ParticleStack(Particles.neutron), null, 56000, 0.32, -23000);
		
		addQMDBalancedRecipe("dustGraphite", new ParticleStack(Particles.proton, 1, 40000), "ingotBoron10",
				new ParticleStack(Particles.proton,2), null, null, 50000, 0.064, -25000);
		
		addQMDBalancedRecipe(null, fluidStack("oxygen",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.proton, 1, 40000), null, fluidStack("nitrogen",FluidStackHelper.BUCKET_VOLUME),
				new ParticleStack(Particles.proton), null, new ParticleStack(Particles.deuteron), 65000, 0.05, -20200);
		
		addQMDBalancedRecipe("ingotAluminum",  new ParticleStack(Particles.proton, 1, 40000), "ingotSodium22",
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron), new ParticleStack(Particles.proton), 50000, 0.08, -21500);
		
		addQMDBalancedRecipe("ingotCopper", new ParticleStack(Particles.proton, 1, 45000), "ingotNickel",
				new ParticleStack(Particles.alpha), null, null, 56000, 0.625, 4270);
		
		addQMDBalancedRecipe("dustGraphite", new ParticleStack(Particles.proton, 1, 60000), "ingotBoron11",
				new ParticleStack(Particles.proton), null, new ParticleStack(Particles.deuteron), 85000, 0.08, -25000);
		
		addQMDBalancedRecipe("dustRadium", new ParticleStack(Particles.proton, 1, 60000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron), null, 600000, 0.2, 0);
		
		addQMDBalancedRecipe("ingotThorium", new ParticleStack(Particles.proton, 1, 60000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron), null, 600000, 1.0, 0);
		
		addQMDBalancedRecipe("ingotUranium233All", new ParticleStack(Particles.proton, 1, 60000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron), null, 600000, 1.0, 0);
		
		addQMDBalancedRecipe("ingotUranium234All", new ParticleStack(Particles.proton, 1, 60000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron,2), null, 600000, 1.0, 0);
		
		addQMDBalancedRecipe("ingotUranium235All", new ParticleStack(Particles.proton, 1, 60000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron,4), null, 600000, 1.0, 0);
		
		addQMDBalancedRecipe("ingotUranium238All", new ParticleStack(Particles.proton, 1, 60000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron,2), null, 600000, 1.0, 0);
		
		addQMDBalancedRecipe("ingotNeptunium237All", new ParticleStack(Particles.proton, 1, 60000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron,2), null, 600000, 1.0, 0);
		
		addQMDBalancedRecipe("ingotPlutonium239All", new ParticleStack(Particles.proton, 1, 60000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron,4), null, 600000, 1.0, 0);
		
		addQMDBalancedRecipe("ingotPlutonium241All", new ParticleStack(Particles.proton, 1, 60000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron,4), null, 600000, 1.0, 0);
		
		addQMDBalancedRecipe("ingotPlutonium242All", new ParticleStack(Particles.proton, 1, 60000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron,4), null, 600000, 1.0, 0);
		
		addQMDBalancedRecipe("ingotAmericium241All", new ParticleStack(Particles.proton, 1, 60000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron,4), null, 600000, 1.0, 0);
		
		addQMDBalancedRecipe("ingotAmericium243All", new ParticleStack(Particles.proton, 1, 60000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron,2), null, 600000, 1.0, 0);
		
		addQMDBalancedRecipe("ingotCopernicium291All", new ParticleStack(Particles.proton, 1, 60000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron,8), null, 600000, 1.0, 0);
		
		addQMDBalancedRecipe(null, fluidStack("oxygen",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.proton, 1, 65000), "ingotBoron10", null,
				new ParticleStack(Particles.alpha), null, new ParticleStack(Particles.helion), 150000, 0.02, -25300);
		
		addQMDBalancedRecipe("ingotGold", new ParticleStack(Particles.proton, 1, 100000), "ingotIridium192",
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron), new ParticleStack(Particles.proton), 200000, 0.02, -5780);
		
		addQMDBalancedRecipe("dustBismuth", new ParticleStack(Particles.proton, 1, 100000), "wasteFissionLight",
				null, new ParticleStack(Particles.neutron), null, 600000, 0.4, 0);
		
		addQMDBalancedRecipe("dustGraphite", new ParticleStack(Particles.proton, 1, 150000), "ingotBeryllium",
				new ParticleStack(Particles.proton), null, new ParticleStack(Particles.triton), 1000000, 0.02, -26000);
		
		addQMDBalancedRecipe("ingotAluminum",  new ParticleStack(Particles.proton, 1, 155000), "ingotSodium",
				new ParticleStack(Particles.proton,3), new ParticleStack(Particles.neutron,2), null, 170000, 0.02, -37400);

		addQMDBalancedRecipe("ingotPlatinum", new ParticleStack(Particles.proton, 1, 200000), "wasteFissionLight",
				null,new ParticleStack(Particles.neutron), null, 600000, 0.02, 0);
		
		addQMDBalancedRecipe("ingotGold", new ParticleStack(Particles.proton, 1, 200000), "wasteFissionLight",
				null, new ParticleStack(Particles.neutron), null, 600000, 0.16, 0);
		
		addQMDBalancedRecipe(null, fluidStack("mercury", FluidStackHelper.INGOT_VOLUME), new ParticleStack(Particles.proton, 1, 200000), "wasteFissionLight", null,
				null, new ParticleStack(Particles.neutron), null, 600000, 0.02, 0);
		addQMDBalancedRecipe("ingotMercury", new ParticleStack(Particles.proton, 1, 200000), "wasteFissionLight",
				null, new ParticleStack(Particles.neutron), null, 600000, 0.02, 0);
		
		addQMDBalancedRecipe("ingotLead", new ParticleStack(Particles.proton, 1, 200000), "wasteFissionLight",
				null, new ParticleStack(Particles.neutron), null, 600000, 0.25, 0);
		
		addQMDBalancedRecipe("ingotTungsten", new ParticleStack(Particles.proton, 1, 400000), "wasteFissionLight",
				null, new ParticleStack(Particles.neutron), null, 600000, 0.08, 0);
		

			// Pion production
		for(Entry<String, String> material : SpallationMaterials.entrySet())
		{
			addQMDBalancedRecipe(material.getKey(), new ParticleStack(Particles.proton, 1, 600000), material.getValue(),
					new ParticleStack(Particles.pion_plus),null,new ParticleStack(Particles.pion_minus), 5000000, 0.2, -279000);
		}
		addQMDBalancedRecipe(null, fluidStack("mercury", FluidStackHelper.INGOT_VOLUME), new ParticleStack(Particles.proton, 1, 600000), "wasteSpallationMercury", null,
				new ParticleStack(Particles.pion_plus),null,new ParticleStack(Particles.pion_minus), 5000000, 0.2, -279000);
			
			
			// antiproton production
		for(Entry<String, String> material : SpallationMaterials.entrySet())
		{
			addQMDBalancedRecipe(material.getKey(), new ParticleStack(Particles.proton, 1, 5630000), material.getValue(),
					new ParticleStack(Particles.proton), null, new ParticleStack(Particles.antiproton), 20000000, 0.2, -1880000);
		}
		addQMDBalancedRecipe(null, fluidStack("mercury", FluidStackHelper.INGOT_VOLUME), new ParticleStack(Particles.proton, 1, 5630000), "wasteSpallationMercury", null,
				new ParticleStack(Particles.proton), null, new ParticleStack(Particles.antiproton), 20000000, 0.2, -1880000);
		
		
		
		
		// neutron reactions
		
		addQMDBalancedRecipe(null, fluidStack("helium_3",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.neutron, 1, 0), null, null,
				new ParticleStack(Particles.proton), null, new ParticleStack(Particles.triton), 18000, 0.1, 764);
		
		addQMDBalancedRecipe("ingotBeryllium7", new ParticleStack(Particles.neutron, 1, 0), "ingotLithium7",
				new ParticleStack(Particles.proton), null, null, 10000, 1, 2150);
		
		addQMDBalancedRecipe("ingotBoron10", new ParticleStack(Particles.neutron, 1, 0), "ingotBoron11",
				null, new ParticleStack(Particles.photon), null, 1000, 0.02, 11500);
		
		addQMDBalancedRecipe("ingotSodium22", null, new ParticleStack(Particles.neutron, 1, 0), null, fluidStack("neon",FluidStackHelper.BUCKET_VOLUME),
				new ParticleStack(Particles.proton), null, null, 11000, 0.25, 4140);
		
		addQMDBalancedRecipe("ingotCobalt", new ParticleStack(Particles.neutron, 1, 0), "ingotCobalt60",
				null, new ParticleStack(Particles.photon), null, 10000, 0.5, 7490);
		
		addQMDBalancedRecipe("ingotIridium192", new ParticleStack(Particles.neutron, 1, 0), "ingotIridium",
				null, new ParticleStack(Particles.photon), null, 5000, 1, 7770);
		
		addQMDBalancedRecipe("ingotUranium233", new ParticleStack(Particles.neutron, 1, 0), "ingotUranium234",
				null, new ParticleStack(Particles.photon), null, 5000,  1.0, 6840);
		
		addQMDBalancedRecipe("ingotUranium234", new ParticleStack(Particles.neutron, 1, 0), "ingotUranium235",
				null, new ParticleStack(Particles.photon), null, 5000,  1.0, 5300);
		
		addQMDBalancedRecipe("ingotNeptunium236",new ParticleStack(Particles.neutron, 1, 0), "ingotNeptunium237",
				null, new ParticleStack(Particles.photon), null, 14000, 1, 6580);
		
		addQMDBalancedRecipe("ingotPlutonium238", new ParticleStack(Particles.neutron, 1, 0), "ingotPlutonium239",
				null, new ParticleStack(Particles.photon), null, 5000, 1, 5650);
		
		addQMDBalancedRecipe("ingotPlutonium241", new ParticleStack(Particles.neutron, 1, 0), "ingotPlutonium242",
				null, new ParticleStack(Particles.photon), null, 5000, 1, 6310);
		
		addQMDBalancedRecipe("ingotAmericium241", new ParticleStack(Particles.neutron, 1, 0), "ingotAmericium242",
				null, new ParticleStack(Particles.photon), null, 5000, 1, 5540);
		
		addQMDBalancedRecipe("ingotAmericium242", new ParticleStack(Particles.neutron, 1, 0), "ingotAmericium243",
				null, new ParticleStack(Particles.photon), null, 5000, 1, 5540);
		
		addQMDBalancedRecipe("dustSulfur", new ParticleStack(Particles.neutron, 1, 2800), "itemSilicon",
				new ParticleStack(Particles.alpha), null, null, 4000, 0.2, 2550);
		
		addQMDBalancedRecipe("ingotLithium6", new ParticleStack(Particles.neutron, 1, 3000), null,
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron), new ParticleStack(Particles.deuteron), 21000, 0.2, 60);
		
		addQMDBalancedRecipe("ingotBeryllium", new ParticleStack(Particles.neutron, 1, 4000), null,
				new ParticleStack(Particles.alpha,2), new ParticleStack(Particles.neutron,2), null, 14000, 0.5, -1570);
		
		addQMDBalancedRecipe("ingotLithium7", new ParticleStack(Particles.neutron, 1, 5000), null,
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron), new ParticleStack(Particles.triton), 17000, 0.25, -935);
		
		addQMDBalancedRecipe("ingotZinc", new ParticleStack(Particles.neutron, 1, 5500), "ingotNickel",
				new ParticleStack(Particles.alpha), null, null, 13000, 0.25, 4880);
		
		addQMDBalancedRecipe(null, fluidStack("nitrogen",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.neutron, 1, 6000), "dustGraphite", null,
				new ParticleStack(Particles.triton), null, null, 16000, 0.02, -3500);
		
		addQMDBalancedRecipe("ingotPlutonium242", new ParticleStack(Particles.neutron, 1, 8500), "ingotPlutonium241",
				null, new ParticleStack(Particles.neutron, 2), null, 13000, 1, -6310);
		
		addQMDBalancedRecipe("ingotIridium", new ParticleStack(Particles.neutron, 1, 9000), "ingotIridium192",
				null, new ParticleStack(Particles.neutron,2), null, 19500, 1, -7770);
		
		addQMDBalancedRecipe("ingotPlutonium239", new ParticleStack(Particles.neutron, 1, 9000), "ingotPlutonium238",
				null, new ParticleStack(Particles.neutron,2), null, 14000, 0.32, -5650);
		
		addQMDBalancedRecipe("ingotAmericium243", new ParticleStack(Particles.neutron, 1, 9000), "ingotAmericium242",
				null, new ParticleStack(Particles.neutron, 2), null, 14500, 1, -5540);
		
		addQMDBalancedRecipe(null, fluidStack("tritium",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.neutron, 1, 10000), null, null,
				new ParticleStack(Particles.deuteron), new ParticleStack(Particles.neutron,2), null, 18000, 0.04, -6260);
		
		addQMDBalancedRecipe("ingotNeptunium237",new ParticleStack(Particles.neutron, 1, 10000), "ingotNeptunium236",
				null, new ParticleStack(Particles.neutron, 2), null, 14000, 1, -6580);
		
		addQMDBalancedRecipe(null, fluidStack("deuterium",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.neutron, 1, 11000), null, null,
				new ParticleStack(Particles.proton), new ParticleStack(Particles.neutron,2), null, 54000, 0.16, -2230);
		
		addQMDBalancedRecipe("ingotIron", new ParticleStack(Particles.neutron, 1, 11000), "ingotChromium",
				new ParticleStack(Particles.alpha), null, null, 22000, 0.1, 1350);
		
		addQMDBalancedRecipe("ingotCopper", new ParticleStack(Particles.neutron, 1, 11000), "ingotNickel",
				new ParticleStack(Particles.proton), new ParticleStack(Particles.neutron), null, 15000, 1.0, -5610);
		
		addQMDBalancedRecipe("ingotChromium", new ParticleStack(Particles.neutron, 1, 12000), "ingotTitanium",
				new ParticleStack(Particles.alpha), null, null, 20000, 0.16, -191);
		
		addQMDBalancedRecipe("ingotBoron11", new ParticleStack(Particles.neutron, 1, 12500), "ingotBeryllium",
				new ParticleStack(Particles.triton), null, null, 20000, 0.04, -9050);
		
		addQMDBalancedRecipe("ingotAluminum", new ParticleStack(Particles.neutron, 1, 13000), "ingotMagnesium26",
				new ParticleStack(Particles.proton), new ParticleStack(Particles.neutron), null, 30000, 0.8, -7760);
		
		addQMDBalancedRecipe("ingotMagnesium24", null, new ParticleStack(Particles.neutron, 1, 14000), null, fluidStack("neon",FluidStackHelper.BUCKET_VOLUME),
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron), null, 32000, 0.1, -8290);
		
		addQMDBalancedRecipe("ingotCalcium", new ParticleStack(Particles.neutron, 1, 14000), "ingotPotassium",
				new ParticleStack(Particles.proton), new ParticleStack(Particles.neutron), null, 24000, 1, -7820);
		
		addQMDBalancedRecipe("ingotZinc", new ParticleStack(Particles.neutron, 1, 14000), "ingotCopper",
				new ParticleStack(Particles.proton), new ParticleStack(Particles.neutron), null, 24500, 1, -7200);
		
		addQMDBalancedRecipe("ingotBeryllium", new ParticleStack(Particles.neutron, 1, 15000), "ingotLithium7",
				new ParticleStack(Particles.triton), null, null, 26000, 0.064, -9930);
		
		addQMDBalancedRecipe("ingotPotassium", null, new ParticleStack(Particles.neutron, 1, 15000), null, fluidStack("chlorine",FluidStackHelper.BUCKET_VOLUME),
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron), null, 32000, 0.1, -6200);
		
		addQMDBalancedRecipe("ingotZirconium", new ParticleStack(Particles.neutron, 1, 15000), "ingotStrontium",
				new ParticleStack(Particles.alpha), null, null, 20000, 0.04, 2780);
		
		addQMDBalancedRecipe("ingotBoron11", new ParticleStack(Particles.neutron, 1, 17000), "ingotBoron10",
				null, new ParticleStack(Particles.neutron,2), null, 29000, 0.05, -11500);
		
		addQMDBalancedRecipe("ingotSodium", new ParticleStack(Particles.neutron, 1, 17000), "ingotSodium22",
				null, new ParticleStack(Particles.neutron,2), null, 28000, 0.25, -12400);
		
		addQMDBalancedRecipe("ingotUranium235", new ParticleStack(Particles.neutron, 1, 17000), "ingotUranium233",
				null, new ParticleStack(Particles.neutron, 3), null, 23500,  0.625, -12100);
		
		addQMDBalancedRecipe("itemSilicon", new ParticleStack(Particles.neutron, 1, 18000), "ingotAluminum",
				new ParticleStack(Particles.proton), new ParticleStack(Particles.neutron), null, 27000, 0.8, -11100);
		
		addQMDBalancedRecipe("ingotNickel", new ParticleStack(Particles.neutron, 1, 19000), "ingotIron",
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron), null, 30000, 0.8, -5380);
		
		addQMDBalancedRecipe("ingotPlatinum", new ParticleStack(Particles.neutron, 1, 19000), "ingotOsmium",
				new ParticleStack(Particles.alpha), null, null, 27000, 0.025, 9750);
		
		addQMDBalancedRecipe("dustTerbium", new ParticleStack(Particles.neutron, 1, 20800), "dustEuropium155",
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron), null, 34000, 0.5, 883);
		
		addQMDBalancedRecipe("ingotZirconium", new ParticleStack(Particles.neutron, 1, 21000), "ingotYttrium",
				new ParticleStack(Particles.deuteron), null, null, 36000, 0.625, -5620);
		
		addQMDBalancedRecipe(null, fluidStack("oxygen",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.neutron, 1, 22000), "ingotBeryllium", null,
				new ParticleStack(Particles.alpha,2), null, null, 35000, 0.02, -10800);
		
		addQMDBalancedRecipe("ingotNiobium", new ParticleStack(Particles.neutron, 1, 22000), "ingotYttrium",
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron), null, 34000, 0.125, -909);
		
		addQMDBalancedRecipe("ingotUranium238", new ParticleStack(Particles.neutron, 1, 26000), "ingotUranium235",
				null, new ParticleStack(Particles.neutron, 4), null, 34000, 0.55, -17800);
		
		addQMDBalancedRecipe("ingotManganese", new ParticleStack(Particles.neutron, 1, 29000), "ingotChromium",
				new ParticleStack(Particles.triton), null, null, 46000, 0.625, -8790);
		
		addQMDBalancedRecipe("dustGraphite", new ParticleStack(Particles.neutron, 1, 29500), "ingotBoron11",
				new ParticleStack(Particles.deuteron), null, null, 60000, 0.2, -13200);
		
		addQMDBalancedRecipe("ingotCobalt", new ParticleStack(Particles.neutron, 1, 30000), "ingotIron",
				new ParticleStack(Particles.triton), null, null, 50000, 0.4, -8420);

		addQMDBalancedRecipe("ingotYttrium", new ParticleStack(Particles.neutron, 1, 30000), "ingotStrontium",
				new ParticleStack(Particles.deuteron), null, null, 49000, 0.32, -4330);
		
		addQMDBalancedRecipe("ingotSodium", null, new ParticleStack(Particles.neutron, 1, 30000), null, fluidStack("neon",FluidStackHelper.BUCKET_VOLUME),
				new ParticleStack(Particles.triton), null, null, 60000, 0.1, -10200);
		
		addQMDBalancedRecipe("ingotGold", new ParticleStack(Particles.neutron, 1, 30000), "ingotIridium",
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron), null, 45000, 0.05, 1990);
		
		addQMDBalancedRecipe("ingotMagnesium24", new ParticleStack(Particles.neutron, 1, 35000), "ingotSodium22",
				new ParticleStack(Particles.deuteron), new ParticleStack(Particles.neutron), null, 60000, 0.25, -21400);
		
		addQMDBalancedRecipe(null, fluidStack("helium",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.neutron, 1, 40000), null, null,
				new ParticleStack(Particles.helion), new ParticleStack(Particles.neutron,2), null, 60000, 0.02, -20600);
		
		addQMDBalancedRecipe("ingotAluminum", new ParticleStack(Particles.neutron, 1, 43000), "ingotSodium22",
				new ParticleStack(Particles.helion), new ParticleStack(Particles.neutron,3), null, 80000, 0.05, -42100);
		
		addQMDBalancedRecipe("ingotCopernicium291All", new ParticleStack(Particles.neutron, 1, 60000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron,8), null, 1000000, 1, 0);
		
		addQMDBalancedRecipe("dustBismuth", new ParticleStack(Particles.neutron, 1, 70000), "dustLead",
				new ParticleStack(Particles.triton), null, null, 150000, 0.2, -2170);
		
		addQMDBalancedRecipe("dustGraphite", new ParticleStack(Particles.neutron, 1, 105000), "ingotBeryllium7",
				new ParticleStack(Particles.helion), new ParticleStack(Particles.neutron,3), null, 150000, 0.032, -45800);
		
	
		
		// photon reactions
		
		addQMDBalancedRecipe(null, fluidStack("deuterium",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.photon, 1, 2500), null, null,
				new ParticleStack(Particles.proton), new ParticleStack(Particles.neutron), null, 14000, 0.02, -2230);
		
		addQMDBalancedRecipe("ingotLithium6", new ParticleStack(Particles.photon, 1, 5500), null,
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron), new ParticleStack(Particles.proton), 17000, 0.02, -3700);
		
		addQMDBalancedRecipe(null, fluidStack("helium_3",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.photon, 1, 9000), null, null,
				new ParticleStack(Particles.proton,2), new ParticleStack(Particles.neutron), null, 36000, 0.02, -7720);
		
		addQMDBalancedRecipe("ingotNeptunium237", new ParticleStack(Particles.photon, 1, 9750), "ingotNeptunium236",
				null, new ParticleStack(Particles.neutron), null, 14500, 1.0,-6580);
		
		addQMDBalancedRecipe("ingotPlutonium239", new ParticleStack(Particles.photon, 1, 10000), "ingotPlutonium238",
				null, new ParticleStack(Particles.neutron), null, 13500, 0.4,-5650);
		
		addQMDBalancedRecipe("ingotAmericium243", new ParticleStack(Particles.photon, 1, 10000), "ingotAmericium242",
				null, new ParticleStack(Particles.neutron), null, 12500, 1,-6360);
		
		addQMDBalancedRecipe("ingotTungsten", new ParticleStack(Particles.photon, 1, 11000), "ingotHafnium",
				new ParticleStack(Particles.alpha), null, null, 16500, 0.25, 2680);
		
		addQMDBalancedRecipe("ingotZirconium", new ParticleStack(Particles.photon, 1, 11500), "ingotYttrium",
				new ParticleStack(Particles.proton), null, null, 19000, 0.04,-7830);
		
		addQMDBalancedRecipe("ingotIridium", new ParticleStack(Particles.photon, 1, 12000), "ingotIridium192",
				null, new ParticleStack(Particles.neutron), null, 16000, 0.5,-7770);
		
		addQMDBalancedRecipe("dustBismuth", new ParticleStack(Particles.photon, 1, 12000), "dustLead",
				new ParticleStack(Particles.proton), new ParticleStack(Particles.neutron), null, 15000, 0.5,-10770);
		
		addQMDBalancedRecipe("ingotNiobium", new ParticleStack(Particles.photon, 1, 13000), "ingotYttrium",
				new ParticleStack(Particles.alpha), null, null, 21000, 0.02,-909);
		
		addQMDBalancedRecipe("ingotUranium235", new ParticleStack(Particles.photon, 1, 13500), "ingotUranium233",
				null, new ParticleStack(Particles.neutron,2), null, 18000, 0.32,-12100);
		
		addQMDBalancedRecipe("ingotIron", new ParticleStack(Particles.photon, 1, 14500), "ingotManganese",
				new ParticleStack(Particles.proton), null, null, 21000, 0.125,-9670);
		
		addQMDBalancedRecipe("ingotYttrium", new ParticleStack(Particles.photon, 1, 16000), "ingotStrontium",
				new ParticleStack(Particles.proton), null, null, 23000, 0.016,-6560);
		
		addQMDBalancedRecipe("ingotAluminum", new ParticleStack(Particles.photon,1, 17500), "ingotMagnesium26",
				new ParticleStack(Particles.proton), null, null, 23000, 0.032,-7760);
		
		addQMDBalancedRecipe("itemSilicon", new ParticleStack(Particles.photon, 1, 18000), "ingotAluminum",
				new ParticleStack(Particles.proton), null, null, 23000, 0.08,-11100);
		
		addQMDBalancedRecipe("ingotCalcium", new ParticleStack(Particles.photon, 1, 18000), "ingotPotassium",
				new ParticleStack(Particles.proton), null, null, 22000, 0.05,-7820);
		
		addQMDBalancedRecipe("ingotLithium6", new ParticleStack(Particles.photon, 1, 18500), null,
				new ParticleStack(Particles.helion), null, new ParticleStack(Particles.triton), 25500, 0.02, -14300);
		
		addQMDBalancedRecipe("ingotLithium7", new ParticleStack(Particles.photon, 1, 18500), "ingotLithium6",
				null, new ParticleStack(Particles.neutron), null, 24000, 0.02, -7250);
				
		addQMDBalancedRecipe("ingotCopper", new ParticleStack(Particles.photon, 1, 19500), "ingotNickel",
				new ParticleStack(Particles.proton), new ParticleStack(Particles.neutron), null, 27000, 0.05,-16200);
		
		addQMDBalancedRecipe(null, fluidStack("nitrogen",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.photon, 1, 20000), "dustGraphite", null,
				new ParticleStack(Particles.proton), new ParticleStack(Particles.neutron), null, 27000, 0.02, -12000);
		
		addQMDBalancedRecipe("ingotMagnesium26", new ParticleStack(Particles.photon, 1, 21000), "ingotMagnesium24",
				null, new ParticleStack(Particles.neutron, 2), null, 28000, 0.04, -18400);
		
		addQMDBalancedRecipe("ingotBoron11", new ParticleStack(Particles.photon, 1, 24500), null,
				new ParticleStack(Particles.alpha,2), new ParticleStack(Particles.neutron), new ParticleStack(Particles.deuteron), 30500, 0.02, -14800);
		
		addQMDBalancedRecipe("ingotBeryllium", new ParticleStack(Particles.photon, 1, 26000), null,
				new ParticleStack(Particles.alpha,2), new ParticleStack(Particles.neutron), null, 46000, 0.02, 472);
		
		addQMDBalancedRecipe(null, fluidStack("oxygen",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.photon, 1, 29000), null, fluidStack("nitrogen",FluidStackHelper.BUCKET_VOLUME),
				new ParticleStack(Particles.proton), new ParticleStack(Particles.neutron), null, 41000, 0.02, -22400);
		
		addQMDBalancedRecipe("dustGraphite", new ParticleStack(Particles.photon, 1, 31500), "ingotBeryllium7",
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron), null, 42500, 0.02, -25200);
		
		addQMDBalancedRecipe("dustGraphite", new ParticleStack(Particles.photon, 1, 42500), "ingotLithium6",
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron), new ParticleStack(Particles.proton), 55000, 0.02, -30300);
		
		
		
		// electron reactions
		
		addQMDBalancedRecipe("ingotIron", new ParticleStack(Particles.electron, 1, 50000), "ingotChromium",
				new ParticleStack(Particles.alpha), null, new ParticleStack(Particles.electron), 100000, 0.01, -6590);
		
		addQMDBalancedRecipe("ingotCobalt", new ParticleStack(Particles.electron, 1, 50000), "ingotManganese",
				new ParticleStack(Particles.alpha), null, new ParticleStack(Particles.electron), 100000, 0.01, -5920);
		
		addQMDBalancedRecipe("ingotZinc", new ParticleStack(Particles.electron, 1, 50000), "ingotNickel",
				new ParticleStack(Particles.alpha), null, new ParticleStack(Particles.electron), 100000, 0.01, -2930);
		
		addQMDBalancedRecipe("ingotZirconium", new ParticleStack(Particles.electron, 1, 60000), "ingotYttrium",
				new ParticleStack(Particles.proton), null, new ParticleStack(Particles.electron), 130000, 0.01, -7840);
		
		addQMDBalancedRecipe("ingotUranium233", new ParticleStack(Particles.electron, 1, 170000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron), new ParticleStack(Particles.electron), 300000, 0.01, 0);
		
		addQMDBalancedRecipe("ingotUranium235", new ParticleStack(Particles.electron, 1, 170000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron), new ParticleStack(Particles.electron), 300000, 0.01, 0);
		
		addQMDBalancedRecipe("ingotPlutonium239", new ParticleStack(Particles.electron, 1, 170000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron), new ParticleStack(Particles.electron), 300000, 0.01, 0);
		
		addQMDBalancedRecipe("ingotNeptunium237", new ParticleStack(Particles.electron, 1, 180000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron), new ParticleStack(Particles.electron), 300000, 0.01, 0);
		
		addQMDBalancedRecipe("ingotUranium238", new ParticleStack(Particles.electron, 1, 200000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron), new ParticleStack(Particles.electron), 300000, 0.01, 0);
		
		addQMDBalancedRecipe("ingotAmericium242", new ParticleStack(Particles.electron, 1, 200000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron), new ParticleStack(Particles.electron), 300000, 0.01, 0);
		
		addQMDBalancedRecipe("ingotThorium", new ParticleStack(Particles.electron, 1, 220000), "wasteFissionHeavy",
				null, new ParticleStack(Particles.neutron), new ParticleStack(Particles.electron), 300000, 0.01, 0);
		
		
		//deuteron reactions
		
		addQMDBalancedRecipe(null, fluidStack("tritium",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.deuteron, 1, 50), null, null,
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron), null, 600, 0.5, 17600);
		
		addQMDBalancedRecipe(null, fluidStack("deuterium",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.deuteron, 1, 500), null, null,
				new ParticleStack(Particles.triton), new ParticleStack(Particles.neutron), null, 3000, 0.16, 2870);
		
		addQMDBalancedRecipe("ingotLithium6", new ParticleStack(Particles.deuteron, 1, 500), "ingotBeryllium7",
				null, new ParticleStack(Particles.neutron), null, 3000, 0.16, 2870);
		
		addQMDBalancedRecipe("ingotBeryllium", new ParticleStack(Particles.deuteron, 1, 1500), "ingotLithium7",
				new ParticleStack(Particles.alpha), null, null, 5000, 0.625, 7660);
		
		addQMDBalancedRecipe("ingotBoron11", new ParticleStack(Particles.deuteron,  1, 1500), "dustGraphite",
				null, new ParticleStack(Particles.neutron), null, 3000, 0.5, 13200);
		
		addQMDBalancedRecipe("ingotLithium7", new ParticleStack(Particles.deuteron, 1, 3500), null,
				 new ParticleStack(Particles.alpha,2), new ParticleStack(Particles.neutron), null, 9000, 1.0, 15100);
		
		addQMDBalancedRecipe(null, fluidStack("oxygen",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.deuteron, 1, 3500), null, fluidStack("nitrogen",FluidStackHelper.BUCKET_VOLUME),
				new ParticleStack(Particles.alpha), null, null, 10000, 0.25, 3620);
		
		addQMDBalancedRecipe("dustGraphite", new ParticleStack(Particles.deuteron, 1, 5000), "ingotBoron10",
				new ParticleStack(Particles.alpha), null, null, 13000, 0.625, -830);
		
		addQMDBalancedRecipe("ingotCobalt",new ParticleStack(Particles.deuteron, 1, 5000), "ingotCobalt60",
				new ParticleStack(Particles.proton), null, null, 10000, 0.4, 5270);
		
		addQMDBalancedRecipe("ingotMagnesium24",new ParticleStack(Particles.deuteron, 1, 6000), "ingotSodium22",
				new ParticleStack(Particles.alpha), null, null, 11000, 0.4, 2470);
		
		addQMDBalancedRecipe("dustBismuth",new ParticleStack(Particles.deuteron, 1, 10000), "dustPolonium",
				null, new ParticleStack(Particles.neutron), null, 15000, 0.08, 2250);
		
		addQMDBalancedRecipe("ingotUranium233",new ParticleStack(Particles.deuteron, 1, 10000), "ingotUranium234",
				new ParticleStack(Particles.proton), null, null, 16000, 0.08, 4620);
		
		addQMDBalancedRecipe("ingotPlutonium241",new ParticleStack(Particles.deuteron, 1, 10500), "ingotAmericium241",
				null, new ParticleStack(Particles.neutron,2), null, 19500, 0.5, -3500);
		
		addQMDBalancedRecipe("ingotUranium234",new ParticleStack(Particles.deuteron, 1, 11000), "ingotUranium235",
				new ParticleStack(Particles.proton), null, null, 17000, 0.32, 3070);
		
		addQMDBalancedRecipe("ingotPlutonium238",new ParticleStack(Particles.deuteron, 1, 11000), "ingotPlutonium239",
				new ParticleStack(Particles.proton), null, null, 17000, 0.2, 3420);
		
		addQMDBalancedRecipe("ingotUranium235",new ParticleStack(Particles.deuteron, 1, 11500), "ingotNeptunium236",
				null, new ParticleStack(Particles.neutron), null, 19500, 0.032, 2090);
		
		addQMDBalancedRecipe("ingotOsmium",new ParticleStack(Particles.deuteron, 1, 12000), "ingotIridium192",
				null, new ParticleStack(Particles.neutron,2), null, 14500, 1.0, -4560);
		
		addQMDBalancedRecipe("ingotPlutonium242", new ParticleStack(Particles.deuteron,1,12000), "ingotAmericium242",
				null, new ParticleStack(Particles.neutron, 2), null, 16000,  0.5, -4270);
		
		addQMDBalancedRecipe("ingotAmericium243", new ParticleStack(Particles.deuteron,1,12000), "ingotCurium243",
				null, new ParticleStack(Particles.neutron, 2), null, 15000,  0.2, -3520);
		
		addQMDBalancedRecipe("ingotGold", null, new ParticleStack(Particles.deuteron,1,18000), null, fluidStack("mercury", FluidStackHelper.INGOT_VOLUME),
				null, new ParticleStack(Particles.neutron, 3), null, 24000,  0.5, -10900);
		
		addQMDBalancedRecipe("ingotUranium238",new ParticleStack(Particles.deuteron, 1, 24000), "ingotNeptunium236",
				null, new ParticleStack(Particles.neutron,4), null, 30000, 1.0, -15700);
		
		addQMDBalancedRecipe("ingotSodium",new ParticleStack(Particles.deuteron, 1, 30000), "ingotSodium22",
				new ParticleStack(Particles.deuteron), new ParticleStack(Particles.neutron), null, 55000, 0.4, -12400);
		
		addQMDBalancedRecipe("ingotBeryllium", new ParticleStack(Particles.deuteron, 1, 34000), "ingotBeryllium7",
				new ParticleStack(Particles.deuteron), new ParticleStack(Particles.neutron,2), null, 53000, 0.064, -20600);
		
		addQMDBalancedRecipe("ingotBeryllium", new ParticleStack(Particles.deuteron, 1, 55000), null,
				 new ParticleStack(Particles.alpha,2), new ParticleStack(Particles.neutron), new ParticleStack(Particles.deuteron), 150000, 1.0, -1570);
		
		addQMDBalancedRecipe("ingotAluminum",new ParticleStack(Particles.deuteron, 1, 56000), "ingotSodium22",
				new ParticleStack(Particles.alpha), null, new ParticleStack(Particles.triton), 200000, 0.08, -15200);
		
		addQMDBalancedRecipe("ingotYttrium",new ParticleStack(Particles.deuteron, 1, 60000), "ingotStrontium",
				new ParticleStack(Particles.alpha), null, null, 200000, 0.064, 8400);
		
				// antideuteron production
				
		for(Entry<String, String> material : SpallationMaterials.entrySet())
		{
			addQMDBalancedRecipe(material.getKey(), new ParticleStack(Particles.deuteron, 1, 11300000), material.getValue(),
					new ParticleStack(Particles.deuteron), null, new ParticleStack(Particles.antideuteron), 20000000, 0.1, -3750000);
		}

		addQMDBalancedRecipe(null, fluidStack("mercury", FluidStackHelper.INGOT_VOLUME), new ParticleStack(Particles.deuteron, 1, 11300000), "wasteSpallationMercury", null,
				new ParticleStack(Particles.deuteron), null, new ParticleStack(Particles.antideuteron), 20000000, 0.1, -3750000);

		// triton reactions
		
		addQMDBalancedRecipe("ingotBeryllium", new ParticleStack(Particles.triton, 1, 1000), "ingotBoron11",
				null, new ParticleStack(Particles.neutron), null, 7000, 1, 9050);
		
		addQMDBalancedRecipe(null, fluidStack("tritium",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.triton, 1, 1200), null, null,
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron,2), null, 3000, 0.1, 11300);
		
		addQMDBalancedRecipe("dustGraphite", new ParticleStack(Particles.triton, 1, 2500), "ingotBoron11",
				new ParticleStack(Particles.alpha), null, null, 7500, 0.625, 4370);
		
		addQMDBalancedRecipe("ingotLithium6", new ParticleStack(Particles.triton, 1, 8000), "ingotBeryllium7",
				null, new ParticleStack(Particles.neutron,2), null, 12000, 0.064, -3390);
		
		
		//Helion reactions
		
		addQMDBalancedRecipe("ingotLithium6", new ParticleStack(Particles.helion, 1, 11000), "ingotBeryllium7",
				new ParticleStack(Particles.deuteron), null, null, 30000, 0.5, -397);
		
		addQMDBalancedRecipe("ingotCobalt", new ParticleStack(Particles.helion, 1, 14000), "ingotCobalt60",
				new ParticleStack(Particles.proton,2), null, null, 24000, 0.1, -226);
		
		addQMDBalancedRecipe("dustGraphite", new ParticleStack(Particles.helion, 1, 22000), "ingotBeryllium7",
				new ParticleStack(Particles.alpha,2), null, null, 31000, 0.125, -4670);
		
		addQMDBalancedRecipe("ingotBeryllium", new ParticleStack(Particles.helion, 1, 23000), "ingotBeryllium7",
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron), null, 28000, 0.16, 14);
		
		addQMDBalancedRecipe("ingotLead", new ParticleStack(Particles.helion, 1, 23000), "dustPolonium",
				null, new ParticleStack(Particles.neutron), null, 30000, 0.02, 42);
		
		addQMDBalancedRecipe("ingotNeptunium237", new ParticleStack(Particles.helion, 1, 23500), "ingotPlutonium238",
				new ParticleStack(Particles.proton), new ParticleStack(Particles.neutron), null, 30000, 0.02, -2230);
		
		addQMDBalancedRecipe("ingotLithium6", new ParticleStack(Particles.helion, 1, 30000), null,
				new ParticleStack(Particles.alpha,2), null, new ParticleStack(Particles.proton), 150000, 0.625, 18400);
		
		addQMDBalancedRecipe("ingotBoron10", new ParticleStack(Particles.helion, 1, 30000), "ingotBeryllium7",
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron), new ParticleStack(Particles.proton), 47000, 0.2, -6060);
		
		addQMDBalancedRecipe("dustBismuth", new ParticleStack(Particles.helion, 1, 30000), "dustPolonium",
				new ParticleStack(Particles.proton), new ParticleStack(Particles.neutron), null, 43000, 0.2, -3250);
		
		addQMDBalancedRecipe("ingotAluminum", new ParticleStack(Particles.helion, 1, 90000), "ingotSodium22",
				new ParticleStack(Particles.alpha,2), null, null, 165000, 0.16, -912);
				
		
		// alpha reactions
		
		addQMDBalancedRecipe("ingotBeryllium", new ParticleStack(Particles.alpha, 1, 4000), "dustGraphite",
				null, new ParticleStack(Particles.neutron), null, 6500, 1.0, 4680);
		
		addQMDBalancedRecipe("ingotMagnesium26", new ParticleStack(Particles.alpha, 1, 4000), "itemSilicon",
				null, new ParticleStack(Particles.neutron), null, 6000, 0.32, -988);
		
		addQMDBalancedRecipe("ingotSodium", new ParticleStack(Particles.alpha, 1, 4500), "ingotMagnesium26",
				new ParticleStack(Particles.proton), null, null, 6000, 0.16, 1310);
		
		addQMDBalancedRecipe(null, fluidStack("fluorine",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.alpha, 1, 6000), "ingotSodium22", null,
				null, new ParticleStack(Particles.neutron), null, 15000, 0.25, -2970);
		
		addQMDBalancedRecipe("ingotLithium7", new ParticleStack(Particles.alpha, 1, 6500), "ingotBoron10",
				null, new ParticleStack(Particles.neutron), null, 7600, 0.5, -3810);
		
		addQMDBalancedRecipe("ingotAluminum", new ParticleStack(Particles.alpha, 1, 12000), "itemSilicon",
				new ParticleStack(Particles.positron), new ParticleStack(Particles.neutron), new ParticleStack(Particles.electron_neutrino), 17500, 0.4, -3660);
		
		addQMDBalancedRecipe(null, fluidStack("nitrogen",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.alpha, 1, 14000), null, fluidStack("oxygen",FluidStackHelper.BUCKET_VOLUME),
				new ParticleStack(Particles.proton), new ParticleStack(Particles.neutron), null, 26000, 0.1, -5850);
		
		addQMDBalancedRecipe("ingotCopper", new ParticleStack(Particles.alpha, 1, 16000), "ingotZinc",
				new ParticleStack(Particles.proton), null, null, 20000, 0.16, -2060);
		
		addQMDBalancedRecipe("ingotOsmium", new ParticleStack(Particles.alpha, 1, 19000), "ingotPlatinum",
				null, new ParticleStack(Particles.neutron), null, 27500, 0.025, -9750);
		
		addQMDBalancedRecipe("ingotUranium235", new ParticleStack(Particles.alpha, 1, 21000), "ingotPlutonium238",
				null, new ParticleStack(Particles.neutron), null, 32000, 0.02, -11900);
		
		addQMDBalancedRecipe("dustYtterbium", new ParticleStack(Particles.alpha, 1, 21200), "ingotHafnium",
				null, new ParticleStack(Particles.neutron,2), null, 26600, 1.0, -15800);
		
		addQMDBalancedRecipe("ingotLead", new ParticleStack(Particles.alpha, 1, 26000), "dustPolonium",
				null, new ParticleStack(Particles.neutron,2), null, 32000, 1.0, -20500);
		
		addQMDBalancedRecipe("ingotLithium6", new ParticleStack(Particles.alpha, 1, 27000), "ingotBoron10",
				null, new ParticleStack(Particles.photon), null, 45000, 0.02, 9330);
		
		addQMDBalancedRecipe("ingotUranium233", new ParticleStack(Particles.alpha, 1, 27000), "ingotNeptunium236",
				new ParticleStack(Particles.proton), null, null, 30000, 0.02, -11800);
		
		addQMDBalancedRecipe("ingotUranium238", new ParticleStack(Particles.alpha, 1, 27000), "ingotPlutonium239",
				null, new ParticleStack(Particles.neutron,3), null, 30000, 0.625, -24100);
		
		addQMDBalancedRecipe("ingotPlutonium239", new ParticleStack(Particles.alpha, 1, 28000), "ingotAmericium242",
				new ParticleStack(Particles.proton), null, null, 30000, 0.02, -12300);
		
		addQMDBalancedRecipe(null, fluidStack("nitrogen",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.alpha, 1, 30000), "dustGraphite", null,
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron), new ParticleStack(Particles.proton), 56000, 0.25, -12000);
		
		addQMDBalancedRecipe("ingotUranium235", new ParticleStack(Particles.alpha, 1, 30000), "ingotNeptunium236",
				new ParticleStack(Particles.triton), null, null, 35000, 0.016, -15500);
		
		addQMDBalancedRecipe("ingotUranium238", new ParticleStack(Particles.alpha, 1, 34500), "ingotPlutonium238",
				null, new ParticleStack(Particles.neutron,3), null, 42000, 0.128, -29700);
		
		addQMDBalancedRecipe("dustGraphite", new ParticleStack(Particles.alpha, 1, 38000), "ingotBoron11",
				new ParticleStack(Particles.alpha), null,new ParticleStack(Particles.proton), 50000, 0.25, -15400);
		
		addQMDBalancedRecipe("ingotCobalt", new ParticleStack(Particles.alpha, 1, 38000), "ingotCobalt60",
				new ParticleStack(Particles.proton,2), new ParticleStack(Particles.neutron), null, 54000, 0.16, -20800);
		
		addQMDBalancedRecipe("dustGraphite", new ParticleStack(Particles.alpha, 1, 53000), "ingotBoron10",
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron),new ParticleStack(Particles.proton), 69000, 0.16, -26900);
			
		addQMDBalancedRecipe("ingotBeryllium", new ParticleStack(Particles.alpha, 1, 100000), "ingotBeryllium7",
				new ParticleStack(Particles.alpha), new ParticleStack(Particles.neutron,2), null, 145000, 0.064, -20500);

		
		
		
		
		

		
		// Boron Ion reactions
		
		addQMDBalancedRecipe("waferSilicon", new ParticleStack(Particles.boron_ion, 1, 600,2), "siliconPDoped",
				null,null,null, 1000, 1, 0);
		addQMDBalancedRecipe("ingotLithium7", new ParticleStack(Particles.boron_ion, 1, 6000), "dustGraphite",
				new ParticleStack(Particles.alpha),new ParticleStack(Particles.neutron,2),null, 12000, 0.2, 5010);
		
		
		// Ca-48 reactions
		
		addQMDBalancedRecipe("ingotBerkelium248", new ParticleStack(Particles.calcium_48_ion, 1, 40000,2), "ingotCopernicium291",
				new ParticleStack(Particles.alpha),new ParticleStack(Particles.neutron),new ParticleStack(Particles.electron_neutrino,2), 50000, 0.02,-24400);
		
		
		
		// Electron antineutrino reactions
			// Inverse beta + decay
		
			
		
		addQMDBalancedRecipe("ingotIridium192", new ParticleStack(Particles.electron_antineutrino, 1, 0), "ingotOsmium",
				new ParticleStack(Particles.positron), null, null, 10000, 0.01, 536);
		
		addQMDBalancedRecipe("ingotAmericium242", new ParticleStack(Particles.electron_antineutrino, 1, 0), "ingotPlutonium242",
				new ParticleStack(Particles.positron), null, null, 10000, 0.01, 240);
		
		addQMDBalancedRecipe("ingotCurium243", new ParticleStack(Particles.electron_antineutrino, 1, 600), "ingotAmericium243",
				new ParticleStack(Particles.positron), null, null, 10600, 0.01, -503);
		
		addQMDBalancedRecipe("ingotBerkelium247", new ParticleStack(Particles.electron_antineutrino, 1, 600), "ingotCurium247",
				new ParticleStack(Particles.positron), null, null, 10600, 0.01, -555);
		
		addQMDBalancedRecipe("ingotNickel", new ParticleStack(Particles.electron_antineutrino, 1, 900), "ingotIron",
				new ParticleStack(Particles.positron,2), new ParticleStack(Particles.electron_neutrino), null, 10900, 0.01, -893);
		
		// Electron neutrino reactions
			// Inverse beta - decay
		
		addQMDBalancedRecipe("ingotCurium247", new ParticleStack(Particles.electron_neutrino, 1, 500), "ingotBerkelium247",
				null, null, new ParticleStack(Particles.electron), 10500, 0.01, -467);
		
		addQMDBalancedRecipe("ingotAmericium243", new ParticleStack(Particles.electron_neutrino, 1, 600), "ingotCurium243",
				null, null, new ParticleStack(Particles.electron), 10600, 0.01, -519);
		
		addQMDBalancedRecipe("ingotPlutonium242", new ParticleStack(Particles.electron_neutrino, 1, 1300), "ingotAmericium242",
				null, null, new ParticleStack(Particles.electron), 11300, 0.01, -1260);
		
		addQMDBalancedRecipe("ingotOsmium", new ParticleStack(Particles.electron_neutrino, 1, 1600), "ingotIridium192",
				null, null, new ParticleStack(Particles.electron), 11600, 0.01, -1560);
		

		

		// Pion reactions
		
		addQMDBalancedRecipe("ingotAluminum", new ParticleStack(Particles.pion_minus, 1, 150000), "ingotSodium22",
				new ParticleStack(Particles.proton), new ParticleStack(Particles.neutron), null, 250000, 0.025, 88500);
		
		addQMDBalancedRecipe(null, fluidStack("argon",FluidStackHelper.BUCKET_VOLUME), new ParticleStack(Particles.pion_plus, 1, 70000), null, fluidStack("chlorine",FluidStackHelper.BUCKET_VOLUME),
				new ParticleStack(Particles.proton,2), new ParticleStack(Particles.neutron), null, 320000, 0.04, 114000);
		
		
		
		// Antiproton reactions
			// antiproton anhilation
		for(Entry<String, String> material : SpallationMaterials.entrySet())
		{
			addQMDBalancedRecipe(material.getKey(), new ParticleStack(Particles.antiproton, 1), material.getValue(),
					new ParticleStack(Particles.pion_plus),new ParticleStack(Particles.pion_naught),new ParticleStack(Particles.pion_minus), 10000000, 1, 1460000);
		}

		addQMDBalancedRecipe(null, fluidStack("mercury", FluidStackHelper.INGOT_VOLUME), new ParticleStack(Particles.antiproton, 1), "wasteSpallationMercury", null,
				new ParticleStack(Particles.pion_plus),new ParticleStack(Particles.pion_naught),new ParticleStack(Particles.pion_minus), 10000000, 1, 1460000);
		
		
		// Antideuteron reactions
			// antideuteron anhilation
		for(Entry<String, String> material : SpallationMaterials.entrySet())
		{
			addQMDBalancedRecipe(material.getKey(), new ParticleStack(Particles.antideuteron, 1), material.getValue(),
					new ParticleStack(Particles.pion_plus,4),new ParticleStack(Particles.pion_naught,4),new ParticleStack(Particles.pion_minus,4), 10000000, 1, 2090000);
		}

		addQMDBalancedRecipe(null, fluidStack("mercury", FluidStackHelper.INGOT_VOLUME), new ParticleStack(Particles.antideuteron, 1), "wasteSpallationMercury", null,
				new ParticleStack(Particles.pion_plus,4),new ParticleStack(Particles.pion_naught,4),new ParticleStack(Particles.pion_minus,4), 10000000, 1, 2090000);
		
		
	}
	
	/** old method without fluids*/
	public void addQMDBalancedRecipe(String itemInput, ParticleStack inputParticle, String itemOutput, ParticleStack outputParticle1, ParticleStack outputParticle2, ParticleStack outputParticle3, int maxEnergy, double crossSection, int energyRelased)
	{
		if(inputParticle != null)
		{
			int recpieAmount = (int) ((QMDConfig.rsf_target_chamber/100d)*(QMDConstants.moleAmount/crossSection));
			inputParticle.setAmount(inputParticle.getAmount()*recpieAmount);
		}
		
		
		IItemIngredient inItem = emptyItemStack();
		if(itemInput != null)
		{
			inItem = QMDRecipeHelper.buildItemIngredient(itemInput);
		}
		
		IItemIngredient outItem = emptyItemStack();
		if(itemOutput != null)
		{
			outItem = QMDRecipeHelper.buildItemIngredient(itemOutput);
		}
		
		IFluidIngredient inputfluid = emptyFluidStack();
		IFluidIngredient outputfluid = emptyFluidStack();

		
		IParticleIngredient outParticle1 = emptyParticleStack();
		IParticleIngredient outParticle2 = emptyParticleStack();
		IParticleIngredient outParticle3 = emptyParticleStack();
		
		if(outputParticle1 != null)
		{
			outParticle1 = QMDRecipeHelper.buildParticleIngredient(outputParticle1);
		}
		if(outputParticle2 != null)
		{
			outParticle2 = QMDRecipeHelper.buildParticleIngredient(outputParticle2);
		}
		if(outputParticle3 != null)
		{
			outParticle3 = QMDRecipeHelper.buildParticleIngredient(outputParticle3);
		}

		addRecipe(inItem, inputfluid, inputParticle, outItem, outputfluid, outParticle1, outParticle2, outParticle3, maxEnergy, crossSection, energyRelased);
	}
	
	public void addQMDBalancedRecipe(String itemInput, FluidIngredient fluidInput,ParticleStack inputParticle, String itemOutput, FluidIngredient fluidOutput,ParticleStack outputParticle1,ParticleStack outputParticle2,ParticleStack outputParticle3, int maxEnergy, double crossSection, int energyRelased)
	{
		if(inputParticle != null)
		{
			int recpieAmount = (int) ((QMDConfig.rsf_target_chamber/100d)*(QMDConstants.moleAmount/crossSection));
			inputParticle.setAmount(inputParticle.getAmount()*recpieAmount);
		}
		
		
		IItemIngredient inItem = emptyItemStack();
		if(itemInput != null)
		{
			inItem = QMDRecipeHelper.buildItemIngredient(itemInput);
		}
		
		IItemIngredient outItem = emptyItemStack();
		if(itemOutput != null)
		{
			outItem = QMDRecipeHelper.buildItemIngredient(itemOutput);
		}
		
		IFluidIngredient inputfluid = emptyFluidStack();
		if(fluidInput != null)
		{
			inputfluid = fluidInput;
		}
		
		IFluidIngredient outputfluid = emptyFluidStack();
		if(fluidOutput != null)
		{
			outputfluid = fluidOutput;
		}
		
		IParticleIngredient outParticle1 = emptyParticleStack();
		IParticleIngredient outParticle2 = emptyParticleStack();
		IParticleIngredient outParticle3 = emptyParticleStack();
		
		if(outputParticle1 != null)
		{
			outParticle1 = QMDRecipeHelper.buildParticleIngredient(outputParticle1);
		}
		if(outputParticle2 != null)
		{
			outParticle2 = QMDRecipeHelper.buildParticleIngredient(outputParticle2);
		}
		if(outputParticle3 != null)
		{
			outParticle3 = QMDRecipeHelper.buildParticleIngredient(outputParticle3);
		}

		addRecipe(inItem, inputfluid, inputParticle, outItem, outputfluid, outParticle1, outParticle2, outParticle3, maxEnergy, crossSection, energyRelased);
	}
	
	

	@Override
	public List fixedExtras(List extras)
	{
		List fixed = new ArrayList(4);
		
		fixed.add(extras.size() > 0 && extras.get(0) instanceof Integer ? ((Number) (extras.get(0))).longValue() : 0l);
		fixed.add(extras.size() > 1 && extras.get(1) instanceof Double ? (double) extras.get(1) : 1D);
		fixed.add(extras.size() > 2 && extras.get(2) instanceof Integer ? ((Number) (extras.get(2))).longValue() : 0l);
		fixed.add(extras.size() > 3 && extras.get(3) instanceof Double ? (double) extras.get(3) : 0D);
		return fixed;
	}
	

}
