/*
 * Copyright 2023 Maestro Cloud Control LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.maestro3.agent.chef.service;

import io.maestro3.chef.service.IChefInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Serhii Akhmetshin
 * Created: 19/12/2023
 */
@Service
public class ChefInfoService implements IChefInfoService {
    private final String bucketName;
    private final String chefVersion;
    private final String chefEnv;
    private final String confUrl;
    private final String confPrefix;
    private final String certUrl;
    private final String stateUrl;
    private final String nodeName;
    private final String initStorage;
    private final String apiHost;
    private final boolean chefEnabled;
    private final boolean userscriptsEnabled;

    @Autowired
    public ChefInfoService(@Value("${m3.autoconfiguration.bucket.name}") String bucketName,
                           @Value("${m3.chef.version}") String chefVersion,
                           @Value("${m3.chef.conf.url}") String confUrl,
                           @Value("${m3.chef.prefix}") String confPrefix,
                           @Value("${m3.chef.env}") String chefEnv,
                           @Value("${m3.chef.cert.url}") String certUrl,
                           @Value("${m3.chef.state.url}") String stateUrl,
                           @Value("${m3.chef.node.name}") String nodeName,
                           @Value("${m3.chef.ini.storage.url}") String initStorage,
                           @Value("${m3.chef.api.host}") String apiHost,
                           @Value("${m3.chef.enabled}") boolean chefEnabled,
                           @Value("${m3.userscripts.enabled}") boolean userscriptsEnabled) {
        this.bucketName = bucketName;
        this.chefVersion = chefVersion;
        this.chefEnv = chefEnv;
        this.confPrefix = confPrefix;
        this.confUrl = confUrl;
        this.stateUrl = stateUrl;
        this.certUrl = certUrl;
        this.nodeName = nodeName;
        this.initStorage = initStorage;
        this.apiHost = apiHost;
        this.chefEnabled = chefEnabled;
        this.userscriptsEnabled = userscriptsEnabled;
    }

    @Override
    public String getAutoconfigurationBucketName() {
        return bucketName;
    }

    @Override
    public String getCurrentChefVersion() {
        return chefVersion;
    }

    @Override
    public String getChefEnv() {
        return chefEnv;
    }

    @Override
    public String getPrefix() {
        return confPrefix;
    }

    @Override
    public String getConfigurationUrl() {
        return confUrl;
    }

    @Override
    public String getChefNodeName() {
        return nodeName;
    }

    @Override
    public String getChefStateUrl() {
        return stateUrl;
    }

    @Override
    public String getCertUrl() {
        return certUrl;
    }

    @Override
    public String getInitScriptStorageUrl() {
        return initStorage;
    }

    @Override
    public String getApiHost() {
        return apiHost;
    }

    @Override
    public boolean isChefEnabled() {
        return chefEnabled;
    }

    @Override
    public boolean isUserScriptsEnabled(String s) {
        return userscriptsEnabled;
    }
}
