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

package org.testatoo.selenium.server;

import org.testatoo.selenium.server.archive.Archive;
import org.testatoo.selenium.server.archive.ArchiveFactory;
import org.testatoo.selenium.server.proxy.MethodHandler;
import org.testatoo.selenium.server.proxy.Proxyfier;
import org.testatoo.selenium.server.util.FileUtils;
import org.testatoo.selenium.server.util.IsolatedClassLoader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static org.testatoo.selenium.server.util.FileUtils.createTemporaryFolder;
import static org.testatoo.selenium.server.util.ResourceUtils.*;

public final class SeleniumServerFactory {

    private static final String JARS_LOCATION = "org/testatoo/selenium/server/embedded/";
    private static final String[] JARS = {"log4j-1.2.16.jar", "commons-logging-1.1.1.jar", "selenium-server-standalone-2.17.0-patched.jar"};
    private static final URL TESTATOO_JAR_LOCATION = container(JARS_LOCATION + JARS[0]);
    private static boolean IN_JAR;

    static {
        try {
            IN_JAR = new File(TESTATOO_JAR_LOCATION.toURI()).isFile();
        } catch (Exception e) {
            IN_JAR = false;
        }
    }

    private SeleniumServerFactory() {
    }

    public static SeleniumServerBuilder configure() {
        return commandeLine();
    }

    public static SeleniumServerBuilder commandeLine(String... args) {
        final ClassLoader threadClassLoader = IN_JAR ?
            new IsolatedClassLoader(packagedClasspath()) :
            new URLClassLoader(new URL[]{contextResource(JARS_LOCATION)}, Thread.currentThread().getContextClassLoader());
        final Object configuration = createRemoteControlConfiguration(threadClassLoader, args);
        return Proxyfier.proxify(configuration, threadClassLoader, SeleniumServerBuilder.class,
            new MethodHandler() {
                public boolean canHandle(Method method) {
                    return "create".equals(method.getName());
                }

                public Object invoke(Object instance, Method method, Object... args) throws Throwable {
                    // invokes new org.openqa.selenium.server.SeleniumServer((RemoteControlConfiguration) instance)
                    final Class clRemoteControlConfiguration = threadClassLoader.loadClass("org.openqa.selenium.server.RemoteControlConfiguration");
                    final Class clSeleniumServer = threadClassLoader.loadClass("org.openqa.selenium.server.SeleniumServer");
                    final Object internalSeleniumServer = clSeleniumServer.getConstructor(clRemoteControlConfiguration).newInstance(instance);
                    return Proxyfier.proxify(internalSeleniumServer, threadClassLoader, SeleniumServer.class,
                        new MethodHandler() {
                            public boolean canHandle(Method method) {
                                return "start".equals(method.getName());
                            }

                            public Object invoke(Object instance, Method method, Object... args) throws Throwable {
                                // invokes SeleniumServer.boot()
                                System.setProperty("org.mortbay.http.HttpRequest.maxFormContentSize", "0"); // default max is 200k; zero is infinite
                                instance.getClass().getMethod("boot").invoke(instance);
                                return null;
                            }
                        }, new MethodHandler() {
                            public boolean canHandle(Method method) {
                                return "isRunning".equals(method.getName());
                            }

                            public Object invoke(Object instance, Method method, Object... args) throws Throwable {
                                // invokes SeleniumServer.getServer().isStarted()
                                final Object jettyServer = instance.getClass().getMethod("getServer").invoke(instance);
                                return jettyServer.getClass().getMethod("isStarted").invoke(jettyServer);
                            }
                        },
                        new MethodHandler() {
                            public boolean canHandle(Method method) {
                                return method.getName().equals("toString");
                            }

                            public Object invoke(Object instance, Method method, Object... args) throws Throwable {
                                StringBuilder sb = new StringBuilder("Selenium Server:\n");
                                for (Field field : configuration.getClass().getDeclaredFields()) {
                                    if (!Modifier.isStatic(field.getModifiers())) {
                                        field.setAccessible(true);
                                        sb.append(" - ").append(field.getName()).append(": ").append(field.get(configuration)).append("\n");
                                    }
                                }
                                return sb.toString();
                            }
                        }
                    );
                }
            },
            new MethodHandler() {
                public boolean canHandle(Method method) {
                    return method.getName().equals("toString");
                }

                public Object invoke(Object instance, Method method, Object... args) throws Throwable {
                    StringBuilder sb = new StringBuilder("Selenium RemoteControlConfiguration:\n");
                    for (Field field : instance.getClass().getDeclaredFields()) {
                        if (!Modifier.isStatic(field.getModifiers())) {
                            field.setAccessible(true);
                            sb.append(" - ").append(field.getName()).append(": ").append(field.get(instance)).append("\n");
                        }
                    }
                    return sb.toString();
                }
            }
        );
    }

    private static Object createRemoteControlConfiguration(ClassLoader threadClassLoader, String... args) {
        try {
            // invokes RemoteControlLauncher.parseLauncherOptions(args)
            final Class clRemoteControlLauncher = threadClassLoader.loadClass("org.openqa.selenium.server.cli.RemoteControlLauncher");
            final Method parseLauncherOptions = clRemoteControlLauncher.getMethod("parseLauncherOptions", String[].class);
            return parseLauncherOptions.invoke(null, new Object[]{args});
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) throw (RuntimeException) e.getTargetException();
            else throw new RuntimeException(e.getTargetException().getMessage(), e.getTargetException());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static URL[] packagedClasspath() {
        // extract selenium
        URL location = container(SeleniumServerFactory.class);
        Archive jar = ArchiveFactory.jar(location);
        File tmpDir = createTemporaryFolder("testatoo-");
        jar.extract(tmpDir, "org/testatoo/selenium/server/embedded/**");
        // if the project provides a log4j.xml file, use it
        final URL providedLog4j = Thread.currentThread().getContextClassLoader().getResource("log4j.xml");
        if (providedLog4j != null) {
            try {
                FileUtils.copy(providedLog4j.openStream(), new File(tmpDir, "log4j.xml"));
            } catch (IOException e) {
                throw new RuntimeException("Unable to copy provided log4j.xml from " + providedLog4j + " to " + tmpDir.getAbsolutePath() + ": " + e.getMessage(), e);
            }
        }
        // then return the created classpath
        File folder = new File(tmpDir, JARS_LOCATION);
        List<URL> urls = new ArrayList<URL>(JARS.length + 2);
        urls.add(TESTATOO_JAR_LOCATION);
        urls.add(url(folder));
        for (String jarName : JARS) urls.add(url(new File(folder, jarName)));
        return urls.toArray(new URL[urls.size()]);
    }

}