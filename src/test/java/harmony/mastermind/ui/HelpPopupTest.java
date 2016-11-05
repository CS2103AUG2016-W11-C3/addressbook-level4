package harmony.mastermind.ui;

import static org.junit.Assert.*;
import harmony.mastermind.logic.HelpPopupEntry;
import java.util.ArrayList;
import harmony.mastermind.logic.HelpPopupEntry;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.stage.Popup;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

//@@author A0139194X
public class HelpPopupTest {

    private static final String FXML = "HelpPopup.fxml";
    private HelpPopupStub helpPopup = new HelpPopupStub(); 

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void getFxmlPath_success() {
        assertEquals(FXML, helpPopup.getFxmlPath());
    }
    
    @Test
    public void show_nullInput_assertionFailure() {
        thrown.expect(AssertionError.class);
        helpPopup.show(null);
    }
    
    @Test
    public void getEntries_success() {
        assertTrue(helpPopup.getEntries() instanceof ArrayList<?>);
    }
    
    @Test
    public void injectData_sucess() {
        ArrayList<HelpPopupEntry> list = new ArrayList<HelpPopupEntry>();
        list.add(new HelpPopupEntry("command", "format", "description"));
        list.add(new HelpPopupEntry("command2", "format2", "description2"));
        list.add(new HelpPopupEntry("command3", "format3", "description3"));
        
        helpPopup.injectData(list);
        assertEquals(list.size(), helpPopup.getEntries().size());
    }
    
    class HelpPopupStub extends HelpPopup {
        public HelpPopupStub () {
            super();
        }
    }

}
