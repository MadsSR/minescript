package minescript.networking;

import minescript.networking.packet.StepC2SPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class MineScriptPackets {

    public static final Identifier STEP_ID = new Identifier("minescript", "step");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(STEP_ID, StepC2SPacket::receive);
    }
}
