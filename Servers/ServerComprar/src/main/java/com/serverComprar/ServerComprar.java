package com.serverComprar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import com.serverComprar.Controller.ControllerShop;
import com.serverComprar.model.Product;
import com.serverComprar.model.Sale;




public class ServerComprar {
    private static ControllerShop controllerShop = new ControllerShop();
    public static void main(String[] args) throws IOException {
        try (ZContext context = new ZContext()) {
            Socket subscriber = context.createSocket(SocketType.SUB);
            Socket pubRest =  context.createSocket(SocketType.PUB);
            subscriber.connect("tcp://10.43.100.225:5558");
            pubRest.connect("tcp://10.43.100.225:5559");

            String subscription = "Comprar";
            subscriber.subscribe(subscription.getBytes(ZMQ.CHARSET));
            while (true) {
                ArrayList <Product> buyProducts = new ArrayList<>();
                ArrayList <Sale> saleProducts = new ArrayList<>();
                String topic = subscriber.recvStr();
                if (topic == null)
                    break;
                String data = subscriber.recvStr();
                assert (topic.equals(subscription));
                buyProducts = controllerShop.deserializeMessage(data);

                for (Product pr : buyProducts){
                    if (controllerShop.buyProduct(pr.getId())){
                        Sale saleTemp = new Sale(pr.getId(), 1);
                        saleProducts.add(saleTemp);
                    }
                    else{
                        Sale saleTemp = new Sale(pr.getId(), 0);
                        saleProducts.add(saleTemp);
                    }
                    
                }
                String resp = controllerShop.serializeSale(saleProducts);
                pubRest.send(resp);
            }
        }
    }
}