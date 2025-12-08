package de.maxhenkel.pipez.blocks.tileentity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.maxhenkel.pipez.ModelRegistry.Model;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class PipeRenderer implements BlockEntityRenderer<PipeTileEntity, PipeRenderState> {

    protected Minecraft minecraft;
    protected BlockEntityRendererProvider.Context renderer;
    protected AtomicReference<QuadCollection> cachedModel;

    public PipeRenderer(BlockEntityRendererProvider.Context renderer) {
        this.renderer = renderer;
        minecraft = Minecraft.getInstance();
        cachedModel = getModel().getModel();
    }

    @Override
    public PipeRenderState createRenderState() {
        return new PipeRenderState();
    }

    @Override
    public void extractRenderState(PipeTileEntity entity, PipeRenderState state, float partialTicks, Vec3 pos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, pos, crumblingOverlay);

        for (Direction side : Direction.values()) {
            state.extracting[side.ordinal()] = entity.isExtracting(side);
        }
    }

    @Override
    public void submit(PipeRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        QuadCollection model = cachedModel.get();
        if (model == null) {
            return;
        }

        for (Direction side : Direction.values()) {
            if (state.extracting[side.ordinal()]) {
                renderExtractor(model, side, stack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY);
            }
        }
    }

    private void renderExtractor(QuadCollection model, Direction direction, PoseStack stack, SubmitNodeCollector collector, int combinedLight, int combinedOverlay) {
        stack.pushPose();
        stack.translate(direction.getStepX() * 0.001D, direction.getStepY() * 0.001D, direction.getStepZ() * 0.001D);
        stack.translate(0.5D, 0.5D, 0.5D);
        stack.mulPose(getRotation(direction));
        stack.translate(-0.5D, -0.5D, -0.5D);
        List<BakedQuad> quads = model.getQuads(null);
        collector.submitCustomGeometry(stack, RenderTypes.solidMovingBlock(), (pose, vertexConsumer) -> {
            for (BakedQuad quad : quads) {
                vertexConsumer.putBulkData(pose, quad, 1F, 1F, 1F, 1F, combinedLight, combinedOverlay);
            }
        });

        stack.popPose();
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
