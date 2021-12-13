package paulevs.betternether.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import paulevs.betternether.BlocksHelper;
import paulevs.betternether.MHelper;
import paulevs.betternether.biomes.NetherBiome;
import paulevs.betternether.config.Configs;
import paulevs.betternether.structures.StructureType;
import paulevs.betternether.world.features.CavesFeature;
import ru.bclib.api.BiomeAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class BNWorldGenerator {
	private static float structureDensity;
	private static float lavaStructureDensity;
	private static float globalDensity;
	protected static int biomeSizeXZ;
	protected static int biomeSizeY;

	private MutableBlockPos popPos = new MutableBlockPos();
	private final NetherBiome[][] BIOMES = new NetherBiome[8][8];

	private final List<BlockPos> LIST_FLOOR = new ArrayList<BlockPos>(4096);
	private final List<BlockPos> LIST_WALL = new ArrayList<BlockPos>(4096);
	private final List<BlockPos> LIST_CEIL = new ArrayList<BlockPos>(4096);
	private final List<BlockPos> LIST_LAVA = new ArrayList<BlockPos>(1024);
	private final HashSet<Biome> MC_BIOMES = new HashSet<Biome>();

	private NetherBiome biome;
	
	public static void onModInit() {
		structureDensity = Configs.GENERATOR.getFloat("generator.world", "structures_density", 1F / 16F) * 1.0001F;
		lavaStructureDensity = Configs.GENERATOR.getFloat("generator.world", "lava_structures_density", 1F / 200F) * 1.0001F;
		globalDensity = Configs.GENERATOR.getFloat("generator.world", "global_plant_and_structures_density", 1F) * 1.0001F;

		biomeSizeXZ = Configs.GENERATOR.getInt("generator_world", "biome_size_xz", 200);
		biomeSizeY = Configs.GENERATOR.getInt("generator_world", "biome_size_y", 40);
	}

	private NetherBiome getBiomeLocal(int x, int y, int z, Random random) {
		final int px = (int) Math.round(x + random.nextGaussian() * 0.5) >> 1;
		final int pz = (int) Math.round(z + random.nextGaussian() * 0.5) >> 1;
		return BIOMES[clamp(px, 7)][clamp(pz, 7)];
	}

	private static int clamp(int x, int max) {
		return x < 0 ? 0 : x > max ? max : x;
	}

	public void populate(WorldGenLevel world, int sx, int sz, Random random) {
		final int MAX_HEIGHT = world.dimensionType().logicalHeight();
		// Structure Generator
		if (random.nextFloat() < structureDensity) {
			popPos.set(sx + random.nextInt(16), MHelper.randRange(33, MAX_HEIGHT-28, random), sz + random.nextInt(16));
			StructureType type = StructureType.FLOOR;
			boolean isAir = world.getBlockState(popPos).getMaterial().isReplaceable();
			boolean airUp = world.getBlockState(popPos.above()).getMaterial().isReplaceable() && world.getBlockState(popPos.above(3)).getMaterial().isReplaceable();
			boolean airDown = world.getBlockState(popPos.below()).getMaterial().isReplaceable() && world.getBlockState(popPos.below(3)).getMaterial().isReplaceable();
			NetherBiome biome = getBiomeLocal(popPos.getX() - sx, popPos.getY(), popPos.getZ() - sz, random);
			if (!isAir && !airUp && !airDown && random.nextInt(8) == 0)
				type = StructureType.UNDER;
			else {
				if (popPos.getY() < 45 || (biome!=null && !biome.hasCeilStructures()) || random.nextBoolean()) // Floor
				{
					if (!isAir) {
						while (!world.getBlockState(popPos).getMaterial().isReplaceable() && popPos.getY() > 1) {
							popPos.setY(popPos.getY() - 1);
						}
					}
					while (world.getBlockState(popPos.below()).getMaterial().isReplaceable() && popPos.getY() > 1) {
						popPos.setY(popPos.getY() - 1);
					}
				}
				else // Ceil
				{
					if (!isAir) {
						while (!world.getBlockState(popPos).getMaterial().isReplaceable() && popPos.getY() > 1) {
							popPos.setY(popPos.getY() + 1);
						}
					}
					while (!BlocksHelper.isNetherGroundMagma(world.getBlockState(popPos.above())) && popPos.getY() < 127) {
						popPos.setY(popPos.getY() + 1);
					}
					type = StructureType.CEIL;
				}
			}
			biome = getBiomeLocal(popPos.getX() - sx, popPos.getY(), popPos.getZ() - sz, random);
			if (biome!=null) {
				if (world.getBlockState(popPos)
						 .getMaterial()
						 .isReplaceable()) {
					if (type == StructureType.FLOOR) {
						BlockState down = world.getBlockState(popPos.below());
						if (BlocksHelper.isNetherGroundMagma(down)) biome.genFloorBuildings(world, popPos, random);
					}
					else if (type == StructureType.CEIL) {
						BlockState up = world.getBlockState(popPos.above());
						if (BlocksHelper.isNetherGroundMagma(up)) {
							biome.genCeilBuildings(world, popPos, random);
						}
					}
				}
				else biome.genUnderBuildings(world, popPos, random);
			}
		}

		if (random.nextFloat() < lavaStructureDensity) {
			popPos.set(sx + random.nextInt(16), 32, sz + random.nextInt(16));
			if (world.isEmptyBlock(popPos) && BlocksHelper.isLava(world.getBlockState(popPos.below()))) {
				biome = getBiomeLocal(popPos.getX() - sx, popPos.getY(), popPos.getZ() - sz, random);
				if (biome!=null) {
					biome.genLavaBuildings(world, popPos, random);
				}
			}
		}

		LIST_LAVA.clear();
		LIST_FLOOR.clear();
		LIST_WALL.clear();
		LIST_CEIL.clear();

		int ex = sx + 16;
		int ez = sz + 16;

		for (int x = 0; x < 16; x++) {
			int wx = sx + x;
			for (int z = 0; z < 16; z++) {
				int wz = sz + z;
				for (int y = 1; y < MAX_HEIGHT-2; y++) {
					if (CavesFeature.isInCave(x, y, z))
						continue;

					biome = getBiomeLocal(x, y, z, random);
					if (biome!=null) {
						popPos.set(wx, y, wz);
						BlockState state = world.getBlockState(popPos);
						boolean lava = BlocksHelper.isLava(state);
						if (lava || BlocksHelper.isNetherGroundMagma(state) || state.getBlock() == Blocks.GRAVEL) {
							if (!lava && ((state = world.getBlockState(popPos.above())).isAir() || !state.getMaterial()
																										 .isSolidBlocking() || !state.getMaterial()
																																	 .blocksMotion()) && state.getFluidState()
																																							  .isEmpty())// world.isAir(popPos.up()))
								biome.genSurfColumn(world, popPos, random);
							
							if (((x + y + z) & 1) == 0 && random.nextFloat() < globalDensity && random.nextFloat() < biome.getPlantDensity()) {
								// Ground Generation
								if (world.isEmptyBlock(popPos.above())) {
									if (lava) LIST_LAVA.add(popPos.above());
									else LIST_FLOOR.add(new BlockPos(popPos.above()));
								}
								
								// Ceiling Generation
								else if (world.isEmptyBlock(popPos.below())) {
									LIST_CEIL.add(new BlockPos(popPos.below()));
								}
								
								// Wall Generation
								else {
									boolean bNorth = world.isEmptyBlock(popPos.north());
									boolean bSouth = world.isEmptyBlock(popPos.south());
									boolean bEast = world.isEmptyBlock(popPos.east());
									boolean bWest = world.isEmptyBlock(popPos.west());
									if (bNorth || bSouth || bEast || bWest) {
										BlockPos objPos = null;
										if (bNorth) objPos = popPos.north();
										else if (bSouth) objPos = popPos.south();
										else if (bEast) objPos = popPos.east();
										else objPos = popPos.west();
										
										if ((popPos.getX() >= sx) && (popPos.getX() < ex) && (popPos.getZ() >= sz) && (popPos.getZ() < ez)) {
											boolean bDown = world.isEmptyBlock(objPos.below());
											boolean bUp = world.isEmptyBlock(objPos.above());
											
											if (bDown && bUp) {
												LIST_WALL.add(new BlockPos(objPos));
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		for (BlockPos pos : LIST_LAVA) {
			if (world.isEmptyBlock(pos)) {
				biome = getBiomeLocal(pos.getX() - sx, pos.getY(), pos.getZ() - sz, random);
				if (biome != null)
					biome.genLavaObjects(world, pos, random);
			}
		}

		for (BlockPos pos : LIST_FLOOR)
			if (world.isEmptyBlock(pos)) {
				biome = getBiomeLocal(pos.getX() - sx, pos.getY(), pos.getZ() - sz, random);
				if (biome != null)
					biome.genFloorObjects(world, pos, random);
			}

		for (BlockPos pos : LIST_WALL)
			if (world.isEmptyBlock(pos)) {
				biome = getBiomeLocal(pos.getX() - sx, pos.getY(), pos.getZ() - sz, random);
				if (biome != null)
					biome.genWallObjects(world, pos, random);
			}

		for (BlockPos pos : LIST_CEIL)
			if (world.isEmptyBlock(pos)) {
				biome = getBiomeLocal(pos.getX() - sx, pos.getY(), pos.getZ() - sz, random);
				if (biome != null)
					biome.genCeilObjects(world, pos, random);
			}
	}

	private void makeLocalBiomes(WorldGenLevel world, int sx, int sz) {
		MC_BIOMES.clear();
		popPos.setY(5);
		for (int x = 0; x < 8; x++) {
			popPos.setX(sx + (x << 1) + 2);
			for (int z = 0; z < 8; z++) {
				popPos.setZ(sz + (z << 1) + 2);
				Biome b = world.getBiome(popPos);
				if (BiomeAPI.getFromBiome(b) instanceof NetherBiome nBiome) {
					BIOMES[x][z] = nBiome;
				} else {
					BIOMES[x][z] = null;//BiomesRegistry.BIOME_EMPTY_NETHER;
				}
				//BIOMES[x][y][z] = BiomesRegistry.getFromBiome(b);
				MC_BIOMES.add(b);
			}
		}
	}

	public void prePopulate(WorldGenLevel world, int sx, int sz, Random random) {
		makeLocalBiomes(world, sx, sz);
	}
}
