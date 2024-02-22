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

import io.maestro3.chef.service.IFileService;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystems;

/**
 * @author Serhii Akhmetshin
 * Created: 19/12/2023
 */
@Service
public class FileService implements IFileService {
    @Override
    public String getFileById(String s) {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public byte[] getFileByPath(String bucket, String path, String file) {
        //bucket not needed for local files
        String fileName = path + FileSystems.getDefault().getSeparator() + file;
        URL resourceURL = ResourceLoader.class.getClassLoader().getResource(fileName);
        if (resourceURL != null) {
            try {
                try (InputStream inputStream = resourceURL.openStream()) {
                    return inputStream.readAllBytes();
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file", e);
            }
        }
        return null;
    }

    @Override
    public InputStream getStream(Object o) {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public Object getFile(String s) {
        throw new UnsupportedOperationException("Unsupported");
    }
}
