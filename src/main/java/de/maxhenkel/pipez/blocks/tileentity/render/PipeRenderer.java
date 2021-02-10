package de.maxhenkel.pipez.blocks.tileentity.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import de.maxhenkel.corelib.CachedValue;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.List;

public abstract class PipeRenderer extends TileEntityRenderer<PipeTileEntity> {

    protected Minecraft minecraft;

    protected CachedValue<IBakedModel> cachedModel;

    public PipeRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        minecraft = Minecraft.getInstance();

        cachedModel = new CachedValue<>(() -> {
            IUnbakedModel modelOrMissing = ModelLoader.instance().getModelOrMissing(getModel());
            return modelOrMissing.bakeModel(ModelLoader.instance(), ModelLoader.instance().getSpriteMap()::getSprite, ModelRotation.X0_Y0, getModel());
        });
    }

    @Override
    public void render(PipeTileEntity pipe, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        IBakedModel iBakedModel = cachedModel.get();
        List<BakedQuad> quads = iBakedModel.getQuads(null, null, minecraft.world.rand, EmptyModelData.INSTANCE);
        IVertexBuilder b = buffer.getBuffer(RenderType.getSolid());

        for (Direction side : Direction.values()) {
            if (pipe.isExtracting(side)) {
                renderExtractor(side, matrixStack, b, quads, combinedLight, combinedOverlay);
            }
        }
    }

    private void renderExtractor(Direction direction, MatrixStack matrixStack, IVertexBuilder b, List<BakedQuad> quads, int combinedLight, int combinedOverlay) {
        matrixStack.push();
        matrixStack.translate(0.5D, 0.5D, 0.5D);
        matrixStack.rotate(getRotation(direction));
        matrixStack.translate(-0.5D, -0.5D, -0.5D);
        for (BakedQuad quad : quads) {
            b.addQuad(matrixStack.getLast(), quad, 1F, 1F, 1F, combinedLight, combinedOverlay);
        }

        matrixStack.pop();
    }

    private Quaternion getRotation(Direction direction) {
        Quaternion q = Quaternion.ONE.copy();
        switch (direction) {
            case NORTH:
                return q;
            case SOUTH:
                q.multiply(Vector3f.YP.rotationDegrees(180F));
                return q;
            case WEST:
                q.multiply(Vector3f.YP.rotationDegrees(90F));
                return q;
            case EAST:
                q.multiply(Vector3f.YP.rotationDegrees(270F));
                return q;
            case UP:
                q.multiply(Vector3f.XP.rotationDegrees(90F));
                return q;
            case DOWN:
            default:
                q.multiply(Vector3f.XP.rotationDegrees(270F));
                return q;
        }
    }

    abstract ResourceLocation getModel();

}
