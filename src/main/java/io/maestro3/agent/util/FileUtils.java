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

package io.maestro3.agent.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


public final class FileUtils {

    private FileUtils() {
        throw new UnsupportedOperationException("Instantiation is forbidden.");
    }

    public static String readFile(String fileName) throws IOException {
        ClassPathResource cpr = new ClassPathResource(fileName);
        byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
        return new String(bdata, StandardCharsets.UTF_8);
    }

    public static String read(String fileName) {
        try {
            ClassPathResource cpr = new ClassPathResource(fileName);
            byte[] data = FileCopyUtils.copyToByteArray(cpr.getInputStream());
            return new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
