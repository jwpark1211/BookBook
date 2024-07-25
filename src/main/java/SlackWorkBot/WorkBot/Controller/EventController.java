package SlackWorkBot.WorkBot.Controller;

import com.slack.api.bolt.App;
import com.slack.api.bolt.jakarta_servlet.SlackAppServlet;
import jakarta.servlet.annotation.WebServlet;


@WebServlet("/slack/events")
public class EventController extends SlackAppServlet {
    public EventController(App app) {
        super(app);
    }
}
