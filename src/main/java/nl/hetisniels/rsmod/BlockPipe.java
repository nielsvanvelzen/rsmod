package nl.hetisniels.rsmod;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.List;

class BlockPipe extends Block {
	private static final AxisAlignedBB AABB_BASE = new AxisAlignedBB(4 * (1F / 16F), 4 * (1F / 16F), 4 * (1F / 16F), 12 * (1F / 16F), 12 * (1F / 16F), 12 * (1F / 16F));
	private static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(4 * (1F / 16F), 4 * (1F / 16F), 0 * (1F / 16F), 12 * (1F / 16F), 12 * (1F / 16F), 4 * (1F / 16F));
	private static final AxisAlignedBB AABB_EAST = new AxisAlignedBB(12 * (1F / 16F), 4 * (1F / 16F), 4 * (1F / 16F), 16 * (1F / 16F), 12 * (1F / 16F), 12 * (1F / 16F));
	private static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(4 * (1F / 16F), 4 * (1F / 16F), 12 * (1F / 16F), 12 * (1F / 16F), 12 * (1F / 16F), 16 * (1F / 16F));
	private static final AxisAlignedBB AABB_WEST = new AxisAlignedBB(0 * (1F / 16F), 4 * (1F / 16F), 4 * (1F / 16F), 4 * (1F / 16F), 12 * (1F / 16F), 12 * (1F / 16F));
	private static final AxisAlignedBB AABB_UP = new AxisAlignedBB(4 * (1F / 16F), 12 * (1F / 16F), 4 * (1F / 16F), 12 * (1F / 16F), 16 * (1F / 16F), 12 * (1F / 16F));
	private static final AxisAlignedBB AABB_DOWN = new AxisAlignedBB(4 * (1F / 16F), 0 * (1F / 16F), 4 * (1F / 16F), 12 * (1F / 16F), 4 * (1F / 16F), 12 * (1F / 16F));

	public static final PropertyBool NORTH = PropertyBool.create("north");
	public static final PropertyBool EAST = PropertyBool.create("east");
	public static final PropertyBool SOUTH = PropertyBool.create("south");
	public static final PropertyBool WEST = PropertyBool.create("west");
	public static final PropertyBool UP = PropertyBool.create("up");
	public static final PropertyBool DOWN = PropertyBool.create("down");

	BlockPipe(Material blockMaterialIn, MapColor blockMapColorIn) {
		super(blockMaterialIn, blockMapColorIn);

		setHardness(0.8F);
		setUnlocalizedName(RSMod.MODID + ".pipe");
		setRegistryName(RSMod.MODID, "pipe");
		setCreativeTab(RSMod.CREATIVE_TAB);
	}

	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, NORTH,
				EAST,
				SOUTH,
				WEST,
				UP,
				DOWN);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.withProperty(NORTH, hasConnectionWith(world, pos.north(), EnumFacing.SOUTH))
				.withProperty(EAST, hasConnectionWith(world, pos.east(), EnumFacing.WEST))
				.withProperty(SOUTH, hasConnectionWith(world, pos.south(), EnumFacing.NORTH))
				.withProperty(WEST, hasConnectionWith(world, pos.west(), EnumFacing.EAST))
				.withProperty(UP, hasConnectionWith(world, pos.up(), EnumFacing.DOWN))
				.withProperty(DOWN, hasConnectionWith(world, pos.down(), EnumFacing.UP));
	}

	private static boolean hasConnectionWith(IBlockAccess world, BlockPos pos, EnumFacing facing) {
		return (world.getBlockState(pos).getBlock().hasTileEntity() &&
				world.getTileEntity(pos).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) ||
				world.getBlockState(pos).getBlock() instanceof BlockPipe;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isTranslucent(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d origin, Vec3d direction) {
		state = getActualState(state, world, pos);

		RayTraceResult result = AABB_BASE.expandXyz(0.01).offset(pos).calculateIntercept(origin, direction);

		if (result == null && state.getValue(NORTH))
			result = AABB_NORTH.offset(pos).calculateIntercept(origin, direction);

		if (result == null && state.getValue(EAST))
			result = AABB_EAST.offset(pos).calculateIntercept(origin, direction);

		if (result == null && state.getValue(SOUTH))
			result = AABB_SOUTH.offset(pos).calculateIntercept(origin, direction);

		if (result == null && state.getValue(WEST))
			result = AABB_WEST.offset(pos).calculateIntercept(origin, direction);

		if (result == null && state.getValue(UP))
			result = AABB_UP.offset(pos).calculateIntercept(origin, direction);

		if (result == null && state.getValue(DOWN))
			result = AABB_DOWN.offset(pos).calculateIntercept(origin, direction);

		return result == null ? null : new RayTraceResult(result.hitVec.addVector((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()), result.sideHit, pos);
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity) {
		state = getActualState(state, world, pos);

		addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BASE);

		if (state.getValue(NORTH))
			addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_NORTH);

		if (state.getValue(EAST))
			addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_EAST);

		if (state.getValue(SOUTH))
			addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_SOUTH);

		if (state.getValue(WEST))
			addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_WEST);

		if (state.getValue(UP))
			addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_UP);

		if (state.getValue(DOWN))
			addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_DOWN);
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return true;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return super.getBoundingBox(state, source, pos);
	}

	public Item createItemForBlock() {
		ItemBlock itemBlock = new ItemBlock(this);
		itemBlock.setRegistryName(this.getRegistryName());

		return itemBlock;
	}

	public void drawBlockHighlight(DrawBlockHighlightEvent e) {
		BlockPos blockPos = e.getTarget().getBlockPos();
		Block block = e.getPlayer().worldObj.getBlockState(blockPos).getBlock();
		IBlockState state = e.getPlayer().worldObj.getBlockState(blockPos).getActualState(e.getPlayer().worldObj, blockPos);

		if (!(block instanceof BlockPipe) || !(state.getBlock() instanceof BlockPipe))
			return;

		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
		GL11.glLineWidth(2.0F);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDepthMask(false);

		double d0 = e.getPlayer().lastTickPosX + (e.getPlayer().posX - e.getPlayer().lastTickPosX) * (double) e.getPartialTicks();
		double d1 = e.getPlayer().lastTickPosY + (e.getPlayer().posY - e.getPlayer().lastTickPosY) * (double) e.getPartialTicks();
		double d2 = e.getPlayer().lastTickPosZ + (e.getPlayer().posZ - e.getPlayer().lastTickPosZ) * (double) e.getPartialTicks();


		RenderGlobal.drawSelectionBoundingBox(AABB_BASE.expandXyz(0.01).offset(blockPos.getX(), blockPos.getY(), blockPos.getZ()).offset(-d0, -d1, -d2));

		if (state.getValue(NORTH))
			RenderGlobal.drawSelectionBoundingBox(AABB_NORTH.expandXyz(0.01).offset(blockPos.getX(), blockPos.getY(), blockPos.getZ()).offset(-d0, -d1, -d2));

		if (state.getValue(EAST))
			RenderGlobal.drawSelectionBoundingBox(AABB_EAST.expandXyz(0.01).offset(blockPos.getX(), blockPos.getY(), blockPos.getZ()).offset(-d0, -d1, -d2));

		if (state.getValue(SOUTH))
			RenderGlobal.drawSelectionBoundingBox(AABB_SOUTH.expandXyz(0.01).offset(blockPos.getX(), blockPos.getY(), blockPos.getZ()).offset(-d0, -d1, -d2));

		if (state.getValue(WEST))
			RenderGlobal.drawSelectionBoundingBox(AABB_WEST.expandXyz(0.01).offset(blockPos.getX(), blockPos.getY(), blockPos.getZ()).offset(-d0, -d1, -d2));

		if (state.getValue(UP))
			RenderGlobal.drawSelectionBoundingBox(AABB_UP.expandXyz(0.01).offset(blockPos.getX(), blockPos.getY(), blockPos.getZ()).offset(-d0, -d1, -d2));

		if (state.getValue(DOWN))
			RenderGlobal.drawSelectionBoundingBox(AABB_DOWN.expandXyz(0.01).offset(blockPos.getX(), blockPos.getY(), blockPos.getZ()).offset(-d0, -d1, -d2));

		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		e.setCanceled(true);
	}
}
