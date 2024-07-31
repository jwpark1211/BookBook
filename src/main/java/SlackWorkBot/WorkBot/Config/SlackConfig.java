package SlackWorkBot.WorkBot.Config;

import SlackWorkBot.WorkBot.Service.AttendanceService;
import SlackWorkBot.WorkBot.Service.NoticeService;
import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SlackConfig {
    @Value("${slack.token}")
    private String token;
    @Value("${slack.signingSecret}")
    private String signingSecret;

    private final AttendanceService attendanceService;
    private final NoticeService noticeService;

    @Bean
    public AppConfig loadSingleWorkspaceAppConfig() {
        return AppConfig.builder()
                .singleTeamBotToken(token)
                .signingSecret(signingSecret)
                .build();
    }

    @Bean
    public App initSlackApp(AppConfig config) {
        App app = new App(config);

        /** Slash Commands [1] : /근무시간 **/
        app.command("/근무시간", (req, ctx) -> {
            log.info("command[1] 명령어가 입력");

            SlashCommandPayload payload = req.getPayload();
            String userId = payload.getUserId();
            String userName = payload.getUserName();
            String content = payload.getText();

            attendanceService.saveAttendanceData(payload.getChannelId(), userName, userId, content);
            return ctx.ack();
        });

        /** Slash Commands [2] : /월별기록 **/
        app.command("/월별기록", (req, ctx)-> {

            log.info("command[2] 명령어가 입력");
            SlashCommandPayload payload = req.getPayload();
            String content = payload.getText();
            String userId = payload.getUserId();
            String [] contents = content.split(" ");

            attendanceService.getMonthlyRecord(payload.getChannelId() , userId , contents);

            return ctx.ack();
        });

        /** Slash Commands [3] : /공지등록 **/
        app.command("/공지등록", (req,ctx)-> {
            log.info("command[3] 명령어가 입력");
            SlashCommandPayload payload = req.getPayload();
            String content = payload.getText();
            String userId = payload.getUserId();
            String channelId = payload.getChannelId();

            noticeService.saveNoticeData(channelId, userId, content);
            return ctx.ack();
        });

        /** Slash Commands [4] : /이달의공지 **/
        app.command("/이달의공지", (req,ctx) -> {
           log.info("command[4] 명령어가 입력");
           SlashCommandPayload payload = req.getPayload();
           String channelId = payload.getChannelId();
           String userId = payload.getUserId();

           noticeService.getMonthlyNoticeByChannelId(channelId,userId);

           return ctx.ack();
        });

        return app;
    }
}
