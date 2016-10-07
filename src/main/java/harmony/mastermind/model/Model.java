package harmony.mastermind.model;

import java.util.Set;
import java.util.Stack;

import harmony.mastermind.commons.core.UnmodifiableObservableList;
import harmony.mastermind.logic.commands.Command;
import harmony.mastermind.model.tag.Tag;
import harmony.mastermind.model.task.ReadOnlyTask;
import harmony.mastermind.model.task.Task;
import harmony.mastermind.model.task.UniqueTaskList;

/**
 * The API of the Model component.
 */
public interface Model {
    /** Clears existing backing model and replaces with the provided new data. */
    void resetData(ReadOnlyTaskManager newData);

    /** Returns the TaskManager*/
    ReadOnlyTaskManager getTaskManager();

    /** Deletes the given task. */
    void deleteTask(ReadOnlyTask target) throws UniqueTaskList.TaskNotFoundException;

    /** Adds the given task */
    void addTask(Task task) throws UniqueTaskList.DuplicateTaskException;

    /** Marks the given task as done */
    void markTask(ReadOnlyTask target) throws UniqueTaskList.TaskNotFoundException;
    
    /** Returns the stack of command history */
    Stack<Command> getCommandHistory();

    /** Returns the filtered task list as an {@code UnmodifiableObservableList<ReadOnlyTask>} */
    UnmodifiableObservableList<ReadOnlyTask> getFilteredTaskList();

    /** Updates the filter of the filtered task list to show all tasks */
    void updateFilteredListToShowAll();

    /** Updates the filter of the filtered task list to filter by the given keywords*/
    void updateFilteredTaskList(Set<String> keywords);

    /** Updates the filter of the filtered task list to filter by the given keywords*/
    void updateFilteredTagTaskList(Set<Tag> keywords);

}