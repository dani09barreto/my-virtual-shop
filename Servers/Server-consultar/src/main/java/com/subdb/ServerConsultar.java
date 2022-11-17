package com.subdb;

import java.io.IOException;
import java.util.ArrayList;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import com.google.gson.Gson;
import com.subdb.Controller.ControllerShop;
import com.subdb.Model.Product;
import com.subdb.Model.Sale;

public class ServerConsultar {
    private static ControllerShop controllerShop = new ControllerShop();

    public static void main(String[] args) throws IOException {
        try (ZContext context = new ZContext()) {
            Socket subscriber = context.createSocket(SocketType.SUB);
            Socket pubRest = context.createSocket(SocketType.PUB);
            subscriber.connect("tcp://10.43.100.225:5558");
            pubRest.connect("tcp://10.43.100.225:5559");

            String subscription = "Consultar";
            String subscriptionError = "Error";
            String subsOk = "OK";
            subscriber.subscribe(subscriptionError);
            subscriber.subscribe(subsOk);
            subscriber.subscribe(subscription.getBytes(ZMQ.CHARSET));
            while (true) {
                ArrayList<Product> buyProducts = new ArrayList<>();
                ArrayList<Sale> saleProducts = new ArrayList<>();
                String topic = subscriber.recvStr();
                if (topic == null)
                    break;
                String data = subscriber.recvStr();
                if (topic.equals("Comprar")) {
                    System.out.println("Solicitud: " + topic);
                    buyProducts = controllerShop.deserializeMessage(data);

                    for (Product pr : buyProducts) {
                        if (controllerShop.buyProduct(pr.getId())) {
                            Sale saleTemp = new Sale(pr.getId(), 1);
                            saleProducts.add(saleTemp);
                        } else {
                            Sale saleTemp = new Sale(pr.getId(), 0);
                            saleProducts.add(saleTemp);
                        }

                    }
                    String resp = controllerShop.serializeSale(saleProducts);
                    System.out.println("Respuesta: " + resp);
                    pubRest.send(resp);
                } else if (topic.equals("Consultar")) {
                    System.out.println("Solicitud: " + topic);
                    Gson gson = new Gson();
                    ArrayList<Product> products = new ArrayList<>();
                    products = controllerShop.listProducts();
                    String resp = gson.toJson(products);
                    System.out.println("Resp: " + resp);
                    pubRest.send(resp);
                } else if (topic.equals("Error")) {
                    if (data.equals("1")) {
                        System.out.println("Suscribiendose a topico comprar");
                        subscriber.subscribe("Comprar");
                    }
                } else if (topic.equals("OK")) {
                    if (data.equals("1")) {
                        System.out.println("desuscribiendose a topico comprar");
                        subscriber.unsubscribe("Comprar");
                    }
                }
            }
        }

    }
}