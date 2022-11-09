package com.pubSensor;

import java.util.Random;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

public class PubSensor {
    public static void main(String[] args) throws Exception {
        try (ZContext context = new ZContext()) {
            Socket publisher = context.createSocket(SocketType.PUB);
            Socket subRest = context.createSocket(SocketType.SUB);

            publisher.connect("tcp://10.43.100.225:5557");
            subRest.connect("tcp://10.43.100.225:5551");
            subRest.subscribe(ZMQ.SUBSCRIPTION_ALL);
            //  Ensure subscriber connection has time to complete
            Thread.sleep(1000);
            float tempe;
            //  Send out all 1,000 topic messages
            Random Temp = new Random();
            while (!Thread.currentThread().isInterrupted()) {
                publisher.send("Temperatura", ZMQ.SNDMORE);
                tempe = Temp.nextFloat() * 50;
                System.out.println("Enviado Temperatura: "+tempe);
                publisher.send(String.valueOf(tempe));
                Thread.sleep((long) (Math.random() * 15000));
                String rest = subRest.recvStr();
                System.out.println(rest);
            }
            //  Send one random update per second

        }
    }
}
