package harmony.mastermind.model;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.Collections;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

import harmony.mastermind.commons.core.ComponentManager;
import harmony.mastermind.commons.core.LogsCenter;
import harmony.mastermind.commons.core.UnmodifiableObservableList;
import harmony.mastermind.commons.events.model.TaskManagerChangedEvent;
import harmony.mastermind.commons.util.StringUtil;
import harmony.mastermind.logic.commands.Command;
import harmony.mastermind.model.tag.Tag;
import harmony.mastermind.model.task.ArchiveTaskList;
import harmony.mastermind.model.task.ReadOnlyTask;
import harmony.mastermind.model.task.Task;
import harmony.mastermind.model.task.UniqueTaskList;
import harmony.mastermind.model.task.UniqueTaskList.DuplicateTaskException;
import harmony.mastermind.model.task.UniqueTaskList.TaskNotFoundException;

/**
 * Represents the in-memory model of the address book data.
 * All changes to any model should be synchronized.
 */
public class ModelManager extends ComponentManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final TaskManager taskManager;
    private final FilteredList<Task> filteredTasks;
    private final Stack<Command> commandHistory;

    /**
     * Initializes a ModelManager with the given TaskManager
     * TaskManager and its variables should not be null
     */
    public ModelManager(TaskManager src, UserPrefs userPrefs) {
        super();
        assert src != null;
        assert userPrefs != null;

        logger.fine("Initializing with task manager: " + src + " and user prefs " + userPrefs);

        taskManager = new TaskManager(src);
        filteredTasks = new FilteredList<>(taskManager.getTasks());
        commandHistory = new Stack<>();
    }

    public ModelManager() {
        this(new TaskManager(), new UserPrefs());
    }

    public ModelManager(ReadOnlyTaskManager initialData, UserPrefs userPrefs) {
        taskManager = new TaskManager(initialData);
        filteredTasks = new FilteredList<>(taskManager.getTasks());
        commandHistory = new Stack<>();
    }

    @Override
    public void resetData(ReadOnlyTaskManager newData) {
        taskManager.resetData(newData);
        indicateTaskManagerChanged();
    }

    @Override
    public ReadOnlyTaskManager getTaskManager() {
        return taskManager;
    }

    /** Raises an event to indicate the model has changed */
    private void indicateTaskManagerChanged() {
        raise(new TaskManagerChangedEvent(taskManager));
    }
    
    @Override
    public Stack<Command> getCommandHistory() {
        return commandHistory;
    }

    @Override
    public synchronized void deleteTask(ReadOnlyTask target) throws TaskNotFoundException {
        taskManager.removeTask(target);
        indicateTaskManagerChanged();
    }

    @Override
    public synchronized void addTask(Task task) throws UniqueTaskList.DuplicateTaskException {
        taskManager.addTask(task);
        updateFilteredListToShowAll();
        indicateTaskManagerChanged();
    }
    
    //@author A0124797R
    @Override
    public synchronized void markTask(Task target) throws TaskNotFoundException {
        taskManager.markTask(target);
        indicateTaskManagerChanged();
    }
    
    //@author A0124797R
    @Override
    public synchronized void unmarkTask(Task target) throws ArchiveTaskList.TaskNotFoundException,
    UniqueTaskList.DuplicateTaskException {
        taskManager.unmarkTask((Task)target);
        indicateTaskManagerChanged();
    }
    
    @Override
    public synchronized void relocateSaveLocation(String target) {
        taskManager.relocateSaveLocation(target);
        indicateTaskManagerChanged();
    }
    
    //=========== Filtered Archived List Accessors ===============================================================
    //@@author A0124797R
    @Override
    public UnmodifiableObservableList<ReadOnlyTask> getFilteredArchiveList() {
        return new UnmodifiableObservableList<>(taskManager.getArchives());
    }

    //=========== Filtered Task List Accessors ===============================================================

    @Override
    public UnmodifiableObservableList<ReadOnlyTask> getFilteredTaskList() {
        return new UnmodifiableObservableList<>(filteredTasks);
    }
    
    //@@author A0124797R
    @Override
    public ObservableList<Task> getListToMark() {
        return filteredTasks;
    }

    @Override
    public void updateFilteredListToShowAll() {
        filteredTasks.setPredicate(null);
    }

    @Override
    public void updateFilteredTaskList(Set<String> keywords){
        updateFilteredTaskList(new PredicateExpression(new NameQualifier(keywords)));
    }

    //@author A0124797R
    @Override
    public void updateFilteredTagTaskList(Set<Tag> keywords){
        updateFilteredTaskList(new PredicateExpression(new TagQualifier(keywords)));
    }

    private void updateFilteredTaskList(Expression expression) {
        filteredTasks.setPredicate(expression::satisfies);
    }

    //========== Inner classes/interfaces used for filtering ==================================================

    interface Expression {
        boolean satisfies(ReadOnlyTask task);
        String toString();
    }

    private class PredicateExpression implements Expression {

        private final Qualifier qualifier;

        PredicateExpression(Qualifier qualifier) {
            this.qualifier = qualifier;
        }

        @Override
        public boolean satisfies(ReadOnlyTask task) {
            return qualifier.run(task);
        }

        @Override
        public String toString() {
            return qualifier.toString();
        }
    }

    interface Qualifier {
        boolean run(ReadOnlyTask task);
        String toString();
    }

    private class NameQualifier implements Qualifier {
        private Set<String> nameKeyWords;

        NameQualifier(Set<String> nameKeyWords) {
            this.nameKeyWords = nameKeyWords;
        }

        @Override
        public boolean run(ReadOnlyTask task) {
            return nameKeyWords.stream()
                    .filter(keyword -> StringUtil.containsIgnoreCase(task.getName(), keyword))
                    .findAny()
                    .isPresent();
        }

        @Override
        public String toString() {
            return "name=" + String.join(", ", nameKeyWords);
        }
    }
    
    //@author A0124797R
    private class TagQualifier implements Qualifier {
        private Set<Tag> tagKeyWords;

        TagQualifier(Set<Tag> tagKeyWords) {
            this.tagKeyWords = tagKeyWords;
        }

        @Override
        public boolean run(ReadOnlyTask task) {
            final Set<Tag> tagList = task.getTags().toSet();
            
            return !Collections.disjoint(tagList, tagKeyWords);
        }

        @Override
        public String toString() {
            return "tags=" + String.join(", ", tagKeyWords.toString());
        }
    }

}
