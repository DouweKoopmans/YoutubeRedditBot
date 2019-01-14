package com.fallingdutchman.youtuberedditbot;

import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManagerFactory;
import com.fallingdutchman.youtuberedditbot.listeners.ListenerFactory;
import com.fallingdutchman.youtuberedditbot.listeners.filtering.FilterFactory;
import com.fallingdutchman.youtuberedditbot.listeners.filtering.FilterFactoryImpl;
import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.fallingdutchman.youtuberedditbot.processing.ProcessorFactory;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Created by douwe on 11-1-17.
 */
class YrbModule extends AbstractModule {
    private final AppConfig appConfig;

    YrbModule(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Override
    protected void configure() {
        bind(AppConfig.class).toInstance(appConfig);
        bind(FilterFactory.class).to(FilterFactoryImpl.class);
        install(new FactoryModuleBuilder().build(ProcessorFactory.class));
        install(new FactoryModuleBuilder().build(ListenerFactory.class));
        install(new FactoryModuleBuilder().build(RedditManagerFactory.class));
    }
}
