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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public final class ZipUtils {

    private ZipUtils() {
        throw new UnsupportedOperationException("Instantiation is forbidden.");
    }

    public static byte[] compress(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        return compressBytes(data.getBytes());
    }

    public static byte[] compress(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        return compressBytes(data);
    }

    private static byte[] compressBytes(byte[] data) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
             GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(data);
            gzip.finish();
            bos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String decompress(byte[] compressed) {
        if (compressed == null || compressed.length == 0) {
            return null;
        }
        try (ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
             GZIPInputStream gis = new GZIPInputStream(bis);
             BufferedReader br = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
