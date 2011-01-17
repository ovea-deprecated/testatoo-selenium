/**
 * Copyright (C) 2008 Ovea <dev@testatoo.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.testatoo.selenium.server.archive;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static org.testatoo.selenium.server.util.FileUtils.copy;

final class JarArchive extends AbstractArchive {

    public JarArchive(URL location) {
        super(location);
    }

    public Archive extract(File dest, String... patterns) {
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(new File(location().toURI()));
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (matchesOne(entry.getName(), patterns)) {
                    if (entry.isDirectory()) {
                        //noinspection ResultOfMethodCallIgnored
                        new File(dest, entry.getName()).mkdirs();
                    } else {
                        File file = new File(dest, entry.getName());
                        File parent = file.getParentFile();
                        if (parent != null) {
                            //noinspection ResultOfMethodCallIgnored
                            parent.mkdirs();
                        }
                        InputStream in = new BufferedInputStream(jarFile.getInputStream(entry));
                        copy(in, file);
                        in.close();
                    }
                }
            }
            jarFile.close();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException ignored) {
                }
            }
        }
        return this;
    }
}
