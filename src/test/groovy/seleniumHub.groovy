#!/usr/bin/env groovy

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import org.testatoo.selenium.server.SeleniumServer
import org.testatoo.selenium.server.SeleniumServerFactory

@GrabResolver(name = 'testatoo', root = 'http://oss.sonatype.org/content/repositories/snapshots/')
@Grab('org.testatoo:testatoo-selenium:1.0-rc2')

def LISTEN_PORT = args.length > 0 ? args[0] as int : 4444
def REMOTE_CONTROL_COUNT = args.length > 1 ? args[1] as int : 5
def ports = new LinkedBlockingQueue()
def servers = new TreeMap<Integer, SeleniumServer>()
def cleaners = new ConcurrentHashMap<Thread, Object>()

def showServers = {list ->
    println "[${Thread.currentThread().name}] Available Selenium Servers: ${(list ? list : ports)}"
}

def findFreePort = {
    r = new Random()
    for (;;) {
        p = 1025 + r.nextInt(64000)
        try {
            new ServerSocket(p).close()
            return p
        } catch (IOException ignored) {
        }
    }
}

def clean = {int p, Socket client, Socket rc ->
    synchronized (ports) {
        if (!ports.contains(p)) {
            println "[${Thread.currentThread().name}] Disconnecting client ${client.remoteSocketAddress} from Selenium Server ${p}..."
            try {
                client?.close()
            } catch (e) {}
            try {
                rc?.close()
            } catch (e) {}
            try {
                servers[p].stop()
            } catch (e) {}
            servers[p] = SeleniumServerFactory.configure().setPort(p).setSingleWindow(true).create()
            servers[p].start()
            if (!Thread.currentThread().isInterrupted()) {
                showServers(new LinkedList(ports) + p)
            }
            ports.offer(p)
        }
    }
}

def tunnel = {Socket client, int p ->
    println "[${Thread.currentThread().name}] Connecting client ${client.remoteSocketAddress} to Selenium Server ${p}..."
    Socket rc = null
    try {
        rc = new Socket('localhost', p as int)
    } catch (e) {
        clean(p, client, rc)
    }
    Thread reader = null, writer = null, cleaner = null, hook = null;
    reader = Thread.start "${client.remoteSocketAddress} READER", {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                rc.outputStream.write(client.inputStream.read())
            } catch (e) {
                Thread.currentThread().interrupt()
                writer?.interrupt()
            }
        }
    }
    writer = Thread.start "${client.remoteSocketAddress}  WRITER", {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                client.outputStream.write(rc.inputStream.read())
            } catch (e) {
                Thread.currentThread().interrupt()
                reader?.interrupt()
            }
        }
    }
    cleaner = Thread.start client.remoteSocketAddress.toString(), {
        try {
            reader.join()
        } catch (e) {
            writer.interrupt()
        }
        try {
            writer.join()
        } catch (e) {
            reader.interrupt()
        };
        clean(p, client, rc)
    }
    cleaners.put(cleaner, Void.TYPE)
}

Runtime.runtime.addShutdownHook(new Thread('CLEANER') {
    @Override
    void run() {
        cleaners.each {k, v -> k.interrupt()}
    }
})

println "Starting ${REMOTE_CONTROL_COUNT} Selenium Servers..."

REMOTE_CONTROL_COUNT.times {
    int port = findFreePort()
    servers[port] = SeleniumServerFactory.configure().setPort(port).setSingleWindow(true).create()
    servers[port].start()
    ports.offer(port)
}

showServers()

def serverSocket = new ServerSocket(LISTEN_PORT)
for (;;) {
    socket = null
    try {
        socket = serverSocket.accept()
    } catch (IOException) {
        serverSocket.close()
        break
    }
    tunnel(socket, ports.take() as int)
}
