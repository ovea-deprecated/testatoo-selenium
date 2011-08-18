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

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer;
import org.openqa.selenium.server.cli.RemoteControlLauncher;

final class OpenQARunner {
    private static SeleniumServer seleniumServer = null;

    public static void main(final String... args) throws Exception {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    RemoteControlConfiguration configuration = RemoteControlLauncher.parseLauncherOptions(new String[]{"-port", "5555", "-singleWindow"});
                    seleniumServer = new SeleniumServer(false, configuration);
                    seleniumServer.boot();
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });
        t.start();
        Thread.sleep(5000);
        Selenium s = new DefaultSelenium("localhost", 5555, "*firefox", "http://www.amazon.ca/");
        s.start();
        s.open("/");
        Thread.sleep(5000);
        s.close();
        seleniumServer.stop();
    }
}
