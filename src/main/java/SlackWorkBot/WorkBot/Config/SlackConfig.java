package SlackWorkBot.WorkBot.Config;

import SlackWorkBot.WorkBot.Service.AttendanceService;
import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Bean
    public AppConfig loadSingleWorkspaceAppConfig() {
        return AppConfig.builder()
                .singleTeamBotToken(token)
                .signingSecret(signingSecret)
                .build();
    }

    @Qualifier("attendance")
    @Bean
    public App initAttendanceApp(AppConfig config) {
        App app = new App(config);
        app.command("/근무시간", (req, ctx) -> {
            log.info("command 명령어가 입력");
            SlashCommandPayload payload = req.getPayload();
            String userId = "<@" + payload.getUserId() + ">";
            String content = payload.getText();

            try {
                boolean flag = attendanceService.saveAttendanceData(payload.getChannelId(), userId, content);
                log.info("attendanceData가 저장되었습니다.");
                if(flag) { ctx.respond(r -> r.responseType("in_channel")
                        .text(":white_check_mark: " + userId + "님의 근무시간 : " + content)); }
            } catch (IllegalArgumentException e) {
                log.error("잘못된 시간 형식: {}", content);
                ctx.respond(r -> r.responseType("ephemeral")
                        .text(":x: 잘못된 시간 형식입니다. 올바른 형식: yyyy-MM-dd HH:mm~yyyy-MM-dd HH:mm"));
            } catch (Exception e) {
                log.error("근무시간 저장 중 오류 발생", e);
                ctx.respond(r -> r.responseType("ephemeral")
                        .text(":x: 근무시간 저장 중 오류가 발생했습니다. 관리자에게 문의하세요."));
            }
            return ctx.ack();
        });

        return app;
    }

}
