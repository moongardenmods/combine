package dev.moongarden.mixin;

import dev.moongarden.Combine;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

	@Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;getBackendDescription()Ljava/lang/String;"), method = "<init>")
	private void init(CallbackInfo info) {
		if (Combine.ENABLED) {
			Combine.generate();
			System.exit(0);
		}
	}
}