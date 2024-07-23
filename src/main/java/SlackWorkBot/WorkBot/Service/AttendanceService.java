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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
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
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public boolean saveAttendanceData(String channelId, String employName, String content) {
        try {
            String[] workTime = content.split("~");
            if (workTime.length != 2) {
                throw new IllegalArgumentException("잘못된 시간 형식");
            }

            LocalDateTime checkInTime = convertToLocalDateTime(workTime[0].trim());
            LocalDateTime checkOutTime = convertToLocalDateTime(workTime[1].trim());

            if(checkInTime.isAfter(checkOutTime)){
                log.error("checkInTime > checkOutTime");
                sendMessageToChannel(channelId, ":x: 퇴근 시간은 반드시 출근 시간 이후 이어야 합니다.");
                return false;
            }else{
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
            throw e;
        } catch (Exception e) {
            log.error("근무 시간 저장 중 오류 발생", e);
            sendMessageToChannel(channelId, ":x: 근무시간 저장 중 오류가 발생했습니다.");
            return false;
        }
        return true;
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
            return LocalDateTime.parse(time, formatter);
        } catch (DateTimeParseException e) {
            log.error("시간 변환 실패: {}", time, e);
            throw new IllegalArgumentException("잘못된 시간 형식");
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
