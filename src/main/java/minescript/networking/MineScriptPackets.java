package minescript.networking;

import minescript.networking.packet.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class MineScriptPackets {

    public static final Identifier STEP_ID = new Identifier("minescript", "step");
    public static final Identifier TURN_RELDIR_ID = new Identifier("minescript", "turn_reldir");
    public static final Identifier TURN_ABSDIR_ID = new Identifier("minescript", "turn_absdir");
    public static final Identifier PRINT_ID = new Identifier("minescript", "print");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(STEP_ID, StepC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(TURN_RELDIR_ID, TurnRelDirC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(TURN_ABSDIR_ID, TurnAbsDirC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(PRINT_ID, PrintC2SPacket::receive);
    }
}
