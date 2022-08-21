package issame.potionhud.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(GuiIngame.class)
public class GuiIngameMixin extends Gui {
    private static final int FRAME_SIZE = 24;
    private static final int ICON_SIZE = 18;

    private static final int POTION_FRAME_X = 0;
    private static final int BEACON_FRAME_X = POTION_FRAME_X + FRAME_SIZE;
    private static final int FRAME_Y = 0;

    private static final int ICON_COLUMNS = 8;
    private static final int ICON_Y = 198;

    private static final int FLASH_TICKS = 200;

    private static final Comparator<PotionEffect> COMPARATOR = (e1, e2) -> {
        if (e1.getIsAmbient()) {
            return -1;
        } else if (e2.getIsAmbient()) {
            return 1;
        }
        return Integer.compare(e1.getDuration(), e2.getDuration());
    };

    @Shadow @Final private Minecraft mc;

    @SuppressWarnings("unchecked")
    @Inject(method = "renderGameOverlay(FZII)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isDemo()Z"))
    private void injection(float par1, boolean par2, int par3, int par4, CallbackInfo ci) {
        Collection<PotionEffect> potionEffects = mc.thePlayer.getActivePotionEffects();
        if (potionEffects.isEmpty()) { return; }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int scaledWidth = (new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight)).getScaledWidth();

        int goodCount = 0;
        int badCount = 0;

        List<PotionEffect> potionList = potionEffects.stream()
                .sorted(COMPARATOR)
                .collect(Collectors.toList());

        for (PotionEffect effect : potionList) {
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            if (!potion.hasStatusIcon()) { return; }

            // Determine coordinates to place icons.
            int iconX = scaledWidth;
            int iconY = 1;
            if (potion.isBadEffect()) {
                badCount++;
                iconX -= (FRAME_SIZE + 1) * badCount;
                iconY += FRAME_SIZE + 2;
            } else {
                goodCount++;
                iconX -= (FRAME_SIZE + 1) * goodCount;
            }

            // Draw background frame.
            mc.renderEngine.bindTexture("/potionhud/gui/potion_frame.png");
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            int frameX = effect.getIsAmbient() ? BEACON_FRAME_X : POTION_FRAME_X;
            drawTexturedModalRect(iconX, iconY, frameX, FRAME_Y, FRAME_SIZE, FRAME_SIZE);

            // Draw potion icon.
            mc.renderEngine.bindTexture("/gui/inventory.png");
            GL11.glColor4f(1.0f, 1.0f, 1.0f, getAlpha(effect));
            int offset = (FRAME_SIZE - ICON_SIZE) / 2;
            int iconIndex = potion.getStatusIconIndex();
            drawTexturedModalRect(iconX + offset, iconY + offset,
                    (iconIndex % ICON_COLUMNS) * ICON_SIZE,
                    (iconIndex / ICON_COLUMNS) * ICON_SIZE + ICON_Y,
                    ICON_SIZE, ICON_SIZE
            );
        }
    }

    private float getAlpha(PotionEffect effect) {
        if (effect.getIsAmbient() || effect.getDuration() > FLASH_TICKS) { return 1.0f; }

        int temp = 10 - effect.getDuration() / 20;
        return MathHelper.clamp_float(effect.getDuration() / 100.0f, 0.0f, 0.5f)
                + MathHelper.cos((float) (effect.getDuration() * Math.PI / 5.0f))
                * MathHelper.clamp_float(temp / 40.0f, 0.0f, 0.25f);
    }
}
