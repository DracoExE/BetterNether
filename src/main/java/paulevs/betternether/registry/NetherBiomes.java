package paulevs.betternether.registry;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import paulevs.betternether.BetterNether;
import paulevs.betternether.biomes.CrimsonGlowingWoods;
import paulevs.betternether.biomes.CrimsonPinewood;
import paulevs.betternether.biomes.FloodedDeltas;
import paulevs.betternether.biomes.NetherBiome;
import paulevs.betternether.biomes.NetherBoneReef;
import paulevs.betternether.biomes.NetherGrasslands;
import paulevs.betternether.biomes.NetherGravelDesert;
import paulevs.betternether.biomes.NetherJungle;
import paulevs.betternether.biomes.NetherMagmaLand;
import paulevs.betternether.biomes.NetherMushroomForest;
import paulevs.betternether.biomes.NetherMushroomForestEdge;
import paulevs.betternether.biomes.NetherPoorGrasslands;
import paulevs.betternether.biomes.NetherSoulPlain;
import paulevs.betternether.biomes.NetherSulfuricBoneReef;
import paulevs.betternether.biomes.NetherSwampland;
import paulevs.betternether.biomes.NetherSwamplandTerraces;
import paulevs.betternether.biomes.NetherWartForest;
import paulevs.betternether.biomes.NetherWartForestEdge;
import paulevs.betternether.biomes.OldFungiwoods;
import paulevs.betternether.biomes.OldSwampland;
import paulevs.betternether.biomes.OldWarpedWoods;
import paulevs.betternether.biomes.UpsideDownForest;
import paulevs.betternether.config.Configs;
import paulevs.betternether.world.features.CavesFeature;
import paulevs.betternether.world.features.NetherChunkPopulatorFeature;
import paulevs.betternether.world.features.PathsFeature;
import paulevs.betternether.world.structures.CityFeature;
import ru.bclib.api.BiomeAPI;
import ru.bclib.api.LifeCycleAPI;
import ru.bclib.world.biomes.BCLBiome;

public class NetherBiomes {
	private static final ArrayList<NetherBiome> REGISTRY = new ArrayList<NetherBiome>();
	private static final ArrayList<NetherBiome> ALL_BIOMES = new ArrayList<NetherBiome>();
	private static final Set<Integer> OCCUPIED_IDS = Sets.newHashSet();

	
	public static final NetherBiome BIOME_GRAVEL_DESERT = new NetherGravelDesert("Gravel Desert");
	public static final NetherBiome BIOME_NETHER_JUNGLE = new NetherJungle("Nether Jungle");
	public static final NetherBiome BIOME_WART_FOREST = new NetherWartForest("Wart Forest");
	public static final NetherBiome BIOME_GRASSLANDS = new NetherGrasslands("Nether Grasslands");
	public static final NetherBiome BIOME_MUSHROOM_FOREST = new NetherMushroomForest("Nether Mushroom Forest");
	public static final NetherBiome BIOME_MUSHROOM_FOREST_EDGE = new NetherMushroomForestEdge("Nether Mushroom Forest Edge");
	public static final NetherBiome BIOME_WART_FOREST_EDGE = new NetherWartForestEdge("Wart Forest Edge");
	public static final NetherBiome BIOME_BONE_REEF = new NetherBoneReef("Bone Reef");
	public static final NetherBiome BIOME_SULFURIC_BONE_REEF = new NetherSulfuricBoneReef("Sulfuric Bone Reef");
	public static final NetherBiome BIOME_POOR_GRASSLANDS = new NetherPoorGrasslands("Poor Nether Grasslands");
	public static final NetherBiome NETHER_SWAMPLAND = new NetherSwampland("Nether Swampland");
	public static final NetherBiome NETHER_SWAMPLAND_TERRACES = new NetherSwamplandTerraces("Nether Swampland Terraces");
	public static final NetherBiome MAGMA_LAND = new NetherMagmaLand("Magma Land");
	public static final NetherBiome SOUL_PLAIN = new NetherSoulPlain("Soul Plain");
	public static final NetherBiome CRIMSON_GLOWING_WOODS = new CrimsonGlowingWoods("Crimson Glowing Woods");
	public static final NetherBiome OLD_WARPED_WOODS = new OldWarpedWoods("Old Warped Woods");
	public static final NetherBiome CRIMSON_PINEWOOD = new CrimsonPinewood("Crimson Pinewood");
	public static final NetherBiome OLD_FUNGIWOODS = new OldFungiwoods("Old Fungiwoods");
	public static final NetherBiome FLOODED_DELTAS = new FloodedDeltas("Flooded Deltas");
	public static final NetherBiome UPSIDE_DOWN_FOREST = new UpsideDownForest("Upside Down Forest");
	public static final NetherBiome OLD_SWAMPLAND = new OldSwampland("Old Swampland");

	private static int maxDefChance = 0;
	private static int maxChance = 0;
	private static int biomeID = 7000;

	public static void register() {
		BuiltinRegistries.BIOME.forEach((biome) -> {
			if (biome.getBiomeCategory() == BiomeCategory.NETHER) {
				ResourceLocation id = BuiltinRegistries.BIOME.getKey(biome);
				Configs.GENERATOR.getFloat("biomes." + id.getNamespace() + ".main", id.getPath() + "_chance", 1);
			}
		});
		
		registerNetherBiome(BIOME_GRAVEL_DESERT);
		registerNetherBiome(BIOME_NETHER_JUNGLE);
		registerNetherBiome(BIOME_WART_FOREST);
		registerNetherBiome(BIOME_GRASSLANDS);
		registerNetherBiome(BIOME_MUSHROOM_FOREST);
		registerEdgeBiome(BIOME_MUSHROOM_FOREST_EDGE, BIOME_MUSHROOM_FOREST, 2);
		registerEdgeBiome(BIOME_WART_FOREST_EDGE, BIOME_WART_FOREST, 2);
		registerNetherBiome(BIOME_BONE_REEF);
		registerSubBiome(BIOME_SULFURIC_BONE_REEF, BIOME_BONE_REEF, 0.3F);
		registerSubBiome(BIOME_POOR_GRASSLANDS, BIOME_GRASSLANDS, 0.3F);
		registerNetherBiome(NETHER_SWAMPLAND);
		registerSubBiome(NETHER_SWAMPLAND_TERRACES, NETHER_SWAMPLAND, 1F);
		registerNetherBiome(MAGMA_LAND);
		registerSubBiome(SOUL_PLAIN, BIOME_WART_FOREST, 1F);
		registerSubBiome(CRIMSON_GLOWING_WOODS, BiomeAPI.CRIMSON_FOREST_BIOME, 0.3F);
		registerSubBiome(OLD_WARPED_WOODS, BiomeAPI.WARPED_FOREST_BIOME, 1F);
		registerSubBiome(CRIMSON_PINEWOOD, BiomeAPI.CRIMSON_FOREST_BIOME, 0.3F);
		registerSubBiome(OLD_FUNGIWOODS, BIOME_MUSHROOM_FOREST, 0.3F);
		registerSubBiome(FLOODED_DELTAS, BiomeAPI.BASALT_DELTAS_BIOME, 1F);
		registerNetherBiome(UPSIDE_DOWN_FOREST);
		registerSubBiome(OLD_SWAMPLAND, NETHER_SWAMPLAND, 1F);
		
		RegistryEntryAddedCallback.event(BuiltinRegistries.BIOME).register((i, id, biome) -> {
			if (biome.getBiomeCategory() == BiomeCategory.NETHER) {
				ResourceLocation bioID = BuiltinRegistries.BIOME.getKey(biome);
				Configs.GENERATOR.getFloat("biomes." + bioID.getNamespace() + ".main", bioID.getPath() + "_chance", 1);
			}
		});
		
		BiomeAPI.registerNetherBiomeModification((biomeID, biome) -> {
			if (!biomeID.getNamespace().equals(BetterNether.MOD_ID)) {
				modifyNonBNBiome(biome);
				NetherFeatures.modifyNonBNBiome(biome);
				NetherStructures.modifyNonBNBiome(biome);
			}
		});

		LifeCycleAPI.onLevelLoad(NetherBiomes::onWorldLoad);
	}
	
	private static void modifyNonBNBiome(Biome biome) {
		BiomeAPI.addBiomeMobSpawn(biome, EntityRegistry.FIREFLY, 5, 3, 6);
		BiomeAPI.addBiomeMobSpawn(biome, EntityRegistry.HYDROGEN_JELLYFISH, 5, 2, 5);
		BiomeAPI.addBiomeMobSpawn(biome, EntityRegistry.NAGA, 8, 3, 5);
	}
	
	private static void register(NetherBiome biome) {
		if (BuiltinRegistries.BIOME.get(biome.getID()) == null) {
			if (OCCUPIED_IDS.isEmpty()) {
				BuiltinRegistries.BIOME.forEach((bio) -> {
					OCCUPIED_IDS.add(BuiltinRegistries.BIOME.getId(bio));
				});
			}
			biomeID ++;
			while (OCCUPIED_IDS.contains(biomeID)) {
				biomeID ++;
			}
			//Registry.registerMapping(BuiltinRegistries.BIOME, biomeID, biome.getID().toString(), biome.getBiome());
		}
	}

	private static void registerNetherBiome(NetherBiome biome) {
		float chance = Configs.GENERATOR.getFloat("biomes." + biome.getID().getNamespace() + ".main", biome.getID().getPath() + "_chance", 1);
		if (chance > 0.0F) {
			maxChance += chance;
			String path = "generator.biome." + biome.getID().getNamespace() + "." + biome.getID().getPath();
			biome.setPlantDensity(Configs.BIOMES.getFloat(path, "plants_and_structures_density", 1));
			//biome.setGenChance(maxChance);
			biome.build();
			REGISTRY.add(biome);
			ALL_BIOMES.add(biome);
			register(biome);
			
			Biome b = BuiltinRegistries.BIOME.get(biome.getID());
			if (b==null) {
				BiomeAPI.registerNetherBiome(biome);
			}
		}
	}
	
	private static void registerEdgeBiome(NetherBiome biome, NetherBiome mainBiome, int size) {
		String regName = biome.getRegistryName();
		int sizeConf = (int)Configs.GENERATOR.getFloat("biomes.betternether.edge", regName + "_size", size);
		if (sizeConf > 0.0F) {
			String path = "generator.biome." + biome.getID().getNamespace() + "." + biome.getID().getPath();
			biome.setPlantDensity(Configs.BIOMES.getFloat(path, "plants_and_structures_density", 1));
			mainBiome.setEdge(biome);
			mainBiome.setEdgeSize(sizeConf);
			biome.build();
			ALL_BIOMES.add(biome);
			register(biome);
			
			BiomeAPI.registerBiome(biome);
		}
	}
	
	private static void registerSubBiome(NetherBiome biome, BCLBiome mainBiome, float chance) {
		String regName = biome.getRegistryName();
		chance = Configs.GENERATOR.getFloat("biomes.betternether.variation", regName + "_chance", chance);
		if (chance > 0.0F) {
			String path = "generator.biome." + biome.getID().getNamespace() + "." + biome.getID().getPath();
			biome.setPlantDensity(Configs.BIOMES.getFloat(path, "plants_and_structures_density", 1));
			biome.build();
			ALL_BIOMES.add(biome);
			register(biome);
			
			BiomeAPI.registerSubBiome(mainBiome, biome);
		}
	}

	public static BCLBiome getBiome(Random random) {
		return BiomeAPI.NETHER_BIOME_PICKER.getBiome(random);
	}

	public static ArrayList<NetherBiome> getAllBiomes() {
		return ALL_BIOMES;
	}
	
	public static void onWorldLoad(ServerLevel level, long seed, Registry<Biome> registry) {
		CavesFeature.onLoad(seed);
		PathsFeature.onLoad(seed);
		CityFeature.initGenerator();
        NetherChunkPopulatorFeature.clearGeneratorPool();
	}
}
