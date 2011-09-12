#!/usr/bin/env groovy

import com.ovea.network.tunnel.Tunnel
import java.util.concurrent.LinkedBlockingQueue
import org.testatoo.selenium.server.SeleniumServer
import org.testatoo.selenium.server.SeleniumServerFactory
import com.ovea.network.tunnel.TunnelListener
import com.ovea.network.tunnel.BrokenTunnelException
import java.util.concurrent.CopyOnWriteArrayList
import com.ovea.network.util.NetUtils

@GrabResolver(name = 'testatoo', root = 'http://oss.sonatype.org/content/repositories/snapshots/')
@Grab('org.testatoo:testatoo-selenium:1.0-rc2')
@Grab('com.ovea:ovea-pipe:1.0.ga')

def LISTEN_PORT = args.length > 0 ? args[0] as int : 4444
def REMOTE_CONTROL_COUNT = args.length > 1 ? args[1] as int : 5
def ports = new LinkedBlockingQueue()
def servers = new TreeMap<Integer, SeleniumServer>()
def tunnels = new CopyOnWriteArrayList<Tunnel>()
def shutdown = false

def showServers = {
    println "[${Thread.currentThread().name}] Available Selenium Servers: ${new ArrayList(ports)}"
}

def addRC = {
    int port = NetUtils.findAvailablePort()
    servers[port] = SeleniumServerFactory.configure().setPort(port).setSingleWindow(true).create()
    servers[port].start()
    ports.offer(port)
}

Runtime.runtime.addShutdownHook(new Thread('CLEANER') {
    @Override
    void run() {
        shutdown = true
        tunnels.each {Tunnel t -> t.interrupt()}
    }
})

println "Starting ${REMOTE_CONTROL_COUNT} Selenium Servers..."

REMOTE_CONTROL_COUNT.times {addRC()}

showServers()

println "Listening for incoming requests on port ${LISTEN_PORT}..."

def serverSocket = new ServerSocket(LISTEN_PORT)
for (;;) {
    try {
        int port = ports.take() as int
        println "[${Thread.currentThread().name}] Took port ${port} and wait for client..."
        showServers()
        Socket client = serverSocket.accept()
        Socket rc = new Socket('localhost', port)
        tunnels << Tunnel.connect(client, rc, new TunnelListener() {
            void onConnect(Tunnel tunnel) {
                println "[${Thread.currentThread().name}] Connected client ${client.remoteSocketAddress} to Selenium Server ${port}..."
            }

            void onClose(Tunnel tunnel) {
                tunnels.remove(tunnel)
                println "[${Thread.currentThread().name}] Disconnecting client ${client.remoteSocketAddress} from Selenium Server ${port}..."
                try {
                    client.close()
                } catch (e) {}
                try {
                    rc.close()
                } catch (e) {}
                try {
                    servers[port].stop()
                } catch (e) {}
                if (!shutdown) {
                    addRC()
                    showServers()
                }
            }

            void onBroken(Tunnel tunnel, BrokenTunnelException e) {
                onClose(tunnel)
            }

            void onInterrupt(Tunnel tunnel) {
                onClose(tunnel)
            }
        })
    } catch (IOException) {
        serverSocket.close()
        break
    }
}

