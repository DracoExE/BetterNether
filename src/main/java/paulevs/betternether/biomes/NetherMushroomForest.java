package paulevs.betternether.biomes;

import java.util.Random;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.BiomeParticleConfig;
import paulevs.betternether.BlocksHelper;
import paulevs.betternether.registers.BlocksRegister;
import paulevs.betternether.registers.SoundsRegister;
import paulevs.betternether.structures.StructureType;
import paulevs.betternether.structures.plants.StructureGiantMold;
import paulevs.betternether.structures.plants.StructureGrayMold;
import paulevs.betternether.structures.plants.StructureLucis;
import paulevs.betternether.structures.plants.StructureMedBrownMushroom;
import paulevs.betternether.structures.plants.StructureMedRedMushroom;
import paulevs.betternether.structures.plants.StructureMushroomFir;
import paulevs.betternether.structures.plants.StructureOrangeMushroom;
import paulevs.betternether.structures.plants.StructureRedMold;
import paulevs.betternether.structures.plants.StructureVanillaMushroom;
import paulevs.betternether.structures.plants.StructureWallBrownMushroom;
import paulevs.betternether.structures.plants.StructureWallRedMushroom;

public class NetherMushroomForest extends NetherBiome
{
	public NetherMushroomForest(String name)
	{
		super(new BiomeDefenition(name)
				.setColor(166, 38, 95)
				.setLoop(SoundsRegister.AMBIENT_MUSHROOM_FOREST)
				.setAdditions(SoundEvents.AMBIENT_CRIMSON_FOREST_ADDITIONS)
				.setParticleConfig(new BiomeParticleConfig(
						ParticleTypes.MYCELIUM,
						0.1F,
						(random) -> { return 0.0; },
						(random) -> { return 0.0; },
						(random) -> { return 0.0; })));
		this.setNoiseDensity(0.5F);
		addStructure("large_red_mushroom", new StructureMedRedMushroom(), StructureType.FLOOR, 0.12F, true);
		addStructure("large_brown_mushroom", new StructureMedBrownMushroom(), StructureType.FLOOR, 0.12F, true);
		addStructure("giant_mold", new StructureGiantMold(), StructureType.FLOOR, 0.12F, true);
		addStructure("mushroom_fir", new StructureMushroomFir(), StructureType.FLOOR, 0.2F, true);
		addStructure("vanilla_mushrooms", new StructureVanillaMushroom(), StructureType.FLOOR, 0.1F, false);
		addStructure("orange_mushroom", new StructureOrangeMushroom(), StructureType.FLOOR, 0.05F, true);
		addStructure("red_mold", new StructureRedMold(), StructureType.FLOOR, 0.5F, true);
		addStructure("gray_mold", new StructureGrayMold(), StructureType.FLOOR, 0.5F, true);
		addStructure("lucis", new StructureLucis(), StructureType.WALL, 0.05F, false);
		addStructure("wall_red_mushroom", new StructureWallRedMushroom(), StructureType.WALL, 0.8F, true);
		addStructure("wall_brown_mushroom", new StructureWallBrownMushroom(), StructureType.WALL, 0.8F, true);
	}

	@Override
	public void genSurfColumn(IWorld world, BlockPos pos, Random random)
	{
		BlocksHelper.setWithoutUpdate(world, pos, BlocksRegister.NETHER_MYCELIUM.getDefaultState());
	}
}
