package smsk.deltatime.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickManager;
import net.minecraft.util.Util;
import smsk.deltatime.DT;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    long tickstart;
    long timesincecount;
    @Shadow public ServerTickManager getTickManager(){return(null);}
    
    @Inject(method = "tickTickLog", at = @At("HEAD"))
    protected void tickTickLog(long nanos, CallbackInfo ci){
        //DT.lastTickDuration=nanos;
    }
    
    @Inject(method = "runServer",at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;startTickMetrics()V"))
    protected void atTickMetricsStart(CallbackInfo ci){
        tickstart=Util.getMeasuringTimeNano();
    }
    @Inject(method = "runServer",at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;endTickMetrics()V"))
    protected void atTickMetricsEnd(CallbackInfo ci){
        DT.lastTickDuration=Util.getMeasuringTimeNano()-tickstart;
        
        var a=getTickManager().getNanosPerTick();
        timesincecount+=DT.lastTickDuration;
        DT.tickstoadd=0;
        while(timesincecount>a){
            timesincecount-=a;
            DT.tickstoadd++;
        }
    }
}
