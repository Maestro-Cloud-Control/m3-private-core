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

import io.maestro3.agent.chef.dao.IKeyValueDao;
import io.maestro3.chef.service.ISecretsService;
import io.maestro3.sdk.internal.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Serhii Akhmetshin
 * Created: 19/12/2023
 */
@Service
public class SecretService implements ISecretsService {
    private final IKeyValueDao secretDao;

    @Autowired
    public SecretService(IKeyValueDao secretDao) {
        this.secretDao = secretDao;
    }

    @Override
    public String getSecretValue(String key) {
        return secretDao.getValue(key);
    }

    @Override
    public void saveSecret(String key, String value) {
        secretDao.save(key, value);
    }

    @Override
    public void deleteSecret(String key) {
        secretDao.remove(key);
    }

    @Override
    public boolean exists(String s) {
        return StringUtils.isNotBlank(getSecretValue(s));
    }
}
