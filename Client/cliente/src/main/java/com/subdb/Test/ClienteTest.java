package com.subdb.Test;

public class ClienteTest {
  public static void main(String[] args) {
    // test listar
    // for (int i = 0; i < 5; i++) {
    //   new ClienteListarTest().start();
    // }
    // test comprar
    for (int i = 0; i < 3; i++) {
      new ClientComprarTest().start();
    }
  }
}
