package minescript.network;

import minescript.network.packet.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class MineScriptPackets {

    public static final Identifier START_INTERPRETER_ID = new Identifier("minescript", "start_interpreter");
    public static final Identifier STOP_INTERPRETER_ID = new Identifier("minescript", "stop_interpreter");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(START_INTERPRETER_ID, StartInterpreterC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(STOP_INTERPRETER_ID, StopInterpreterC2SPacket::receive);
    }
}
