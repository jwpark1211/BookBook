package SlackWorkBot.WorkBot.Service;

import SlackWorkBot.WorkBot.Entity.Attendance;
import SlackWorkBot.WorkBot.Repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SlackService slackService;
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

    public void saveAttendanceData(String channelId, String userName, String userId, String content) {
        try {
            String[] workTime = content.split("~");
            if (workTime.length != 2) {
                throw new IllegalArgumentException("근무시간 형식이 잘못되었습니다.");
            }

            LocalDateTime checkInTime = convertToLocalDateTime(workTime[0].trim());
            LocalDateTime checkOutTime = convertToLocalDateTime(workTime[1].trim());

            if (checkInTime.isAfter(checkOutTime)) {
                log.error("checkInTime > checkOutTime");
                throw new IllegalArgumentException("퇴근 시간은 출근 시간 이후이어야 합니다.");
            } else {
                Attendance attendance = Attendance.builder()
                        .channelId(channelId)
                        .userName("@"+userName)
                        .userId("<@"+userId+">")
                        .checkInTime(checkInTime)
                        .checkOutTime(checkOutTime)
                        .createdAt(LocalDateTime.now())
                        .build();

                attendanceRepository.save(attendance);
                slackService.sendMessageToChannel(channelId,
                        ":white_check_mark: <@" + userId + ">님의 근무시간 : " + content);
            }

        } catch (DataIntegrityViolationException e) {
            log.error("중복 데이터 저장 시도: {}", content, e);
            slackService.sendEphemeralMessageToUser(channelId, userId,":x: 이미 저장된 데이터입니다.");
        } catch (IllegalArgumentException e) {
            log.error("잘못된 시간 형식: {}", content, e);
            slackService.sendEphemeralMessageToUser(channelId, userId ,":x: 잘못된 시간 형식입니다 : " + e.getMessage());
        } catch (Exception e) {
            log.error("근무 시간 저장 중 오류 발생", e);
            slackService.sendEphemeralMessageToUser(channelId, userId,":x: 근무시간 저장 중 오류가 발생했습니다.");
        }
    }

    public void getMonthlyRecord(String channelId, String userId, String[] contents){
        if(contents.length < 1 || contents.length > 2){
            slackService.sendEphemeralMessageToUser(channelId,
                    userId,":x: 잘못된 입력입니다. [ex] /월별기록 2024-05 {option : @userName}");
        }else{
            try {
                YearMonth recordMonth = convertToYearMonth(contents[0]);
                StringBuilder sendText = new StringBuilder();
                List<Attendance> attendances = new ArrayList<>();
                slackService.sendMessageToChannel(channelId,"\uD83D\uDCDD "+contents[0]+"의 기록...\n");
                if (contents.length == 1) {
                    attendances = attendanceRepository.findByChannelId(channelId);
                } else if (contents.length == 2) {
                    attendances = attendanceRepository.findByChannelIdAndUserName(channelId, contents[1]);
                }
                for (Attendance att : attendances) {
                    YearMonth attendanceMonth = YearMonth.from(att.getCheckInTime());
                    if (recordMonth.equals(attendanceMonth)) {
                        LocalDateTime cIn = att.getCheckInTime();
                        LocalDateTime cOut = att.getCheckOutTime();
                        String text = "[" + att.getId() + "] " + att.getUserId() + " \uD83D\uDCAD " +
                                cIn.getDayOfMonth() + "일 " + cIn.getHour() + "시 " + cIn.getMinute() + "분" + " ~ " +
                                cOut.getDayOfMonth() + "일 " + cOut.getHour() + "시 " + cOut.getMinute() + "분" + "\n";
                        sendText.append(text);
                    }
                }
                if (sendText.length() == 0) {
                    log.info("attendances.size={}",attendances.size());
                    sendText.append("해당 월에 대한 기록이 없습니다.");
                }
                slackService.sendMessageToChannel(channelId, sendText.toString());
            } catch (IllegalArgumentException e) {
                log.error("잘못된 날짜 형식: {}", contents[0], e);
                slackService.sendMessageToChannel(channelId, ":x: 잘못된 날짜 형식입니다.");
            } catch (Exception e) {
                log.error("근무 시간 출력 중 오류 발생", e);
                slackService.sendMessageToChannel(channelId, ":x: 근무시간 출력 중 오류가 발생했습니다.");
            }
        }
    }

    private LocalDateTime convertToLocalDateTime(String time) {
        try {
            return LocalDateTime.parse(time, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            log.error("시간 변환 실패: {}", time, e);
            throw new IllegalArgumentException("잘못된 시간 형식");
        }
    }

    private YearMonth convertToYearMonth(String date) {
        try {
            return YearMonth.parse(date, monthFormatter);
        } catch (DateTimeParseException e) {
            log.error("날짜 변환 실패: {}", date, e);
            throw new IllegalArgumentException("잘못된 날짜 형식");
        }
    }


}
