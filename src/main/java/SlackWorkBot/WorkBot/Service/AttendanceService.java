package SlackWorkBot.WorkBot.Service;

import SlackWorkBot.WorkBot.Entity.Attendance;
import SlackWorkBot.WorkBot.Repository.AttendanceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {
    @Value("${slack.token}")
    private String slackToken;
    private final RestTemplate restTemplate;
    @Value("${slack.postMsgUrl}")
    private String SLACK_POST_MESSAGE_URL;
    private final AttendanceRepository attendanceRepository;
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

    public boolean saveAttendanceData(String channelId, String employName, String content) {
        try {
            String[] workTime = content.split("~");
            if (workTime.length != 2) {
                throw new IllegalArgumentException("잘못된 시간 형식");
            }

            LocalDateTime checkInTime = convertToLocalDateTime(workTime[0].trim());
            LocalDateTime checkOutTime = convertToLocalDateTime(workTime[1].trim());

            if (checkInTime.isAfter(checkOutTime)) {
                log.error("checkInTime > checkOutTime");
                sendMessageToChannel(channelId, ":x: 퇴근 시간은 반드시 출근 시간 이후 이어야 합니다.");
                return false;
            } else {
                Attendance attendance = Attendance.builder()
                        .channelId(channelId)
                        .employName(employName)
                        .checkInTime(checkInTime)
                        .checkOutTime(checkOutTime)
                        .createdAt(LocalDateTime.now())
                        .build();

                attendanceRepository.save(attendance);
            }

        } catch (DataIntegrityViolationException e) {
            log.error("중복 데이터 저장 시도: {}", content, e);
            sendMessageToChannel(channelId, ":x: 이미 저장된 데이터입니다.");
            return false;
        } catch (IllegalArgumentException e) {
            log.error("잘못된 시간 형식: {}", content, e);
            sendMessageToChannel(channelId, ":x: 잘못된 시간 형식입니다.");
            return false;
        } catch (Exception e) {
            log.error("근무 시간 저장 중 오류 발생", e);
            sendMessageToChannel(channelId, ":x: 근무시간 저장 중 오류가 발생했습니다.");
            return false;
        }
        return true;
    }

    public void getMonthlyRecord(String channelId, String content) {
        try {
            YearMonth recordMonth = convertToYearMonth(content);
            List<Attendance> channelData = attendanceRepository.findByChannelId(channelId);
            StringBuilder sendText = new StringBuilder();
            for (Attendance data : channelData) {
                YearMonth attendanceMonth = YearMonth.from(data.getCheckInTime());
                if (recordMonth.equals(attendanceMonth)) {
                    LocalDateTime cIn = data.getCheckInTime(); LocalDateTime cOut = data.getCheckOutTime();
                    String text = data.getEmployName() + " \uD83D\uDCAD " +
                            cIn.getDayOfMonth() + "일 " + cIn.getHour() + "시 " + cIn.getMinute() + "분" + " ~ " +
                            cOut.getDayOfMonth() + "일 " + cOut.getHour() + "시 " + cOut.getMinute() + "분" + "\n";
                    sendText.append(text);
                }
            }
            if (sendText.length() == 0) {
                sendText.append("해당 월에 대한 기록이 없습니다.");
            }
            sendMessageToChannel(channelId, sendText.toString());
        } catch (IllegalArgumentException e) {
            log.error("잘못된 날짜 형식: {}", content, e);
            sendMessageToChannel(channelId, ":x: 잘못된 날짜 형식입니다.");
        } catch (Exception e) {
            log.error("근무 시간 출력 중 오류 발생", e);
            sendMessageToChannel(channelId, ":x: 근무시간 출력 중 오류가 발생했습니다.");
        }
    }

    public void sendMessageToChannel(String channelId, String content) {
        HttpHeaders headers = getSlackHeaders(slackToken);
        try {
            String body = getJsonBody(channelId, content);
            restTemplate.exchange(SLACK_POST_MESSAGE_URL, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        } catch (Exception e) {
            log.error("메시지 전송 실패: {}", content, e);
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

    private HttpHeaders getSlackHeaders(String slackToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + slackToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String getJsonBody(String channelId, String text) throws JsonProcessingException {
        Map<String, String> jsonObject = new HashMap<>();
        jsonObject.put("channel", channelId);
        jsonObject.put("text", text);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(jsonObject);
    }
}
