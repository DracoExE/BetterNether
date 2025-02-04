package paulevs.betternether.world.structures.plants;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import paulevs.betternether.BlocksHelper;
import paulevs.betternether.blocks.BlockProperties;
import paulevs.betternether.world.structures.IStructure;

public class StructureVine implements IStructure {
	private final Block block;

	public StructureVine(Block block) {
		this.block = block;
	}

	@Override
	public void generate(ServerLevelAccessor world, BlockPos pos, Random random, final int MAX_HEIGHT) {
		final MutableBlockPos blockPos = new MutableBlockPos();

		int h = BlocksHelper.downRay(world, pos, 25);
		if (h < 2)
			return;
		h = random.nextInt(h) + 1;

		BlockState bottom = block.defaultBlockState().setValue(BlockProperties.BOTTOM, true);
		BlockState middle = block.defaultBlockState().setValue(BlockProperties.BOTTOM, false);

		blockPos.set(pos);
		for (int y = 0; y < h; y++) {
			blockPos.setY(pos.getY() - y);
			if (world.isEmptyBlock(blockPos.below()))
				BlocksHelper.setWithoutUpdate(world, blockPos, middle);
			else {
				BlocksHelper.setWithoutUpdate(world, blockPos, bottom);
				return;
			}
		}
		BlocksHelper.setWithoutUpdate(world, blockPos.below(), bottom);
	}
}