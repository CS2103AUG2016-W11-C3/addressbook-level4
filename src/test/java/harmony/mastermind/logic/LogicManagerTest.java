package harmony.mastermind.logic;

import static harmony.mastermind.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static harmony.mastermind.commons.core.Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX;
import static harmony.mastermind.commons.core.Messages.MESSAGE_UNKNOWN_COMMAND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.eventbus.Subscribe;

import harmony.mastermind.commons.core.EventsCenter;
import harmony.mastermind.commons.events.model.TaskManagerChangedEvent;
import harmony.mastermind.commons.events.ui.JumpToListRequestEvent;
import harmony.mastermind.commons.events.ui.ShowHelpRequestEvent;
import harmony.mastermind.logic.commands.AddCommand;
import harmony.mastermind.logic.commands.ClearCommand;
import harmony.mastermind.logic.commands.Command;
import harmony.mastermind.logic.commands.CommandResult;
import harmony.mastermind.logic.commands.DeleteCommand;
import harmony.mastermind.logic.commands.ExitCommand;
import harmony.mastermind.logic.commands.FindCommand;
import harmony.mastermind.logic.commands.HelpCommand;
import harmony.mastermind.logic.commands.ListCommand;
import harmony.mastermind.model.Model;
import harmony.mastermind.model.ModelManager;
import harmony.mastermind.model.ReadOnlyTaskManager;
import harmony.mastermind.model.TaskManager;
import harmony.mastermind.model.tag.Tag;
import harmony.mastermind.model.tag.UniqueTagList;
import harmony.mastermind.model.task.ReadOnlyTask;
import harmony.mastermind.model.task.Task;
import harmony.mastermind.storage.StorageManager;

public class LogicManagerTest {

    /**
     * See https://github.com/junit-team/junit4/wiki/rules#temporaryfolder-rule
     */
    @Rule
    public TemporaryFolder saveFolder = new TemporaryFolder();

    private Model model;
    private Logic logic;

    //These are for checking the correctness of the events raised
    private ReadOnlyTaskManager latestSavedTaskManager;
    private boolean helpShown;
    private int targetedJumpIndex;
    private static String TAB_HOME = "Home";
    final Date endDate = new Date();

    @Subscribe
    private void handleLocalModelChangedEvent(TaskManagerChangedEvent abce) {
        latestSavedTaskManager = new TaskManager(abce.data);
    }

    @Subscribe
    private void handleShowHelpRequestEvent(ShowHelpRequestEvent she) {
        helpShown = true;
    }

    @Subscribe
    private void handleJumpToListRequestEvent(JumpToListRequestEvent je) {
        targetedJumpIndex = je.targetIndex;
    }

    @Before
    public void setup() {
        model = new ModelManager();
        String tempTaskManagerFile = saveFolder.getRoot().getPath() + "TempTaskManager.xml";
        String tempPreferencesFile = saveFolder.getRoot().getPath() + "TempPreferences.json";
        logic = new LogicManager(model, new StorageManager(tempTaskManagerFile, tempPreferencesFile));
        EventsCenter.getInstance().registerHandler(this);

        latestSavedTaskManager = new TaskManager(model.getTaskManager()); // last saved assumed to be up to date
        helpShown = false;
        targetedJumpIndex = -1; // non yet
    }

    @After
    public void teardown() {
        EventsCenter.clearSubscribers();
    }

    @Test
    public void execute_invalid() throws Exception {
        String invalidCommand = "       ";
        assertCommandBehavior(invalidCommand,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.COMMAND_SUMMARY));
    }

    /**
     * Executes the command and confirms that the result message is correct.
     * Both the 'task manager' and the 'last shown list' are expected to be empty.
     * @see #assertCommandBehavior(String, String, ReadOnlyTaskManager, List)
     */
    private void assertCommandBehavior(String inputCommand, String expectedMessage) throws Exception {
        assertCommandBehavior(inputCommand, expectedMessage, new TaskManager(), Collections.emptyList());
    }

    /**
     * Executes the command and confirms that the result message is correct and
     * also confirms that the following three parts of the LogicManager object's state are as expected:<br>
     *      - the internal task manager data are same as those in the {@code expectedTaskManager} <br>
     *      - the backing list shown by UI matches the {@code shownList} <br>
     *      - {@code expectedTaskManager} was saved to the storage file. <br>
     */
    private void assertCommandBehavior(String inputCommand, String expectedMessage,
                                       ReadOnlyTaskManager expectedTaskManager,
                                       List<? extends ReadOnlyTask> expectedShownList) throws Exception {

        //Execute the command
        CommandResult result = logic.execute(inputCommand);

        //Confirm the ui display elements should contain the right data
        assertEquals(expectedMessage, result.feedbackToUser);
        assertEquals(expectedShownList, model.getFilteredTaskList());
        
        //Confirm the state of data (saved and in-memory) is as expected
        assertEquals(expectedTaskManager, model.getTaskManager());
        assertEquals(expectedTaskManager, latestSavedTaskManager);
    }


    @Test
    public void execute_unknownCommandWord() throws Exception {
        String unknownCommand = "uicfhmowqewca";
        assertCommandBehavior(unknownCommand, MESSAGE_UNKNOWN_COMMAND+": uicfhmowqewca");
    }

    //@@author A0139194X
    @Test
    public void execute_help() throws Exception {
        assertCommandBehavior("help", HelpCommand.SUCCESSFULLY_SHOWN);
        assertTrue(helpShown);
    }

    //@@author
    @Test
    public void execute_exit() throws Exception {
        assertCommandBehavior("exit", ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT);
    }

    @Test
    public void execute_clear() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        model.addTask(helper.generateTask(1));
        model.addTask(helper.generateTask(2));
        model.addTask(helper.generateTask(3));

        assertCommandBehavior("clear", ClearCommand.MESSAGE_SUCCESS, new TaskManager(), Collections.emptyList());
    }

    @Test
    public void execute_add_successful() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        Task toBeAdded = helper.task();
        TaskManager expectedAB = new TaskManager();
        expectedAB.addTask(toBeAdded);

        // execute command and verify result
        assertCommandBehavior(helper.generateAddCommand(toBeAdded),
                String.format(AddCommand.MESSAGE_SUCCESS, toBeAdded),
                expectedAB,
                expectedAB.getTaskList());

    }
    
    @Test
    //@@author A0138862W
    public void execute_undoAndRedo_add() throws Exception{
        TestDataHelper helper = new TestDataHelper();
        Task toBeAdded = helper.task();
        String timeCheckEnd = toBeAdded.parseForConsole(endDate);
        
        logic.execute(helper.generateAddCommand(toBeAdded));
        
        assertCommandBehavior("undo", "Undo successfully.\n"
                + "=====Undo Details=====\n"
                + "[Undo Add Command] Task deleted: task "
                + "end:" + timeCheckEnd + " "
                + "Tags: [tag1],[tag2]\n"
                + "==================",
                model.getTaskManager(),
                model.getTaskManager().getTaskList());
        
        assertCommandBehavior("redo", "Redo successfully.\n"
                + "=====Redo Details=====\n"
                + "[Redo Add Command] Task added: task "
                + "end:" + timeCheckEnd + " "
                + "Tags: [tag1],[tag2]\n"
                + "==================",
                model.getTaskManager(),
                model.getTaskManager().getTaskList());
    }
    
    @Test
    //@@author A0138862W
    public void execute_undo_delete() throws Exception{
        TestDataHelper helper = new TestDataHelper();
        Task toBeEdited = helper.task();
        String timeCheckEnd = toBeEdited.parseForConsole(endDate);
        List<Task> oneTask = helper.generateTaskList(toBeEdited);
        TaskManager expectedTM = helper.generateTaskManager(oneTask);
        List<Task>expectedList = oneTask;
        
        helper.addToModel(model, oneTask);

        logic.execute("delete 1");
        
        assertCommandBehavior("undo",
                "Undo successfully.\n"
                + "=====Undo Details=====\n"
                + "[Undo Delete Command] Task added: task "
                + "end:" + timeCheckEnd + " "
                + "Tags: [tag1],[tag2]\n"
                + "==================",       
                expectedTM,
                expectedList);
        
        assertCommandBehavior("redo",
                "Redo successfully.\n"
                + "=====Redo Details=====\n"
                + "[Redo Delete Command] Deleted Task: task "
                + "end:" + timeCheckEnd + " "
                + "Tags: [tag1],[tag2]\n"
                + "==================",
                model.getTaskManager(),
                model.getListToMark());
    }
    
    @Test
    //@@author A0138862W
    public void execute_undo_mark() throws Exception{
        TestDataHelper helper = new TestDataHelper();
        Task toBeEdited = helper.task();
        String timeCheckEnd = toBeEdited.parseForConsole(endDate);
        List<Task> oneTask = helper.generateTaskList(toBeEdited);
        TaskManager expectedTM = helper.generateTaskManager(oneTask);
        List<Task>expectedList = oneTask;
        
        helper.addToModel(model, oneTask);

        logic.execute("mark 1");
        
        assertCommandBehavior("undo",
                "Undo successfully.\n"
                + "=====Undo Details=====\n"
                + "[Undo Mark Command] task "
                + "end:" + timeCheckEnd + " "
                + "Tags: [tag1],[tag2] has been unmarked\n"
                + "==================",       
                expectedTM,
                expectedList);
        
        assertCommandBehavior("redo",
                "Redo successfully.\n"
                + "=====Redo Details=====\n"
                + "[Redo Mark Command] task "
                + "end:" + timeCheckEnd + " "
                + "Tags: [tag1],[tag2] has been archived\n"
                + "==================",       
                model.getTaskManager(),
                model.getListToMark());
    }
    
    @Test
    //@@author A0138862W
    public void execute_undo_unmark() throws Exception{
        TestDataHelper helper = new TestDataHelper();
        Task toBeEdited = helper.task();
        String timeCheckEnd = toBeEdited.parseForConsole(endDate);
        List<Task> oneTask = helper.generateTaskList(toBeEdited);
        TaskManager expectedTM = helper.generateTaskManager(oneTask);
        List<Task>expectedList;
        
        helper.addToModel(model, oneTask);
        
        logic.execute("mark 1");
        
        logic.execute("unmark 1");
        
        assertCommandBehavior("undo",
                "Undo successfully.\n"
                + "=====Undo Details=====\n"
                + "[Undo Mark Command] task "
                + "end:" + timeCheckEnd + " "
                + "Tags: [tag1],[tag2] has been unmarked\n"
                + "==================",       
                model.getTaskManager(),
                model.getListToMark());
        
        assertCommandBehavior("redo",
                "Redo successfully.\n"
                + "=====Redo Details=====\n"
                + "[Redo Mark Command] task "
                + "end:" + timeCheckEnd + " "
                + "Tags: [tag1],[tag2] has been archived\n"
                + "==================",       
                model.getTaskManager(),
                model.getListToMark());
    }
    
    @Test
    //@@author A0138862W
    public void execute_undo_invalidAddTaskNotFound() throws Exception{
        TestDataHelper helper = new TestDataHelper();
        Task toBeAdded = helper.task();

        logic.execute(helper.generateAddCommand(toBeAdded));
        
        model.deleteTask(toBeAdded);
        
        assertCommandBehavior("undo", "Undo successfully.\n"
                + "=====Undo Details=====\n"
                + "Task could not be found in Mastermind\n"
                + "==================",
                model.getTaskManager(),
                model.getTaskManager().getTaskList());
    }
    
    @Test
    //@@author A0138862W
    public void execute_undo_invalidEditDuplicate() throws Exception{
        TestDataHelper helper = new TestDataHelper();
        Task task1 = helper.generateTaskWithName("edited2 task name");
        Task task2 = helper.generateTaskWithName("edited2 task name");
        
        List<Task> twoTasks = helper.generateTaskList(task1);
        TaskManager expectedTM = helper.generateTaskManager(twoTasks);
        List<Task>expectedList = twoTasks;
        
        model.addTask(task1);

        logic.execute(helper.generateEditCommand());

        model.getTaskManager().getUniqueTaskList().getInternalList().add(task1);
        model.getTaskManager().getUniqueTaskList().getInternalList().add(task2);
        
        assertCommandBehavior("undo", "Undo successfully.\n"
                + "=====Undo Details=====\n"
                + "This task already exists in Mastermind\n"
                + "==================",
                model.getTaskManager(),
                model.getFilteredTaskList());
    }
    
    @Test
    //@@author A0138862W
    public void execute_undo_invalidDeleteDuplicate() throws Exception{
        TestDataHelper helper = new TestDataHelper();
        Task task1 = helper.generateTaskWithName("edited2 task name");
        Task task2 = helper.generateTaskWithName("edited2 task name");
        
        List<Task> twoTasks = helper.generateTaskList(task1);
        
        model.addTask(task1);

        logic.execute("delete 1");

        model.getTaskManager().getUniqueTaskList().getInternalList().add(task1);
        model.getTaskManager().getUniqueTaskList().getInternalList().add(task2);
        
        assertCommandBehavior("undo", "Undo successfully.\n"
                + "=====Undo Details=====\n"
                + "This task already exists in Mastermind\n"
                + "==================",
                model.getTaskManager(),
                model.getFilteredTaskList());
    }
    //@@author

    /*
    @Test
    public void execute_addDuplicate_notAllowed() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        Task toBeAdded = helper.task();
        TaskManager expectedAB =        new TaskManager();
        expectedAB.addTask(toBeAdded);

        // setup starting state
        model.addTask(toBeAdded); // task already in internal task manager

        // execute command and verify result
        assertCommandBehavior(
                helper.generateAddCommand(toBeAdded),
                AddCommand.MESSAGE_DUPLICATE_TASK,
                expectedAB,
                expectedAB.getTaskList());

    }
*/

    @Test
    public void execute_list_showsAllTasks() throws Exception {
        // prepare expectations
        TestDataHelper helper = new TestDataHelper();
        TaskManager expectedAB = helper.generateTaskManager(2);
        List<? extends ReadOnlyTask> expectedList = expectedAB.getTaskList();

        // prepare task manager state
        helper.addToModel(model, 2);

        assertCommandBehavior("list",
                ListCommand.MESSAGE_SUCCESS,
                expectedAB,
                expectedList);
    }


    /**
     * Confirms the 'invalid argument index number behaviour' for the given command
     * targeting a single task in the shown list, using visible index.
     * @param commandWord to test assuming it targets a single task in the last shown list
     *                    based on visible index.
     */
    private void assertIncorrectIndexFormatBehaviorForCommand(String commandWord, String expectedMessage)
            throws Exception {
        assertCommandBehavior(commandWord , expectedMessage); //index missing
        assertCommandBehavior(commandWord + " +1", expectedMessage); //index should be unsigned
        assertCommandBehavior(commandWord + " -1", expectedMessage); //index should be unsigned
        assertCommandBehavior(commandWord + " 0", expectedMessage); //index cannot be 0
        assertCommandBehavior(commandWord + " not_a_number", expectedMessage);
    }

    /**
     * Confirms the 'invalid argument index number behaviour' for the given command
     * targeting a single task in the shown list, using visible index.
     * @param commandWord to test assuming it targets a single task in the last shown list
     *                    based on visible index.
     */
    private void assertIndexNotFoundBehaviorForCommand(String commandWord) throws Exception {
        String expectedMessage = MESSAGE_INVALID_TASK_DISPLAYED_INDEX;
        TestDataHelper helper = new TestDataHelper();
        List<Task> taskList = helper.generateTaskList(2);

        // set AB state to 2 tasks
        model.resetData(new TaskManager());
        for (Task p : taskList) {
            model.addTask(p);
        }

        assertCommandBehavior(commandWord + " 3", expectedMessage, model.getTaskManager(), taskList);
    }

    @Test
    public void execute_deleteInvalidArgsFormat_errorMessageShown() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE);
        assertIncorrectIndexFormatBehaviorForCommand("delete", expectedMessage);
    }

    @Test
    public void execute_deleteIndexNotFound_errorMessageShown() throws Exception {
        assertIndexNotFoundBehaviorForCommand("delete");
    }

    @Test
    public void execute_delete_removesCorrectTask() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        List<Task> threeTasks = helper.generateTaskList(3);

        TaskManager expectedAB = helper.generateTaskManager(threeTasks);
        expectedAB.removeTask(threeTasks.get(1));
        helper.addToModel(model, threeTasks);

        assertCommandBehavior("delete 2",
                String.format(DeleteCommand.MESSAGE_DELETE_TASK_SUCCESS, threeTasks.get(1)),
                expectedAB,
                expectedAB.getTaskList());
    }


    @Test
    public void execute_find_invalidArgsFormat() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        assertCommandBehavior("find ", expectedMessage);
    }

    @Test
    public void execute_find_onlyMatchesFullWordsInNames() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Task pTarget1 = helper.generateTaskWithName("bla bla KEY bla");
        Task pTarget2 = helper.generateTaskWithName("bla KEY bla bceofeia");
        Task p1 = helper.generateTaskWithName("KE Y");
        Task p2 = helper.generateTaskWithName("KEYKEYKEY sduauo");

        List<Task> fourTasks = helper.generateTaskList(p1, pTarget1, p2, pTarget2);
        TaskManager expectedAB = helper.generateTaskManager(fourTasks);
        List<Task> expectedList = helper.generateTaskList(pTarget1, pTarget2);
        helper.addToModel(model, fourTasks);

        assertCommandBehavior("find KEY",
                Command.getMessageForTaskListShownSummary(expectedList.size()),
                expectedAB,
                expectedList);
    }

    @Test
    public void execute_find_isNotCaseSensitive() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Task p1 = helper.generateTaskWithName("bla bla KEY bla");
        Task p2 = helper.generateTaskWithName("bla KEY bla bceofeia");
        Task p3 = helper.generateTaskWithName("key key");
        Task p4 = helper.generateTaskWithName("KEy sduauo");

        List<Task> fourTasks = helper.generateTaskList(p3, p1, p4, p2);
        TaskManager expectedAB = helper.generateTaskManager(fourTasks);
        List<Task> expectedList = fourTasks;
        helper.addToModel(model, fourTasks);

        assertCommandBehavior("find KEY",
                Command.getMessageForTaskListShownSummary(expectedList.size()),
                expectedAB,
                expectedList);
    }

    @Test
    public void execute_find_matchesIfAnyKeywordPresent() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Task pTarget1 = helper.generateTaskWithName("bla bla KEY bla");
        Task pTarget2 = helper.generateTaskWithName("bla rAnDoM bla bceofeia");
        Task pTarget3 = helper.generateTaskWithName("key key");
        Task p1 = helper.generateTaskWithName("sduauo");

        List<Task> fourTasks = helper.generateTaskList(pTarget1, p1, pTarget2, pTarget3);
        TaskManager expectedAB = helper.generateTaskManager(fourTasks);
        List<Task> expectedList = helper.generateTaskList(pTarget1, pTarget2, pTarget3);
        helper.addToModel(model, fourTasks);

        assertCommandBehavior("find key rAnDoM",
                Command.getMessageForTaskListShownSummary(expectedList.size()),
                expectedAB,
                expectedList);
    }
    
    /**
     * A utility class to generate test data.
     */
    class TestDataHelper{

        Task task() throws Exception {
            String name = "task";
            String recur = null;
            Tag tag1 = new Tag("tag1");
            Tag tag2 = new Tag("tag2");
            UniqueTagList tags = new UniqueTagList(tag1, tag2);
            
            return new Task(name, null, endDate, tags, recur, null);
        }

        /**
         * Generates a valid task using the given seed.
         * Running this function with the same parameter values guarantees the returned task will have the same state.
         * Each unique seed will generate a unique Task object.
         *
         * @param seed used to generate the task data field values
         */
        Task generateTask(int seed) throws Exception {
            
            return new Task(
                    "task"+seed,
                    null,
                    endDate,
                    new UniqueTagList(new Tag("tag" + Math.abs(seed)), new Tag("tag" + Math.abs(seed + 1))),
                    null,
                    null
                    );
        }

        /** Generates the correct add command based on the task given */
        String generateAddCommand(Task p) {
            StringBuffer cmd = new StringBuffer();

            cmd.append("add");

            cmd.append(" ").append(p.getName().toString());
            cmd.append(" by ").append(p.getEndDate());
            cmd.append(" #");

            UniqueTagList tags = p.getTags();
            for (Tag t: tags) {
                cmd.append(t.tagName);
                cmd.append(',');
            }
            
            cmd.deleteCharAt(cmd.length()-1);
            
            return cmd.toString();
        }
        
        String generateEditCommand() {
            StringBuffer cmd = new StringBuffer();

            cmd.append("edit 1");

            cmd.append(" name to edited task name");

            return cmd.toString();
        }

        /**
         * Generates an TaskManager with auto-generated tasks.
         */
        TaskManager generateTaskManager(int numGenerated) throws Exception{
            TaskManager taskManager = new TaskManager();
            addToTaskManager(taskManager, numGenerated);
            return taskManager;
        }

        /**
         * Generates an TaskManager based on the list of Tasks given.
         */
        TaskManager generateTaskManager(List<Task> tasks) throws Exception{
            TaskManager taskManager = new TaskManager();
            addToTaskManager(taskManager, tasks);
            return taskManager;
        }

        /**
         * Adds auto-generated Task objects to the given TaskManager
         */
        void addToTaskManager(TaskManager taskManager, int numGenerated) throws Exception {
            addToTaskManager(taskManager, generateTaskList(numGenerated));
        }

        /**
         * Adds the given list of Tasks to the given TaskManager
         */
        void addToTaskManager(TaskManager taskManager, List<Task> tasksToAdd) throws Exception {
            for (Task p: tasksToAdd) {
                taskManager.addTask(p);
            }
        }

        /**
         * Adds auto-generated Task objects to the given model
         * @param model The model to which the Tasks will be added
         */
        void addToModel(Model model, int numGenerated) throws Exception{
            addToModel(model, generateTaskList(numGenerated));
        }

        /**
         * Adds the given list of Tasks to the given model
         */
        void addToModel(Model model, List<Task> tasksToAdd) throws Exception{
            for (Task p: tasksToAdd) {
                model.addTask(p);
            }
        }

        /**
         * Generates a list of Tasks based on the flags.
         */
        List<Task> generateTaskList(int numGenerated) throws Exception{
            List<Task> tasks = new ArrayList<>();
            for (int i = 1; i <= numGenerated; i++) {
                tasks.add(generateTask(i));
            }
            return tasks;
        }

        List<Task> generateTaskList(Task... tasks) {
            return Arrays.asList(tasks);
        }

        /**
         * Generates a Task object with given name. Other fields will have some dummy values.
         */
        Task generateTaskWithName(String name) throws Exception {
            return new Task(
                    name,
                    new Date(),
                    new Date(),
                    new UniqueTagList(new Tag("tag1"), new Tag("tag2")),
                    null,
                    null
            );
        }
    }
}
