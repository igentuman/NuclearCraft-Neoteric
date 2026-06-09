package igentuman.nc.datagen;

import igentuman.nc.setup.registration.*;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.multiblock.particle_chamber.TargetChamberRegistration.TARGET_CHAMBER_BLOCKS;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_CORE_PROXY;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.EXPL_PROXY_BLOCK;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BLOCKS;
import static igentuman.nc.multiblock.particle_chamber.TargetChamberRegistration.TARGET_CHAMBER_DETECTORS;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCKS;
import static igentuman.nc.setup.registration.NCBlocks.*;
import static igentuman.nc.setup.registration.NCItems.*;
import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_BLOCKS;
import static igentuman.nc.util.TextUtils.convertToName;

public class NCLanguageProvider extends LanguageProvider {

    public NCLanguageProvider(DataGenerator gen, String locale) {
        super(gen.getPackOutput(), MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + MODID+"_items", "NuclearCraft Items");
        add("itemGroup." + MODID+"_blocks", "NuclearCraft Blocks");
        add("itemGroup." + MODID+"_fission_reactor", "NuclearCraft Fission Reactor");
        add("itemGroup." + MODID+"_fusion_reactor", "NuclearCraft Fusion Reactor");
        add("itemGroup." + MODID+"_fluids", "NuclearCraft Fluids");
        add("itemGroup." + MODID+"_turbine", "NuclearCraft Turbine");
        add("itemGroup." + MODID+"_kugelblitz", "NuclearCraft Kugelblitz");
        add("itemGroup." + MODID+"_accelerator", "NuclearCraft Accelerators");
        add("entity.nuclearcraft.q36_pulse_projectile", "Quantite Pulse");
        add("entity.nuclearcraft.feral_ghoul", "Feral Ghoul");
        add("entity.nuclearcraft.feral_ghoul_boss", "Boss of the Wasteland");
        add("biome.nuclearcraft.wasteland", "Wasteland");
        ponders();
        ores();
        ingots();
        plates();
        dusts();
        nuggets();
        gems();
        parts();
        chunks();
        blocks();
        food();
        armor();
        records();
        particleSources();
        tools();
        items();
        fuel();
        tooltips();
        isotopes();
        waste();
        shielding();
        buckets();
        fluids();
        processors();
        energyBlocks();
        multiblocks();
        labels();
        messages();
        storageBlocks();
        sounds();
        advancements();
        particles();
    }

    private void ponders() {
        add("nuclearcraft.ponder.fission_reactor.header", "Fission Reactor");
        add("nuclearcraft.ponder.fission_reactor.text_1", "Walls are mainly made of Fission Reactor Casing or Reactor Glass.");
        add("nuclearcraft.ponder.fission_reactor.text_2", "Place the Fission Reactor Controller anywhere you like to form the structure.");
        add("nuclearcraft.ponder.fission_reactor.text_3", "The Reactor Port is a universal block allowing you to load/unload fuel and liquids, read or send redstone signals, and attach computers.");
        add("nuclearcraft.ponder.fission_reactor.text_4", "Use as many ports as you like.");
        add("nuclearcraft.ponder.fission_reactor.text_5", "Start the reactor with a redstone signal to the controller or port (make sure to select redstone mode in the port GUI).");
        add("nuclearcraft.ponder.fission_reactor.text_6", "There are no strict requirements for how internal reactor blocks must be placed.");
        add("nuclearcraft.ponder.fission_reactor.text_7", "The Fuel Cell block is used for energy and heat generation.");
        add("nuclearcraft.ponder.fission_reactor.text_8", "Place as many fuel cells as you like anywhere inside the reactor.");
        add("nuclearcraft.ponder.fission_reactor.text_9", "The resulting energy and heat generation is multiplied by the number of fuel cells.");
        add("nuclearcraft.ponder.fission_reactor.text_10", "It affects the fuel depletion speed at the same rate.");
        add("nuclearcraft.ponder.fission_reactor.text_11", "Another way to get more energy and heat is to attach moderator blocks to fuel cells.");
        add("nuclearcraft.ponder.fission_reactor.text_12", "Each moderator block face connected to a fuel cell increases FE generation by 17 % and the heat rate by 33 %.");
        add("nuclearcraft.ponder.fission_reactor.text_13", "Moderators between two fuel cells give an additional bonus.");
        add("nuclearcraft.ponder.fission_reactor.text_14", "The reactor will melt down without heatsinks. SL-1 was an isolated incident; do not be the second.");
        add("nuclearcraft.ponder.fission_reactor.text_15", "Each heatsink has specific placement rules to be active.");
        add("nuclearcraft.ponder.fission_reactor.text_16", "You are free to design your reactor as you like. Just make sure you place heatsinks according to their placement rules.");
        add("nuclearcraft.ponder.fission_reactor.text_17", "Fission Reactor irradiation feature.");
        add("nuclearcraft.ponder.fission_reactor.text_18", "An irradiation line is a set of three blocks in a row: Fuel Cell -> Moderator -> Irradiation Chamber.");
        add("nuclearcraft.ponder.fission_reactor.text_19", "Up to six irradiation lines for each Irradiation Chamber block.");
        add("nuclearcraft.ponder.fission_reactor.text_20", "Place the Irradiator anywhere in the reactor wall.");
        add("nuclearcraft.ponder.fission_reactor.text_21", "When the reactor is up and running, the Irradiator will use all irradiation lines to produce recipes.");
        add("nuclearcraft.ponder.fission_reactor.text_22", "Swap in a Pile-Driver Irradiation Chamber for 5x irradiation speed.");
        add("nuclearcraft.ponder.fusion_reactor.header", "Fusion Reactor");
        add("nuclearcraft.ponder.fusion_reactor.text_1", "The Fusion Core is the central part of the reactor.");
        add("nuclearcraft.ponder.fusion_reactor.text_2", "It automatically occupies a 3x3x3 volume around it.");
        add("nuclearcraft.ponder.fusion_reactor.text_3", "Add one Fusion Reactor Connector in each horizontal direction.");
        add("nuclearcraft.ponder.fusion_reactor.text_4", "You can have up to 10 connectors in each horizontal direction.");
        add("nuclearcraft.ponder.fusion_reactor.text_5", "Bigger ring - more energy and heat.");
        add("nuclearcraft.ponder.fusion_reactor.text_6", "Finally, build the Ring Chamber with a 3x3 cross-section.");
        add("nuclearcraft.ponder.fusion_reactor.text_7", "The chamber must be hollow to allow plasma to circulate.");
        add("nuclearcraft.ponder.fusion_reactor.text_8", "Fusion reactor functional blocks.");
        add("nuclearcraft.ponder.fusion_reactor.text_9", "Functional blocks must be placed anywhere in the corners of reactor ring.");
        add("nuclearcraft.ponder.fusion_reactor.text_10", "RF Amplifiers are used to heat the plasma. Insufficient amplification may prevent ignition.");
        add("nuclearcraft.ponder.fusion_reactor.text_11", "Electromagnets increase the plasma cross-section and stabilize the reaction.");
        add("nuclearcraft.ponder.fusion_reactor.text_12", "When the reactor is ready, charge it and pump in fuel and coolant.");
        add("nuclearcraft.ponder.fusion_reactor.text_13", "Start the reactor with a redstone signal to the Fusion Core. Signal strength directly affects RF amplification.");
        add("nuclearcraft.ponder.fusion_reactor.text_14", "RF amplification can also be adjusted in the reactor GUI.");
        add("nuclearcraft.ponder.target_chamber.text_1", "Target Chamber size can be from 5x5x5 up to 11x11x11.");
        add("nuclearcraft.ponder.target_chamber.text_2", "The center of the structure must be a Target Chamber Camera.");
        add("nuclearcraft.ponder.target_chamber.text_3", "Beam blocks must connect the camera to the beam ports in all 4 horizontal directions.");
        add("nuclearcraft.ponder.target_chamber.text_4", "Structure needs at least 1 input beam port and 3 output beam ports.");
        add("nuclearcraft.ponder.target_chamber.text_5", "Use a Multitool to change the port mode.");
        add("nuclearcraft.ponder.target_chamber.text_6", "Detectors must be placed around the camera to collect data.");
        add("nuclearcraft.ponder.target_chamber.text_7", "Add Target Chamber Ports for energy and item/fluid transport.");
        add("nuclearcraft.ponder.target_chamber.text_8", "Place the Target Chamber Controller on the casing.");
        add("nuclearcraft.ponder.target_chamber.text_9", "When the structure is valid, start it with redstone signal to controller block.");
        add("nuclearcraft.ponder.linear_accelerator.text_1", "One end needs an Ion Source Port or Particle Beam Port (Input).");
        add("nuclearcraft.ponder.linear_accelerator.text_2", "The opposite end needs a Beam Port (Output).");
        add("nuclearcraft.ponder.linear_accelerator.text_3", "RF Amplifiers increase particle energy. Place 8 blocks around a beam block.");
        add("nuclearcraft.ponder.linear_accelerator.text_4", "Electromagnets increase beam focus. Place 4 blocks around a beam block.");
        add("nuclearcraft.ponder.linear_accelerator.text_5", "Accelerator Coolers must be placed inside to regulate temperature.");
        add("nuclearcraft.ponder.linear_accelerator.text_6", "Finalize with Casing, Glass, Ports and a Controller.");
        add("nuclearcraft.ponder.linear_accelerator.text_7", "Connect beamline from beam output port to other structure.");
        add("nuclearcraft.ponder.linear_accelerator.text_8", "Provide redstone signal to controller block. Signal strength affects acceleration energy.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_1", "Kugelblitz Chamber size is 11x11x11.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_2", "All 6 walls must be perfectly symmetric.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_3", "The Kugelblitz Chamber Terminal is the main control block.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_4", "Chamber Ports are used for energy and item transport. Redstone input/output and computers.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_5", "Photon Concentrators must be placed at the center of all 6 walls.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_6", "Quantum Flux Regulators affect the Forge Energy output rate.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_7", "Event Horizon Stabilizers help maintain black hole stability.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_8", "Quantum Transformers improve the efficiency of transformation processes.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_9", "Finally, all 6 Excited Photon Lasers (EXPL) must be burst at the same time.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_10", "They all need to be fully charged and then activated with redstone or in their GUI.");
        add("nuclearcraft.ponder.kugelblitz_chamber.text_11", "When all 6 lasers burst simultaneously, a black hole forms inside the chamber. Containment is, optimistically, a solved problem.");
    }

    private void particles() {
        add("report.nc.validation_count", "Validations: %s");
        add("report.nc.validation_duration", "Validated in: %s ms");
        add("emi.category.nuclearcraft.cooler_placement", "Cooler Placement");
        add("emi.category.nuclearcraft.ingot_former", "Ingot Former");
        add("emi.category.nuclearcraft.fusion_coolant", "Fusion Reactor Coolant");
        add("emi.category.nuclearcraft.fusion_core", "Fusion Reactor");
        add("emi.category.nuclearcraft.crystallizer", "Crystallizer");
        add("emi.category.nuclearcraft.fluid_infuser", "Fluid Infuser");
        add("emi.category.nuclearcraft.manufactory", "Manufactory");
        add("emi.category.nuclearcraft.rock_crusher", "Rock Crusher");
        add("emi.category.nuclearcraft.irradiator", "Irradiator");
        add("emi.category.nuclearcraft.pressurizer", "Pressurizer");
        add("emi.category.nuclearcraft.melter", "Melter");
        add("emi.category.nuclearcraft.nc_ore_veins", "Ore Veins");
        add("emi.category.nuclearcraft.gas_scrubber", "Gas Scrubber");
        add("emi.category.nuclearcraft.centrifuge", "Centrifuge");
        add("emi.category.nuclearcraft.electrolyzer", "Electrolyzer");
        add("emi.category.nuclearcraft.subatomic_liquifier", "Sub-atomic liquifier");
        add("emi.category.nuclearcraft.pump", "Pump");
        add("emi.category.nuclearcraft.nuclear_furnace", "Nuclear Furnace");
        add("emi.category.nuclearcraft.fluid_enricher", "Fluid Enricher");
        add("emi.category.nuclearcraft.fission_reactor_controller", "Fission Reactor");
        add("emi.category.nuclearcraft.msr_controller", "Molten Salt Reactor");
        add("emi.category.nuclearcraft.decay_hastener", "Decay Hastener");
        add("emi.category.nuclearcraft.fission_boiling", "Fission Boiling");
        add("emi.category.nuclearcraft.accelerator_coolant", "Accelerator Coolant");
        add("emi.category.nuclearcraft.analyzer", "Analyzer");
        add("emi.category.nuclearcraft.target_chamber", "Target Chamber");
        add("emi.category.nuclearcraft.kugelblitz_chamber", "Kugelblitz Chamber");
        add("emi.category.nuclearcraft.turbine_controller", "Turbine Controller");
        add("emi.category.nuclearcraft.heat_sink_placement", "Heat Sink Placement");
        add("emi.category.nuclearcraft.assembler", "Assembler");
        add("emi.category.nuclearcraft.isotope_separator", "Isotope Separator");
        add("emi.category.nuclearcraft.chemical_reactor", "Chemical Reactor");
        add("emi.category.nuclearcraft.alloy_smelter", "Alloy Smelter");
        add("emi.category.nuclearcraft.extractor", "Extractor");
        add("emi.category.nuclearcraft.steam_turbine", "Steam Turbine");
        add("emi.category.nuclearcraft.leacher", "Leacher");
        add("emi.category.nuclearcraft.supercooler", "Supercooler");
        add("emi.category.nuclearcraft.fuel_reprocessor", "");
        add("emi.category.nuclearcraft.particle_info", "Particle Info");
        add("emi.category.nuclearcraft.kugelblitz_info", "Kugelblitz Info");
        add("emi.category.nuclearcraft.fuel_info", "Fuel Variants");
        add("emi.category.nuclearcraft.isotope_info", "Isotope Forms");
        add("gui.nuclearcraft.jei.particle.mass", "Mass: %s");
        add("jei.category.nuclearcraft.particle_info", "Particle Info");
        add("jei.category.nuclearcraft.heat_sink_placement", "Heat Sink Placement");
        add("jei.category.nuclearcraft.cooler_placement", "Cooler Placement");
        add("jei.category.nuclearcraft.fuel_info", "Fuel Variants");
        add("jei.category.nuclearcraft.isotope_info", "Isotope Forms");
        add("jei.nuclearcraft.fuel_info.title", "%s - Available Forms");
        add("jei.nuclearcraft.fuel_info.row", "FE/t %d | H/t %s | D %ds | Irrad. %s");
        add("jei.nuclearcraft.fuel_info.row_triso", "Criticality %d | H/t %s | D %ds");
        add("jei.nuclearcraft.isotope_info.title", "%s - Forms");
        add("fuel.irradiation.descr", "Irradiation: %s");
        add("fuel.variant.default", "Metal");
        add("fuel.variant.oxide", "Oxide");
        add("fuel.variant.nitride", "Nitride");
        add("fuel.variant.zirconium_alloy", "Zr");
        add("fuel.variant.triso", "TRISO");
        add("gui.nuclearcraft.jei.particle.charge", "Charge: %s");
        add("gui.nuclearcraft.jei.particle.spin", "Spin: %s");
        add("gui.nuclearcraft.jei.particle.colour", "Feels Strong Force: %s");
        add("gui.nuclearcraft.jei.particle.weak", "Feels Weak Force: %s");
        add("gui.nuclearcraft.jei.particle.components", "Made of:");
        add("gui.nuclearcraft.jei.particle.focus", "Minimum Focus: %s");
        add("gui.nuclearcraft.jei.reaction.range", "Range: %s");
        add("gui.nuclearcraft.jei.reaction.energy_released", "Released Energy: %s");
        add("gui.nuclearcraft.jei.reaction.cross_section", "Cross Section: %s%%");
        add("gui.nuclearcraft.jei.reaction.heat_released", "Heat Released: %s");
        add("gui.nuclearcraft.jei.reaction.max_energy", "Maximum Energy: %s");
        add("nuclearcraft.particle.none.name", "None");
        add("nuclearcraft.particle.up_quark.name", "Up Quark");
        add("nuclearcraft.particle.antiup_quark.name", "Anti-Up Quark");
        add("nuclearcraft.particle.down_quark.name", "Down Quark");
        add("nuclearcraft.particle.antidown_quark.name", "Anti-Down Quark");
        add("nuclearcraft.particle.charm_quark.name", "Charm Quark");
        add("nuclearcraft.particle.anticharm_quark.name", "Anti-Charm Quark");
        add("nuclearcraft.particle.strange_quark.name", "Strange Quark");
        add("nuclearcraft.particle.antistrange_quark.name", "Anti-Strange Quark");
        add("nuclearcraft.particle.top_quark.name", "Top Quark");
        add("nuclearcraft.particle.antitop_quark.name", "Anti-Top Quark");
        add("nuclearcraft.particle.bottom_quark.name", "Bottom Quark");
        add("nuclearcraft.particle.antibottom_quark.name", "Anti-Bottom Quark");
        add("nuclearcraft.particle.electron.name", "Electron");
        add("nuclearcraft.particle.positron.name", "Positron");
        add("nuclearcraft.particle.electron_neutrino.name", "Electron Neutrino");
        add("nuclearcraft.particle.electron_antineutrino.name", "Electron Antineutrino");
        add("nuclearcraft.particle.muon.name", "Muon");
        add("nuclearcraft.particle.antimuon.name", "Anti-Muon");
        add("nuclearcraft.particle.muon_neutrino.name", "Muon Neutrino");
        add("nuclearcraft.particle.muon_antineutrino.name", "Muon Antineutrino");
        add("nuclearcraft.particle.tau.name", "Tau");
        add("nuclearcraft.particle.antitau.name", "Anti-Tau");
        add("nuclearcraft.particle.tau_neutrino.name", "Tau Neutrino");
        add("nuclearcraft.particle.tau_antineutrino.name", "Tau Antineutrino");
        add("nuclearcraft.particle.photon.name", "Photon");
        add("nuclearcraft.particle.gluon.name", "Gluon");
        add("nuclearcraft.particle.w_plus_boson.name", "W+ Boson");
        add("nuclearcraft.particle.w_minus_boson.name", "W- Boson");
        add("nuclearcraft.particle.z_boson.name", "Z Boson");
        add("nuclearcraft.particle.higgs_boson.name", "Higgs Boson");
        add("nuclearcraft.particle.proton.name", "Proton");
        add("nuclearcraft.particle.antiproton.name", "Anti-Proton");
        add("nuclearcraft.particle.neutron.name", "Neutron");
        add("nuclearcraft.particle.antineutron.name", "Anti-Neutron");
        add("nuclearcraft.particle.deuteron.name", "Deuteron");
        add("nuclearcraft.particle.antideuteron.name", "Anti-Deuteron");
        add("nuclearcraft.particle.alpha.name", "Alpha Particle");
        add("nuclearcraft.particle.antialpha.name", "Anti-Alpha Particle");
        add("nuclearcraft.particle.pion_plus.name", "Pion +");
        add("nuclearcraft.particle.pion_naught.name", "Pion 0");
        add("nuclearcraft.particle.pion_minus.name", "Pion -");
        add("nuclearcraft.particle.triton.name", "Triton");
        add("nuclearcraft.particle.antitriton.name", "Anti-Triton");
        add("nuclearcraft.particle.helion.name", "Helion");
        add("nuclearcraft.particle.antihelion.name", "Anti-Helion");
        add("nuclearcraft.particle.boron_ion.name", "Boron Ion");
        add("nuclearcraft.particle.calcium_48_ion.name", "Calcium-48 Ion");
        add("nuclearcraft.particle.kaon_plus.name", "Kaon +");
        add("nuclearcraft.particle.kaon_minus.name", "Kaon -");
        add("nuclearcraft.particle.kaon_naught.name", "Kaon 0");
        add("nuclearcraft.particle.antikaon_naught.name", "Anti-Kaon 0");
        add("nuclearcraft.particle.eta.name", "Eta Meson");
        add("nuclearcraft.particle.eta_prime.name", "Eta Prime Meson");
        add("nuclearcraft.particle.charmed_eta.name", "Charmed Eta Meson");
        add("nuclearcraft.particle.bottom_eta.name", "Bottom Eta Meson");
        add("nuclearcraft.particle.glueball.name", "Glueball");
        add("nuclearcraft.particle.sigma_plus.name", "Sigma +");
        add("nuclearcraft.particle.antisigma_plus.name", "Anti-Sigma +");
        add("nuclearcraft.particle.sigma_naught.name", "Sigma 0");
        add("nuclearcraft.particle.antisigma_naught.name", "Anti-Sigma 0");
        add("nuclearcraft.particle.sigma_minus.name", "Sigma -");
        add("nuclearcraft.particle.antisigma_minus.name", "Anti-Sigma -");
        add("nuclearcraft.particle.delta_plus_plus.name", "Delta ++");
        add("nuclearcraft.particle.antidelta_plus_plus.name", "Anti-Delta ++");
        add("nuclearcraft.particle.delta_minus.name", "Delta -");
        add("nuclearcraft.particle.antidelta_minus.name", "Anti-Delta -");
        add("nuclearcraft.particle.up_quark.desc", "The Up Quark is the lightest quark. Up and Down quarks combine to form Protons and Neutrons");
        add("nuclearcraft.particle.antiup_quark.desc", "The Anti-Up Quark is the antimatter partner of the Up Quark.");
        add("nuclearcraft.particle.down_quark.desc", "The Down Quark is the second lightest quark. Up and Down quarks combine to form Protons and Neutrons");
        add("nuclearcraft.particle.antidown_quark.desc", "The Anti-Down Quark is the antimatter partner of the Down Quark.");
        add("nuclearcraft.particle.charm_quark.desc", "The Charm Quark is a heavy version of the Up Quark.");
        add("nuclearcraft.particle.anticharm_quark.desc", "The Anti-Charm Quark is the antimatter partner of the Charm Quark.");
        add("nuclearcraft.particle.strange_quark.desc", "The Strange Quark is a heavy version of the Down Quark.");
        add("nuclearcraft.particle.antistrange_quark.desc", "The Anti-Strange Quark is the antimatter partner of the Strange Quark.");
        add("nuclearcraft.particle.top_quark.desc", "The Top Quark is a very heavy version of the Up Quark.");
        add("nuclearcraft.particle.antitop_quark.desc", "The Anti-Top Quark is the antimatter partner of the Top Quark.");
        add("nuclearcraft.particle.bottom_quark.desc", "The Bottom Quark is a very heavy version of the Down Quark.");
        add("nuclearcraft.particle.antibottom_quark.desc", "The Anti-Bottom Quark is the antimatter partner of the Bottom Quark.");
        add("nuclearcraft.particle.electron.desc", "The Electron is the lightest charged lepton. It is commonly found in orbitals around nuclei, forming atoms.");
        add("nuclearcraft.particle.positron.desc", "The Positron is the antimatter partner of the Electron. When an electron and positron meet they annihilate, converting all their mass into energy in the form of two gamma rays.");
        add("nuclearcraft.particle.electron_neutrino.desc", "The Electron Neutrino is the neutrino partner of the Electron.");
        add("nuclearcraft.particle.electron_antineutrino.desc", "The Electron Antineutrino is the antimatter partner of the Electron Neutrino.");
        add("nuclearcraft.particle.muon.desc", "The Muon is essentially a heavy electron.");
        add("nuclearcraft.particle.antimuon.desc", "The Anti-Muon is the antimatter partner of the Muon.");
        add("nuclearcraft.particle.muon_neutrino.desc", "The Muon Neutrino is the neutrino partner of the Muon.");
        add("nuclearcraft.particle.muon_antineutrino.desc", "The Muon Antineutrino is the antimatter partner of the Muon Neutrino.");
        add("nuclearcraft.particle.tau.desc", "The Tau is essentially a very heavy electron.");
        add("nuclearcraft.particle.antitau.desc", "The Anti-Tau is the antimatter partner of the Tau.");
        add("nuclearcraft.particle.tau_neutrino.desc", "The Tau Neutrino is the neutrino partner of the Tau.");
        add("nuclearcraft.particle.tau_antineutrino.desc", "The Tau Antineutrino is the antimatter partner of the Tau Neutrino.");
        add("nuclearcraft.particle.photon.desc", "Photons are the particles that make up light. They are the carriers of the electromagnetic force. High-energy Photons are called gamma rays.");
        add("nuclearcraft.particle.gluon.desc", "Gluons are the carriers of the strong force. The strong force binds quarks together to create composite particles like protons and neutrons.");
        add("nuclearcraft.particle.w_plus_boson.desc", "The Z and W bosons are the carriers of the weak force. The weak force allows certain particles to decay and is responsible for beta decay.");
        add("nuclearcraft.particle.w_minus_boson.desc", "The Z and W bosons are the carriers of the weak force. The weak force allows certain particles to decay and is responsible for beta decay.");
        add("nuclearcraft.particle.z_boson.desc", "The Z and W bosons are the carriers of the weak force. The weak force allows certain particles to decay and is responsible for beta decay.");
        add("nuclearcraft.particle.higgs_boson.desc", "The Higgs Boson is the boson of the Higgs Field which is responsible for giving particles their mass.");
        add("nuclearcraft.particle.proton.desc", "The Proton is a nucleon. Together with the Neutron, it makes up the nucleus of atoms.");
        add("nuclearcraft.particle.antiproton.desc", "The Anti-Proton is the antimatter partner of the Proton.");
        add("nuclearcraft.particle.neutron.desc", "The Neutron is a nucleon. Together with the Proton, it makes up the nucleus of atoms. Neutrons are used in nuclear fission to split fissile nuclei.");
        add("nuclearcraft.particle.antineutron.desc", "The Anti-Neutron is the antimatter partner of the Neutron.");
        add("nuclearcraft.particle.deuteron.desc", "The Deuteron is the nucleus of a Deuterium Atom.");
        add("nuclearcraft.particle.antideuteron.desc", "The Anti-Deuteron is the antimatter partner of the Deuteron.");
        add("nuclearcraft.particle.alpha.desc", "Alpha Particle is another name for the nucleus of helium 4. It is commonly released in the decay of heavy elements like uranium and plutonium.");
        add("nuclearcraft.particle.antialpha.desc", "The Anti-Alpha Particle is the antimatter partner of the Alpha Particle.");
        add("nuclearcraft.particle.pion_plus.desc", "Pions are responsible for holding nuclei together. Although not colored themselves, they distribute the \"residual\" strong force that keeps nuclei bound.");
        add("nuclearcraft.particle.pion_naught.desc", "Pions are responsible for holding nuclei together. Although not colored themselves, they distribute the \"residual\" strong force that keeps nuclei bound.");
        add("nuclearcraft.particle.pion_minus.desc", "Pions are responsible for holding nuclei together. Although not colored themselves, they distribute the \"residual\" strong force that keeps nuclei bound.");
        add("nuclearcraft.particle.triton.desc", "The Triton is the nucleus of a Tritium Atom.");
        add("nuclearcraft.particle.antitriton.desc", "The Anti-Triton is the antimatter partner of the Triton.");
        add("nuclearcraft.particle.helion.desc", "The Helion is the nucleus of a Helium-3 Atom.");
        add("nuclearcraft.particle.antihelion.desc", "The Anti-Helion is the antimatter partner of the Helion.");

        add("nuclearcraft.particle.boron_ion.desc", "A Boron atom with one electron missing.");
        add("nuclearcraft.particle.calcium_48_ion.desc", "A Calcium-48 atom with one electron missing. A particularly neutron-rich atom, useful for creating superheavy elements.");

        add("nuclearcraft.particle.kaon_plus.desc", "The Kaon + is a meson with strangeness of 1.");
        add("nuclearcraft.particle.kaon_naught.desc", "The Kaon 0 is a meson with strangeness of 1.");
        add("nuclearcraft.particle.antikaon_naught.desc", "The Anti-Kaon 0 is a meson with strangeness of -1.");
        add("nuclearcraft.particle.kaon_minus.desc", "The Kaon - is a meson with strangeness of -1.");

        add("nuclearcraft.particle.eta.desc", "Eta Mesons are flavorless mesons, meaning their flavor numbers like strangeness and isospin are 0.");
        add("nuclearcraft.particle.eta_prime.desc", "Eta Mesons are flavorless mesons, meaning their flavor numbers like strangeness and isospin are 0.");
        add("nuclearcraft.particle.charmed_eta.desc", "Eta Mesons are flavorless mesons, meaning their flavor numbers like strangeness and isospin are 0.");
        add("nuclearcraft.particle.bottom_eta.desc", "Eta Mesons are flavorless mesons, meaning their flavor numbers like strangeness and isospin are 0.");
        add("nuclearcraft.particle.glueball.desc", "Glueballs are particles made entirely out of gluons.");

        add("nuclearcraft.particle.sigma_plus.desc", "Sigma Baryons contain one Strange Quark. They are heavier than Protons and Neutrons.");
        add("nuclearcraft.particle.antisigma_plus.desc", "Anti-Sigma Baryons contain one Anti-Strange Quark. They are heavier than Anti-Protons and Anti-Neutrons.");
        add("nuclearcraft.particle.sigma_naught.desc", "Sigma Baryons contain one Strange Quark. They are heavier than Protons and Neutrons.");
        add("nuclearcraft.particle.antisigma_naught.desc", "Anti-Sigma Baryons contain one Anti-Strange Quark. They are heavier than Anti-Protons and Anti-Neutrons.");
        add("nuclearcraft.particle.sigma_minus.desc", "Sigma Baryons contain one Strange Quark. They are heavier than Protons and Neutrons.");
        add("nuclearcraft.particle.antisigma_minus.desc", "Anti-Sigma Baryons contain one Anti-Strange Quark. They are heavier than Anti-Protons and Anti-Neutrons.");

        add("nuclearcraft.particle.delta_plus_plus.desc", "Delta ++ is a baryon containing three Up Quarks. It quickly decays via the strong force.");
        add("nuclearcraft.particle.antidelta_plus_plus.desc", "Anti-Delta ++ is a baryon containing three Anti-Up Quarks. It quickly decays via the strong force.");

        add("nuclearcraft.particle.delta_minus.desc", "Delta - is a baryon containing three Down Quarks. It quickly decays via the strong force.");
        add("nuclearcraft.particle.antidelta_minus.desc", "Anti-Delta - is a baryon containing three Anti-Down Quarks. It quickly decays via the strong force.");
    }

    private void messages() {
        add("message.nc.player_radiation_contamination", "Radiation Dose: %s");
        add("message.nc.debug_logging.enable", "Enabled debug logging");
        add("message.nc.debug_logging.disable", "Disabled debug logging");
        add("message.nc.geiger_radiation_measure", "Radiation Level: %s");
        add("death.attack.radiation", "Died of Radiation Poisoning");
        add("nc.message.patrons", "Special thanks to patrons: Noteclip, marcin212, PersonBelowRocks, tomdodd4598, ethantabler, endleon201, sancho.lucky, Cerusvi, tocix9730 and others...");
    }
    private void sounds() {
        add("music.hyperspace", "Hyperspace");
        add("music.end_of_the_world", "End of the World");
        add("music.wanderer", "Wanderer");
        add("music.money_for_nothing", "Money For Nothing");

        add("sound_event.nuclearcraft.feral_ghoul.idle", "Feral Ghoul idle");
        add("sound_event.nuclearcraft.feral_ghoul.death", "Feral Ghoul death");

        add("sound_event.nuclearcraft.item.geiger_1", "Geiger Counter Ticks Level 1 Intensity");
        add("sound_event.nuclearcraft.item.geiger_2", "Geiger Counter Ticks Level 2 Intensity");
        add("sound_event.nuclearcraft.item.geiger_3", "Geiger Counter Ticks Level 3 Intensity");
        add("sound_event.nuclearcraft.item.geiger_4", "Geiger Counter Ticks Level 4 Intensity");
        add("sound_event.nuclearcraft.item.geiger_5", "Geiger Counter Ticks Level 5 Intensity");
        add("sound_event.nuclearcraft.item.geiger_6", "Geiger Counter Ticks Max Intensity");

        add("sound_event.nuclearcraft.bomb.blast", "Distant Detonation");

        add("sound_event.nuclearcraft.fusion.ready", "Fusion Reactor Ready");
        add("sound_event.nuclearcraft.fusion.running", "Fusion Reactor Running");
        add("sound_event.nuclearcraft.fusion.charging", "Fusion Reactor Charging");
        add("sound_event.nuclearcraft.fusion.switch", "Fusion Reactor Switch");

        add("sound_event.nuclearcraft.fission_reactor", "Fission Reactor Ticking");
        add("sound_event.nuclearcraft.turbine", "Turbine is spinning");
        add("sound_event.nuclearcraft.q36.beam_shot", "Q-36 Quantite Disruptor fires");
        add("sound_event.nuclearcraft.q36.pulse_shot", "Q-36 Quantite Pulse discharge");
    }

    private void advancements() {
        // Root advancement
        add("advancement.nc.root", "NuclearCraft");
        add("advancement.nc.root.desc", "The beginning of your nuclear career. Vault-Tec wishes you a long and productive tenure.");

        add("advancement.nc.basic_barrel", "Basic Barrel");
        add("advancement.nc.basic_barrel.desc", "Got the barrel");

        add("advancement.nc.basic_storage_container", "Basic Storage Container");
        add("advancement.nc.basic_storage_container.desc", "Better than chests");

        add("advancement.nc.basic_voltaic_pile", "Basic Voltaic Pile");
        add("advancement.nc.basic_voltaic_pile.desc", "Keeping energy, just in case");

        add("advancement.nc.alloy_smelter", "Alloy Smelter");
        add("advancement.nc.alloy_smelter.desc", "Combine metals into alloys");

        add("advancement.nc.q36", "Q-36 Quantite Disruptor");
        add("advancement.nc.q36.desc", "");

        add("advancement.nc.qnp", "QNP");
        add("advancement.nc.qnp.desc", "Craft a QNP for advanced excavation");

        add("advancement.nc.leacher", "Leacher");
        add("advancement.nc.leacher.desc", "Craft a Leacher to extract minerals using acids");

        // Wasteland advancements
        add("advancement.nc.wasteland", "Wasteland");
        add("advancement.nc.wasteland.desc", "Enter the desolate Wasteland biome");
        
        add("advancement.nc.wasteland_boss", "Wasteland Conqueror");
        add("advancement.nc.wasteland_boss.desc", "Defeat the Boss of the Wasteland");

        // Basic machines
        add("advancement.nc.manufactory", "Manufactory");
        add("advancement.nc.manufactory.desc", "Craft a Manufactory to process ores and other materials");

        add("advancement.nc.isotope_separator", "Isotope Separator");
        add("advancement.nc.isotope_separator.desc", "Craft an Isotope Separator to separate isotopes");

        add("advancement.nc.assembler", "Assembler");
        add("advancement.nc.assembler.desc", "Craft an Assembler to create complex components");

        add("advancement.nc.pressurizer", "Pressurizer");
        add("advancement.nc.pressurizer.desc", "The compression");

        add("advancement.nc.decay_hastener", "Decay Hastener");
        add("advancement.nc.decay_hastener.desc", "C'mon, hurry up!");

        add("advancement.nc.chemical_reactor", "Chemical Reactor");
        add("advancement.nc.chemical_reactor.desc", "Mixing fluids and gases");

        add("advancement.nc.analyzer", "Analyzer");
        add("advancement.nc.analyzer.desc", "It's time to analyze");

        // Energy generation
        add("advancement.nc.solar_panel_basic", "Basic Solar Panel");
        add("advancement.nc.solar_panel_basic.desc", "Harness the power of the sun");

        add("advancement.nc.decay_generator", "Decay Generator");
        add("advancement.nc.decay_generator.desc", "Generate energy from radioactive decay");

        add("advancement.nc.uranium_rtg", "Uranium RTG");
        add("advancement.nc.uranium_rtg.desc", "Radioisotope Thermoelectric generation");

        // Fission reactor
        add("advancement.nc.fission_reactor_controller", "Chicago Pile, Domestic Edition");
        add("advancement.nc.fission_reactor_controller.desc", "Splitting the atom in the comfort of your own basement.");

        // Irradiation
        add("advancement.nc.irradiator", "Irradiator");
        add("advancement.nc.irradiator.desc", "Transforming materials with neutron flux");

        add("advancement.nc.chamber_terminal", "Chamber Terminal");
        add("advancement.nc.chamber_terminal.desc", "Can you keep that blackhole stable?");

        // Turbine
        add("advancement.nc.turbine_controller", "Turbine Controller");
        add("advancement.nc.turbine_controller.desc", "Convert steam into energy efficiently");

        // Fusion
        add("advancement.nc.fusion_core", "Fusion Core");
        add("advancement.nc.fusion_core.desc", "Fusing elements = energy");

        // Tools and special items
        add("advancement.nc.research_paper", "Research Paper");
        add("advancement.nc.research_paper.desc", "Analyze chunks for mineral veins");

        add("advancement.nc.spaxelhoe_thorium", "Thorium Spaxelhoe");
        add("advancement.nc.spaxelhoe_thorium.desc", "Craft a multi-tool made from Thorium");

        add("advancement.nc.expl", "EXPL");
        add("advancement.nc.expl.desc", "How about making a black hole out of light?");

        add("advancement.nc.light_shielding", "Light Radiation Shielding");
        add("advancement.nc.light_shielding.desc", "Your first step in radiation protection");

        add("advancement.nc.radaway", "RadAway");
        add("advancement.nc.radaway.desc", "Flush radiation contamination from your body. Side effects may include not glowing in the dark.");

        add("advancement.nc.hazmat_mask", "Hazmat Protection");
        add("advancement.nc.hazmat_mask.desc", "Full-body protection against radiation exposure");

        add("advancement.nc.hev_helmet", "Advanced Protection");
        add("advancement.nc.hev_helmet.desc", "High-tech suit with enhanced radiation shielding");

        add("advancement.nc.contamination", "Acceptable Daily Intake");
        add("advancement.nc.contamination.desc", "Accumulate 1 Rad of bodily contamination. Vault-Tec assures you this is well within tolerance.");
    }

    private void labels() {
        add("multiblock.analyze.report", "Multiblock Report");
        add("report.nc.1.stabilizers", "Stabilizers: %s");
        add("report.nc.2.flux_regulators", "Flux Regulators: %s");
        add("report.nc.3.transformers", "Transformers: %s");
        add("report.nc.1.target_chamber.all_detectors", "All Detectors: %s");
        add("report.nc.1.target_chamber.valid_detectors", "Valid Detectors: %s");
        add("report.nc.1.accelerator.all_coolers", "All Coolers: %s");
        add("report.nc.1.accelerator.valid_coolers", "Valid Coolers: %s");
        add("report.nc.1.reactor_all_moderators", "All Moderators: %s");
        add("report.nc.2.reactor_moderators", "Valid Moderators: %s");
        add("report.nc.3.reactor_moderator_attachments", "Moderator Attachments: %s");
        add("report.nc.4.reactor_all_heat_sinks", "All Heat Sinks: %s");
        add("report.nc.5.reactor_heat_sinks", "Valid Heat Sinks: %s");
        add("report.nc.6.active_cooling_heatsinks", "Active Cooling Heat Sinks: %s");
        add("report.nc.7.all_irradiators", "All Irradiators: %s");
        add("report.nc.8.irradiators", "Valid Irradiators: %s");
        add("report.nc.9.ports", "Ports: %s");
        add("report.nc.10.reactor_fuel_cells", "Fuel Cells: %s");
        add("report.nc.11.has_recipe", "Has Recipe: %s");
        add("report.nc.multiblock_ticks_count", "Multiblock ticks: %s");
        add("report.nc.1.fusion_size", "Fusion Reactor Size: %s");
        add("report.nc.2.magnets", "Magnets: %s");
        add("report.nc.3.amplifiers", "RF Amplifiers: %s");
        add("report.nc.4.fusion_rf_amplification", "RF Amplification: %s");
        add("report.nc.5.casing_blocks", "Casing Blocks: %s");
        add("report.nc.6.connectors", "Connectors: %s");
        add("jei.category.nuclearcraft.multiblock_structure", "NuclearCraft Multiblocks");
        add("jei.recipe.nc.turbine", "Turbine");
        add("jei.recipe.nc.fusion_reactor", "Fusion Reactor");
        add("jei.recipe.nc.fission_reactor", "Fission Reactor");
        add("jei.recipe.nc.target_chamber", "Target Chamber");
        add("jei.recipe.nc.linear_accelerator", "Linear Accelerator");
        add("jei.recipe.nc.kugelblitz_chamber", "Kugelblitz Chamber");
        add("jei.recipe.nc.leacher", "Leacher Setup");
        add("jei.info.nuclearcraft.kugelblitz.description", "The Kugelblitz Chamber transforms items using black hole quantum fields.");
        add("jei.info.nuclearcraft.kugelblitz.problem", "The catch: we don't know the transformation recipes. We only know what the chamber can produce.");
        add("jei.info.nuclearcraft.kugelblitz.input_output", "Whatever a Kugelblitz transformation produces is also what it can take as input.");
        add("entity.minecraft.villager.nuclearcraft.nuclear_scientist", "Nuclear Scientist");
        add("block.nuclearcraft.expl", "EXPL");
        add("gui.nuclearcraft:button.burst", "Activate");
        add("nc.guide_book.name", "NuclearCraft Guide");
        add("nc.guide_book.desc", "Basics and advanced topics about NuclearCraft");
        add("nc.guide_book.edition", "Neoteric Edition");
        add("fusion_core.rf_amplifiers.power", "RF Amplifiers: %s%%");
        add("fusion_core.rf_amplifiers.adjustment", "RF Adjustment: %s%%");
        add("fusion_core.stability", "Plasma Stability: %s%%");
        add("nc.label.leacher_wrong_position", "Wrong Position");
        add("nc.label.leacher_no_source", "No Data Source");
        add("nc.label.leacher_no_acid", "No Acid");
        add("block.nuclearcraft.glowing_mushroom", "Glowing Mushroom");
        add("fusion_core", "Fusion Reactor Core");
        add("fusion_core.efficiency", "Efficiency: %s%%");
        add("nc_jei_cat.accelerator_coolant", "Accelerator Coolant");
        add("nc_jei_cat.fusion_core", "Fusion Reactor");
        add("nc_jei_cat.target_chamber", "Target Chamber");
        add("nc_jei_cat.kugelblitz_chamber", "Quantum Transformation");
        add("nc_jei_cat.turbine", "Turbine");
        add("nc_jei_cat.fusion_coolant", "Fusion Reactor Coolant");
        add("nc_jei_cat.mek_chemical_conversion", "NC - GAS -> Fluid Conversion");
        add("nc_jei_cat.fission_boiling", "Boiling Reactor");
        add("nc_jei_cat.msr_controller", "Molten Salt Reactor");

        add("label.nuclearcraft.energy_range", "Energy: %s - %s");
        add("label.nuclearcraft.energy", "Energy: %s");
        add("label.nuclearcraft.cross_section", "Cross-section: %s%%");
        add("label.kugelblitz.stability", "Stability: %s%%");
        add("label.kugelblitz.charge", "Charge: %s");
        add("label.kugelblitz.evaporation", "Evaporation Rate: %s");
        add("label.kugelblitz.blackhole_mass", "Blackhole Mass: %s");
        add("label.kugelblitz.frequency", "Quantum Frequency: %s");
        add("label.kugelblitz.transformation", "Transformation");
        add("label.kugelblitz.feeding", "Feeding Rate: %s");
        add("label.kugelblitz.energy_gen", "FE Gen");

        add("tooltip.nc.structure.size", "Structure size: %sx%sx%s");
        add("fission.casing.wrong.block", "Wrong block at: %s");
        add("fission_reactor.efficiency", "Efficiency: %s%%");
        add("fission_reactor.net_heat", "Net Heat: %s H/t");
        add("fission.casing.reactor_incomplete", "Reactor Incomplete");
        add("fission_reactor.heat_multiplier", "Heat Multiplier: %sx");

        add("msr.pressure.bar.amount", "Pressure: %s / %s");
        add("msr.reactivity", "Reactivity: %s");
        add("msr.status", "Status: %s");
        add("msr.critical", "CRITICAL");
        add("msr.subcritical", "SUBCRITICAL");
        add("msr.locked", "PORTS LOCKED");

        add("turbine.efficiency", "Efficiency: %s%%");
        add("turbine.real_flow", "Real Flow: %smB");
        add("turbine.ratio", "Pressure: %s%%");

        add("processor_side_config.title", "Select Slot");
        add("processor_slot_mode.title", "Slot Mode");

        add("commands.nuclearcraft.no_permission", "No permissions");
        add("message.heat_sink.valid0", "This one is looking good.");
        add("message.heat_sink.valid1", "I like this one.");
        add("message.heat_sink.valid2", "This heat sink design shows promise.");
        add("message.heat_sink.valid3", "The heat dissipation capability looks good.");
        add("message.heat_sink.valid4", "The thermal conductivity appears efficient.");
        add("message.heat_sink.valid5", "Attention to detail is evident in this design.");
        add("message.heat_sink.valid6", "This heat sink design seems promising for our project.");
        add("message.heat_sink.valid7", "Attention to detail is impressive. Operations would approve.");
        add("message.heat_sink.valid8", "A perfect fit for our application.");
        add("message.heat_sink.valid9", "Meets nominal standards. Nominal conditions sold separately.");

        add("message.heat_sink.invalid0", "Not sure if it's valid.");
        add("message.heat_sink.invalid1", "You should check it again.");
        add("message.heat_sink.invalid2", "Hm...");
        add("message.heat_sink.invalid3", "Will it explode? Probably not.");
        add("message.heat_sink.invalid4", "This doesn't seem to meet our standards.");
        add("message.heat_sink.invalid5", "There are some concerns about the validity of this.");
        add("message.heat_sink.invalid6", "I have some reservations about this. So would the safety inspector.");
        add("message.heat_sink.invalid7", "This may not be suitable for our project.");
        add("message.heat_sink.invalid8", "This will need significant revisions before it can be considered valid.");
        add("message.heat_sink.invalid9", "More work needed before this can be considered valid.");

        add("nc_jei_cat.fission_reactor_controller", "Fission Reactor Fuel Depletion");
        add("nc_jei_cat.nc_ore_veins", "Ore Veins");
        add("container.nc.storage", "Item Storage Container");
    }

    private void multiblocks() {
        String prefix = "";
        for(String name: ACCELERATOR_BLOCKS.keySet()) {
            prefix = "";
            if(name.contains("cooler")) {
                prefix = "Accelerator ";
            }
            String title = convertToName(name);
            if(name.contains("ring")) {
                title = "(WIP) " + title;
            }
            add(ACCELERATOR_BLOCKS.get(name).get(), prefix+title);
        }
        for(String name: TARGET_CHAMBER_BLOCKS.keySet()) {
            String title = convertToName(name);
            if(TARGET_CHAMBER_DETECTORS.containsKey(name)) {
                title += " Detector";
            }
            add(TARGET_CHAMBER_BLOCKS.get(name).get(), title);
        }
        for(String name: KUGELBLITZ_BLOCKS.keySet()) {
            String title = convertToName(name);
            add(KUGELBLITZ_BLOCKS.get(name).get(), title);
        }
        for(String name: FISSION_BLOCKS.keySet()) {
            prefix = "";
            if(name.contains("heat_sink")) {
                prefix = "Fission Reactor ";
            }
            if(name.contains("msr_")) {
                prefix = "";
            }
            String title = convertToName(name);
            if(name.equals("msr_controller")) {
                title = "MSR Controller";
            }
            if(name.equals("msr_fuel_cell")) {
                title = "MSR Fuel Cell";
            }
            add(FISSION_BLOCKS.get(name).get(), prefix+title);
        }

        for(String name: TURBINE_BLOCKS.keySet()) {
            String title = convertToName(name);
            add(TURBINE_BLOCKS.get(name).get(), title);
        }
        add(FUSION_CORE_PROXY.get(), "Fusion Reactor Core");
        add(EXPL_PROXY_BLOCK.get(), "EXPL");
    }

    private void storageBlocks() {
        for(String name: STORAGE_BLOCKS.keySet()) {
            String title = convertToName(name);
            add(STORAGE_BLOCKS.get(name).get(), title);
        }
    }

    private void energyBlocks() {
        for(String name: NCEnergyBlocks.ENERGY_BLOCKS.keySet()) {
            String title = convertToName(name);
            add(NCEnergyBlocks.ENERGY_BLOCKS.get(name).get(), title);
        }
    }

    private void processors() {
        for(String name: NCProcessors.PROCESSORS.keySet()) {
            String title = convertToName(name);
            add(NCProcessors.PROCESSORS.get(name).get(), title);
            add("nc_jei_cat."+name, title);
        }
    }

    private void buckets() {
        for(String name: NCFluids.NC_MATERIALS.keySet()) {
            String molten = "";
            if(NC_INGOTS.containsKey(name)) {
                molten = "Molten ";
            }
            add(NCFluids.NC_MATERIALS.get(name).getBucket(), "Bucket of " + molten + convertToName(name));
        }
        for(String name: NCFluids.NC_GASES.keySet()) {
            add(NCFluids.NC_GASES.get(name).getBucket(), "Bucket of " + convertToName(name));
        }
    }

    private void fluids() {
        for(String name: NCFluids.NC_MATERIALS.keySet()) {
            String molten = "";
            if(NC_INGOTS.containsKey(name)) {
                molten = "Molten ";
            }
            add("fluid_type."+NCFluids.NC_MATERIALS.get(name).type().getId().toLanguageKey(), molten + convertToName(name));
        }
        for(String name: NCFluids.NC_GASES.keySet()) {
            add("fluid_type."+NCFluids.NC_GASES.get(name).type().getId().toLanguageKey(), convertToName(name));
        }
    }

    private void shielding() {
        for(String name: NCItems.NC_SHIELDING.keySet()) {
            add(NCItems.NC_SHIELDING.get(name).get(), convertToName(name)+" Shielding");
        }
    }

    private void tooltips() {
        add("tooltip.nc.decay_generator_allowed.desc", "Can be used with Decay Generator");
        add("tooltip.nc.energy_eu_tier.depends_on_terminal", "EU Tier inherits Tier from Terminal");
        add("tooltip.nc.energy_eu_tier.depends_on_controller", "EU Tier inherits Tier from Controller");
        add("tooltip.nc.wasteland.disabled", "Wasteland dimension is disabled");
        add("tooltip.nc.wasteland.portal.descr", "Teleport to Wasteland dimension");
        add("tooltip.ion_source.particle", "Particle: %s");
        add("tooltip.nc.expl", "Produces a powerful beam of light. Converging 6 beams on a single point can create a black hole inside a Kugelblitz Chamber.");
        add("tooltip.nc.magnet.disable", "Disable Auto-pickup");
        add("tooltip.nc.magnet.enable", "Enable Auto-pickup");
        add("tooltip.ion_source.amount", "Amount: %spu / %spu");
        add("tooltip.target_chamber.detectors", "Detectors: %s");
        add("tooltip.detector.distance", "Max distance to chamber camera: %s");
        add("tooltip.detector.power", "FE required: %s/t");
        add("tooltip.detector.efficiency", "Efficiency: %s%%");
        add("tooltip.structure.sizes", "Structure sizes: Min %s - Max: %s");
        add("tooltip.turbine.max_eu_energy", "Max EU gen: %s");
        add("tooltip.nc.particle_beam.desc", "Forms the central beam line for particle accelerators.");
        add("tooltip.turbine.max_energy", "Max FE gen: %s");
        add("tooltip.nc.report_issue", "Copy link to post a bug");
        add("tooltip.nc.accelerator.strength", "Magnet Strength: %s T");
        add("tooltip.nc.accelerator.efficiency", "Efficiency: %s");
        add("tooltip.nc.accelerator.focus", "Focus: %s");
        add("tooltip.nc.accelerator.quadroupoles", "Quadrupoles: %s");
        add("tooltip.nc.accelerator.dipoles", "Dipoles: %s");
        add("tooltip.nc.accelerator.too_hot", "Accelerator is too hot");
        add("tooltip.nc.accelerator.voltage", "Voltage: %sV");
        add("tooltip.nc.accelerator.amplifiers", "Amplifiers: %s");
        add("tooltip.nc.accelerator.coolers", "Coolers: %s");
        add("tooltip.nc.max_fe_extract_per_tick","FE/t output: %s");
        add("tooltip.nc.upgrade_energy.tier","+1 Energy Tier for every %s Upgrades");
        add("tooltip.nc.eu_energy_stored","EU Stored: %s / %s");
        add("tooltip.nc.energy_base_eu_tier", "Base EU Tier: %s");
        add("tooltip.nc.energy_eu_tier", "EU Tier: %s");
        add("tooltip.nc.energy_eu_capacity", "EU Capacity: %s");
        add("tooltip.nc.energy_eu_generation", "EU Generation: %s/t");
        add("tooltip.nc.eu_amplifier.power", "EU Required: %s/t");
        add("tooltip.nc.build", "Build Multiblock");
        add("tooltip.nc.paste_json", "Paste JSON");
        add("tooltip.nc.paste_json.descr", "File content or file itself");
        add("tooltip.nc.wiki", "Open WIKI");
        add("tooltip.nc.link.designs", "Reactor Bots Discord");
        add("tooltip.nc.link.designs.descr", "Place where you can generate reactors");
        add("tooltip.nuclearcraft.particlestack.name", "Type: %s");
        add("tooltip.nuclearcraft.particlestack.amount", "Amount: %s");
        add("tooltip.nuclearcraft.particlestack.mean_energy", "Energy: %s");
        add("tooltip.nuclearcraft.particlestack.focus", "Focus: %s");
        add("tooltip.nuclearcraft.particlestack.focus_loss", "Focus Loss: %s/Block");
        add("tooltip.nuclearcraft.particlestack.travel_distance", "Travel Distance: %s Blocks");
        add("tooltip.nuclearcraft.particlestack.empty", "No Particles");
        add("tooltip.nc.analyze", "Analyze structure");
        add("tooltip.nc.analyze.descr", "Refresh multiblock data, analyze and provide more details");
        add("multiblock.build_in_chunk.advise", "Consider building structure within one chunk for better performance");

        add("decay_generator.fe_generation", "Result FE generation depends on attached blocks radiation");
        add("tooltip.nc.lightning_rod_charge", "Can charge NC Energy blocks by %s FE");
        add("tooltip.nc.jei.gas_to_fluid.desc","NC blocks automatically convert Mek Gases into Fluids on input.");

        add("tooltip.nc.hev.desc","Grants additional protection and passive effects when charged");
        add("tooltip.nc.moderator.desc","Fission Reactor moderator. Must be placed adjacent to a fuel cell. \n Each face adjacent to a fuel cell adds +%s%% efficiency and +%s%% heat gen.");

        add("boiling.recipe.heat_required","Heat required: %s H");

        add("tooltip.active_heatsink","Needs coolant fluid supply into reactor to work.");

        add("tooltip.kugelblitz.stability_info","You can stabilize blackhole with EXPL lasers burst. \n On low values blackhole vibrates at 15 level.");
        add("tooltip.kugelblitz.photon_concentrator","Concentrates photons into a single point. \n Need to be placed at center of all 6 chamber walls.");
        add("tooltip.kugelblitz.ready_for_burst","Ready for Laser Burst");
        add("tooltip.kugelblitz.flux_regulators","Flux Regulators: %s");
        add("tooltip.kugelblitz.transformers","Quantum Transformers: %s");
        add("tooltip.kugelblitz.stabilizers","Stabilizers: %s");
        add("tooltip.kugelblitz.block_neutronium_frame","Casing block of Kugelblitz Chamber");
        add("tooltip.kugelblitz.block_event_horizon_stabilizer","Helps to stabilize blackhole");
        add("tooltip.kugelblitz.block_quantum_flux_regulator","Increases FE generation of Kugelblitz Chamber");
        add("tooltip.kugelblitz.block_quantum_transformer","Increases Quantum Transformation rate of Kugelblitz Chamber");

        add("tooltip.upgrade_stack","Improved speed upgrade with parallel processing");
        add("tooltip.upgrade_speed","Increases recipe speed and energy consumption");
        add("tooltip.upgrade_energy","Reduces energy consumption and increases energy buffer.");
        add("tooltip.upgrade_quantum","Quantum tier: x5 speed and 1 parallel recipe per item");

        add("processor.recipe.power","Process Power: %s FE/t");
        add("processor.recipe.duration","Process Duration: %s t");
        add("processor.recipe.radiation","Process Radiation: %s uRad");

        add("fusion_core.charge","Charging: %s%%");
        add("fusion_core.recipe.cooling_rate","Cooling Rate: %s H");
        add("fusion_core.recipe.power","Base Energy Generation: %s FE/t");
        add("fusion_core.recipe.duration","Reaction Duration: %s t");
        add("fusion_core.recipe.radiation","Reaction Radiation: %s uRad");
        add("fusion_core.recipe.temperature","Optimal Temperature: %s MK");

        add("fission.recipe.criticality","Criticality: %s");
        add("fission.recipe.power","Base Energy Generation: %s FE/t");
        add("fission.recipe.duration","Depletion Time: %s s");
        add("fission.recipe.radiation","Reaction Radiation: %s pRad");
        add("fission.recipe.heat","Heat Generation: %s H/t");
        add("gui.nc.kugelblits_port.tooltip_1","Comparator: FE Gen");
        add("gui.nc.kugelblits_port.tooltip_2","Comparator: Mass");
        add("gui.nc.kugelblits_port.tooltip_3","Comparator: Transformation Progress");
        add("gui.nc.kugelblits_port.tooltip_4","Comparator: Inventory Size");
        add("gui.nc.kugelblits_port.tooltip_5","Input: Quantum Frequency");
        add("gui.nc.kugelblits_port.tooltip_6","Input: FE Gen vs Transformation rate");
        add("gui.nc.kugelblits_port.tooltip_strength","Signal strength: %s");
        add("gui.nc.reactor_mode.tooltip_steam","Boiling Mode");
        add("gui.nc.reactor_mode.tooltip_energy","Energy Mode");
        add("gui.nc.reactor_mode.timer","Changing mode in: %s sec");
        add("reactor.steam_per_tick","Boiling rate: %s mB/t");
        add("reactor.max_boiling_rate","Max rate: %s mB/t");

        add("nc.redstone_dimmer.description", "Adjusts redstone output power based on impulses received from the left or right sides.");
        add("nc.multiblock_builder.description", "For automated building.");
        add("tooltip.nc.fusion_connector.descr", "Used to connect fusion core and toroidal reactor chamber");
        add("tooltip.nc.fusion_casing.descr", "Used to build toroidal fusion reactor chamber");
        add("tooltip.nc.rf_amplifier.not_found","No RF Amplifiers attached");
        add("tooltip.nc.rf_amplifier.power","Energy Required: %s FE/t");
        add("tooltip.nc.rf_amplifier.voltage","Amplification: %s V");
        add("tooltip.nc.rf_amplifier.efficiency","Efficiency: %s%%");
        add("tooltip.nc.rf_amplifier.heat","Heat: %s H/t");
        add("tooltip.nc.rf_amplifier.max_temp","Max Temperature: %s K");

        add("tooltip.nc.reactor.charge","Charged: %s");
        add("tooltip.nc.reactor.running","Activation: %s");
        add("tooltip.nc.reactor.has_magnets","Electromagnets: %s");
        add("tooltip.nc.reactor.has_amplifiers","RF Amplifiers: %s");
        add("tooltip.nc.reactor.has_coolant","Coolant: %s");
        add("tooltip.nc.reactor.has_energy","Energy: %s");
        add("tooltip.nc.reactor.has_fuel","Fuel: %s");
        add("tooltip.nc.reactor.ready","Ready");
        add("tooltip.nc.reactor.not_ready","Not Ready");
        add("tooltip.nc.show_recipes","Show Recipes");
        add("gui.nc.fluid_tank_renderer.can_void","SHIFT+Mouse 1 to void content");

        add("tooltip.nc.electromagnet.not_found","No Electromagnets attached");
        add("tooltip.nc.electromagnet.power","Energy Required: %s FE/t");
        add("tooltip.nc.electromagnet.magnetic_field","Magnetic Field: %s T");
        add("tooltip.nc.description.efficiency","Efficiency: %s%%");
        add("tooltip.nc.description.expansion","Expansion: %s%%");
        add("tooltip.nc.electromagnet.heat","Heat: %s H/t");
        add("tooltip.nc.electromagnet.max_temp","Max Temperature: %s K");
        add("tooltip.nc.blade.desc","Converts the energy of the oncoming fluid flow into rotational energy in the rotor shaft. The expansion coefficient is larger than unity, so the volume of the fluid flow will increase each time it passes through a set. Must be placed in complete sets of four coplanar groups extending from the turbine shaft to the wall. Each blade block can process up to %s of oncoming fluid.");
        add("tooltip.nc.rotor_shaft.desc","Connects the rotor blades to the dynamo to convert the generated kinetic energy into electrical energy. Must be placed axially as a cuboid along the centre of the turbine interior.");
        add("tooltip.nc.bearing.desc","Connects the rotor shaft to the turbine wall and dynamo. Must cover the full area of each end of the shaft.");
        add("turbine.active.coils", "Active coils: %s");
        add("turbine.blades.flow", "Max steam flow: %s mB/t");
        add("tooltip.nc.liquid_empty","Stored: 0 of %s");
        add("tooltip.nc.liquid_stored","Stored: %s %s / %s");
        add("tooltip.nc.liquid_capacity","Capacity: %s");
        add("effect.nuclearcraft.radiation_resistance","Radiation Resistance");
        add("effect.nuclearcraft.radiation_decay","Radiation Decay");
        add("leacher.tooltip.valid_pump","Pump - Ok");
        add("leacher.tooltip.invalid_pump","Pump in the corner not found");
        add("tooltip.nc.target_chamber.camera","Center of Target Chamber structure");
        add("processor.description.nuclear_furnace","Fast furnace that uses uranium ingots as fuel. Surprisingly safe, by furnace standards.");
        add("processor.description.alloy_smelter","Smelts and alloys items.");
        add("processor.description.centrifuge","Separates fluids into their components.");
        add("processor.description.fuel_reprocessor","Separates depleted fuel into its components.");
        add("processor.description.melter","Melts items into liquids.");
        add("processor.description.ingot_former","Forms solid items from molten liquids.");
        add("processor.description.crystallizer","Grows crystals from solutions.");
        add("processor.description.chemical_reactor","Mixes fluids and gases together.");
        add("processor.description.assembler","Assembles items from prepared components.");
        add("processor.description.decay_hastener","Accelerates the decay of radioactive materials. Half-lives are merely a suggestion.");
        add("processor.description.electrolyzer","Separates fluids and gases into their components.");
        add("processor.description.extractor","Extracts liquids from solid items.");
        add("processor.description.fluid_enricher","Enriches fluids and gases with solid items.");
        add("processor.description.fluid_infuser","Combines fluids with items to produce new items.");
        add("processor.description.irradiator","Transforms items and fluids using radiative flux. Must be placed in the reactor wall.");
        add("processor.description.isotope_separator","Splits items into isotopes.");
        add("processor.description.manufactory","Crushes items into dusts and other materials.");
        add("processor.description.pressurizer","Compresses items under high pressure.");
        add("processor.description.rock_crusher","Produces dusts from rocks.");
        add("processor.description.supercooler","Cools down fluids and gases.");
        add("processor.description.steam_turbine","Generates energy from steam pressure.");
        add("processor.description.gas_scrubber","Cleans contaminants from ventilation. Recommended in the unlikely event of a release.");
        add("processor.description.pump","Pumps fluids and gases from the environment.");
        add("processor.description.analyzer","Analyzes items and environmental samples.");
        add("processor.description.leacher","Leaches underground minerals with acids and pumps the slurry back to the surface.");
        add("processor.description.subatomic_liquifier","Decomposes elements into their subatomic constituents.");

        add("amount","Amount: %s");
        add("sound_event.nuclearcraft.item.charged","Item Charged");
        add("tooltip.nc.analyzed","Item analyze completed");
        add("tooltip.nc.shielding.desc","Combine with armor in crafting grid");
        add("tooltip.nc.rad_shielding","Rad Shielding LVL: %s");
        add("tooltip.nc.use_in_leacher","Item can be used in Leacher");
        add("tooltip.nc.energy_stored","Energy Stored: %s / %s");
        add("tooltip.nc.energy_capacity","Energy Capacity: %s");
        add("tooltip.nc.radiation","Radiation: %s");
        add("tooltip.nc.radiation_removal","Removes Radiation: %s");
        add("tooltip.toggle_description_keys","Toggle description: CTRL+N");
        add("fuel.heat.descr","Base Heat Gen: %s H/t");
        add("message.nc.battery.side_config","Mode: %s");
        add("message.nc.switch_side.mode","Mode: %s");
        add("gui.nc.reactor_comparator_config.tooltip_1","Comparator: Energy Stored");
        add("gui.nc.turbine_comparator_config.tooltip_0","Comparator: Energy Stored");
        add("gui.nc.turbine_comparator_config.tooltip_2","Comparator: Overflow");
        add("gui.nc.reactor_comparator_config.tooltip_2","Comparator: Heat Stored");
        add("gui.nc.reactor_comparator_config.tooltip_3","Comparator: Depletion Progress");
        add("gui.nc.reactor_comparator_config.tooltip_4","Comparator: Fuel Left");
        add("gui.nc.reactor_comparator_config.tooltip_5","Input: On/Off Reactor");
        add("gui.nc.reactor_comparator_config.tooltip_6","Input: Moderation Control");
        add("gui.nc.reactor_comparator_config.tooltip_11","Comparator: Energy Stored");
        add("gui.nc.reactor_comparator_config.tooltip_12","Comparator: Heat Stored");
        add("gui.nc.reactor_comparator_config.tooltip_13","Comparator: Efficiency");
        add("gui.nc.reactor_comparator_strength.tooltip","Current Signal Strength: %s");
        add("gui.nc.redstone_config.tooltip_0","WORK MODE: IGNORE SIGNAL");
        add("gui.nc.redstone_config.tooltip_1","WORK MODE: ON SIGNAL");
        add("gui.nc.fluid_tank_renderer.amount_capacity","%s/%s mB");
        add("gui.nc.fluid_tank_renderer.amount","%s mB");
        add("fuel.forge_energy.descr","Forge Energy: %s FE/t");
        add("rtg.fe_generation","Energy Generation: %s FE/t");
        add("tooltip.nc.shift_rbm_to_change","Sneak+Use to change");
        add("tooltip.nc.qnp_mode","Mode: %s");
        add("tooltip.nc.q36_mode","Mode: %s");
        add("tooltip.nc.q36_mode.pulse","Pulse - short bursts");
        add("tooltip.nc.q36_mode.beam","Beam - sustained discharge");
        add("tooltip.nc.q36_charge","Charge: %s / %s QE");
        add("tooltip.nc.q36_cooldown","Recharging: %s ticks");
        add("tooltip.nc.q36_hint","Right-click to switch mode. Left-click to fire.");
        add("tooltip.mode.one_block","One Block");
        add("tooltip.mode.3x3","3x3");
        add("tooltip.mode.3x3x3","3x3x3");
        add("tooltip.mode.5x5","5x5");
        add("tooltip.mode.vein","Vein");
        add("tooltip.nc.chunk_position","Chunk Position: %s");
        add("nc.ore_vein.borax","Vein of Borax");
        add("nc.ore_vein.bornite","Vein of Bornite");
        add("nc.ore_vein.cassiterite","Vein of Cassiterite");
        add("nc.ore_vein.cobaltite","Vein of Cobaltite");
        add("nc.ore_vein.magnesite","Vein of Magnesite");
        add("nc.ore_vein.platinum","Vein of Platinum");
        add("nc.ore_vein.sphalerite","Vein of Sphalerite");
        add("nc.ore_vein.spodumene","Vein of Spodumene");
        add("nc.ore_vein.uraninite","Vein of Uraninite");
        add("nc.ore_vein.none","Veins not found");
        add("nc.ore_vein.mixed","Vein of Mixed minerals");
        add("tooltip.nc.content_saved","Content Saved");
        add("fuel.heat_boiling.descr","Boiling Reactor Heat: %s H/t");
        add("fuel.depletion.descr","Base Depletion Time: %s sec");
        add("fuel.criticality.descr","Criticality Factor: %s N/t");
        add("fuel.efficiency.descr","Base Efficiency: %s%%");
        add("fuel.description","Used in Fission Reactors. Use Ports to Load/Unload. \r\nActual FE generation depends on Reactor Efficiency.");
        add("tr_fuel.description","Used in Molten Salt Reactors. Use Ports to Load/Unload.");
        add("heat_sink.heat.descr", "Cooling Rate: %s H/t");
        add("heat_sink.placement.rule", "Must be placed %s");
        add("heat_sink.between", "between %s %s");
        add("heat_sink.atleast", "next to at least %s %s");
        add("heat_sink.atleasts", "next to at least %s %s blocks");
        add("heat_sink.exact", "next to exactly %s %s");
        add("heat_sink.exacts", "next to exactly %s %s blocks");
        add("heat_sink.less_than", "next to less than %s %s");
        add("heat_sink.in_corner", "in the corner of %s %s blocks");
        add("heat_sink.or", "or");
        add("heat_sink.and", "and");
        add("heat_sink.placement.error", "Error during placement rule generation");
        add("multiblock.interior.complete", "Interior Complete");
        add("multiblock.interior.incomplete", "Interior Incomplete");
        add("multiblock.casing.complete", "Multiblock Casing Complete");
        add("multiblock.casing.incomplete", "Multiblock Casing Incomplete");
        add("energy.bar.amount", "Total FE: %s / %s");
        add("tooltip.eu.bar.amount", "Total EU: %s / %s");
        add("reactor.internal_usage", "Internal usage: %s FE/t");
        add("coolant.bar.amount", "Coolant: %s / %s mB");
        add("hot_coolant.bar.amount", "Heated Coolant: %s / %s mB");
        add("heat.bar.amount", "Total Heat: %s / %s K");
        add("tooltip.nc.reactor.plasma_heat", "Plasma Heat: %s K");
        add("tooltip.nc.reactor.plasma_optimal", "Optimal: %s K");
        add("tooltip.machine.progress", "Progress: %s%%");
        add("reactor.fuel_cells", "Fuel Cells: %s");
        add("reactor.irradiators_connections", "Irradiation lines: %s");
        add("fission.interior.no_fuel_cells", "No Fuel Cells Found");
        add("fission.interior.no_moderators", "No Moderators Found");
        add("fission.interior.no_heat_sink", "No Heat Sinks Found");
        add("tooltip.nc.use_multitool", "Use Multitool to config sides");
        add("tooltip.nc.multitool.desc", "Commonly used to config sides of batteries, barrels, ports...");
        add("tooltip.nc.multitool.shift.desc", "Sneak + RBM to config back side. Can be used as detonator");
        add("message.nc.multitool.connected_to_bomb", "Connected to the bomb at %s,%s,%s");
        add("message.nc.multitool.connected_to_tnt", "Connected to the TNT at %s,%s,%s");
        add("message.nc.multitool.not_your_project", "That's not your project. Access denied");
        add("message.nc.multitool.armed_confirm", "armed, confirm detonation");
        add("message.nc.multitool.bomb_detonated", "Bomb at %s,%s,%s detonated");
        add("message.nc.multitool.tnt_detonated", "TNT at %s,%s,%s detonated");
        add("tooltip.nc.multitool.connected_to_bomb", " Connected to bomb at %s.%s.%s");
        add("tooltip.nc.multitool.connected_to_tnt", " Connected to TNT at %s.%s.%s");
        add("side_config.up", "UP: ");
        add("side_config.down", "DOWN: ");
        add("side_config.left", "LEFT: ");
        add("side_config.right", "RIGHT: ");
        add("side_config.front", "FRONT: ");
        add("side_config.back", "BACK: ");
        add("side_config.input", "INPUT");
        add("side_config.pull", "PULL");
        add("side_config.output", "OUTPUT");
        add("side_config.push", "PUSH");
        add("side_config.push_excess", "PUSH EXCESS");
        add("side_config.disabled", "DISABLED");
        add("side_config.default", "DEFAULT");
        add("gui.nc.side_config.tooltip", "Side Config");

        add("speed.parallel_processing", "Parallel processing: x%s");
        add("speed.multiplier", "Speed Multiplier: x%s");
        add("energy.multiplier", "Energy Multiplier: x%s");
        add("tooltip.nc.energy.per_tick", "Energy Per Tick: %s FE/t");
        add("tooltip.eu.tier", "Energy Tier: %s");
        add("tooltip.eu.per_tick", "Energy Per Tick: %s EU/t");

        add("reactor.cooling", "Cooling: %s H/t");
        add("reactor.heating", "Heat Gen: %s H/t");
        add("reactor.net_heat", "Net Heat: %s H/t");
        add("reactor.boiling_penalty", "Boiling Penalty: %s H/t");
        add("tooltip.nc.forge_energy_per_tick", "FE Gen: %s FE/t");
        add("tooltip.nc.eu_per_tick", "EU Gen: %s EU/t");
        add("reactor.heat_sinks_count", "Active Heat Sinks: %s");
        add("reactor.moderators_count", "Active Moderators: %s");
        add("reactor.moderation_level", "Moderation Level: %s%%");
        add("reactor.reactivity", "Reactivity: %s%%");
        add("validation.structure.too_big", "Structure is too big");
        add("validation.structure.too_small", "Structure is too small");
        add("validation.structure.incomplete", "Incomplete");
        add("validation.structure.wrong_outer", "Wrong Casing at: %s");
        add("validation.structure.wrong_inner", "Wrong Block at: %s");
        add("validation.structure.too_many_controllers", "Too many controllers");
        add("validation.structure.no_controller", "No controllers");
        add("validation.structure.no_port", "No port found");
        add("validation.structure.valid", "Structure is Valid");
        add("validation.structure.wrong_corner", "Wrong corner block at: %s");
        add("validation.structure.wrong_proportions", "Wrong proportions");
        add("validation.structure.wrong_blades", "Wrong blades placement");
        add("validation.structure.photon_concentrator", "Incorrect Photon Concentrators placement");
        add("validation.structure.asymetric_walls", "Walls are asymmetric");
        add("solar_panel.fe_generation", "Daytime Gen: %s FE/t");
        add("fission_port.descr", "One port for everything: Fluids, items, redstone, computers, etc...");
        add("irradiation_chamber.descr", "Irradiates items with neutron flux. \r\nMust be placed in a straight line with a moderator and a fuel cell behind it.");
        add("pile-driver_irradiation_chamber.descr", "x5 efficiency.");
    }

    private void fuel() {
        for(List<String> name: FissionFuel.NC_FUEL.keySet()) {
            add(FissionFuel.NC_FUEL.get(name).get(), convertToName(name.get(0))+" "+convertToName(name.get(1))+" "+name.get(2).toUpperCase()+" "+name.get(3).toUpperCase());
        }
        for(List<String> name: FissionFuel.NC_DEPLETED_FUEL.keySet()) {
            add(FissionFuel.NC_DEPLETED_FUEL.get(name).get(), convertToName(name.get(0))+" "+convertToName(name.get(1))+" "+name.get(2).toUpperCase()+" "+name.get(3).toUpperCase());
        }
    }

    private void ores() {
        for(String ore: NCBlocks.ORE_BLOCKS.keySet()) {
            add(NCBlocks.ORE_BLOCKS.get(ore).get(), convertToName(ore)+" Ore");
        }
    }

    private void items() {
        for(String name: NCItems.NC_ITEMS.keySet()) {
            add(NCItems.NC_ITEMS.get(name).get(), convertToName(name));
        }
        add("item."+MODID+".feral_ghoul_spawn_egg", "Feral Ghoul Spawn Egg");
        add("item."+MODID+".wanderer.desc", "Wanderer");
        add("item."+MODID+".end_of_the_world.desc", "End of the World");
        add("item."+MODID+".hyperspace.desc", "Hyperspace");
        add("item."+MODID+".money_for_nothing.desc", "Money For Nothing");
    }

    private void waste() {
        for(String name: FissionFuel.NC_WASTE.keySet()) {
            add(FissionFuel.NC_WASTE.get(name).get(), convertToName(name));
        }
    }

    private void isotopes() {
        for(String name: FissionFuel.NC_ISOTOPES.keySet()) {
            add(FissionFuel.NC_ISOTOPES.get(name).get(), convertToName(name));
        }
    }

    private void particleSources() {
        for(String name: ION_SOURCES.keySet()) {
            add(NCItems.ION_SOURCES.get(name).get(), convertToName(name));
        }
    }

    private void records() {
        for(String name: NCItems.NC_RECORDS.keySet()) {
            add(NCItems.NC_RECORDS.get(name).get(), convertToName(name));
        }
    }

    private void tools()
    {
        add(QNP.get(), "QNP");
        add(Q36.get(), "Q-36 Quantite Disruptor");
        add(MULTITOOL.get(), "Multitool");
        add(GEIGER_COUNTER.get(), "Geiger Counter");
        add(SPAXELHOE_THORIUM.get(), "Thorium Spaxel");
        add(SPAXELHOE_TOUGH.get(), "Tough Spaxel");
        add(LITHIUM_ION_CELL.get(), "Lithium Ion Cell");
    }

    private void armor() {
        add(TOUGH_HELMET.get(), "Tough Helmet");
        add(TOUGH_PANTS.get(), "Tough Pants");
        add(TOUGH_BOOTS.get(), "Tough Boots");
        add(TOUGH_CHEST.get(), "Tough Chest");
        
        add(HEV_HELMET.get(), "HEV Helmet");
        add(HEV_PANTS.get(), "HEV Pants");
        add(HEV_BOOTS.get(), "HEV Boots");
        add(HEV_CHEST.get(), "HEV Chest");

        add(HAZMAT_MASK.get(), "Hazmat Mask");
        add(HAZMAT_PANTS.get(), "Hazmat Pants");
        add(HAZMAT_BOOTS.get(), "Hazmat Boots");
        add(HAZMAT_CHEST.get(), "Hazmat Chest");
    }
    
    private void food() {
        for(String name: NCItems.NC_FOOD.keySet()) {
            add(NCItems.NC_FOOD.get(name).get(), convertToName(name));
        }
    }

    private void parts() {
        for(String name: NCItems.NC_PARTS.keySet()) {
            add(NCItems.NC_PARTS.get(name).get(), convertToName(name));
        }
        add(UNKNOWN_INGREDIENT.get(), "Unknown Ingredient");
    }

    private void gems() {
        for(String name: NCItems.NC_GEMS.keySet()) {
            add(NCItems.NC_GEMS.get(name).get(), convertToName(name)+" Gem");
        }
    }

    private void ingots() {
        for(String ingot: NC_INGOTS.keySet()) {
            add(NC_INGOTS.get(ingot).get(), convertToName(ingot)+" Ingot");
        }
    }

    private void plates() {
        for(String name: NCItems.NC_PLATES.keySet()) {
            add(NCItems.NC_PLATES.get(name).get(), convertToName(name)+" Plate");
        }
    }

    private void dusts() {
        for(String name: NCItems.NC_DUSTS.keySet()) {
            add(NCItems.NC_DUSTS.get(name).get(), convertToName(name)+" Dust");
        }
    }

    private void nuggets() {
        for(String name: NCItems.NC_NUGGETS.keySet()) {
            add(NCItems.NC_NUGGETS.get(name).get(), convertToName(name)+" Nugget");
        }
    }

    private void chunks() {
        for(String name: NCItems.NC_CHUNKS.keySet()) {
            add(NCItems.NC_CHUNKS.get(name).get(), convertToName(name)+" Chunk");
        }
    }

    private void blocks() {
        for(String name: NC_MATERIAL_BLOCKS.keySet()) {
            add(NC_MATERIAL_BLOCKS.get(name).get(), convertToName(name)+" Block");
        }
        for(String name: NC_BLOCKS.keySet()) {
            add(NC_BLOCKS.get(name).get(), convertToName(name)+" Block");
        }
        for(String name: NC_ELECTROMAGNETS.keySet()) {
            add(NC_ELECTROMAGNETS.get(name).get(), convertToName(name));
        }
        for(String name: NC_RF_AMPLIFIERS.keySet()) {
            add(NC_RF_AMPLIFIERS.get(name).get(), convertToName(name));
        }
        add("block."+MODID+".redstone_dimmer", "Redstone Dimmer");
        add("block."+MODID+".multiblock_builder", "Creative Multiblock Builder");
        add("block."+MODID+".charging_station", "Quantite Charging Station");
        add("block."+MODID+".charging_station.desc", "Bathes a Q-36 Quantite Disruptor in liquid Quantite Energy, restoring charge to the cell. 100 mB per refill tick; ten buckets bring an empty disruptor to full.");
        add("tooltip.nc.charging_station.fluid_empty", "No Quantite Energy");
        add("tooltip.nc.charging_station.energy", "Energy: %s / %s FE");
        add("block."+MODID+".portal", "Wasteland Portal");
        add("block."+MODID+".wasteland_earth", "Wasteland Earth");
        add("block."+MODID+".pu_239_bomb", "Pu-239 Implosion Device");
        add("block."+MODID+".pu_239_bomb.desc", "Plutonium-core implosion assembly, Mk-VII pattern. Arms on redstone input; fuses for 3 s before initiation. Field-rated, civilian-discouraged. (Fat Man wishes it had this trigger logic.)");
    }
}

