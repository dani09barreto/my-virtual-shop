package com.subdb;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.Type;
import com.subdb.Controller.ControllerShop;
import com.subdb.Model.Product;


public class ServerConsultar {
    private static ControllerShop controllerShop = new ControllerShop();
    private static Gson gson = new Gson();
    public static void main(String[] args) throws IOException {
        // try (ZContext context = new ZContext()) {
        //     Socket subscriber = context.createSocket(SocketType.SUB);
        //     Socket pubRest =  context.createSocket(SocketType.PUB);
        //     subscriber.connect("tcp://10.43.100.225:5558");
        //     pubRest.connect("tcp://10.43.100.225:5559");

        //     String subscription = "Consultar";
        //     subscriber.subscribe(subscription.getBytes(ZMQ.CHARSET));
        //     SimpleDateFormat format = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
        //     while (true) {
        //         String topic = subscriber.recvStr();
        //         if (topic == null)
        //             break;
        //         String data = subscriber.recvStr();
        //         assert (topic.equals(subscription));
        //         Calendar calendar = Calendar.getInstance();
        //         String Fdata = format.format(calendar.getTime()) + " " + topic + " " + data + "\n";
        //         System.out.println(Fdata);
        //         pubRest.send("recib");
        //         System.out.println("A");
        //     }
        // }

        //consulta base de datos
        for (Product p : controllerShop.listProducts()){
            System.out.println(p.getNombre());
        }
        //convertir lista de productos a json
        String str = gson.toJson(controllerShop.listProducts());
        System.out.println(str);
        
        java.lang.reflect.Type userListType = new TypeToken<ArrayList<Product>>(){}.getType();
        
        //convertir de json a lista de productos
        ArrayList<Product> userArray = gson.fromJson(str, userListType);  
 
        for(Product pr : userArray) {
            System.out.println(pr.getNombre());
        }
    }
}