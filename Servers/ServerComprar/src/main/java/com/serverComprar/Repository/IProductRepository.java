package com.serverComprar.Repository;


import java.util.ArrayList;
import com.serverComprar.model.Product;


public interface IProductRepository {
    ArrayList <Product> getProducts ();
    Product getProduct (int idProduct);
    int updateProduct (Product product);
}
