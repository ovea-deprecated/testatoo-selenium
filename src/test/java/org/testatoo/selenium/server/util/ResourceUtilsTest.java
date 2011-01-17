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

package org.testatoo.selenium.server.util;

import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.testatoo.selenium.server.util.ResourceUtils.*;

public final class ResourceUtilsTest {
    @Test
    public void test_toURL() {
        assertNotNull(url("src/test/resources/pom2.xml"));
        assertNotNull(url("file:///./pom.xml"));
    }

    @Test
    public void test_getContextResource() {
        assertNotNull(contextResource("pom2.xml"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_getContextResource_throw() {
        contextResource("inexisting.xml");
    }

    @Test
    public void test_hasContextResource() {
        assertTrue(hasContextResource("pom2.xml"));
        assertFalse(hasContextResource("inexisting.xml"));
    }

    @Test
    public void test_classFileName() {
        assertEquals(classFileName(getClass()), "ResourceUtilsTest.class");
    }

    @Test
    public void test_classPackageAsResourcePath() {
        assertEquals(classPackageAsResourcePath(getClass()), "org/testatoo/selenium/server/util");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_container_resource_not_found() {
        container("org/testatoo/common/ResourceUtilsTest.inexisting");
    }

    @Test
    public void test_container_resource() {
        URL u = container("org/testatoo/selenium/server/util/ResourceUtilsTest.class");
        assertNotNull(u);
        assertEquals("file", u.getProtocol());
    }

    @Test
    public void test_container_clazz() {
        URL u = container(getClass());
        assertNotNull(u);
        assertEquals("file", u.getProtocol());
    }
}
