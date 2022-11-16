package com.subdb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.subdb.Controller.ControllerClient;
import com.subdb.Model.Product;
import com.subdb.Model.Sale;
import com.subdb.Model.Token;
import com.subdb.Model.ZHelper;

public class Cliente {
    private final static int REQUEST_TIMEOUT = 250000;
    private final static int REQUEST_RETRIES = 3;
    static Scanner sc = new Scanner(System.in);
    private static ControllerClient controllerClient = new ControllerClient();

    public static void main(String[] args) throws IOException, InterruptedException {
        String user;
        String pass;
        System.out.println("\t--------Login----------");
        System.out.print("Ingrese usuario: ");
        user = sc.next();
        System.out.print("Ingrese contraseña: ");
        pass = sc.next();

        Token tok = Token.getAuthToken(user, pass);
        if (tok == null) {
            System.out.println("Error de autenticacion contraseña o usuario erroneos");
            return;
        }

        try (ZContext context = new ZContext()) {
            Socket client = context.createSocket(SocketType.REQ);
            client.connect("tcp://10.43.100.225:5672");
            ZHelper.setId(client);
            int opcion;
            do {
                System.out.println("\t\n----------Tienda Virtual----------");
                System.out.println("\tSeleccione una opcion");
                System.out.println("\t1. Listar Productos");
                System.out.println("\t2. Comprar Producto");
                System.out.println("\t3.Salir");
                System.out.print("\tOpcion:");
                opcion = sc.nextInt();

                switch (opcion) {
                    case 1:
                        getProducts(client, context);
                        break;
                    case 2:
                        buyProduct(client, context);
                        break;
                    case 3:
                        System.out.println("Saliendo...");
                        break;
                }

            } while (opcion != 3);
        }
        sc.close();
    }

    public static void getProducts(Socket client, ZContext ctx) throws InterruptedException {

        Poller poller = ctx.createPoller(1);
        poller.register(client, Poller.POLLIN);

        int retriesLeft = REQUEST_RETRIES;
        String request = "Consultar";
        client.send(request);

        int expect_reply = 1;
        while (expect_reply > 0) {
            // Poll socket for a reply, with timeout
            int rc = poller.poll(REQUEST_TIMEOUT);
            if (rc == -1)
                break; // Interrupted

            if (poller.pollin(0)) {
                // We got a reply from the server, must match
                // getSequence
                String reply = client.recvStr();
                if (reply == null)
                    break; // Interrupted
                controllerClient.setProducts(deserializeMessage(reply));
                retriesLeft = REQUEST_RETRIES;
                expect_reply = 0;

            } else if (--retriesLeft == 0) {
                System.out.println(
                        "Ocurrio un error en el servidor vuelva a intentarlo\n");
                break;
            } else {
                System.out.println(
                        "No se ha obtenido una respuesta del servidor, se intentara de nuevo\n");
                // Old socket is confused; close it and open a new one
                poller.unregister(client);
                ctx.destroySocket(client);
                System.out.println("Reconectando con el servidor\n");
                client = ctx.createSocket(SocketType.REQ);
                client.connect("tcp://10.43.100.225:5671");
                poller.register(client, Poller.POLLIN);
                // Send request again, on new socket
                client.send(request);
            }
        }
        controllerClient.showProducts();
        poller.unregister(client);
    }

    public static ArrayList <Product> deserializeMessage(String message){
        ArrayList <Product> productsTemp = new ArrayList<>();
        Gson gson = new Gson();
        java.lang.reflect.Type userListType = new TypeToken<ArrayList<Product>>() {
        }.getType();
        productsTemp = gson.fromJson(message, userListType);

        return productsTemp;
    }

    public static void buyProduct(Socket client, ZContext ctx){
        ArrayList<Product> buyProducts = new ArrayList<>();
        if (controllerClient.getProducts().size() == 0){
            System.out.println("Primero debe consultar los productos disponibles");
            return;
        }

        int idProduct;
        controllerClient.showProducts();
        do{
            System.out.println("Inserte el numero del producto a comprar 0 para salir");
            System.out.print("añadir: ");
            idProduct = sc.nextInt();
            Product productTemp = controllerClient.exisProduct(idProduct);
            if (productTemp != null){
                buyProducts.add(productTemp);
                System.out.println("Producto añadido");
            }
            else if (idProduct == 0){
                continue;
            }
            else{
                System.out.println("Producto no existe en el mercado");
            }
        }while(idProduct != 0);

        Poller poller = ctx.createPoller(1);
        poller.register(client, Poller.POLLIN);

        int retriesLeft = REQUEST_RETRIES;
        String request = "Comprar";
        client.sendMore(request);
        client.send(controllerClient.serializeProduct(buyProducts));

        int expect_reply = 1;
        while (expect_reply > 0) {
            ArrayList <Sale> salesProducts = new ArrayList<>();
            // Poll socket for a reply, with timeout
            int rc = poller.poll(REQUEST_TIMEOUT);
            if (rc == -1)
                break; // Interrupted

            if (poller.pollin(0)) {
                // We got a reply from the server, must match
                // getSequence
                String reply = client.recvStr();
                if (reply == null)
                    break; // Interrupted
                
                salesProducts = controllerClient.deserializeSale(reply);
                for (Sale sl : salesProducts){
                    Product prTemp = controllerClient.exisProduct(sl.getIdProduct());
                    if (sl.getIsSale() == 1){
                        System.out.println(String.format("Producto %s comprado", prTemp.getNombre()));
                    }
                    else{
                        System.out.println(String.format("Producto %s no pudo ser comprado", prTemp.getNombre()));
                    }
                }
                retriesLeft = REQUEST_RETRIES;
                expect_reply = 0;

            } else if (--retriesLeft == 0) {
                System.out.println(
                        "Ocurrio un error en el servidor vuelva a intentarlo\n");
                break;
            } else {
                System.out.println(
                        "No se ha obtenido una respuesta del servidor, se intentara de nuevo\n");
                // Old socket is confused; close it and open a new one
                poller.unregister(client);
                ctx.destroySocket(client);
                System.out.println("Reconectando con el servidor\n");
                client = ctx.createSocket(SocketType.REQ);
                client.connect("tcp://10.43.100.225:5671");
                poller.register(client, Poller.POLLIN);
                // Send request again, on new socket
                client.sendMore(request);
                client.send(controllerClient.serializeProduct(buyProducts));
            }
        }
        poller.unregister(client);
    }

}