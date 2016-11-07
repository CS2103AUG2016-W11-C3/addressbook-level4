package guitests;

import guitests.guihandles.*;
import harmony.mastermind.TestApp;
import harmony.mastermind.commons.core.EventsCenter;
import harmony.mastermind.model.TaskManager;
import harmony.mastermind.model.task.ReadOnlyTask;
import harmony.mastermind.testutil.TestUtil;
import harmony.mastermind.testutil.TypicalTestTasks;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.testfx.api.FxToolkit;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A GUI Test class for TaskManger.
 */
public abstract class TaskManagerGuiTest {

    /* The TestName Rule makes the current test name available inside test methods */
    @Rule
    public TestName name = new TestName();

    TestApp testApp;

    protected TypicalTestTasks td = new TypicalTestTasks();
    
    protected TaskManager taskManager;
    
    public static final PrettyTimeParser prettyTimeParser = new PrettyTimeParser();

    /*
     *   Handles to GUI elements present at the start up are created in advance
     *   for easy access from child classes.
     */
    protected MainGuiHandle mainGui;
    protected MainMenuHandle mainMenu;
    protected TaskListPanelHandle taskListPanel;
    protected ResultDisplayHandle resultDisplay;
    protected TabPaneHandle tabPane;
    protected CommandBoxHandle commandBox;
    private Stage stage;

    @BeforeClass
    public static void setupSpec() {
        try {
            FxToolkit.registerPrimaryStage();
            FxToolkit.hideStage();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setup() throws Exception {
        FxToolkit.setupStage((stage) -> {
            mainGui = new MainGuiHandle(new GuiRobot(), stage);
            mainMenu = mainGui.getMainMenu();
            taskListPanel = mainGui.getTaskListPanel();
            resultDisplay = mainGui.getResultDisplay();
            tabPane = mainGui.getTabPane();
            commandBox = mainGui.getCommandBox();
            this.stage = stage;
        });
        EventsCenter.clearSubscribers();
        testApp = (TestApp) FxToolkit.setupApplication(() -> new TestApp(this::getInitialData, getDataFileLocation()));
        FxToolkit.showStage();
        while (!stage.isShowing());
        mainGui.focusOnMainApp();
    }

    /**
     * Override this in child classes to set the initial local data.
     * Return null to use the data in the file specified in {@link #getDataFileLocation()}
     */
    protected TaskManager getInitialData() {
        TaskManager tm = TestUtil.generateEmptyTaskManager();
        taskManager = tm;
        TypicalTestTasks.loadTaskManagerWithSampleData(tm);
        return tm;
    }

    /**
     * Override this in child classes to set the data file location.
     * @return
     */
    protected String getDataFileLocation() {
        return TestApp.SAVE_LOCATION_FOR_TESTING;
    }

    @After
    public void cleanup() throws TimeoutException {
        FxToolkit.cleanupStages();
    }

    /**
     * Asserts the task shown in the card is same as the given task
     */
    public void assertMatching(ReadOnlyTask t1, ReadOnlyTask t2) {
        assertTrue(TestUtil.compareTasks(t1, t2));
    }

    /**
     * Asserts the size of the task list is equal to the given number.
     */
    protected void assertListSize(int size) {
        int numberOfTask = taskListPanel.getNumberOfTask();
        assertEquals(size, numberOfTask);
    }

    /**
     * Asserts the message shown in the Result Display area is same as the given string.
     * @param expected
     */
    protected void assertResultMessage(String expected) {
        assertEquals(expected, resultDisplay.getText());
    }
    
    /**
     * Asserts the current tab is the same as the given tab name
     */
    protected void assertCurrentTab(String expected) {
        assertEquals(expected, tabPane.getCurrentTab());        
    }
    
    /**
     * Asserts the current tab is the same as the given tab name
     */
    protected void assertCommandBox(String expected) {
        assertEquals(expected, commandBox.getCommandInput());        
    }
    
    
}
