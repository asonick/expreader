/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package expression;

/**
 * Created on April 22, 2013, 12:00 AM PST
 * Modified on April 27, 2013, 2:36 AM PST 
 * @author Aso
 */
public class Stack{

    /**
     * @param args the command line arguments
     */
    private int counter;
    private int capacity;
    private final int increase;
    private Object[] stack;
    
    public Stack(){
        counter = 0; //Index of Stack; stack pointer
        capacity = 2; //Hard-coded value; most finite amount for a stack size
        increase = capacity = Math.abs(capacity); //Impossible to have neg. increase
        stack = new Object[capacity];
    }
    
    public void Push(Object token){
        stack[counter++] = token;
        
        if(counter == capacity){ //then Stack has reached its max capacity
            capacity*=increase; //increase value for max capacity
            Object[] temp = new Object[capacity];
            //System.out.println("Increased Stack capacity to " + capacity);
            System.arraycopy(stack, 0, temp, 0, stack.length); //Populates the new stack
            //insert counter here for measurement
            
            stack = temp; //Assign address of temp stack to original
        }
    }
    
    public Object Pop(){
        Object token = ' ';
        
        if(counter <= 0){
            System.out.println("The Stack's counter is at " + counter);
            return token;
        }
        
        //insert counter here for measurement
        token = stack[--counter]; //Pre-decr. needed since counter is offset 1 ahead of top of stack (null)
        
        if(counter < capacity/increase){ //then stack has excess space
            capacity/=increase; //Shrinks (resizes) the stack
            Object[] temp = new Object[capacity];
            //System.out.println("Decreased Stack capacity to " + capacity);
            System.arraycopy(stack, 0, temp, 0, counter); //Populates the new stack
            //insert counter here for measurement
            
            stack = temp; //Assign address of temp stack to original
        }
        
        return token;
    }
    
    public boolean isEmpty(){ //Checks to see if stack is empty
        return (counter == 0);
    }
    
    public int Position(){ //Returns index of stack pointer
        return counter;
    }
    
    public int stackSize(){ //Returns stack size
        return stack.length;
    }
    
    public Object FindAmount(Object token){
        int limit = counter; //holds counter
        String found = "";
        
        for(int i = 0; i < limit; i++){ 
            if(stack[i] == token) found = String.valueOf(Integer.valueOf(found) + 1);
            //insert counter here for measurement
        }
        
        return found;
    }
    
    private void setPosition(int index){
        counter = index;
    }
    
    public Object Get(int pos){
        return stack[pos];
    }
    
    public void showAll(){ //Prints stats of Stack
        System.out.println("\nStack size: " + stack.length);
        System.out.println("@ Position: " + (Position() + 1));
        System.out.println("Available space(s): " + (stack.length - Position()) + " cell(s).");
        if(isEmpty()) System.out.print("Stack is EMPTY!!! ");
        else{
            System.out.print("Object(s) on stack: ");
            for(int i = 0; stack[i] != null; i++){
                if(i > 0) System.out.print(", ");
                System.out.print(stack[i]);
            }
        System.out.println("\n");
        }
    }
}