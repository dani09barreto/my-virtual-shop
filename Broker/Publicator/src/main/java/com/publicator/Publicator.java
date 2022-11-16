package com.publicator;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

public class Publicator {

    public static void main(String[] args) throws Exception {
        try (ZContext context = new ZContext()) {
            Socket publisher = context.createSocket(SocketType.PUB);
            Socket subRest = context.createSocket(SocketType.SUB);
            Socket worker = context.createSocket(SocketType.REP);
            ZHelper.setId(worker);

            worker.bind("tcp://10.43.100.225:5672");
            publisher.connect("tcp://10.43.100.225:5557");
            subRest.connect("tcp://10.43.100.225:5551");
            subRest.subscribe(ZMQ.SUBSCRIPTION_ALL);

            Thread.sleep(1000);

            while (!Thread.currentThread().isInterrupted()) {
                String request = worker.recvStr();
                System.out.println("Worker: " + request);

                if (request.equals("Comprar")) {
                    String data = worker.recvStr();
                    System.out.println(data);
                    publisher.send(request, ZMQ.SNDMORE);
                    publisher.send(data);
                } else {
                    publisher.send(request, ZMQ.SNDMORE);
                    publisher.send(request);
                }
                System.out.println("esperando respuesta.....");
                String rest = subRest.recvStr();
                System.out.println(rest);
                worker.send(rest);
                System.out.println("enviando al cliente: " + rest);
            }

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
