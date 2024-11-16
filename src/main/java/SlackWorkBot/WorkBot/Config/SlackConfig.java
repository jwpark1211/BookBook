package SlackWorkBot.WorkBot.Config;

import SlackWorkBot.WorkBot.Service.AttendanceService;
import SlackWorkBot.WorkBot.Service.NoticeService;
import SlackWorkBot.WorkBot.Service.SlackMessageBuilder;
import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.model.block.LayoutBlock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
    private final SlackMessageBuilder slackMessageBuilder;

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
            // Slack Block Kit 메시지 전송
            List<LayoutBlock> layoutBlocks = slackMessageBuilder.createAttendanceLayoutBlocks();
            ctx.respond(r -> r.blocks(layoutBlocks));
            return ctx.ack();
        });

        app.blockAction("check_in_datepicker", (req, ctx) -> {
            String selectedDate = req.getPayload().getActions().get(0).getSelectedDate();
            return ctx.ack();
        });
        app.blockAction("check_in_timepicker",(req,ctx)->{
            String selectedTime = req.getPayload().getActions().get(0).getSelectedTime();
            return ctx.ack();
        });
        app.blockAction("check_out_datepicker", (req, ctx) -> {
            String selectedDate = req.getPayload().getActions().get(0).getSelectedDate();
            return ctx.ack();
        });
        app.blockAction("check_out_timepicker",(req,ctx)->{
            String selectedTime = req.getPayload().getActions().get(0).getSelectedTime();
            return ctx.ack();
        });


        /** Block Actions : 근무시간 저장 **/
        app.blockAction("action_save_attendance", (req, ctx) -> {
            // 사용자가 입력한 데이터 가져오기
            String channelId = req.getPayload().getChannel().getId();
            String userId = req.getPayload().getUser().getId();
            String userName = req.getPayload().getUser().getName();

            // 출근 날짜와 시간 가져오기
            String checkInDate = req.getPayload().getState().getValues().get("check_in_date").get("check_in_datepicker").getSelectedDate();
            String checkInTime = req.getPayload().getState().getValues().get("check_in_time").get("check_in_timepicker").getSelectedTime();

            // 퇴근 날짜와 시간 가져오기
            String checkOutDate = req.getPayload().getState().getValues().get("check_out_date").get("check_out_datepicker").getSelectedDate();
            String checkOutTime = req.getPayload().getState().getValues().get("check_out_time").get("check_out_timepicker").getSelectedTime();

            // AttendanceService의 handleAttendanceSave 메서드 호출 (근무시간 저장 처리)
            attendanceService.saveAttendanceData(channelId, userName, userId, checkInDate, checkInTime, checkOutDate, checkOutTime);

            return ctx.ack();
        });

        //***************************************************************************************************************//

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

        //***************************************************************************************************************//

        /** Slash Commands [3] : /공지등록 **/
        app.command("/공지등록", (req,ctx)-> {
            List<LayoutBlock> layoutBlocks = slackMessageBuilder.createNoticeLayoutBlocks();
            ctx.respond(r -> r.blocks(layoutBlocks));
            return ctx.ack();
        });

        app.blockAction("notice_input_block",(req,ctx)->{
            return ctx.ack();
        });

        app.blockAction("action_save_notice", (req, ctx) -> {
            String channelId = req.getPayload().getChannel().getId();
            String userId = req.getPayload().getUser().getId();
            String noticeContent = req.getPayload().getState().getValues().get("notice_input_block").get("notice_input").getValue();

            noticeService.saveNoticeData(channelId,userId,noticeContent);
            return ctx.ack();
        });

        //***************************************************************************************************************//

        /** Slash Commands [4] : /이달의공지 **/
        app.command("/이달의공지", (req,ctx) -> {
           log.info("command[4] 명령어가 입력");
           SlashCommandPayload payload = req.getPayload();
           String channelId = payload.getChannelId();
           String userId = payload.getUserId();

           noticeService.getMonthlyNoticeByChannelId(channelId,userId);

           return ctx.ack();
        });

        //***************************************************************************************************************//

        /** Slash Commands [5] : /외치기 **/
        app.command("/외치기", (req,ctx)-> {
            List<LayoutBlock> layoutBlocks = slackMessageBuilder.createScreamLayoutBlocks();
            ctx.respond(r -> r.blocks(layoutBlocks));
            return ctx.ack();
        });

        app.blockAction("scream_input_block",(req,ctx)->{
            return ctx.ack();
        });

        app.blockAction("scream_action", (req, ctx) -> {
            String channelId = req.getPayload().getChannel().getId();
            String userId = req.getPayload().getUser().getId();
            String screamContent = req.getPayload().getState().getValues().get("scream_input_block").get("scream_input").getValue();

            noticeService.screamNotice(channelId, userId,screamContent);
            return ctx.ack();
        });

        //***************************************************************************************************************//

        /** Slash Commands [6] : /월급계산기 **/
        app.command("/월급계산기", (req, ctx)->{
            List<LayoutBlock> layoutBlocks = slackMessageBuilder.createScreamLayoutBlocks();
            ctx.respond(r-> r.blocks(layoutBlocks));
            return ctx.ack();
        });

        return app;
    }

}
