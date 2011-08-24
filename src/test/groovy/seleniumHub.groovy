#!/usr/bin/env groovy

import java.util.concurrent.LinkedBlockingQueue
import org.testatoo.selenium.server.SeleniumServerFactory

@GrabResolver(name = 'testatoo', root = 'http://oss.sonatype.org/content/repositories/snapshots/')
@Grab(group = 'org.testatoo', module = 'testatoo-selenium', version = '1.0-rc2-SNAPSHOT')

////////////// CONF //////////////
LISTEN_PORT = 4444
REMOTE_CONTROL_COUNT = 5
//////////////////////////////////

def findFreePort = {
    def r = new Random()
    for (;;) {
        def p = 1025 + r.nextInt(64000)
        try {
            new ServerSocket(p).close()
            return p;
        } catch (IOException ignored) {
        }
    }
}

println "Starting Selenium Servers..."

def ports = new LinkedBlockingQueue()
def servers = new TreeMap()
REMOTE_CONTROL_COUNT.times {
    def port = findFreePort()
    servers[port] = SeleniumServerFactory.configure().setPort(port).setSingleWindow(true).create()
    servers[port].start()
    ports.offer(ports)
}

println "Started ${REMOTE_CONTROL_COUNT} Selenium Servers on ports:"
servers.each {k, v ->
    println " - ${k}"
}

class Tunnel {

}

ServerSocket serverSocket = new ServerSocket(LISTEN_PORT)
for (;;) {
    def socket
    try {
        socket = serverSocket.accept()
    } catch (IOException) {
        serverSocket.close()
        break
    }
    new Tunnel(socket, ports.take())
}

//Socket socket = new Socket('localhost', 9, true)
