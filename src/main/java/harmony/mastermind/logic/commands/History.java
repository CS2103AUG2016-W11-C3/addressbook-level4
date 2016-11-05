package harmony.mastermind.logic.commands;

import java.util.ArrayList;
import java.util.Stack;

import harmony.mastermind.memory.GenericMemory;

public class History { 
    private static ArrayList<GenericMemory> current = null;
    private static Stack<ArrayList<GenericMemory>> back = new Stack<ArrayList<GenericMemory>>();
    private static Stack<ArrayList<GenericMemory>> forward = new Stack<ArrayList<GenericMemory>>();
    
}