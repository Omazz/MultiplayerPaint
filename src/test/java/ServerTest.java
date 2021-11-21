import static org.junit.Assert.assertEquals;
import org.junit.Test;
import server.Server;

public class ServerTest {
    @Test
    public void testPort() {
        Server server = new Server(true);
        assertEquals(true, server.isCreated());
    }

    @Test
    public void testSendReport() {
        Server server = new Server(true);
        assertEquals(true, server.sendReport("test report"));
    }

    @Test
    public void testCheckBoard() {
        Server server = new Server(true);
        assertEquals(false, server.checkBoards("testBoardName"));
        assertEquals(false, server.checkBoards(null));
    }

    @Test
    public void testBoardsEmpty(){
        Server server = new Server(true);
        assertEquals(true, server.isBoardsEmpty());
    }

    @Test
    public void testClientsEmpty(){
        Server server = new Server(true);
        assertEquals(true, server.isClientsEmpty());
    }
}
