package guitests;

import static harmony.mastermind.commons.core.Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX;
import static harmony.mastermind.logic.commands.MarkCommand.MESSAGE_MARK_SUCCESS;
import static harmony.mastermind.logic.commands.MarkCommand.MESSAGE_MARK_FAILURE;
import static harmony.mastermind.logic.commands.MarkCommand.MESSAGE_MARK_DUE_SUCCESS;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import harmony.mastermind.testutil.TestTask;
import harmony.mastermind.testutil.TestUtil;
import harmony.mastermind.testutil.TypicalTestTasks;

//@@author A0124797R
public class MarkCommandTest extends TaskManagerGuiTest {

    @Test
    public void mark_nonRecurringTask_success() {
        
        //marks first task
        TestTask[] currentList = td.getTypicalTasks();
        int targetIndex = 4;
        assertMarkSuccess(targetIndex, currentList);
        
        //undo Mark command
        commandBox.runCommand("undo");
        assertTrue(taskListPanel.isListMatching(currentList));
        
        //redo Mark command
        commandBox.runCommand("redo");
        TestTask[] expectedList = TestUtil.removeTaskFromList(currentList, targetIndex);
        assertTrue(taskListPanel.isListMatching(expectedList));
        
        //invalid index
        commandBox.runCommand("mark " + (currentList.length + 1));
        assertResultMessage(MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
    }
    
    @Test
    public void mark_recurringTask_addNextRecurTask() {
        //add a recurring task and mark 
        TestTask[] currentList = td.getTypicalTasks();
        commandBox.runCommand(TypicalTestTasks.task9.getAddCommand());
        currentList = TestUtil.addTasksToList(currentList, TypicalTestTasks.task9); 
        assertTrue(taskListPanel.isListMatching(currentList));
        
        int targetIndex = 5;
        assertMarkRecurSuccess(targetIndex, currentList);
        
    }
    
    @Test
    public void mark_TaskAlreadyMarked_markFailure() {
        commandBox.runCommand("list archives");
        commandBox.runCommand("mark 1");

        assertResultMessage(MESSAGE_MARK_FAILURE);
        
    }
    
    @Test
    public void markDue_dueTasks_markAllDueTask() {
        TestTask[] expectedList = {TypicalTestTasks.task3, TypicalTestTasks.task4};
        
        commandBox.runCommand("mark due");
        assertTrue(taskListPanel.isListMatching(expectedList));
        assertResultMessage(MESSAGE_MARK_DUE_SUCCESS);
        
    }

    //@@author A0124797R
    private void assertMarkSuccess(int targetIndexOneIndexed, final TestTask[] currentList) {
        TestTask taskToMark = currentList[targetIndexOneIndexed-1]; //-1 because array uses zero indexing
        TestTask[] expectedRemainder = TestUtil.removeTaskFromList(currentList, targetIndexOneIndexed);

        commandBox.runCommand("mark " + targetIndexOneIndexed);
        
        //confirm the list now contains all previous tasks except the deleted task
        assertTrue(taskListPanel.isListMatching(expectedRemainder));

        //confirm the result message is correct
        assertResultMessage(String.format(MESSAGE_MARK_SUCCESS, taskToMark));
    }
    
    private void assertMarkRecurSuccess(int targetIndexOneIndexed, final TestTask[] currentList) {
        TestTask taskToMark = currentList[targetIndexOneIndexed-1]; //-1 because array uses zero indexing
        TestTask[] expectedRemainder = TestUtil.removeTaskFromList(currentList, targetIndexOneIndexed);
        expectedRemainder = TestUtil.addTasksToList(expectedRemainder, td.task10);
        
        commandBox.runCommand("mark " + targetIndexOneIndexed);
        
        //confirm the list now contains all previous tasks except the deleted task
        assertTrue(taskListPanel.isListMatching(expectedRemainder));

        //confirm the result message is correct
        assertResultMessage(String.format(MESSAGE_MARK_SUCCESS, taskToMark));
    }
    
}
