package dendron.machine;

import java.util.List;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import dendron.Errors;

/**
 * An abstraction of a computing machine that reads instructions
 * and executes them. It has an instruction set, a symbol table
 * for variables (instead of general-purpose memory), and a
 * value stack on which calculations are performed.
 *
 * (Everything is static to avoid the need to master the subtleties
 * of nested class instantiation or to pass the symbol table and
 * stack into every instruction when it executes.)
 *
 *
 * @author James Heliotis
 * @author William Johnson
 */
public class Machine {

    private Machine() {}
    public static interface Instruction {
        /**
         * Run this instruction on the Machine, using the Machine's
         * value stack and symbol table.
         */
        void execute();

        /**
         * Show the instruction using text so it can be understood
         * by a person.
         * @return a short string describing what this instruction will do
         */
        @Override
        String toString();
    }

    private static Map< String, Integer > table = null;
    private static Stack< Integer > stack = null;

    /**
     * Reset the Machine to a pristine state.
     * @see Machine#execute
     */
    private static void reset() {
        stack = new Stack<>();
        table = new HashMap<>();
    }



    /**
     * Generate a listing of a program on standard output by
     * calling the toString() method on each instruction
     * contained therein, in order.
     *
     * @param program the list of instructions in the program
     */

    public static void displayInstructions(List< Machine.Instruction > program ) {
        System.out.println( "\nCompiled code:" );
        for ( Machine.Instruction instr: program ) {
            System.out.println( instr );
        }
        System.out.println();
    }

    /**
     * Run a "compiled" program by executing in order each instruction
     * contained therein.
     * Report on the final size of the stack (should normally be empty)
     * and the contents of the symbol table.
     * @param program a list of Machine instructions
     */

    public static void execute( List< Instruction > program ) {
        reset();
        System.out.println("Executing compiled code...");
        for ( Instruction instr: program ) {
            instr.execute();
            if (instr.toString().equals("PRINT")){
                Machine.Print p =(Machine.Print) instr;
                System.out.println("*** "+p.getValue());
            }


        }
        System.out.println( "Machine: execution ended with " +
                stack.size() + " items left on the stack." );
        System.out.println();
        Errors.dump( table );
    }

    /**
     * The ADD instruction
     */
    public static class Add implements Instruction {
        /**
         * Run the microsteps for the ADD instruction.
         */
        @Override
        public void execute() {
            int op2 = stack.pop();
            int op1 = stack.pop();
            stack.push( op1 + op2 );
        }

        /**
         * Show the ADD instruction as plain text.
         * @return "ADD"
         */
        @Override
        public String toString() {
            return "ADD";
        }
    }

    /**
     * The STORE instruction
     */
    public static class Store implements Instruction {
        /** stores name of target variable */
        private String name;

        /**
         * Create a STORE instruction
         * @param ident the name of the target variable
         */
        public Store( String ident ) {
            this.name = ident;
        }
        /**
         * Run the microsteps for the STORE instruction.
         */
        @Override
        public void execute() {
            table.put( this.name, stack.pop() );
        }
        /**
         * Show the STORE instruction as plain text.
         * @return "STORE" followed by the target variable name
         */
        @Override
        public String toString() {
            return "STORE " + this.name;
        }

        public String getName() {
            return name;
        }
    }
    /**
     * The PUSH instruction
     */
    public static class PushConst implements Instruction{
        /** stores constant of target variable */
        private int constant;
        /**
         * Create a PUSH instruction
         * @param constant the constant of the target variable
         */
        public PushConst(int constant){
            this.constant = constant;
        }
        @Override
        /**
         * Run the microsteps for the PUSH instruction.
         */
        public void execute() {
            stack.push(getConstant());
        }

        /**
         * Show the PUSH instruction as plain text.
         * @return "PUSH" followed by the target variable constant
         */
        @Override
        public String toString() {
            return "PUSH " + getConstant();
        }

        /**
         * Show the constant of the variable
         * @return the target variable constant
         */
        public int getConstant() {
            return constant;
        }
    }

    /**
     * The NEGATE instruction
     */
    public static class Negate implements Instruction{
        /**
         * Run the microsteps for the NEGATE instruction.
         */
        @Override
        public void execute() {
            int op1 = stack.pop();
            op1 = op1 * -1;
            stack.push( op1 );
        }

        /**
         * Show the NEGATE instruction as plain text.
         * @return "NEG"
         */
        @Override
        public String toString() {
            return "NEG";
        }
    }

    /**
     * The PRINT instruction
     */
    public static class Print implements Instruction{
        private int value;
        /**
         * Create a PRINT instruction
         */
        public Print(int value){
            this.value = value;
        }
        /**
         * Run the microsteps for the PRINT instruction.
         */
        @Override
        public void execute() {
            int op2 = stack.pop();
            this.value = op2;
        }

        public int getValue() {
            return value;
        }

        /**
         * Show the PRINT instruction as plain text.
         * @return "PRINT"
         */
        @Override
        public String toString() {
            return "PRINT";
        }
    }

    /**
     * The SQUAREROOT instruction
     */
    public static class SquareRoot implements Instruction{
        /**
         * Run the microsteps for the SQUAREROOT instruction.
         */
        @Override
        public void execute() {
            double op1 = stack.pop();
            stack.push((int) Math.pow(op1,0.5));
        }

        /**
         * Show the SQRT instruction as plain text.
         * @return "SQRT"
         */
        @Override
        public String toString() {
            return "SQRT";
        }
    }

    /**
     * The MULTIPLY instruction
     */
    public static class Multiply implements Instruction{
        /**
         * Run the microsteps for the MULTIPLY instruction.
         */
        @Override
        public void execute() {
            int op2 = stack.pop();
            int op1 = stack.pop();
            stack.push( op1 * op2 );
        }

        /**
         * Show the MULTIPLY instruction as plain text.
         * @return "MUL"
         */
        @Override
        public String toString() {
            return "MUL";
        }
    }

    /**
     * The LOAD instruction
     */
    public static class Load implements Instruction{
        /** stores varname of target variable */
        private String varname;
        /**
         * Create a LOAD instruction
         * @param load the name of the target variable
         */
        public Load(String load){
            this.varname = load;
        }
        /**
         * Run the microsteps for the LOAD instruction.
         */
        @Override
        public void execute() {
            int i = table.get(getVarname());
            stack.push(i);
        }

        /**
         * Show the LOAD instruction as plain text.
         * @return "LOAD" followed by the target variable varname
         */
        @Override
        public String toString() {
            return "LOAD " + getVarname();
        }

        /**
         * Show the varname of the variable
         * @return the target variable varname
         */
        public String getVarname() {
            return this.varname;
        }
    }

    /**
     * The SUBTRACT instruction
     */
    public static class Subtract implements Instruction{
        /**
         * Run the microsteps for the SUBTRACT instruction.
         */
        @Override
        public void execute() {
            int op2 = stack.pop();
            int op1 = stack.pop();
            stack.push( op1 - op2 );
        }

        /**
         * Show the NEGATE instruction as plain text.
         * @return "NEG"
         */
        @Override
        public String toString() {
            return "SUB";
        }
    }

    /**
     * The DIVIDE instruction
     */
    public static class Divide implements Instruction{
        /**
         * Run the microsteps for the DIVIDE instruction.
         */
        @Override
        public void execute() {
            int op2 = stack.pop();
            int op1 = stack.pop();
            stack.push( op1 / op2 );
        }

        /**
         * Show the DIVIDE instruction as plain text.
         * @return "DIV"
         */
        @Override
        public String toString() {
            return "DIV";
        }
    }
}
