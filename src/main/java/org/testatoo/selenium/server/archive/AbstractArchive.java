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

import org.testatoo.selenium.server.util.AntPathMatcher;
import org.testatoo.selenium.server.util.PathMatcher;

import java.net.URL;

abstract class AbstractArchive implements Archive {

    private final PathMatcher pathMatcher = new AntPathMatcher();
    private final URL location;

    public AbstractArchive(URL location) {
        this.location = location;
    }

    public final URL location() {
        return location;
    }

    protected boolean matchesOne(String path, String... patterns) {
        for (String pattern : patterns) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return location().toExternalForm();
    }
}
