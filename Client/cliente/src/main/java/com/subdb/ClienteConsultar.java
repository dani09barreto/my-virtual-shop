package com.subdb;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.stream.Collectors;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import com.subdb.Model.Product;
import com.subdb.Model.Token;


public class ClienteConsultar {
    public static void main(String[] args) throws IOException {
        Token tok = Token.getAuthToken("casa", "1234");
        if (tok == null){
            System.out.println("Error de autenticacion contraseña o usuario erroneos");
        }
        try (ZContext context = new ZContext()) {
            Socket client = context.createSocket(SocketType.REQ);

        }

        // for (Product p : controllerShop.listProducts()){
        //     System.out.println(p);
        // }

    }
}