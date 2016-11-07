package harmony.mastermind.commons.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertNotNull;

public class AppUtilTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();



    @Test
    public void getImage_exitingImage(){
        assertNotNull(AppUtil.getImage("/images/to_do_list_32.png"));
    }


    @Test
    public void getImage_nullGiven_nullPointerException(){
        thrown.expect(NullPointerException.class);
        AppUtil.getImage(null);
    }


}