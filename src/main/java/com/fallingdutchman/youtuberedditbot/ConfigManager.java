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

    ConfigManager() {
        val defaultAppConfig = ConfigFactory.defaultApplication().resolve();
        val file = new File(AppConfig.of(defaultAppConfig).getUserConfig().getAppConfigLocation());
        appConfig = AppConfig.of(ConfigFactory.parseFile(file)
                .withFallback(defaultAppConfig)
                .resolve());
    }

    private List<Instance> loadInstances() {
        final List<Instance> result = Lists.newArrayList();
        try {
            val userConfigLocation = getAppConfig().getUserConfig().getUserConfigLocation();
            val userConfig = ConfigFactory.parseFile(new File(userConfigLocation));
            result.addAll(getInstancesFromConfig(prepareConfig(userConfig)));
        } catch (Exception e) {
            log.error("a fatal error occurred whilst trying to load the configurations", e);
        }

        return result;
    }

    @VisibleForTesting
    static List<Instance> getInstancesFromConfig(Config config) {
        return Collections.unmodifiableList(config.getList("instances")
                .stream()
                .map(cv -> Instance.of(((ConfigObject) cv.withFallback(config.getConfig("defaultEntry"))).toConfig()))
                .collect(Collectors.toList()));
    }

    @VisibleForTesting
    static Config prepareConfig(@NonNull Config userConfig) {
        return ConfigFactory.defaultOverrides()
                .withFallback(userConfig)
                .withFallback(ConfigFactory.parseResources("UserDefaults.conf"))
                .resolve();
    }
}
