package com.fallingdutchman.youtuberedditbot;

import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.google.api.client.util.Lists;
import com.google.common.annotations.VisibleForTesting;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by douwe on 10-1-17.
 */
@Slf4j
class ConfigManager {
    @NonNull @Getter @Setter(AccessLevel.PRIVATE)
    private AppConfig appConfig;
    @Getter(lazy = true, onMethod = @__({@SuppressWarnings("unchecked")}))
    private final List<Instance> instances = loadInstances();


    public ConfigManager() {
        final Config defaultAppConfig = ConfigFactory.defaultApplication().resolve();
        final File file = new File(AppConfig.of(defaultAppConfig).getUserConfig().getAppConfigLocation());
        setAppConfig(AppConfig.of(ConfigFactory.parseFile(file)
                .withFallback(defaultAppConfig)
                .resolve()));
    }

    List<Instance> loadInstances() {
        final List<Instance> result = Lists.newArrayList();
        try {
            final String userConfigLocation = getAppConfig().getUserConfig().getUserConfigLocation();
            final Config userConfig = ConfigFactory.parseFile(new File(userConfigLocation));
            result.addAll(loadConfig(prepareConfig(userConfig)));
        } catch (Exception e) {
            log.error("a fatal error occurred whilst trying to load the configurations", e);
        }

        return result;
    }

    @VisibleForTesting
    static List<Instance> loadConfig(Config config) {
        return Collections.unmodifiableList(config.getList("instances")
                .stream()
                .map(cv -> Instance.of(((ConfigObject) cv).toConfig()))
                .collect(Collectors.toList()));
    }

    @VisibleForTesting
    static Config prepareConfig(@NonNull Config userConfig) {
        return ConfigFactory.defaultOverrides()
                .withFallback(userConfig)
                .withFallback(ConfigFactory.load("UserDefaults"))
                .resolve();
    }
}
