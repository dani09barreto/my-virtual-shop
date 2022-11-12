package com.publicador;

import java.util.Random;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

public class Publicador {
    public static void main(String[] args) throws Exception {
        try (ZContext context = new ZContext()) {
            Socket publisher = context.createSocket(SocketType.PUB);
            Socket subRest = context.createSocket(SocketType.SUB);
            Socket server = context.createSocket(SocketType.REP);

            server.bind("tcp://10.43.100.225:5671");
            publisher.connect("tcp://10.43.100.225:5557");
            subRest.connect("tcp://10.43.100.225:5551");
            subRest.subscribe(ZMQ.SUBSCRIPTION_ALL);
            //  Ensure subscriber connection has time to complete
            Thread.sleep(1000);
            //  Send out all 1,000 topic messages
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("a");
                server.recvStr();
                server.recvStr();
                String request = server.recvStr();
                if (request.equals("Comprar")){
                    publisher.send(request, ZMQ.SNDMORE);
                }else{
                    System.out.println(request);
                    publisher.send(request, ZMQ.SNDMORE);
                    publisher.send(request);
                }
                System.out.println("esperando respuesta.....");
                String rest = subRest.recvStr();
                System.out.println(rest);
                server.sendMore(rest);
                // publisher.send("Temperatura", ZMQ.SNDMORE);
                // tempe = Temp.nextFloat() * 50;
                // System.out.println("Enviado Temperatura: "+tempe);
                // publisher.send(String.valueOf(tempe));
                // Thread.sleep((long) (Math.random() * 15000));
                // String rest = subRest.recvStr();
                // System.out.println(rest);
            }
            //  Send one random update per second

        }
    }
}
