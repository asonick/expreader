/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expression;

/**
 *
 * @author Nick
 */
import java.lang.*;

public class StringBuilderDemo {

  public static void main(String[] args) {
  
    StringBuilder str = new StringBuilder("Tutorial");
    System.out.println("string = " + str);
        
    // insert character value at offset 8
    str.insert(8, 's');
        
    // prints StringBuilder after insertion
    System.out.print("After insertion = ");
    System.out.println(str.toString());
  }      
}  
