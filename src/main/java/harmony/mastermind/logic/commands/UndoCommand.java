package harmony.mastermind.logic.commands;

import java.util.EmptyStackException;
// @@author A0138862W
public class UndoCommand extends Command{
    
    public static final String COMMAND_WORD = "undo";
    
    public static final String MESSAGE_SUCCESS = "Undo successfully.";
    public static final String MESSAGE_EMPTY_COMMAND_HISTORY = "There's no more action available to undo.";
    public static final String MESSAGE_COMMAND_NOT_UNDOABLE = "This command is not undoable";
    public static final String COMMAND_DESCRIPTION = "Undo an action";

    @Override
    //@@author A0138862W
    public CommandResult execute() {

        try{
            // All Command supports undo operation must implement Undoable interface
            
            // execute the undo strategy implemented by the underlying command
            CommandResult undoResult = model.undo();
            
            // display successful message and the details of the undo operations
            return new CommandResult(COMMAND_WORD, 
                    MESSAGE_SUCCESS + "\n" +
                    "=====Undo Details=====\n" +
                    undoResult.feedbackToUser + "\n"+
                    "==================");
        }catch(EmptyStackException ex){
            return new CommandResult(COMMAND_WORD, MESSAGE_EMPTY_COMMAND_HISTORY);
        }
    }
    
    

}

