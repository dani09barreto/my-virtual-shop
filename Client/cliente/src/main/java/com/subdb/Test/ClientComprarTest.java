package com.subdb.Test;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.zeromq.ZContext;
import org.zeromq.SocketType;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

import com.subdb.Controller.ControllerClient;
import com.subdb.Model.Product;
import com.subdb.Model.Sale;

public class ClientComprarTest extends Thread{
    private final static int REQUEST_TIMEOUT = 250000;
    private final static int REQUEST_RETRIES = 3;
    private static ControllerClient controllerClient = new ControllerClient();
    
    @Override
    public void run() {
        super.run();
        //genera numeros aleatorios de 0 a 10
        ClienteListarTest list = new ClienteListarTest();
        ArrayList <Product > products = new ArrayList<>();
        list.start();
        controllerClient.setProducts(list.listar());
        for (int i = 0; i < 3; i++){
            int id = (int)(Math.random()*10 + 1);
            products.add(controllerClient.exisProduct(id));
        }
        try (ZContext ctx = new ZContext()){
            Socket client = ctx.createSocket(SocketType.REQ);
            client.connect("tcp://10.43.100.225:5672");
            Poller poller = ctx.createPoller(1);
            poller.register(client, Poller.POLLIN);
    
            int retriesLeft = REQUEST_RETRIES;
            String request = "Comprar";
            client.sendMore(request);
            client.send(controllerClient.serializeProduct(products));
    
            int expect_reply = 1;
            Double startTime = (double) System.currentTimeMillis();
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
                    client.send(controllerClient.serializeProduct(products));
                }
            }
            poller.unregister(client);
            long endTime = (long) (System.currentTimeMillis() - startTime);
            long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(endTime);
            System.out.println("Tiempo en completar solicitud: " + timeSeconds + " Segundos");
        }
    }
}
