package harmony.mastermind.logic.commands;

import java.util.ArrayList;

import harmony.mastermind.commons.core.Messages;
import harmony.mastermind.commons.core.UnmodifiableObservableList;
import harmony.mastermind.memory.GenericMemory;
import harmony.mastermind.memory.Memory;
import harmony.mastermind.model.task.ArchiveTaskList;
import harmony.mastermind.model.task.ReadOnlyTask;
import harmony.mastermind.model.task.Task;
import harmony.mastermind.model.task.UniqueTaskList.DuplicateTaskException;
import harmony.mastermind.model.task.UniqueTaskList.TaskNotFoundException;

/**
 * Deletes a task identified using it's last displayed index from the task
 * manager.
 */
public class DeleteCommand extends Command implements Undoable, Redoable {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
                                               + ": Deletes the task identified by the index number used in the last task listing.\n"
                                               + "Parameters: INDEX (must be a positive integer)\n"
                                               + "Example: "
                                               + COMMAND_WORD
                                               + " 1";

    public static final String MESSAGE_DELETE_TASK_SUCCESS = "Deleted Task: %1$s";
    public static final String COMMAND_FORMAT = COMMAND_WORD + " INDEX";
    public static final String COMMAND_DESCRIPTION = "Deletes the task identified by the index number";

    public static final String MESSAGE_UNDO_SUCCESS = "[Undo Delete Command] Task added: %1$s";
    public static final String MESSAGE_REDO_SUCCESS = "[Redo Delete Command] Deleted Task: %1$s";

    public final int targetIndex;

    private ReadOnlyTask toDelete;
    
    public static GenericMemory detailedView = null;
    public static ArrayList<GenericMemory> listView = null;
    public static String listName = null;
    Memory memory;

    public DeleteCommand(int targetIndex, Memory mem) {
        this.targetIndex = targetIndex;
        this.memory = mem;
    }

    @Override
    public CommandResult execute() {
        try {
            executeDelete();

            model.pushToUndoHistory(this);

            model.clearRedoHistory();
            
            return new CommandResult(COMMAND_WORD, String.format(MESSAGE_DELETE_TASK_SUCCESS, toDelete));

        } catch (TaskNotFoundException | IndexOutOfBoundsException | ArchiveTaskList.TaskNotFoundException tnfe) {
            
            return new CommandResult(COMMAND_WORD, Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);

        }
    }

    @Override
    
    // @@author A0138862W
    /** action to perform when ModelManager requested to undo this delete command **/
    public CommandResult undo() {
        try {
            model.addTask((Task) toDelete);

            model.pushToRedoHistory(this);
            
            requestHighlightLastActionedRow((Task) toDelete);

            return new CommandResult(COMMAND_WORD, String.format(MESSAGE_UNDO_SUCCESS, toDelete));
        } catch (DuplicateTaskException e) {
            return new CommandResult(COMMAND_WORD, AddCommand.MESSAGE_DUPLICATE_TASK);
        }
    }

    @Override
    // @@author A0138862W
    /** action to perform when ModelManager requested to redo this delete command **/
    public CommandResult redo() {
        try {
            executeDelete();
            
            model.pushToUndoHistory(this);

        } catch (TaskNotFoundException | IndexOutOfBoundsException | ArchiveTaskList.TaskNotFoundException tnfe) {
            return new CommandResult(COMMAND_WORD, Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
        }

        return new CommandResult(COMMAND_WORD, String.format(MESSAGE_REDO_SUCCESS, toDelete));
    }

    private void executeDelete() throws TaskNotFoundException, IndexOutOfBoundsException, ArchiveTaskList.TaskNotFoundException {
        UnmodifiableObservableList<ReadOnlyTask> lastShownList = model.getCurrentList();

        if (lastShownList.size() < targetIndex) {
            indicateAttemptToExecuteIncorrectCommand();
            throw new IndexOutOfBoundsException();
        }

        if (toDelete == null) {
            toDelete = lastShownList.get(targetIndex - 1);
            deleteDirectly(toDelete.toString(), memory);
        }

        if (toDelete.isMarked()) {
            model.deleteArchive(toDelete);
        }else {
            model.deleteTask(toDelete);
        }
    }
    
    //@@author A0143378Y
    // Perform a delete by name operation
    public static void deleteDirectly(String name, Memory memory) {
        ArrayList<GenericMemory> searchResult = FindCommand.searchExact(name, memory);
        if (searchResult.size() == 1){
            deleteItem(searchResult.get(0), memory);
            History.advance(memory);
        } 
    }
    
    //@@author A0143378Y
    // Delete given GenericMemory item from memory
    private static void deleteItem(GenericMemory item, Memory memory) {
        assert item != null;
        memory.remove(item);
    }
}
