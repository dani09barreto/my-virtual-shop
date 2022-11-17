package com.broker;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

public class Broker{
    private final static int REQUEST_TIMEOUT = 60;
    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            Socket frontend = context.createSocket(SocketType.SUB);
            frontend.bind("tcp://10.43.100.225:5557");
            Socket backend = context.createSocket(SocketType.XPUB);
            backend.bind("tcp://10.43.100.225:5558");

            Socket rest = context.createSocket(SocketType.SUB);
            rest.bind("tcp://10.43.100.225:5559");
            Socket restSend = context.createSocket(SocketType.XPUB);
            restSend.bind("tcp://10.43.100.225:5551");

            //  Subscribe to every single topic from publisher
            frontend.subscribe(ZMQ.SUBSCRIPTION_ALL);
            rest.subscribe(ZMQ.SUBSCRIPTION_ALL);

            //  Store last instance of each topic in a cache
            Map<String, String> cache = new HashMap<String, String>();

            Poller poller = context.createPoller(2);
            poller.register(frontend, Poller.POLLIN);
            poller.register(backend, Poller.POLLIN);

            Poller poller2 = context.createPoller(2);
            poller2.register(rest, Poller.POLLIN);
            poller2.register(restSend, Poller.POLLIN);

            boolean activateTime = false;
            String serverActivate = "";

            //  .split main poll loop
            //  We route topic updates from frontend to backend, and we handle
            //  subscriptions by sending whatever we cached, if anything:
            int cont = 1;
            while (true) {
                if (poller.poll(1000) == -1)
                    break; //  Interrupted
                if (poller2.poll(1000) == -1)
                    break;
                //  Any new topic data we cache and then forward
                if (poller.pollin(0)) {
                    String topic = frontend.recvStr();
                    String current = frontend.recvStr();

                    if (topic == null)
                        break;
                    cache.put(topic, current);
                    backend.sendMore(topic);
                    backend.send(current);
                }
                //  .split handle subscriptions
                //  When we get a new subscription, we pull data from the cache:
                if (poller.pollin(1)) {
                    ZFrame frame = ZFrame.recvFrame(backend);
                    if (frame == null)
                        break;
                    //  Event is one byte 0=unsub or 1=sub, followed by topic
                    byte[] event = frame.getData();
                    if (event[0] == 1) {
                        String topic = new String(event, 1, event.length - 1, ZMQ.CHARSET);
                        System.out.printf("servidor conectado con topico %s\n", topic);
                    }
                    if (event[0] == 0) {
                        String topic = new String(event, 1, event.length - 1, ZMQ.CHARSET);
                        System.out.printf("servidor desconectado con topico %s\n", topic);
                        if (topic.equals("Comprar")){
                            activateTime = true;
                            backend.sendMore("Error");
                            serverActivate = "1";
                            backend.send(serverActivate);
                        }
                        else if (topic.equals("Consultar")){
                            activateTime = true;
                            backend.sendMore("Error");
                            serverActivate = "2";
                            backend.send(serverActivate);
                        }
                    }
                    frame.destroy();
                }
                if (poller2.pollin(0)){
                    System.out.println("rest");
                    String restString = rest.recvStr();
                    System.out.println(restString);
                    if (restString == null)
                        break;
                    restSend.send(restString);
                }
                if (cont == REQUEST_TIMEOUT){
                    System.out.println("Se envia mensaje de ok sevidor devio volver");
                    backend.sendMore("OK");
                    backend.send(serverActivate);
                    activateTime = false;
                    cont = 0;  
                }
                if (activateTime){
                    System.out.println("Timer : " + cont);
                    cont ++;
                }
            }
        }
    }
}