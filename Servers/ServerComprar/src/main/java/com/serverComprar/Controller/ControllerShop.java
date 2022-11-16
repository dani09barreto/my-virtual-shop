package com.serverComprar.Controller;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.serverComprar.Repository.IProductRepository;
import com.serverComprar.Service.ProductRepositotyImp;
import com.serverComprar.model.Product;
import com.serverComprar.model.Sale;

import lombok.Getter;

@Getter
public class ControllerShop {
    private IProductRepository productRepository = new ProductRepositotyImp();

    public ArrayList <Product> listProducts (){
        return productRepository.getProducts();
    }

    public boolean buyProduct (int id){
        Product productTemp = productRepository.getProduct(id);
        if (productTemp == null){
            return false;
        }
        int cantProduct = productTemp.getCant();
        cantProduct --;
        if (cantProduct < 0){
            return false;
        }
        
        productTemp.setCant(cantProduct);

        int updated  = productRepository.updateProduct(productTemp);

        if (updated == 0){
            return false;
        }

        return true;
    }

    public ArrayList <Product> deserializeMessage(String message){
        ArrayList <Product> productsTemp = new ArrayList<>();
        Gson gson = new Gson();
        java.lang.reflect.Type userListType = new TypeToken<ArrayList<Product>>() {
        }.getType();
        productsTemp = gson.fromJson(message, userListType);

        return productsTemp;
    }

    public String serializeSale (ArrayList <Sale> saleProducts){
        Gson gson = new Gson();
        String str = gson.toJson(saleProducts);
        return str;
    }

}
