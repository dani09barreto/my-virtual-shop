package com.subdb;

import java.io.IOException;
import java.util.Scanner;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import com.subdb.Model.Product;
import com.subdb.Model.Token;

public class Cliente {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        Token tok = Token.getAuthToken("dani09barreto", "123456");
        if (tok == null) {
            System.out.println("Error de autenticacion contraseña o usuario erroneos");
        }

        try (ZContext context = new ZContext()) {
            Socket client = context.createSocket(SocketType.REQ);
            client.connect("tcp://10.43.100.225:5671");
            int opcion;
            do {
                System.out.println("\t----------Tienda Virtual----------");
                System.out.println("\tSeleccione una opcion");
                System.out.println("\t1. Listar Productos");
                System.out.println("\t2. Comprar Producto");
                System.out.println("\t3.Salir");
                System.out.print("\tOpcion:");
                opcion = sc.nextInt();

                switch (opcion) {
                    case 1:
                        client.send("Consultar");
                        break;
                    case 2:
                        client.send("Comprar");
                        break;
                    case 3:
                        System.out.println("Saliendo...");
                        break;
                }
                System.out.println("Esperando respuesta ....");
                String resp = client.recvStr();
                System.out.println(resp);

            } while (opcion != 0);
        }
        sc.close();
    }
}