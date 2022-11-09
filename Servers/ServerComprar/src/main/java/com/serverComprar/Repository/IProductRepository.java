package com.subdb.Repository;

import java.util.ArrayList;

import com.subdb.Model.Product;

public interface IProductRepository {
    ArrayList <Product> getProducts ();
    Product getProduct (int idProduct);
    int updateProduct (Product product);
}
