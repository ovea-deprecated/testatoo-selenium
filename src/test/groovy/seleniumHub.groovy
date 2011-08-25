#!/usr/bin/env groovy

import java.util.concurrent.LinkedBlockingQueue
import org.testatoo.selenium.server.SeleniumServer
import org.testatoo.selenium.server.SeleniumServerFactory

@GrabResolver(name = 'testatoo', root = 'http://oss.sonatype.org/content/repositories/snapshots/')
@Grab(group = 'org.testatoo', module = 'testatoo-selenium', version = '1.0-rc2-SNAPSHOT')

////////////// CONF //////////////
LISTEN_PORT = args.length > 0 ? args[0] as int : 4444
REMOTE_CONTROL_COUNT = args.length > 1 ? args[1] as int : 5
//////////////////////////////////

findFreePort = {
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

println "Starting Selenium Servers..."

ports = new LinkedBlockingQueue()
servers = new TreeMap<Integer, SeleniumServer>()
REMOTE_CONTROL_COUNT.times {
    int port = findFreePort()
    servers[port] = SeleniumServerFactory.configure().setPort(port).setSingleWindow(true).create()
    servers[port].start()
    ports.offer(port)
}

println "Started ${REMOTE_CONTROL_COUNT} Selenium Servers on ports:"
servers.each {k, v -> println " - ${k}" }

clean = {int p, Socket client, Socket rc ->
    if (!ports.contains(p)) {
        ports.offer(p)
        println "Disconnecting client ${client.remoteSocketAddress} from Selenium Server ${p}..."
        client?.close()
        rc?.close()
        servers[p].stop()
        servers[p] = SeleniumServerFactory.configure().setPort(p).setSingleWindow(true).create()
        servers[p].start()
    }
}

def threads = []
Runtime.runtime.addShutdownHook(new Thread() {
    @Override
    void run() {
        threads.each {Thread t ->
            t.interrupt()
        }
    }
})

tunnel = {Socket client, int p ->
    println "Connecting client ${client.remoteSocketAddress} to Selenium Server ${p}..."
    Socket rc = null
    try {
        rc = new Socket('localhost', p as int)
    } catch (e) {
        clean(p, client, rc)
    }
    Thread reader = null, writer = null
    reader = Thread.start("${client.remoteSocketAddress} READER", {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                rc.outputStream.write(client.inputStream.read())
            }
        } catch (e) {
            reader?.interrupt()
            writer?.interrupt()
            threads.remove(reader)
            threads.remove(writer)
            clean(p, client, rc)
        }
    })
    writer = Thread.start("${client.remoteSocketAddress} WRITER", {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                client.outputStream.write(rc.inputStream.read())
            }
        } catch (e) {
            reader?.interrupt()
            writer?.interrupt()
            threads.remove(reader)
            threads.remove(writer)
            clean(p, client, rc)
        }
    })
    threads << reader
    threads << writer
}

ServerSocket serverSocket = new ServerSocket(LISTEN_PORT)
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
