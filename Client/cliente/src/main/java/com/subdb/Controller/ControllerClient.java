package com.subdb.Controller;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.subdb.Model.Product;
import com.subdb.Model.Sale;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ControllerClient {
    private ArrayList<Product> products = new ArrayList<>();

    public void showProducts(){
        System.out.println("\t\n----Productos Disponibles----");
        for (Product p : this.products) {
            System.out.println(
                    String.format("\t%d. %s %d unidades", p.getId(), p.getNombre(), p.getCant()));
        }
    }

    public Product exisProduct (int id){
        for (Product pr : this.products){
            if (pr.getId() == id){
                return pr;
            }
        }
        return null;
    }

    public String serializeProduct (ArrayList <Product> buyProducts){
        Gson gson = new Gson();
        String str = gson.toJson(buyProducts);
        return str;
    }

    public ArrayList <Product> deserializeMessage(String message){
        ArrayList <Product> productsTemp = new ArrayList<>();
        Gson gson = new Gson();
        java.lang.reflect.Type userListType = new TypeToken<ArrayList<Product>>() {
        }.getType();
        productsTemp = gson.fromJson(message, userListType);

        return productsTemp;
    }

    public ArrayList <Sale> deserializeSale(String messageResp){
        ArrayList <Sale> salesTemp = new ArrayList<>();
        Gson gson = new Gson();
        java.lang.reflect.Type userListType = new TypeToken<ArrayList<Sale>>() {
        }.getType();
        salesTemp = gson.fromJson(messageResp, userListType);

        return salesTemp;
    }
}
