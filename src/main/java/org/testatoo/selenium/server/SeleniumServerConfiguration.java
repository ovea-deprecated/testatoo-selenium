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

import java.io.File;

/*
 * THIS INTERFACE IS A PROXY => METHODS MUST HAVE EXACLTY THE SAME SIGNATURE TO OpenQA's class RemoteControlConfiguration
 */
public interface SeleniumServerConfiguration<T extends SeleniumServerConfiguration> {

    int getPort();

    T setPort(int newPortNumber);

    boolean isSingleWindow();

    T setSingleWindow(boolean useSingleWindow);

    File getProfilesLocation();

    T setProfilesLocation(File profilesLocation);

    T setProxyInjectionModeArg(boolean proxyInjectionModeArg);

    boolean getProxyInjectionModeArg();

    T setPortDriversShouldContact(int newPortDriversShouldContact);

    int getPortDriversShouldContact();

    T setHTMLSuite(boolean isHTMLSuite);

    boolean isHTMLSuite();

    boolean isSelfTest();

    T setSelfTest(boolean isSelftest);

    T setSelfTestDir(File newSelfTestDir);

    File getSelfTestDir();

    boolean isInteractive();

    T setInteractive(boolean isInteractive);

    File getUserExtensions();

    T setUserExtensions(File newuserExtensions);

    boolean userJSInjection();

    T setUserJSInjection(boolean useUserJSInjection);

    T setTrustAllSSLCertificates(boolean trustAllSSLCertificates);

    boolean trustAllSSLCertificates();

    String getDebugURL();

    T setDebugURL(String newDebugURL);

    boolean isDebugMode();

    T setDebugMode(boolean debugMode);

    T setDontInjectRegex(String newdontInjectRegex);

    String getDontInjectRegex();

    File getFirefoxProfileTemplate();

    T setFirefoxProfileTemplate(File newFirefoxProfileTemplate);

    T setReuseBrowserSessions(boolean reuseBrowserSessions);

    boolean reuseBrowserSessions();

    T setLogOutFileName(String newLogOutFileName);

    String getLogOutFileName();

    T setLogOutFile(File newLogOutFile);

    File getLogOutFile();

    T setForcedBrowserMode(String newForcedBrowserMode);

    String getForcedBrowserMode();

    boolean honorSystemProxy();

    T setHonorSystemProxy(boolean willHonorSystemProxy);

    boolean shouldOverrideSystemProxy();

    int getTimeoutInSeconds();

    T setTimeoutInSeconds(int newTimeoutInSeconds);

    int getRetryTimeoutInSeconds();

    T setRetryTimeoutInSeconds(int newRetryTimeoutInSeconds);

    boolean dontTouchLogging();

    T setDontTouchLogging(boolean newValue);

    int shortTermMemoryLoggerCapacity();

    boolean isEnsureCleanSession();

    T setEnsureCleanSession(boolean value);

    boolean isASeleniumServerConfigurationProxy();

    T setASeleniumServerConfigurationProxy(boolean value);

    boolean isBrowserSideLogEnabled();

    T setBrowserSideLogEnabled(boolean value);

    int getJettyThreads();

    T setJettyThreads(int jettyThreads);

}
