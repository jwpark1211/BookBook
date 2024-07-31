package SlackWorkBot.WorkBot.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlackService {

    @Value("${slack.token}")
    private String slackToken;
    private final RestTemplate restTemplate;
    @Value("${slack.postMsgUrl}")
    private String SLACK_POST_MESSAGE_URL;
    @Value("${slack.postEphemeralUrl}")
    private String SLACK_POST_EPHEMERAL_URL;

    public void sendMessageToChannel(String channelId, String title ,String content) {
        HttpHeaders headers = getSlackHeaders(slackToken);
        try {
            String body = getJsonBody(channelId, title, content);
            restTemplate.exchange(SLACK_POST_MESSAGE_URL, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        } catch (Exception e) {
            log.error("메시지 전송 실패: {}", content, e);
        }
    }

    public void sendEphemeralMessageToUser(String channelId, String userId,String title, String content) {
        HttpHeaders headers = getSlackHeaders(slackToken);
        try {
            String body = getJsonBody(channelId, userId,title, content);
            restTemplate.exchange(SLACK_POST_EPHEMERAL_URL, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        } catch (Exception e) {
            log.error("Ephemeral 메시지 전송 실패: {}", content, e);
        }
    }

    private HttpHeaders getSlackHeaders(String slackToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + slackToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /*private String getJsonBody(String channelId, String text) throws JsonProcessingException {
        Map<String, String> jsonObject = new HashMap<>();
        jsonObject.put("channel", channelId);
        jsonObject.put("text", text);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(jsonObject);
    }*/

    private String getJsonBody(String channelId, String title, String text) throws JsonProcessingException{
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put("channel", channelId);

        // Attachment 추가
        Map<String, String> attachment = new HashMap<>();
        attachment.put("color", "good");
        attachment.put("pretext", title);
        attachment.put("text", text);
        jsonObject.put("attachments", List.of(attachment));

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(jsonObject);
    }

    private String getJsonBody (String channelId, String userId,String title, String text) throws JsonProcessingException {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put("channel", channelId);
        jsonObject.put("user", userId);

        //Attachment 추가
        Map<String, String> attachment = new HashMap<>();
        attachment.put("color","danger");
        attachment.put("pretext",title);
        attachment.put("text",text);
        jsonObject.put("attachments",List.of(attachment));

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(jsonObject);
    }

}
