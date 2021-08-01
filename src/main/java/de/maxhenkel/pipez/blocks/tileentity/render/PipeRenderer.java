package de.maxhenkel.pipez.blocks.tileentity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import de.maxhenkel.corelib.CachedValue;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import de.maxhenkel.pipez.ModelRegistry.Model;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.List;

public abstract class PipeRenderer implements BlockEntityRenderer<PipeTileEntity> {

    protected Minecraft minecraft;
    protected BlockEntityRendererProvider.Context renderer;
    protected CachedValue<BakedModel> cachedModel;

    public PipeRenderer(BlockEntityRendererProvider.Context renderer) {
        this.renderer = renderer;
        minecraft = Minecraft.getInstance();
        cachedModel = getModel().getCachedModel();
    }

    @Override
    public void render(PipeTileEntity pipe, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        BakedModel iBakedModel = cachedModel.get();
        List<BakedQuad> quads = iBakedModel.getQuads(null, null, minecraft.level.random, EmptyModelData.INSTANCE);
        VertexConsumer b = buffer.getBuffer(RenderType.solid());

        for (Direction side : Direction.values()) {
            if (pipe.isExtracting(side)) {
                renderExtractor(side, matrixStack, b, quads, combinedLight, combinedOverlay);
            }
        }
    }

    private void renderExtractor(Direction direction, PoseStack matrixStack, VertexConsumer b, List<BakedQuad> quads, int combinedLight, int combinedOverlay) {
        matrixStack.pushPose();
        matrixStack.translate(direction.getStepX() * 0.001D, direction.getStepY() * 0.001D, direction.getStepZ() * 0.001D);
        matrixStack.translate(0.5D, 0.5D, 0.5D);
        matrixStack.mulPose(getRotation(direction));
        matrixStack.translate(-0.5D, -0.5D, -0.5D);
        for (BakedQuad quad : quads) {
            b.putBulkData(matrixStack.last(), quad, 1F, 1F, 1F, combinedLight, combinedOverlay);
        }

        matrixStack.popPose();
    }

    private Quaternion getRotation(Direction direction) {
        Quaternion q = Quaternion.ONE.copy();
        switch (direction) {
            case NORTH:
                return q;
            case SOUTH:
                q.mul(Vector3f.YP.rotationDegrees(180F));
                return q;
            case WEST:
                q.mul(Vector3f.YP.rotationDegrees(90F));
                return q;
            case EAST:
                q.mul(Vector3f.YP.rotationDegrees(270F));
                return q;
            case UP:
                q.mul(Vector3f.XP.rotationDegrees(90F));
                return q;
            case DOWN:
            default:
                q.mul(Vector3f.XP.rotationDegrees(270F));
                return q;
        }
    }

    abstract Model getModel();

}
