package io.fairyproject.bootstrap.app;

import com.google.gson.JsonObject;
import io.fairyproject.Fairy;
import io.fairyproject.app.Application;
import io.fairyproject.app.FairyAppPlatform;
import io.fairyproject.bootstrap.BasePluginHolder;
import io.fairyproject.plugin.PluginAction;
import io.fairyproject.plugin.initializer.PluginClassInitializer;

public class ApplicationHolder extends BasePluginHolder {

    public ApplicationHolder(PluginClassInitializer initializer, JsonObject jsonObject) {
        super(initializer, jsonObject);
    }

    @Override
    protected ClassLoader getClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }

    @Override
    protected PluginAction getPluginAction() {
        return new ApplicationAction(this);
    }

    @Override
    public void onLoad() {
        ((FairyAppPlatform) Fairy.getPlatform()).setMainApplication((Application) this.plugin);
        super.onLoad();
    }
}
