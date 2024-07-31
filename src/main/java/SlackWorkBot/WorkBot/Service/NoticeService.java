package SlackWorkBot.WorkBot.Service;

import SlackWorkBot.WorkBot.Entity.Notice;
import SlackWorkBot.WorkBot.Repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeService {

    private final SlackService slackService;
    private final NoticeRepository noticeRepository;

    public void saveNoticeData(String channelId, String userId, String content){
        try {
            if(content.length() <= 0){
                throw new IllegalArgumentException("빈 입력입니다. 다시 시도하세요.");
            }else{
                Notice notice = Notice.builder()
                        .channelId(channelId)
                        .userId("<@"+userId+">")
                        .content(content)
                        .build();
                noticeRepository.save(notice);
                slackService.sendMessageToChannel(channelId,"\uD83D\uDC8C 공지사항 등록 완료",content);
            }
        }catch(Exception e){
            log.error("공지사항 저장 중 오류 발생", e);
            slackService.sendEphemeralMessageToUser(channelId,userId,"\uD83D\uDCA5 오류 발생",
                    ":x: 공지사항 등록 중 오류가 발생했습니다. 관리자에게 문의하세요.");
        }
    }

    public void getMonthlyNoticeByChannelId(String channelId, String userId){
        try{
            List<Notice> monthlyNotice = noticeRepository.findByChannelId(channelId);
            YearMonth yearMonthNow = YearMonth.now();
            StringBuilder sendText = new StringBuilder();
            for(Notice notice : monthlyNotice){
                LocalDateTime noTime = notice.getCreatedAt();
                if(noTime.getYear() == yearMonthNow.getYear() &&
                   noTime.getMonth() == yearMonthNow.getMonth()){
                    String text = "[No."+notice.getId()+" / " + noTime.getMonthValue() +"월 " + noTime.getDayOfMonth() + "일 공지사항]" +"\n"
                            + notice.getContent() + "\n\n";
                    sendText.append(text);
                }
            }
            if(sendText.length() == 0) sendText.append("해당 달의 공지사항이 없습니다.");
            slackService.sendMessageToChannel(channelId,"\uD83D\uDC8C 이달의 공지사항",sendText.toString());
        }catch(Exception e){
            log.error("이번달 공지사항 출력 처리 도중 에러 발생", e);
            slackService.sendEphemeralMessageToUser(channelId, userId,"\uD83D\uDCA5 오류 발생",
                    "공지사항 출력 중 오류가 발생했습니다. 관리자에게 문의하세요.");
        }
    }

}
