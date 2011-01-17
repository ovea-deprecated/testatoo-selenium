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

/*
 * THIS INTERFACE IS A PROXY => METHODS MUST HAVE EXACLTY THE SAME SIGNATURE TO OpenQA's class SeleniumServer
 */
public interface SeleniumServer {

    void start(); // maps to OpenQA's SeleniumServer.boot()

    void stop(); // maps to OpenQA's SeleniumServer.stop()

    int getPort(); // maps to OpenQA's SeleniumServer.getPort()

    boolean isRunning(); // maps to OpenQA's SeleniumServer.getServer().isStarted()
}
