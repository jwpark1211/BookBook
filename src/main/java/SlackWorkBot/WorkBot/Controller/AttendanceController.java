package SlackWorkBot.WorkBot.Controller;

import com.slack.api.bolt.App;
import com.slack.api.bolt.jakarta_servlet.SlackAppServlet;
import jakarta.servlet.annotation.WebServlet;
import org.springframework.beans.factory.annotation.Qualifier;


@Qualifier("attendance")
@WebServlet("/slack/attendance")
public class AttendanceController extends SlackAppServlet {
    public AttendanceController(App app) {
        super(app);
    }
}
