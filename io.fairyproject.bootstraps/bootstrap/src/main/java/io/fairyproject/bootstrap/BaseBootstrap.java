package io.fairyproject.bootstrap;

import io.fairyproject.FairyPlatform;
import io.fairyproject.bootstrap.type.PlatformType;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginManager;
import org.jetbrains.annotations.Nullable;

public abstract class BaseBootstrap {

    private FairyPlatform fairyPlatform;

    public final boolean preload() {
        if (FairyPlatform.class.getClassLoader() != this.getClass().getClassLoader()) {
            return true;
        }
        if (FairyPlatform.INSTANCE != null) {
            return true;
        }

        try {
            this.fairyPlatform = this.createPlatform();
            this.fairyPlatform.preload();
        } catch (Throwable throwable) {
            this.onFailure(throwable);
            return false;
        }

        return true;
    }

    public final void load(Plugin plugin) {
        if (this.fairyPlatform == null) {
            return;
        }
        try {
            this.fairyPlatform.load(plugin);
        } catch (Throwable throwable) {
            this.onFailure(throwable);
        }
    }

    public final void enable() {
        if (this.fairyPlatform == null) {
            return;
        }
        this.fairyPlatform.enable();
    }

    public final void disable() {
        if (this.fairyPlatform == null) {
            return;
        }
        this.fairyPlatform.disable();
        PluginManager.INSTANCE.unload();
    }

    protected abstract void onFailure(@Nullable Throwable throwable);

    protected abstract PlatformType getPlatformType();

    protected abstract FairyPlatform createPlatform();

}
