package com.subdb.Model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Token {
    private String token;

    public static Token getAuthToken (String user, String pass){
        try {
            Gson gson = new Gson();
            URL url = new URL ("http://10.43.100.213:8080/token");
            String encoding = Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setRequestProperty  ("Authorization", "Basic " + encoding);
            InputStream content = (InputStream)connection.getContent();
            String result = new BufferedReader(new InputStreamReader(content)).lines().collect(Collectors.joining("\n"));
            Token tok = gson.fromJson(result, Token.class);
            
            return tok;

        } catch(Exception e) {
            return null;
        }
    }
}
