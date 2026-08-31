package xyz.jpenilla.squaremap.addon.essentialsx.task;

import com.artillexstudios.axplayerwarps.enums.Access;
import com.artillexstudios.axplayerwarps.warps.Warp;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.jpenilla.squaremap.addon.essentialsx.config.EssXWorldConfig;
import xyz.jpenilla.squaremap.addon.essentialsx.hook.AxPlayerWarpsHook;
import xyz.jpenilla.squaremap.addon.essentialsx.hook.SquaremapHook;
import xyz.jpenilla.squaremap.api.BukkitAdapter;
import xyz.jpenilla.squaremap.api.Key;
import xyz.jpenilla.squaremap.api.MapWorld;
import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.SimpleLayerProvider;
import xyz.jpenilla.squaremap.api.marker.Icon;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;

public final class SquaremapTask extends BukkitRunnable {
    private final MapWorld world;
    private final SimpleLayerProvider provider;
    private final EssXWorldConfig worldConfig;

    private boolean stop;

    public SquaremapTask(MapWorld world, EssXWorldConfig worldConfig, SimpleLayerProvider provider) {
        this.world = world;
        this.provider = provider;
        this.worldConfig = worldConfig;
    }

    @Override
    public void run() {
        if (this.stop) {
            this.cancel();
        }

        this.provider.clearMarkers();

        final World bukkitWorld = BukkitAdapter.bukkitWorld(this.world);

        // Matched by world name rather than the warp's Location#getWorld(), since
        // AxPlayerWarps may hold a warp whose world wasn't loaded yet when it read
        // it from the database, leaving that Location's world null.
        for (final Warp warp : AxPlayerWarpsHook.getWarps()) {
            if (!warp.getWorldName().equals(bukkitWorld.getName())) {
                continue;
            }
            if (warp.getAccess() == Access.PRIVATE && !this.worldConfig.warpsIncludePrivate) {
                continue;
            }
            if (warp.getAccess() == Access.WHITELISTED && !this.worldConfig.warpsIncludeWhitelisted) {
                continue;
            }
            this.handle(warp);
        }
    }

    private void handle(Warp warp) {
        Icon icon = Marker.icon(BukkitAdapter.point(warp.getLocation()), SquaremapHook.WARP_ICON_KEY, this.worldConfig.iconSize);

        icon.anchor(Point.of(this.worldConfig.iconAnchorX, this.worldConfig.iconAnchorZ));

        icon.markerOptions(
            MarkerOptions.builder()
                .hoverTooltip(this.worldConfig.warpsTooltip
                    .replace("{warp}", warp.getName())
                    .replace("{owner}", warp.getOwnerName())
                    .replace("{description}", warp.getDescription()))
        );

        String markerid = "axplayerwarps_warp_" + warp.getId();
        this.provider.addMarker(Key.of(markerid), icon);
    }

    public void disable() {
        this.cancel();
        this.stop = true;
        this.provider.clearMarkers();
    }
}
