package harmony.mastermind.testutil;

import java.util.Date;

import harmony.mastermind.commons.exceptions.IllegalValueException;
import harmony.mastermind.model.TaskManager;
import harmony.mastermind.model.task.*;

/**
 * @@author A0138862W
 */
public class TypicalTestTask {

    public static TestTask floatingTask;
    public static TestTask deadlineTask;
    public static TestTask eventTask;

    public static TestTask floatingTaskWithTags;
    public static TestTask deadlineTaskWithTags;
    public static TestTask eventTaskWithTags;

    public static TestTask task1, task2, task3, task4, task5;

    // @@author A0124797R
    public TypicalTestTask() {

        try {
            floatingTask = new TaskBuilder().withName("floatingTask").build();
            deadlineTask = new TaskBuilder().withName("deadlineTask").withEndDate(new Date()).build();
            eventTask = new TaskBuilder().withName("eventTask").withStartDate(new Date()).withEndDate(new Date()).build();

            floatingTaskWithTags = new TaskBuilder().withName("floatingTaskWithTags").withTags("tag1", "tag2").build();
            deadlineTaskWithTags = new TaskBuilder().withName("deadlineTaskWithTags").withEndDate(new Date()).withTags("tag3", "tag4").build();
            eventTaskWithTags = new TaskBuilder().withName("eventTaskWithTags").withStartDate(new Date()).withEndDate(new Date()).withTags("tag5", "tag6").build();

            task1 = new TaskBuilder().withName("do laundry").withTags("chores").build();
            task2 = new TaskBuilder().withName("finish assignment").build();
            task3 = new TaskBuilder().withName("do past year papers").withTags("examPrep").build();
            task4 = new TaskBuilder().withName("complete cs2103 lecture quiz").withTags("homework").build();
            task5 = new TaskBuilder().withName("complete cs2105 assignment").withTags("homework").build();
        } catch (IllegalValueException ive) {
            ive.printStackTrace();
            assert false : "not possible";
        }

    }

    // @@author A0138862W, A0124797R
    public static void loadTaskManagerWithSampleData(TaskManager taskManager) {

        try {
            taskManager.addTask(new Task(floatingTask));
            taskManager.addTask(new Task(deadlineTask));
            taskManager.addTask(new Task(eventTask));
            taskManager.addTask(new Task(floatingTaskWithTags));
            taskManager.addTask(new Task(deadlineTaskWithTags));
            taskManager.addTask(new Task(eventTaskWithTags));

            taskManager.addTask(new Task(task1));
            taskManager.addTask(new Task(task2));
            taskManager.addTask(new Task(task3));
            taskManager.addTask(new Task(task4));
            taskManager.addTask(new Task(task5));

        } catch (UniqueTaskList.DuplicateTaskException e) {
            assert false : "not possible";
        } catch (IllegalValueException e) {
            e.printStackTrace();
            assert false : "not possible";
        }
    }

    // @@author A0124797R
    public TestTask[] getTypicalTasks() {
        return new TestTask[] { floatingTask, deadlineTask, eventTask, floatingTaskWithTags, deadlineTaskWithTags, eventTaskWithTags, task1, task2, task3, task4 };
    }

    public TaskManager getTypicalTaskManager() {
        TaskManager tm = new TaskManager();
        loadTaskManagerWithSampleData(tm);
        return tm;

    }
}
