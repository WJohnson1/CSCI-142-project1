package dendron.tree;

import dendron.machine.Machine;
import dendron.tree.ActionNode;
import dendron.tree.ExpressionNode;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Operations that are done on a Dendron code parse tree.
 *
 * @author William Johnson
 */
public class ParseTree implements ExpressionNode, ActionNode {
    private List<Machine.Instruction> instructions;
    private Map<String, Integer> symTab;
    private List<String> program;

    /**
     * Parse the entire list of program tokens. The program is a
     * sequence of actions (statements), each of which modifies something
     * in the program's set of variables. The resulting parse tree is
     * stored internally.
     * @param program the token list (Strings)
     */
    public ParseTree( List< String > program ) {
        this.instructions = new ArrayList<>();
        this.symTab = new HashMap<>();
        int start = 0;
        int end = program.size();
        for (int i = 1; i<program.size();i++){
            if (program.get(i).equals(":=") || program.get(i).equals("@")){
                parseExpr(program.subList(start,i));
                start = i;
            }
        }
        parseExpr(program.subList(start,end));
        this.program = program;
    }
    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i =0;
        if (str.charAt(0) == '-'){
            if (length ==1){
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
    /**
     * Parse the next action (statement) in the list.
     * (This method is not required, just suggested.)
     * @param program the list of tokens
     * @return a parse tree for the action
     */
    private ActionNode parseAction( List< String > program ) {
        if (program.get(0).equals("@")){
            if (isInteger(program.get(1))) {
                //Machine.Instruction p = new Machine.PushConst(Integer.parseInt(program.get(1)));
                //this.instructions.add(p);
            }
            else if(this.symTab.containsKey(program.get(1))){
                Machine.Load l = new Machine.Load(program.get(1));
                this.instructions.add(l);
            }
            Machine.Instruction pr = new Machine.Print();
            this.instructions.add(pr);
        }
        else if(program.get(0).equals("_")){
            int num2 = 0;
            if (isInteger(program.get(1))) {
                //Machine.PushConst p = new Machine.PushConst(Integer.parseInt(program.get(1)));
                //this.instructions.add(p);
                Machine.Negate n = new Machine.Negate();
                this.instructions.add(n);
                num2 = -1 * Integer.parseInt(program.get(1));
            }
            else if(this.symTab.containsKey(program.get(1))) {
                Machine.Load l = new Machine.Load(program.get(1));
                this.instructions.add(l);
                Machine.Negate n = new Machine.Negate();
                this.instructions.add(n);
                num2 = -1 * this.symTab.get(program.get(1));
                this.symTab.put(program.get(1),this.symTab.get(program.get(1))*-1);

            }
            ArrayList<String> program1 = new ArrayList<>(program);
            program1.add(0,String.valueOf(num2));
            program1.remove(1);
            program1.remove(1);
            List<String> program2 = new ArrayList<>(program1);
            this.program = program2;

        }
        else if(program.get(0).equals("/")){
            int count = 0;
            int num = 0;
            int num1 = 0;
            boolean a =true;
            for (int i = 1; i<program.size();i++){
                if (isInteger(program.get(i)) && count < 1 && !this.symTab.containsValue(program.get(i))) {
                    //Machine.PushConst p = new Machine.PushConst(Integer.parseInt(program.get(i)));
                    num = Integer.parseInt(program.get(i));
                    //this.instructions.add(p);
                    if (program.get(i-1).equals("_")){
                        List<String> p1 = program.subList(0,i-1);
                        ArrayList<String> p2 = new ArrayList<>(p1);
                        parseExpr(program.subList(i-1,program.size()));
                        p2.addAll(p2.size(),this.program);
                        program = p2;
                        this.program = p2;
                        num*=-1;
                    }
                    count++;
                }
                else if (isInteger(program.get(i)) && count < 2 && !this.symTab.containsValue(program.get(i))) {
                    num1 = Integer.parseInt(program.get(i));
                    if (!a) {
                        Machine.PushConst p = new Machine.PushConst(Integer.parseInt(program.get(i)));
                        this.instructions.add(p);
                    }
                    if (program.get(i-1).equals("_")){
                        List<String> p1 = program.subList(0,i-1);
                        ArrayList<String> p2 = new ArrayList<>(p1);
                        parseExpr(program.subList(i-1,program.size()));
                        p2.addAll(p2.size(),this.program);
                        program = p2;
                        this.program = p2;
                        num1*=-1;
                    }
                    count++;
                }
                else if(this.symTab.containsKey(program.get(i))&& count < 1){
                    Machine.Load l = new Machine.Load(program.get(i));
                    num = this.symTab.get(program.get(i));
                    if (program.get(i-1).equals("_")){
                        List<String> p1 = program.subList(0,i-1);
                        ArrayList<String> p2 = new ArrayList<>(p1);
                        parseExpr(program.subList(i-1,program.size()));
                        p2.addAll(p2.size(),this.program);
                        program = p2;
                        this.program = p2;
                        num*=-1;
                    }
                    this.instructions.add(l);
                    a =false;
                    count++;
                }
                else if(this.symTab.containsKey(program.get(i))&& count < 2){
                    Machine.Load l = new Machine.Load(program.get(i));
                    num1 = this.symTab.get(program.get(i));
                    this.instructions.add(l);
                    if (program.get(i-1).equals("_")){
                        List<String> p1 = program.subList(0,i-1);
                        ArrayList<String> p2 = new ArrayList<>(p1);
                        parseExpr(program.subList(i-1,program.size()));
                        p2.addAll(p2.size(),this.program);
                        program = p2;
                        this.program = p2;
                        num1*=-1;
                    }
                    count++;
                }
            }
            Machine.Divide d = new Machine.Divide();
            this.instructions.add(d);
            int num2 = num/num1;
            ArrayList<String> program1 = new ArrayList<>(program);
            program1.add(0,String.valueOf(num2));
            program1.remove(1);
            program1.remove(1);
            program1.remove(1);
            List<String> program2 = new ArrayList<>(program1);
            this.program = program2;
        }
        else if(program.get(0).equals("*")){
            int count = 0;
            int mul = 1;
            for (int i = 0; i<program.size();i++){
                if (isInteger(program.get(i)) && count<2 && !this.symTab.containsValue(program.get(i))){
                    //Machine.PushConst p = new Machine.PushConst(Integer.parseInt(program.get(i)));
                    //this.instructions.add(p);
                    mul*=Integer.parseInt(program.get(i));
                    if (program.get(i-1).equals("_")){
                        List<String> p1 = program.subList(0,i-1);
                        ArrayList<String> p2 = new ArrayList<>(p1);
                        parseExpr(program.subList(i-1,program.size()));
                        p2.addAll(p2.size(),this.program);
                        program = p2;
                        this.program = p2;
                        mul = mul * -1;
                    }
                    count++;
                }
                else if(this.symTab.containsKey(program.get(i))&& count < 2){
                    Machine.Load l = new Machine.Load(program.get(i));
                    this.instructions.add(l);
                    mul*=this.symTab.get(program.get(i));
                    if (program.get(i-1).equals("_")){
                        List<String> p1 = program.subList(0,i-1);
                        ArrayList<String> p2 = new ArrayList<>(p1);
                        parseExpr(program.subList(i-1,program.size()));
                        p2.addAll(p2.size(),this.program);
                        program = p2;
                        this.program = p2;
                        mul = mul * -1;
                    }
                    count++;
                }
            }
            Machine.Multiply m = new Machine.Multiply();
            this.instructions.add(m);
            ArrayList<String> program1 = new ArrayList<>(program);
            program1.add(0,String.valueOf(mul));
            program1.remove(1);
            program1.remove(1);
            program1.remove(1);
            List<String> program2 = new ArrayList<>(program1);
            this.program = program2;
        }
        else if(program.get(0).equals("+")){
            int count = 0;
            int num = 0;
            for (int i = 1; i<program.size();i++){
                if (isInteger(program.get(i)) && count<2){
                    //Machine.PushConst p = new Machine.PushConst(Integer.parseInt(program.get(i)));
                    //this.instructions.add(p);
                    int num1 = Integer.parseInt(program.get(i));
                    count++;
                    if (program.get(i-1).equals("_")){
                        List<String> p1 = program.subList(0,i-1);
                        ArrayList<String> p2 = new ArrayList<>(p1);
                        parseExpr(program.subList(i-1,program.size()));
                        p2.addAll(p2.size(),this.program);
                        program = p2;
                        this.program = p2;
                        num1*= -1;
                    }
                    num = num + num1;
                }
                else if (this.symTab.containsKey(program.get(i))&& count < 2){
                    Machine.Load l = new Machine.Load(program.get(i));
                    this.instructions.add(l);
                    int num1 = this.symTab.get(program.get(i));
                    num = num + num1;
                    if (program.get(i-1).equals("_")){
                        List<String> p1 = program.subList(0,i-1);
                        ArrayList<String> p2 = new ArrayList<>(p1);
                        parseExpr(program.subList(i-1,program.size()));
                        p2.addAll(p2.size(),this.program);
                        program = p2;
                        this.program = p2;
                        num1*= -1;
                    }
                    count++;
                }
            }
            Machine.Add a = new Machine.Add();
            this.instructions.add(a);
            ArrayList<String> program1 = new ArrayList<>(program);
            program1.add(0,String.valueOf(num));
            program1.remove(1);
            program1.remove(1);
            program1.remove(1);
            List<String> program2 = new ArrayList<>(program1);
            this.program = program2;
        }
        else if(program.get(0).equals("-")) {
            int count = 0;
            int num = 0;
            int num1 = 0;
            for (int i = 1; i < program.size(); i++) {
                if (isInteger(program.get(i)) && count < 1) {
                    //Machine.PushConst p = new Machine.PushConst(Integer.parseInt(program.get(i)));
                    num = Integer.parseInt(program.get(i));
                    //this.instructions.add(p);
                    count++;
                    if (program.get(i-1).equals("_")){
                        List<String> p1 = program.subList(0,i-1);
                        ArrayList<String> p2 = new ArrayList<>(p1);
                        parseExpr(program.subList(i-1,program.size()));
                        p2.addAll(p2.size(),this.program);
                        program = p2;
                        this.program = p2;
                        num*= -1;
                    }
                }
                else if (isInteger(program.get(i)) && count < 2) {
                    //Machine.PushConst p = new Machine.PushConst(Integer.parseInt(program.get(i)));
                    num1 = Integer.parseInt(program.get(i));
                    //this.instructions.add(p);
                    count++;
                    if (program.get(i-1).equals("_")){
                        List<String> p1 = program.subList(0,i-1);
                        ArrayList<String> p2 = new ArrayList<>(p1);
                        parseExpr(program.subList(i-1,program.size()));
                        p2.addAll(p2.size(),this.program);
                        program = p2;
                        this.program = p2;
                        num1*= -1;
                    }
                }
                else if (this.symTab.containsKey(program.get(i))&& count < 1){
                    Machine.Load l = new Machine.Load(program.get(i));
                    this.instructions.add(l);
                    num = this.symTab.get(program.get(i));
                    if (program.get(i-1).equals("_")){
                        List<String> p1 = program.subList(0,i-1);
                        ArrayList<String> p2 = new ArrayList<>(p1);
                        parseExpr(program.subList(i-1,program.size()));
                        p2.addAll(p2.size(),this.program);
                        program = p2;
                        this.program = p2;
                        num*= -1;
                    }
                    count++;

                }
                else if (this.symTab.containsKey(program.get(i))&& count < 2){

                    num1 = this.symTab.get(program.get(i));
                    if (program.get(i-1).equals("_")){
                        List<String> p1 = program.subList(0,i-1);
                        ArrayList<String> p2 = new ArrayList<>(p1);
                        parseAction(program.subList(i-1,program.size()));

                        p2.addAll(p2.size(),this.program);
                        program = p2;
                        this.program = p2;
                        num1*= -1;
                    }
                    else{
                        Machine.Load l = new Machine.Load(program.get(i));
                        this.instructions.add(l);
                    }
                    count++;
                }
            }
            int num2 = num - num1;
            Machine.Subtract sub = new Machine.Subtract();
            this.instructions.add(sub);
            ArrayList<String> program1 = new ArrayList<>(program);
            program1.add(0,String.valueOf(num2));
            program1.remove(1);
            program1.remove(1);
            program1.remove(1);
            List<String> program2 = new ArrayList<>(program1);
            this.program = program2;
        }
        else if(program.get(0).equals("#")){
            int num = 0;
            if (isInteger(program.get(1))) {
                //Machine.PushConst p = new Machine.PushConst(Integer.parseInt(program.get(1)));
                num = (int) Math.pow(Double.parseDouble(program.get(1)),0.5);
                //this.instructions.add(p);
            }
            else if(this.symTab.containsKey(program.get(1))){
                Machine.Load l = new Machine.Load(program.get(1));
                num = (int) Math.pow(Double.valueOf(String.valueOf(this.symTab.get(program.get(1)))),0.5);
                this.instructions.add(l);
            }
            Machine.SquareRoot n = new Machine.SquareRoot();
            this.instructions.add(n);
            ArrayList<String> program1 = new ArrayList<>(program);
            program1.add(0,String.valueOf(num));
            program1.remove(1);
            program1.remove(1);
            List<String> program2 = new ArrayList<>(program1);
            this.program = program2;
        }
        return null;
    }

    /**
     * Parse the next expression in the list.
     * (This method is not required, just suggested.)
     * @param program the list of tokens
     * @return a parse tree for this expression
     */
    private ExpressionNode parseExpr( List< String > program ) {
        int count = 2;
        int count1 = 1;
        boolean a = false;
        if (program.get(0).equals("@") == false){
            if (program.size() == 3) {
                a = true;
            }
            while (program.size() > 3 && count < 100) {
                if (count >= program.size()) {
                    count = 2;
                    count1++;
                }
                if (isInteger(program.get(count)) || this.symTab.containsKey(program.get(count))) {
                    if (!isInteger(program.get(count - 1)) && !this.symTab.containsKey(program.get(count - 1))) {
                        if (isInteger(program.get(count)) && !program.get(count-1).equals("#")){
                            Machine.PushConst p = new Machine.PushConst(Integer.parseInt(program.get(count)));
                            this.instructions.add(p);
                        }
                        List<String> p1 = program.subList(0, count - 1);
                        ArrayList<String> p2 = new ArrayList<>(p1);
                        parseAction(program.subList(count - 1, program.size()));
                        p2.addAll(p2.size(), this.program);
                        program = p2;
                        this.program = p2;
                        count -= count1;
                    }
                }
                count++;

            }
            if (a) {
                Machine.PushConst p = new Machine.PushConst(Integer.parseInt(program.get(2)));
                this.instructions.add(p);
            }



            this.symTab.put(program.get(1), Integer.parseInt(program.get(2)));
            Machine.Store s = new Machine.Store(program.get(1));
            this.instructions.add(s);
            ArrayList<String> p3 = new ArrayList<>(program);
            p3.remove(0);
            p3.remove(0);
            p3.remove(0);
            this.program = p3;
        }
        else{
            while (program.size() > 2 && count < 100) {
                if (count >= program.size()) {
                    count = 0;
                    count1++;
                }
                if (isInteger(program.get(count)) || this.symTab.containsKey(program.get(count))) {
                    if (!isInteger(program.get(count - 1)) && !this.symTab.containsKey(program.get(count - 1))) {
                        List<String> p1 = program.subList(0, count - 1);
                        ArrayList<String> p2 = new ArrayList<>(p1);
                        parseAction(program.subList(count - 1, program.size()));
                        p2.addAll(p2.size(), this.program);
                        program = p2;
                        this.program = p2;
                        count -= count1;
                    }
                }
                count++;

            }
            parseAction(program);
            ArrayList<String> p3 = new ArrayList<>(program);
            p3.remove(0);
            p3.remove(0);
            this.program = p3;
        }
        return null;
    }
    /**
     * Print the program the tree represents in a more typical
     * infix style, and with one statement per line.
     * @see dendron.tree.ActionNode#infixDisplay()
     */
    public void displayProgram() {
        this.infixDisplay();
    }

    /**
     * Run the program represented by the tree directly
     * @see dendron.tree.ActionNode#execute(Map)
     */
    public void interpret() {
        System.out.println("Interpreting the parse tree...");
        for (Machine.Instruction a: this.instructions){
            if (a.toString().equals("PRINT") ){
                Machine.Print b = (Machine.Print) a;
                System.out.println("=== "+b.getValue());
            }
        }
        System.out.println("Interpretation complete");
    }

    /**
     * Build the list of machine instructions for
     * the program represented by the tree.
     * @return the Machine.Instruction list
     * @see Machine.Instruction#execute()
     */
    public List< Machine.Instruction > compile() {
        return this.instructions;
    }

    /**
     *
     * @param symTab the table where variable values are stored
     */
    @Override
    public void execute(Map<String, Integer> symTab) {
        System.out.println("truth");
    }

    /**
     *
     * @param symTab symbol table, if needed, to fetch variable values
     * @return
     */
    @Override
    public int evaluate(Map<String, Integer> symTab) {
        return 0;
    }

    /**
     *
     */
    @Override
    public void infixDisplay() {
        int start = 0;
        int end = program.size();
        for (int i = 1; i<program.size();i++){
            if (program.get(i).equals(":=") || program.get(i).equals("@")) {
                List<String> e = program.subList(start, i);
                ArrayList<String> equation = new ArrayList<>(e);
                if (equation.get(0).equals(":=")){
                    System.out.print(equation.get(1)+ " ");
                    System.out.print(equation.get(0)+ " (");
                    for (int j = 2;j<equation.size();j++){
                        System.out.print(equation.get(j));
                        if (!equation.get(j).equals("_")){
                            System.out.print(" ");
                        }
                    }
                }
                else{
                    System.out.println("PRINT "+equation.get(i);
                }
                start = i;
            }
        }
        System.out.println(program.subList(start,end));
    }

    /**
     *
     * @return
     */
    @Override
    public List<Machine.Instruction> emit() {
        return null;
    }

    public static void main(String[] args){
        if (args.length == 1) {
            try(FileInputStream fileStr = new FileInputStream( args[0])){
                List<String> program = new ArrayList<>();
                Scanner in = new Scanner( fileStr );
                while(in.hasNextLine()){
                    String expression = in.nextLine();
                    program.add(expression);
                }
                ParseTree p = new ParseTree(program);
                //p.interpret();
                Machine.execute(p.instructions);
                //p.infixDisplay();
            }
            catch( IOException ioe ) {
                System.err.println( "Could not open file " + args[0] );
            }
        }
        else{
            System.out.println("Illegal number of arguments");
        }
    }
}
