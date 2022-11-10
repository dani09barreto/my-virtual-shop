package com.serverComprar.Controller;

import java.util.ArrayList;

import com.serverComprar.Repository.IProductRepository;
import com.serverComprar.Service.ProductRepositotyImp;
import com.serverComprar.model.Product;

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
}
