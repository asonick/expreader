/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package expression;
/**
 * Created on April 25, 2013, 9:44 PM PST
 * Modified on May 15, 2013, 4:41 AM PST
 * @author Aso
 */
public class Expression{
    /**
     * @param args the command line arguments
     */    
    private static boolean POWER;
    private static boolean TIMES;
    private static boolean DIVIDE;
    private static boolean ADD;
    private static boolean ABS; // Default context of '|' is false (non-existent)
    private static char OPERATOR;
    private static final Stack now = new Stack();
    private static int SIG_FIG, matched;
    private static StringBuilder build;
    
    private static String Parse(String exp){
        Reset();
        boolean result = true;
        char check;
        int i = 0;
        
        // Parse loop, iterate thru each letter of index
        do{
            if(isOpenBrace(exp.charAt(i))){ // then, Push onto stack
                if(exp.charAt(i) == '|') Switch(exp.charAt(i)); // Switch the next expected context of '|' to closing
                sop("\nPushing... '" + exp.charAt(i) + "'");
                now.Push(exp.charAt(i));
            } else { //its a possible closed-brace or other character
                if(isCloseBrace(exp.charAt(i))) {
                    if(exp.charAt(i) == '|') Switch(exp.charAt(i)); // Switch the next expected context of '|' to closing
                    check = (char)now.Pop(); // capture token on top of stack
                    sop("\nPopping... '" + exp.charAt(i) + "'");
                    result = Match(check, exp.charAt(i));
                    if(!result){
                        sop("\nERROR:: Searched: '" + check + "' | FOUND: '" + exp.charAt(i) + "'\n");
                        sop("Parsing error within expression!!!");
                        exit();
                    }
                }
            }
            
            i++;
        }while(i < exp.length() && result == true);
        
        if(result == false || !now.isEmpty()){ 
            sop("\nParsing error within expression!!!");
            sop("\n!STACK.isEpmty()... DISPLAYED FROM PARSE");
            now.showAll();
            exit();
        }
        else sopln("\nNo Parsing Errors...");
        
        sopln("Terminating Parse(). . .");
        now.showAll();
        sopln("\n\nEquation: " + exp);
        return exp;
    }
  
    private static String Evaluate(String exp){
        Reset(); //Invokes Reset()
        
        build = new StringBuilder(exp);
        /* Preliminary loop, inserts a pair '(' ')' where needed to solve ambiguity of expression
         * EXPECTED BEHAVIOR TO ONLY HAPPEN, & MUTATE EXPRESSION ONCE */
        for(int i = 0; i < exp.length(); i++){
            if(exp.charAt(i) == '^'){
                for(int j = i + 1; j < exp.length(); j++){
                    if(exp.charAt(j) != ' '){
                        if(!isOpenBrace(exp.charAt(j)) || exp.charAt(j) != '|'){
                            build.insert(j, '('); // Inserts operator '(' in this instance of exp
                            exp = build.toString(); // Since StringBuild has changed, update current string
                            build = new StringBuilder(exp); // Solidify build as current string
                            sopln("Expression0 is now: " + exp);
                            //i++;
                            int k;
                            for(k = j + 1; k < exp.length(); k++){
                                if(exp.charAt(k) == ' '){
                                    build.insert(k, ')'); // Inserts operator ')' in this instance of exp
                                    exp = build.toString(); // Since StringBuild has changed, update current string
                                    build = new StringBuilder(exp); // Solidify build as current string
                                    sopln("Expression0.1 is now: " + exp);
                                    //i++;
                                    break;
                                } else if(isOpenBrace(exp.charAt(k)) || exp.charAt(k) == '|'){ // Bypass all gaps with subexp
                                    int m;
                                    for(m = k + 1; !Match(exp.charAt(k), exp.charAt(m)) && m < exp.length(); m++){}
                                    k = m; // new index                                    
                                }
                            }
                            
                            if(k == exp.length()){ // @ end of exp. Insert compliment parens anyway
                                build.insert(k, ')'); // Inserts operator ')' in this instance of exp
                                exp = build.toString(); // Since StringBuild has changed, update current string
                                build = new StringBuilder(exp); // Solidify build as current string
                                sopln("Expression0.11 is now: " + exp);
                            }
                        } 
                        /*else*/ break; // Proper syntax now. There may be '^' within found parens, continue traversal
                    }
                }
            }
        }
        
        sopln("Prelim #0 complete. Insertion of parens in assumed positions. Expression is now: " + exp);
        
        /* Preliminary loop, deletes excess spaces ' ' 
         * EXPECTED BEHAVIOR TO ONLY HAPPEN, & MUTATE EXPRESSION ONCE */
        String temp = "";
        for(int i = 0; i < exp.length(); i++){
            if(exp.charAt(i) != ' ') temp += exp.charAt(i); // Deletes all spaces from the instance of the exp 
        }
        exp = temp;
        sopln("Unnecessary gaps removed, to increase program optimality. Expression is now: " + exp);
        
        build = new StringBuilder(exp);
        
        /* Preliminary loop, inserts '+' in before of all -x, i.e 1-5 |-> 1+-5 
         * EXPECTED BEHAVIOR TO ONLY HAPPEN WHEN EXP IS LONGER THAN 2 */
        for(int i = 1; i < exp.length(); i++){
            if(exp.charAt(i) == '-' && exp.charAt(i - 1) != '+'){
                build.insert(i, '+'); // Inserts operator '+' in this instance of exp
                exp = build.toString(); // Since StringBuild has changed, update current string
                build = new StringBuilder(exp); // Solidify build as current string
                sopln("Expression is now: " + exp);
                i++; // Increase by 1, to acct. for newly added char
            }
        }        
        
        sopln("Prelim #1 complete. Added '+' in front of every '-'. Expression is now: " + exp);
        /* Preliminary loop, inserts '*' between any }{, ][, )(, || 
         * EXPECTED BEHAVIOR TO ONLY HAPPEN, & MUTATE EXPRESSION ONCE */
        for(int i = 0; i < exp.length(); i++){            
            if(i + 1 < exp.length() - 1){ // Protects from ArrayIndexOutOfBoundsException                
                if( isCloseBrace(exp.charAt(i)) && isOpenBrace(exp.charAt(i + 1)) ){
                    build.insert(i + 1, '*'); // Inserts operator '*' in this instance of exp
                    exp = build.toString(); // Since StringBuild has changed, update current string
                    build = new StringBuilder(exp); // Solidify build as current string
                    sopln("Expression1 is now: " + exp);
                    i++; // Increase by 1, to acct. for newly added char
                } else if(i > 1){ // Then following wont flag ArrayOutOfBoundsException
                    if( isOpenBrace(exp.charAt(i)) && !isOperator(exp.charAt(i - 1)) && exp.charAt(i - 1) != '-' && exp.charAt(i - 1) != '|'){
                        build.insert(i, '*'); // Inserts operator '*' in this instance of exp
                        exp = build.toString(); // Since StringBuild has changed, update current string
                        build = new StringBuilder(exp); // Solidify build as current string
                        sopln("Expression1.1 is now: " + exp);
                        i++; // Increase by 1, to acct. for newly added char
                    } else if(exp.charAt(i) == '|'){
                        if( (!isOperator(exp.charAt(i - 1)) && exp.charAt(i - 1) != '-' /* <- THIS MATTERS */
                            && exp.charAt(i - 1) != '|' // A beginning abs. val (implies empty abs) OR an end abs. value (implies inner abs)
                            && exp.charAt(i - 1) != '#' // Supplemtental '*' placemarker
                            && exp.charAt(i - 1) != '@' && exp.charAt(i + 1) != '@') // Has ! already been assessed
                                || isCloseBrace(exp.charAt(i - 1))
                            ){ // then unassessed '|' was found with no preceeding explicit operators, implying closed brace or var.
                            build.insert(i, '*'); // Inserts operator '*' in this instance of exp
                            build.insert(i + 2, '@'); // Assessed
                            exp = build.toString(); // Since StringBuild has changed, update current string
                            build = new StringBuilder(exp); // Solidify build as current string
                            sopln("Expression2 is now: " + exp);
                            i += 2; // Increase by 2, to acct. for newly added chars                        
                        
                            // Loop finds unassessed pair, check to see if operator resides thereafter
                            for(int j = exp.length() - 1; j >= 0 && j != (i - 1); j--){
                                if(exp.charAt(j) == '|'){
                                    if(j != exp.length() - 1){ // Then following wont flag ArrayOutOfBoundsException, AND match is not at end of string
                                        if( exp.charAt(j + 1) == '#' && exp.charAt(j - 1) == '@' ) continue; // Has been assessed already. Not THE matching parens
                                        else if( (exp.charAt(j - 1) != '@' // unassessed
                                                  && exp.charAt(j + 1) != '@'
                                                  && exp.charAt(j + 1) != '#' // unassessed
                                                  && exp.charAt(j + 1) != '|' 
                                                  && !isOperator(exp.charAt(j + 1)) /*&& exp.charAt(j + 1) != '-'*/
                                                  && !isCloseBrace(exp.charAt(j + 1))  )
                                                  || isOpenBrace(exp.charAt(j + 1))
                                                ){ // then theres no assessed marker before match AND no intended marker or operator after match, or theres an open brace after 
                                                build.insert(j + 1, '#'); // Inserts special char in this instance of exp, to be replaced later
                                                build.insert(j, '@');
                                                exp = build.toString(); // Since StringBuild has changed, update current string
                                                build = new StringBuilder(exp); // Solidify build as current string
                                                sopln("Expression2.1 is now: " + exp);
                                                i += 2; // Increase by 2, to acct. for newly added chars
                                                break; // now that insertion is done
                                        } else {
                                                if( (isOperator(exp.charAt(j + 1)) || exp.charAt(j + 1) == '#') && exp.charAt(j - 1) != '@' ){                                            
                                                    build.insert(j, '@'); // Inserts special char in this instance of exp, to be replaced later
                                                    exp = build.toString(); // Since StringBuild has changed, update current string
                                                    build = new StringBuilder(exp); // Solidify build as current string
                                                    sopln("Expression2.1 is now: " + exp);
                                                    i++; // Increase by 1, to acct. for newly added char
                                                    break; // now that insertion is done
                                                }
                                        }                                   
                                    }                                
                                }
                            }
                        } else { // Properly defined. Pad compliment with '@'
                            // Do until insertion is made or completes string traversal
                            for(int k = exp.length() - 1; k >= 0 && k != i; k--){
                                if(exp.charAt(k) == '|' && exp.charAt(k - 1) != '@'){
                                    build.insert(k, '@'); // Inserts special char in this instance of exp, to be replaced later
                                    exp = build.toString(); // Since StringBuild has changed, update current string
                                    build = new StringBuilder(exp); // Solidify build as current string
                                    sopln("Expression2.2 is now: " + exp);
                                    i++; // Increase by 1, to acct. for newly added char
                                    
                                    // Ensure compliment is properly defined as well
                                    k++; // Because char has been added to string
                                    if(k < exp.length() - 1){
                                        if( (exp.charAt(k) == '|' && (exp.charAt(k + 1) != '#' && exp.charAt(k + 1) != '|'
                                            && exp.charAt(k + 1) != '@' && !isOperator(exp.charAt(k + 1))) )
                                            || isOpenBrace(exp.charAt(k + 1))
                                           ){
                                            if(isCloseBrace(exp.charAt(k + 1))){
                                                build.insert(k + 2, '#'); // Inserts special char in this instance of exp, to be replaced later
                                                exp = build.toString(); // Since StringBuild has changed, update current string
                                                build = new StringBuilder(exp); // Solidify build as current string
                                                sopln("Expression2.3 is now: " + exp);
                                                i++; // Increase by 1, to acct. for newly added char
                                            } else {
                                                build.insert(k + 1, '#'); // Inserts special char in this instance of exp, to be replaced later
                                                exp = build.toString(); // Since StringBuild has changed, update current string
                                                build = new StringBuilder(exp); // Solidify build as current string
                                                sopln("Expression2.3 is now: " + exp);
                                                i++; // Increase by 1, to acct. for newly added char
                                            }
                                        }
                                    }
                                    break; // now that insertion is done                                    
                                }
                            }                            
                        }
                    } 
                }
            }
            //sopln("CHAR IS NOW: [" + i + "] " + exp.charAt(i));
        }
        
        /* Removal & replacement loop */
        for(int i = 0; i < exp.length(); i++){
            if(exp.charAt(i) == '@'){
                build.deleteCharAt(i); // Removes special char from exp
                exp = build.toString(); // Since StringBuild has changed, update current string
                build = new StringBuilder(exp); // Solidify build as current string
                sopln("Expression3.1 is now: " + exp);
                i--;
            } else if(exp.charAt(i) == '#'){
                build.replace(i, i + 1, "*"); // Replaces special intended char in this instance of exp to indicate multiplication
                exp = build.toString(); // Since StringBuild has changed, update current string
                build = new StringBuilder(exp); // Solidify build as current string
                sopln("Expression3.1 is now: " + exp);
                i++;
            }        
        }
        
        sopln("Prelim #2 complete. Operable expression is now: " + exp);
        
        /* Preliminary loop, grabs outermost parens, unpackages it 
         * EXPECTED BEHAVIOR TO INDUCE RECURSION UNTIL A FINITE REG EXP IS LEFT
         * I.E - 12+5*2 */
        String leftExp = "", midExp = "", rightExp = "";
        for(int i = 0; i < exp.length(); i++){
            if(isOpenBrace(exp.charAt(i))){ // Finds FIRST open brace, if any
                leftExp = build.substring(0, i); // W/o left parens
                
                /* By this rationale, there exist an open brace. Find counterpart
                 * from end to start & unpackage it, if '|' */
                if(exp.charAt(i) == '|'){
                    for(int j = exp.length() - 1; j >= 0; j--){
                        if(Match(exp.charAt(i), exp.charAt(j))){
                            rightExp = build.substring(j + 1, exp.length()); // W/o right parens
                            temp = build.substring(i + 1, j); // Everything inside parens

                            if(temp.length() == 1){ // !!!COULD POSSIBLY BE A PARENS, nullify it
                                if(isOpenBrace(temp.charAt(0)) || isCloseBrace(temp.charAt(0))) temp = "";
                            }

                            midExp = Evaluate(temp); // Evaluate everything within parens
                            
                            try {
                                midExp = String.valueOf(Math.abs(Double.valueOf(midExp)));
                            } catch(Exception e) { // Possible invalid character w/n center section
                                if(!midExp.equals("")) midExp = ';' + midExp + ';'; // Special wrapper to add AND fix later
                            }                            

                            exp = leftExp + midExp + rightExp; // New exp; recusive step
                            sopln("\nExpression is now: " + exp);
                            build = new StringBuilder(exp);
                            i = -1; //Reset i
                            leftExp = ""; midExp = ""; rightExp = "";
                            break; // Out of inner loop
                        }
                    }
                } else {
                    /* By this rationale, there exist an open brace. Find counterpart
                     * from start to "latest" match & unpackage it */
                    matched = -1;
                    for(int j = i + 1; j < exp.length() && (exp.charAt(i) != exp.charAt(j)); j++){
                        if(Match(exp.charAt(i), exp.charAt(j))) matched = j;                        
                    }
                    sopln("the build: " + build.toString());
                    rightExp = build.substring(matched + 1, exp.length()); // W/o right parens
                    temp = build.substring(i + 1, matched); // Everything inside parens

                    if(temp.length() == 1){ // !!!COULD POSSIBLY BE A PARENS, nullify it
                        if(isOpenBrace(temp.charAt(0)) || isCloseBrace(temp.charAt(0))) temp = "";
                    }

                    midExp = Evaluate(temp); // Evaluate everything within parens

                    if(!leftExp.equals("") && !midExp.equals("")){
                        if(leftExp.charAt(leftExp.length() - 1) == '-' && midExp.charAt(0) == '-'){ // Remove excess '-' from leftExp end & midExp beginning
                            temp = "";

                            for(int k = 0; k < leftExp.length() - 1; k++) temp += leftExp.charAt(k);

                            leftExp = temp;

                            temp = "";

                            for(int l = 1; l < midExp.length(); l++) temp += midExp.charAt(l);

                            midExp = temp;
                        }
                    }

                    exp = leftExp + midExp + rightExp; // New exp; recusive step
                    sopln("\nExpression is now: " + exp);
                    build = new StringBuilder(exp);
                    i = -1; //Reset i
                    leftExp = ""; midExp = ""; rightExp = "";
                    //break; // Out of inner loop
                }
            }
        }
        
        sopln("leftExp: " + leftExp + " | midExp: " + midExp + " | rightExp: " + rightExp + " | OP: " + OPERATOR);
        /* By this rationale, exp or subExp should not have any enclosing
         * parens */        
        String output = "", leftOP = "", rightOP = "", excessLS = "", excessRS = "";
        
        for(int i = 0; (POWER || TIMES || DIVIDE || ADD); i++){
            if(exp.charAt(i) == OPERATOR){ //Perfroms operation of the current 'Operator' of the heirarchy
                int saved = i; //Save index of OPERATOR
                
                sop("\nleftOP: " + leftOP);
                // Builds left operand of a binary operation
                for(i--; i >= 0 ; i--){ //START FROM POSITION, BACKWARDS
                    if(isOperator(exp.charAt(i))) break;                    
                    
                    leftOP = String.valueOf(exp.charAt(i)) + leftOP; //copy left side of operating exp
                    sop("\nleftOP: " + leftOP);
                }                
                
                if(i >= 0) excessLS = build.substring(0, i + 1); // Condition protects from ArrayOutOfBoundsIndex                
                
                // Builds right operand of binary expression
                for(i = saved + 1; i <= exp.length() - 1; i++){
                    if(isOperator(exp.charAt(i))) break; //Cannot embed condition in for-loop; Causes error
                    rightOP += exp.charAt(i); //copy right side of operating exp
                    sop("\nrightOP: " + rightOP);
                }

                if(i < exp.length()) excessRS = build.substring(i, exp.length());
                
                output = Operate(exp.charAt(saved), leftOP, rightOP); //Calculate
                if(output.endsWith(".0")) output = String.valueOf((int)Double.parseDouble(output));
                
                sopln("\nExcess Left -> " + excessLS);
                sopln("Excess Right -> " + excessRS);
                sopln("Output -> " + output);               

                exp = excessLS + output + excessRS;
                build = new StringBuilder(exp);
                exp = build.toString();
                sopln("Expression is now: " + exp);
                Reset(); // SEE FUNCTION DETAILS
                leftOP = ""; // Nullify leftOP for reuse
                rightOP = ""; // Nullify rightOP for reuse
                excessLS = "";
                excessRS = "";
                i = -1; // Reset i
                continue; // To top of loop, exec. finale statement             
            }

            // Reaches here if 'i' was not an operator
            if(i == exp.length() - 1){ //INDEX IS @ END OF LOOP
                Switch(OPERATOR); //FALSIFIES OPERATOR AS !FOUND
                OPERATOR = PEMDAS(OPERATOR, exp); //MOVES TO NEW OPERATOR IN THE PEMDAS HEIRARCHY
                i = -1; //Resets index to 0 after incrementation        
            }
        } //END OF LOOP

        Reset();
        if(output.equals("")) output = exp; // Precursor if no op is performed
        sopln("\n\nEXPRESSION IS... " + exp + " returning >> " + output);
        return output;
    }
    
    /* Returns next OPERATOR in the PEMDAS heirarchy */
    private static char PEMDAS(char operator, String equation){ 
        sop("\nINSIDE PEMDAS()");
        int i = 0;
        if(operator == '^'){ // then following operation in heirarchy is ambiguous && can be either '*' || '/'
            for( ; i < equation.length() - 1; i++){ // Finds next operator, L->R
                if(equation.charAt(i) == '*' || equation.charAt(i) == '/'){
                    sopln("\nAmbiguous operator found: '" + equation.charAt(i) + "' is next"); 
                    break; // If found
                }
            }            
        }

        switch(operator){
            case '^' : return ((equation.charAt(i) == '*' || equation.charAt(i) == '/') ? equation.charAt(i) : '*' /* by default */);
            case '*' : return '/';
            case '/' : return '+';
            default : return '+';
        }
    }

    /* Inverts status of global variable */
    private static void Switch(char symbol){ 
        sop("\nINSIDE SWITCH()");
        if(symbol == '|') ABS = !ABS;
        else if(symbol == '^') POWER = !POWER;
        else if(symbol == '*') TIMES = !TIMES;
        else if(symbol == '/') DIVIDE = !DIVIDE;
        else if(symbol == '+') ADD = !ADD;            
    }

    private void Status(){ //Prints status of global variables
        sop("\nINSIDE STATUS()\n");
        sopln("ABS:: " + ABS);
        sopln("POWER:: " + POWER);
        sopln("TIMES:: " + TIMES);
        sopln("DIVIDE:: " + DIVIDE);
        sopln("ADD:: " + ADD);
        sopln("OPERATOR:: '" + OPERATOR + "'");
    }

    /* Default global variables */
    private static void Reset(){
        sopln("\nINSIDE RESET()");
        ABS = false;
        POWER = true;
        TIMES = true;
        DIVIDE = true;
        ADD = true;
        OPERATOR = '^'; //Highest priority operation in PEMDAS
    }
    
    private static String Operate(char sign, String left, String right){
        if(left.equals("") && right.equals("")) return "";
        sop("\nOperation >> ");
        //WARNING: No parameters of any <null> operands shall be concatenated
        if(sign == '^'){ 
            try { 
                sop("Power: base(" + left + ")^exp(" + right + ")\n");
                return (String.valueOf(Math.pow(Double.valueOf(left), Double.valueOf(right))));
            } catch(Exception e) { // Possible variable or other illegal char
                if(left.equals("") || right.equals("")){
                    sopln("CRITICAL SYNTACTICAL ERROR! " + e.toString());
                    exit();
                } else return ('?' + left + sign + '?' + right + "$$"); // i.e will be (6^(9)) in Finish()
            }
        }
        else if(sign == '*'){ 
            try { 
                sop("Multiplication: left(" + left + ")*right(" + right + ")\n"); 
                return (String.valueOf((Double.valueOf(left))*(Double.valueOf(right))));
            } catch(Exception e) { // Possible variable or other illegal char
                if(left.equals("") && !right.equals("")) return right;
                else if(!left.equals("") && right.equals("")) return left;
                else return (left + right); 
            }
        }
        else if(sign == '/'){ 
            try { 
                sop("Division: numerator(" + left + ")/denominator(" + right + ")\n"); 
                return (String.valueOf((Double.valueOf(left))/(Double.valueOf(right))));
            } catch(Exception e) { // Possible variable or other illegal char
                if(left.equals("") || right.equals("")){
                    sop("CRITICAL SYNTACTICAL ERROR! " + e.toString());
                    exit();
                } else return ('?' + left + sign + right + '$'); // Special wrapping, will fix later
            }
        }
        else if(sign == '+'){ 
            try { 
                sop("Addition: left(" + left + ") + right(" + right + ")\n"); 
                if(left.equals("-")) left = "";
                if(right.equals("-")) right = "";
                return (String.valueOf((Double.valueOf(left)) + (Double.valueOf(right)))); 
            } catch(Exception e) { // Possible variable or other illegal char
                if(right.equals("") || left.equals("")) return (left + right); 
                else return ('?' + left + sign + right + '$'); // Special wrapping, will fix later
            }
        }
        
        return "";// ('@' + left + sign + right + '$'); // STATEMENT NEVER REACHED
    }
    
//    private Object LikeTerm(String var, int i){ //Returns a whole "like" term
//        Stack now = new Stack();
//        //WARNING: Positioning of i should ALWAYS yield any characters other than explicit operands
//        String term = "";
//        char lastTerm = ' ';
//        sopln("INSIDE LikeTerm(), the string is " + var + " with length = " + var.length() + " & var.charAt(" + i + ") = '" + var.charAt(i) + "'");
//        
//        if(var.charAt(i) == ' '){ 
//            for( ; var.charAt(i) == ' ' || i < var.length() - 1; sopln("i = " + i++)){}
//        } //Loops if initial character if empty
//        sopln("\nThe string length is " + var.length() + "; index of var.charAt(" + i + ") = " + var.charAt(i));
//        if(var.charAt(i) == '*' || var.charAt(i) == '/'){ //If initial reading is an operand * or /: i.e. (after operand, +|-|*|/)_*8 
//            sopln("Improper syntax determined by LikeTerm()! Expression confusing or ambiguous!! Termination evaluation!!!");
//            sopln("STACK DISPLAY FROM LIKETERM()");
//            now.showAll();
//            exit();
//        }
//        
//        while(i < var.length() && (!SpecialChar(var.charAt(i)) || var.charAt(i) == '*' || var.charAt(i) == '/')){
//            if(var.charAt(i) == ' ' || ((lastTerm == ' ' || lastTerm == '+') && var.charAt(i) == '+')) i++; //Skips any gaps in-between characters: i.e 5_5 -> '55'; 1st iteration is always skipped
//            else if(!Operand(lastTerm) && Operand(var.charAt(i))) break; //"Whole" term reached: i.e. start -> 5_-5;
//            else{
//                term = term + var.charAt(i);
//                lastTerm = var.charAt(i++);    
//            }
//        }
//        
//        Adjust(i);
//        sopln("The lastTerm was '" + lastTerm + "' & returning term is '" + term + "' with index being = " + i);
//        return (Object)term;
//    }
    
    /* Verifies if there is a matching pair of braces */
    private static Boolean Match(char left, char right){ 
        switch(left){
            case '(' : return (right == ')');
            case '[' : return (right == ']');
            case '{' : return (right == '}');
            case '|' : return (right == '|');
            default: return false;
        }
    }
    
    /* Verifies if there is a binary operater found */
    private static Boolean isOperator(char token){
        return (token == '^' || token == '/' || token == '*' || token == '+');
    }
    
    /* Verifies if there is an open brace */
    private static Boolean isOpenBrace(char token){ 
        switch(token){
            case '(' : return true;
            case '[' : return true;
            case '{' : return true;
            case '|' : return !ABS; // Varies
            default: return false; 
        } 
    }
    
    /* Verifies if there is an open brace */
    private static Boolean isCloseBrace(char token){ 
        switch(token){
            case ')' : return true;
            case ']' : return true;
            case '}' : return true;
            case '|' : return ABS; // Varies
            default: return false; 
        } 
    }
    
    /* Returns compliment of character */
    private char Flip(char token){ 
        switch(token){
            case ')' : return '(';
            case ']' : return '[';
            case '}' : return '{';
            case '(' : return ')';
            case '[' : return ']';
            case '{' : return '}';
            case '<' : return '>';
            case '>' : return '<';
            default: return token;
        }        
    }
    
    /* Verifies if character is a brace */
    private boolean isBrace(char token){ 
        return (token == '(' || token == ')' || token == '[' || token == ']' || 
                token == '{' || token == '}' || token == '|');       
    }
    
    public double Round(double number, int decimal){        
        double doa = Math.pow(10, decimal); //degree of accuracy
        
        if(((number*doa) - (int)(number*doa)) >= .5) return ((int)(number*doa) + 1)/doa;
        else {
            if(number < 0) return ((int)(number*doa) - 1)/doa;
            else return ((int)(number*doa))/doa;
        }
    }
    
    private double Round(double number){        
        double doa = Math.pow(10, SIG_FIG); //degree of accuracy
        
        if(((number*doa) - (int)(number*doa)) >= .5) return ((int)(number*doa) + 1)/doa;
        else {
            if(number < 0) return ((int)(number*doa) - 1)/doa;
            else return ((int)(number*doa))/doa;
        }
    }
    
    public static void main(String[] args){
        // TODO code application logic here
        // Disallow user usage of @, #, $, ?, ;
//        int x = 2147483647; // <- largest rep # by cpu 2^31 - 1
        
        String equation = "5(4 + (5+3)6/2)"; //
        sopln("Equation: " + equation);
        sop("\nAnswer = " + Evaluate(Parse(equation)) + "\n");
    }
    
    public Expression(){}
    
    public Expression(int accuracy){
        SIG_FIG = accuracy;
    }
    
    public static void sop(String stmnt){
        System.out.print(stmnt);
    }
    
    public static void sopln(String stmnt){
        System.out.println(stmnt);
    }
    
    private static void exit(){
        System.exit(0);
    }
}