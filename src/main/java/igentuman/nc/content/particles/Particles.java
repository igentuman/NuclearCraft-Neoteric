package igentuman.nc.content.particles;

import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.Map;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.NuclearCraft.rl;

/**
 *  * source https://github.com/Lach01298/QMD
 */

public class Particles
{
	public static final Map<String,Particle> particles = new HashMap<>();
	
	private static ResourceLocation particleLoc(String name){
		return rl("textures/particles/" + name + ".png");
	}

	//quarks
	public static Particle up;
	public static Particle down;
	public static Particle charm;
	public static Particle strange;
	public static Particle top;
	public static Particle bottom;
	public static Particle antiup;
	public static Particle antidown;
	public static Particle anticharm;
	public static Particle antistrange;
	public static Particle antitop;
	public static Particle antibottom;
	
	//leptons
	public static Particle electron;
	public static Particle electron_neutrino;
	public static Particle muon;
	public static Particle muon_neutrino;
	public static Particle tau;
	public static Particle tau_neutrino;
	public static Particle positron;
	public static Particle electron_antineutrino;
	public static Particle antimuon;
	public static Particle muon_antineutrino;
	public static Particle antitau;
	public static Particle tau_antineutrino;
	
	//bosons
	public static Particle photon;
	public static Particle gluon;
	public static Particle w_plus_boson;
	public static Particle w_minus_boson;
	public static Particle z_boson;
	public static Particle higgs_boson;
	
	//Nucleons
	public static Particle proton;
	public static Particle antiproton;
	public static Particle neutron;
	public static Particle antineutron;
	
	//nuclei
	public static Particle deuteron;
	public static Particle antideuteron;
	public static Particle alpha;
	public static Particle antialpha;
	public static Particle triton;
	public static Particle antitriton;
	public static Particle helion;
	public static Particle antihelion;
	
	public static Particle boron_ion;
	public static Particle calcium_48_ion;

	
	//Pions
	public static Particle pion_plus;
	public static Particle pion_naught;
	public static Particle pion_minus;
	
	
	//Kaons
	public static Particle kaon_plus;
	public static Particle kaon_naught;
	public static Particle antikaon_naught;
	public static Particle kaon_minus;
	
	//Eta mesons
	public static Particle eta;
	public static Particle eta_prime;
	public static Particle charmed_eta;
	public static Particle bottom_eta;
	
	//sigma baryons
	public static Particle sigma_plus;
	public static Particle antisigma_plus;
	public static Particle sigma_minus;
	public static Particle antisigma_minus;
	
	//Delta baryons
	public static Particle delta_plus_plus;
	public static Particle antidelta_plus_plus;
	public static Particle delta_minus;
	public static Particle antidelta_minus;


	//other
	public static Particle glueball;
	
	public static void registerParticle(Particle particle)
	{
		if(!particles.containsKey(particle.getName()))
		{
			particles.put(particle.getName(), particle);
		}
		else
		{
			debugLog("tried registering paticle " + particle.getName() + " but " + particle.getName() + " already exists");
		}
	}
	
	
	
	public static Particle getParticleFromName(String name)
	{
		if(name != null)
		{
			if (!particles.containsKey(name))
			{
				debugLog("there is no particle with name " + name);
				return null;
			}
			return particles.get(name);
		}
		return null;
	}
	
	public static void init()
	{
		
		//quarks
		up = new Particle("up_quark", particleLoc("up_quark"),2.2,2d/3d,1d/2d,true,true);
		down = new Particle("down_quark",particleLoc("down_quark"),4.7,-1d/3d,1d/2d,true,true);
		charm = new Particle("charm_quark",particleLoc("charm_quark"),1280d,2d/3d,1d/2d,true,true);
		strange = new Particle("strange_quark",particleLoc("strange_quark"),95d,-1d/3d,1d/2d,true,true);
		top = new Particle("top_quark",particleLoc("top_quark"),173000d,2d/3d,1d/2d,true,true);
		bottom = new Particle("bottom_quark",particleLoc("bottom_quark"),4180d,-1d/3d,1d/2d,true,true);
		
		//antiquarks
		antiup = makeAntiParticle(up,"antiup_quark",particleLoc("antiup_quark"));
		antidown = makeAntiParticle(down,"antidown_quark",particleLoc("antidown_quark"));
		anticharm = makeAntiParticle(charm,"anticharm_quark",particleLoc("anticharm_quark"));
		antistrange = makeAntiParticle(strange,"antistrange_quark",particleLoc("antistrange_quark"));
		antitop = makeAntiParticle(top,"antitop_quark",particleLoc("antitop_quark"));
		antibottom = makeAntiParticle(bottom,"antibottom_quark",particleLoc("antibottom_quark"));
		
		//leptons
		electron = new Particle("electron",particleLoc("electron"),0.511,-1,1d/2d,true);
		muon = new Particle("muon",particleLoc("muon"),106,-1,1d/2d,true);
		tau = new Particle("tau",particleLoc("tau"),1780,-1,1d/2d,true);
		electron_neutrino = new Particle("electron_neutrino",particleLoc("electron_neutrino"),0.00000012,0,1d/2d,true);
		muon_neutrino = new Particle("muon_neutrino",particleLoc("muon_neutrino"),0.00000012,0,1d/2d,true);
		tau_neutrino = new Particle("tau_neutrino",particleLoc("tau_neutrino"),0.00000012,0,1d/2d,true);
		
		//antileptons
		positron = makeAntiParticle(electron,"positron",particleLoc("positron"));
		antimuon = makeAntiParticle(muon,"antimuon",particleLoc("antimuon"));
		antitau = makeAntiParticle(tau,"antitau",particleLoc("antitau"));
		electron_antineutrino = makeAntiParticle(electron_neutrino,"electron_antineutrino",particleLoc("electron_antineutrino"));
		muon_antineutrino = makeAntiParticle(muon_neutrino,"muon_antineutrino",particleLoc("muon_antineutrino"));
		tau_antineutrino = makeAntiParticle(tau_neutrino,"tau_antineutrino",particleLoc("tau_antineutrino"));
		
		//bosons
		photon = new Particle("photon",particleLoc("photon"),0,0,1,false,false);
		gluon = new Particle("gluon",particleLoc("gluon"),0,0,1,false,true);
		w_plus_boson = new Particle("w_plus_boson",particleLoc("w_plus_boson"),80400d,1,1,true,false);
		w_minus_boson = new Particle("w_minus_boson",particleLoc("w_minus_boson"),80400d,-1,1,true,false);
		z_boson = new Particle("z_boson",particleLoc("z_boson"),91200d,0,1,false,false);
		higgs_boson = new Particle("higgs_boson",particleLoc("higgs_boson"),125000d,0,0,true,false);
		
		
		//register anti particles
		up.setAntiParticle(antiup);
		down.setAntiParticle(antidown);
		charm.setAntiParticle(anticharm);
		strange.setAntiParticle(antistrange);
		top.setAntiParticle(antitop);
		bottom.setAntiParticle(antibottom);
		electron.setAntiParticle(positron);
		electron_neutrino.setAntiParticle(electron_antineutrino);
		muon.setAntiParticle(antimuon);
		muon_neutrino.setAntiParticle(muon_antineutrino);
		tau.setAntiParticle(antitau);
		tau_neutrino.setAntiParticle(tau_antineutrino);
		w_plus_boson.setAntiParticle(w_minus_boson);

		initComposites();
		register();
	}

	public static void register()
	{
		registerParticle(up);
		registerParticle(antiup);
		registerParticle(down);
		registerParticle(antidown);
		registerParticle(charm);
		registerParticle(anticharm);
		registerParticle(strange);
		registerParticle(antistrange);
		registerParticle(top);
		registerParticle(antitop);
		registerParticle(bottom);
		registerParticle(antibottom);
		
		registerParticle(electron);
		registerParticle(positron);
		registerParticle(electron_neutrino);
		registerParticle(electron_antineutrino);
		registerParticle(muon);
		registerParticle(antimuon);
		registerParticle(muon_neutrino);
		registerParticle(muon_antineutrino);
		registerParticle(tau);
		registerParticle(antitau);
		registerParticle(tau_neutrino);
		registerParticle(tau_antineutrino);
		
		
		registerParticle(photon);
		registerParticle(gluon);
		registerParticle(w_plus_boson);
		registerParticle(w_minus_boson);
		registerParticle(z_boson);
		registerParticle(higgs_boson);
		
		registerParticle(proton);
		registerParticle(antiproton);
		registerParticle(neutron);
		registerParticle(antineutron);

		registerParticle(deuteron);
		registerParticle(antideuteron);
		registerParticle(alpha);
		registerParticle(antialpha);
		registerParticle(triton);
		registerParticle(antitriton);
		registerParticle(helion);
		registerParticle(antihelion);

		registerParticle(boron_ion);
		registerParticle(calcium_48_ion);
		
		registerParticle(pion_plus);
		registerParticle(pion_naught);
		registerParticle(pion_minus);
		
		registerParticle(kaon_plus);
		registerParticle(kaon_naught);
		registerParticle(antikaon_naught);
		registerParticle(kaon_minus);
		
		registerParticle(eta);
		registerParticle(eta_prime);
		registerParticle(charmed_eta);
		registerParticle(bottom_eta);
		
		registerParticle(glueball);
		
		registerParticle(sigma_plus);
		registerParticle(antisigma_plus);
		registerParticle(sigma_minus);
		registerParticle(antisigma_minus);
		
		registerParticle(delta_plus_plus);
		registerParticle(antidelta_plus_plus);
		registerParticle(delta_minus);
		registerParticle(antidelta_minus);
		
	}
	
	
	
	private static void initComposites()
	{
		//Nucleons
		proton = new Particle("proton",particleLoc("proton"),938d,1,1d/2d,true,true);
		proton.addComponentParticle(up,2);
		proton.addComponentParticle(down);
		antiproton = makeAntiParticle(proton, "antiproton",particleLoc("antiproton"));
		
		neutron = new Particle("neutron",particleLoc("neutron"),940d,0,1d/2d,true,true);
		neutron.addComponentParticle(up);
		neutron.addComponentParticle(down,2);
		antineutron = makeAntiParticle(neutron, "antineutron",particleLoc("antineutron"));
		
		
		//nuclei
		deuteron = new Particle("deuteron",particleLoc("deuteron"),1876d,1,1,true,true);
		deuteron.addComponentParticle(proton);
		deuteron.addComponentParticle(neutron);
		antideuteron = makeAntiParticle(deuteron, "antideuteron",particleLoc("antideuteron"));
		
		alpha = new Particle("alpha",particleLoc("alpha"),3727d,2,0,true,true);
		alpha.addComponentParticle(proton,2);
		alpha.addComponentParticle(neutron,2);

		antialpha = makeAntiParticle(alpha, "antialpha",particleLoc("antialpha"));
		
		
		triton = new Particle("triton",particleLoc("triton"),2809d,1,1d/2d,true,true);
		triton.addComponentParticle(proton);
		triton.addComponentParticle(neutron,2);
		antitriton = makeAntiParticle(triton, "antitriton",particleLoc("antitriton"));
		
		helion = new Particle("helion",particleLoc("helion"),2808d,2,1d/2d,true,true);
		helion.addComponentParticle(proton,2);
		helion.addComponentParticle(neutron);
		antihelion = makeAntiParticle(helion, "antihelion",particleLoc("antihelion"));
		
		
		boron_ion = new Particle("boron_ion",particleLoc("boron_ion"),10250d,1,1d/2d,true,true);
		boron_ion.addComponentParticle(proton,5);
		boron_ion.addComponentParticle(neutron,6);
		boron_ion.addComponentParticle(electron,4);
		
		calcium_48_ion = new Particle("calcium_48_ion",particleLoc("calcium_48_ion"),44600d,1,0,true,true);
		calcium_48_ion.addComponentParticle(proton,20);
		calcium_48_ion.addComponentParticle(neutron,28);
		calcium_48_ion.addComponentParticle(electron,19);
		
		
		//Pions
		pion_plus = new Particle("pion_plus",particleLoc("pion_plus"),140d,1,0,true,true);
		pion_plus.addComponentParticle(up);
		pion_plus.addComponentParticle(antidown);
		pion_minus = makeAntiParticle(pion_plus, "pion_minus",particleLoc("pion_minus"));
		
		pion_naught = new Particle("pion_naught",particleLoc("pion_naught"),135d,0,0,true,true);
		pion_naught.addComponentParticle(up);
		pion_naught.addComponentParticle(antiup);
		
		
		//Kaons
		kaon_plus =  new Particle("kaon_plus",particleLoc("kaon_plus"),464d,1,0,true,true);
		kaon_plus.addComponentParticle(up);
		kaon_plus.addComponentParticle(antistrange);
		kaon_minus =  makeAntiParticle(kaon_plus, "kaon_minus",particleLoc("kaon_minus"));
		
		kaon_naught =  new Particle("kaon_naught",particleLoc("kaon_naught"),498d,1,0,true,true);
		kaon_naught.addComponentParticle(down);
		kaon_naught.addComponentParticle(antistrange);
		antikaon_naught =  makeAntiParticle(kaon_naught, "antikaon_naught",particleLoc("antikaon_naught"));
		
		//eta
		
		eta =  new Particle("eta",particleLoc("eta"),548d,0,0,true,true);
		eta.addComponentParticle(down);
		eta.addComponentParticle(antidown);
		
		eta_prime =  new Particle("eta_prime",particleLoc("eta_prime"),958d,0,0,true,true);
		eta_prime.addComponentParticle(strange);
		eta_prime.addComponentParticle(antistrange);
		
		charmed_eta =  new Particle("charmed_eta",particleLoc("charmed_eta"),2980d,0,0,true,true);
		charmed_eta.addComponentParticle(charm);
		charmed_eta.addComponentParticle(anticharm);
		
		bottom_eta =  new Particle("bottom_eta",particleLoc("bottom_eta"),9400d,0,0,true,true);
		bottom_eta.addComponentParticle(bottom);
		bottom_eta.addComponentParticle(antibottom);
		
		
		
		//other
		glueball =  new Particle("glueball",particleLoc("glueball"),1730d,0,0,false,true);
		glueball.addComponentParticle(gluon,2);
		
		
		
		//sigma
		sigma_plus =  new Particle("sigma_plus",particleLoc("sigma_plus"),1190d,1,1d/2d,true,true);
		sigma_plus.addComponentParticle(up,2);
		sigma_plus.addComponentParticle(strange);
		antisigma_plus= makeAntiParticle(sigma_plus, "antisigma_plus",particleLoc("antisigma_plus"));
		
		sigma_minus =  new Particle("sigma_minus",particleLoc("sigma_minus"),1200d,-1,1d/2d,true,true);
		sigma_minus.addComponentParticle(down,2);
		sigma_minus.addComponentParticle(strange);
		antisigma_minus= makeAntiParticle(sigma_minus, "antisigma_minus",particleLoc("antisigma_minus"));
		
		//delta
		delta_plus_plus = new Particle("delta_plus_plus",particleLoc("delta_plus_plus"),1232d,2,3d/2d,true,true);
		delta_plus_plus.addComponentParticle(up,3);
		antidelta_plus_plus = makeAntiParticle(delta_plus_plus,"antidelta_plus_plus",particleLoc("antidelta_plus_plus"));
		
		delta_minus = new Particle("delta_minus",particleLoc("delta_minus"),1232d,-1,3d/2d,true,true);
		delta_minus.addComponentParticle(down,3);
		antidelta_minus = makeAntiParticle(delta_minus,"antidelta_minus",particleLoc("antidelta_minus"));
	}
	
	
	public static Particle makeAntiParticle(Particle particle, String name,ResourceLocation texture)
	{
		double mass = particle.getMass();
		
		double charge = 0;
		if(particle.getCharge() != 0)
		{
			charge = -particle.getCharge();
		}
		
		double spin = particle.getSpin();
		boolean weakCharged = particle.interactsWithWeak();
		boolean isColoured = particle.interactsWithStrong();
		 HashMap<Particle, Integer> anticomponents = new  HashMap<Particle, Integer>();
		for (Map.Entry<Particle, Integer> component : particle.getComponentParticles().entrySet())
		{
			anticomponents.put(component.getKey().getAntiParticle(), component.getValue());
		}

		Particle antiparticle = new Particle(name, texture, mass, charge, spin, weakCharged,isColoured);
		antiparticle.setComponentParticles(anticomponents);
		antiparticle.setAntiParticle(particle);
		return antiparticle;

	}
	
	
	
	
	
}
