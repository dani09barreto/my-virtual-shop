package com.subdb.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.subdb.Model.Constants;
import com.subdb.Model.Product;
import com.subdb.Repository.IProductRepository;

public class ProductRepositotyImp implements IProductRepository {

    @Override
    public ArrayList<Product> getProducts() {
        ArrayList<Product> listProducts = new ArrayList<>();
        String SQL = "select *\n" +
                "from products";

        try (
                Connection conex = DriverManager.getConnection(Constants.THINCONN, Constants.USERNAME, Constants.PASSWORD);
                PreparedStatement ps = conex.prepareStatement(SQL);
                ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                listProducts.add(createProduct(rs));
            }

        } catch (SQLException ex) {
            System.out.println("Error de conexion:" + ex.toString());
            ex.printStackTrace();
        }
        return listProducts;
    }

    private Product createProduct(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("ID"),
            rs.getString("PRODUCT_NAME"),
            rs.getInt("CANT")
        );
    }

    @Override
    public Product getProduct(int idProduct) {
        Product product = null;

        String SQL = "select *\n"+
                "from productos.productsd p\n"+
                "where p.id = ?;";
        try (
                Connection conex = DriverManager.getConnection(Constants.THINCONN, Constants.USERNAME, Constants.PASSWORD);
                PreparedStatement ps = conex.prepareStatement(SQL);
                ResultSet rs = ps.executeQuery();) {
                    ps.setInt(1, idProduct);
                    while (rs.next()) {
                        product = createProduct(rs);
                    }

        } catch (SQLException ex) {
            System.out.println("Error de conexion:" + ex.toString());
            ex.printStackTrace();
        }

        return product;
    }

    @Override
    public int updateProduct(Product product) {
        int afectadas = 0;
        product.setCant(product.getCant() - 1);
        String SQL = "update productos.productsd set cant = ? where id = ?";
        try (
                Connection conex = DriverManager.getConnection(
                    Constants.THINCONN,
                    Constants.USERNAME,
                    Constants.PASSWORD);
                PreparedStatement ps = conex.prepareStatement(SQL);) {

            ps.setInt(1, product.getCant());
            ps.setInt(2, product.getId());
            afectadas = ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("Error de conexion:" + ex.toString());
            ex.printStackTrace();
        }
        return afectadas;
    }

}
