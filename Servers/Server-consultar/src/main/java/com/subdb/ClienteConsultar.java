package com.subdb;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import com.subdb.Controller.ControllerShop;
import com.subdb.Model.Product;


public class ClienteConsultar {
    private static ControllerShop controllerShop = new ControllerShop();
    public static void main(String[] args) throws IOException {
        try (ZContext context = new ZContext()) {
            Socket subscriber = context.createSocket(SocketType.SUB);
            Socket pubRest =  context.createSocket(SocketType.PUB);
            subscriber.connect("tcp://10.43.100.225:5558");
            pubRest.connect("tcp://10.43.100.225:5559");

            String subscription = "Consultar";
            subscriber.subscribe(subscription.getBytes(ZMQ.CHARSET));
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
            while (true) {
                String topic = subscriber.recvStr();
                if (topic == null)
                    break;
                String data = subscriber.recvStr();
                assert (topic.equals(subscription));
                Calendar calendar = Calendar.getInstance();
                String Fdata = format.format(calendar.getTime()) + " " + topic + " " + data + "\n";
                System.out.println(Fdata);
                pubRest.send("recib");
                System.out.println("A");
            }
        }

        // for (Product p : controllerShop.listProducts()){
        //     System.out.println(p);
        // }

    }
}