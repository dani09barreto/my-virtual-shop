package com.subMostrar;


import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

public class SubMostrar{
    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            Socket subscriber = context.createSocket(SocketType.SUB);
            
            subscriber.connect("tcp://10.195.69.0:5558");

            String subscription = "Temperatura";
            String subscription1 = "Distancia";
            subscriber.subscribe(subscription.getBytes(ZMQ.CHARSET));
            subscriber.subscribe(subscription1.getBytes(ZMQ.CHARSET));
    
            while (true) {
                String topic = subscriber.recvStr();
                if (topic == null)
                    break;
                String data = subscriber.recvStr();
                assert (topic.equals(subscription));
                System.out.println(topic);
                System.out.println(data);
            }
        }
    }
}