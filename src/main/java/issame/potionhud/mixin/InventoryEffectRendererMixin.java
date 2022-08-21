package issame.potionhud.mixin;

import net.minecraft.src.Container;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.InventoryEffectRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InventoryEffectRenderer.class)
public abstract class InventoryEffectRendererMixin extends GuiContainer {
	private final int EFFECT_WIDTH = 124;

	public InventoryEffectRendererMixin(Container par1Container) {
		super(par1Container);
	}

	@Redirect(method = "initGui()V",
			at = @At(value = "FIELD",
					target = "Lnet/minecraft/src/InventoryEffectRenderer;guiLeft:I",
					opcode = Opcodes.PUTFIELD
			)
	)
	private void injection(InventoryEffectRenderer inventoryEffectRenderer, int value) {
		guiLeft = Math.max(guiLeft, EFFECT_WIDTH);
	}
}
