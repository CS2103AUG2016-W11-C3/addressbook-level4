package harmony.mastermind.logic;

import static org.junit.Assert.*;

import org.junit.Test;

//@@author A0139194X
public class HelpPopupEntryTest {

    private HelpPopupEntry entry; 
    
    private void setup() {
        entry = new HelpPopupEntry("test1", "test2", "test3");
    }
    
    @Test
    public void getCommandWord_success() {
        setup();
        assertEquals(entry.getCommandWord(), "test1");
    }
    
    @Test
    public void getFormat_success() {
        setup();
        assertEquals(entry.getFormat(), "test2");
    }
    
    @Test
    public void getDescription_success() {
        setup();
        assertEquals(entry.getDescription(), "test3");
    }

}
