package guitests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommandBoxTest extends TaskManagerGuiTest {

    @Test
    //@@author A0124797R
    public void commandBox_commandSucceeds_textCleared() {
        commandBox.runCommand(td.task4.getAddCommand());
        assertEquals(commandBox.getCommandInput(), "");
    }

}
