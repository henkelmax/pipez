package de.maxhenkel.pipez.blocks.tileentity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.maxhenkel.pipez.ModelRegistry.Model;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class PipeRenderer implements BlockEntityRenderer<PipeTileEntity> {

    protected Minecraft minecraft;
    protected BlockEntityRendererProvider.Context renderer;
    protected AtomicReference<QuadCollection> cachedModel;

    public PipeRenderer(BlockEntityRendererProvider.Context renderer) {
        this.renderer = renderer;
        minecraft = Minecraft.getInstance();
        cachedModel = getModel().getModel();
    }

    @Override
    public void render(PipeTileEntity pipe, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, Vec3 vec) {
        QuadCollection model = cachedModel.get();
        if (model == null) {
            return;
        }
        List<BakedQuad> quads = model.getQuads(null);
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
            b.putBulkData(matrixStack.last(), quad, 1F, 1F, 1F, 1F, combinedLight, combinedOverlay);
        }

        matrixStack.popPose();
    }

    private Quaternionf getRotation(Direction direction) {
        Quaternionf q = new Quaternionf();
        switch (direction) {
            case NORTH:
                return q;
            case SOUTH:
                q.mul(Axis.YP.rotationDegrees(180F));
                return q;
            case WEST:
                q.mul(Axis.YP.rotationDegrees(90F));
                return q;
            case EAST:
                q.mul(Axis.YP.rotationDegrees(270F));
                return q;
            case UP:
                q.mul(Axis.XP.rotationDegrees(90F));
                return q;
            case DOWN:
            default:
                q.mul(Axis.XP.rotationDegrees(270F));
                return q;
        }
    }

    abstract Model getModel();

}
