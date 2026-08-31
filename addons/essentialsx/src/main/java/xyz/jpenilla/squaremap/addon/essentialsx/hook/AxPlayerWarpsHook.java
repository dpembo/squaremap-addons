package xyz.jpenilla.squaremap.addon.essentialsx.hook;

import com.artillexstudios.axplayerwarps.warps.Warp;
import com.artillexstudios.axplayerwarps.warps.WarpManager;
import java.util.List;

public final class AxPlayerWarpsHook {
    public static List<Warp> getWarps() {
        return WarpManager.getWarps();
    }
}
