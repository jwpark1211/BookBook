package SlackWorkBot.WorkBot.Service;

import SlackWorkBot.WorkBot.DTO.BaseResponse;
import SlackWorkBot.WorkBot.DTO.MsgType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlackService {

    @Value("${slack.token}")
    private String slackToken;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${slack.postMsgUrl}")
    private String SLACK_POST_MESSAGE_URL;
    @Value("${slack.postEphemeralUrl}")
    private String SLACK_POST_EPHEMERAL_URL;

    @PostConstruct
    private void init() {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public void sendMessageToChannelUseBaseResponse(BaseResponse response){
        HttpHeaders headers = getSlackHeaders(slackToken);
        try{
            String body = objectMapper.writeValueAsString(response);
            String url;
            if (response.getType()== MsgType.IN_CHANNEL) url = SLACK_POST_MESSAGE_URL;
            else url = SLACK_POST_EPHEMERAL_URL;
            log.info("body: {}",body);
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 실패: {}", e);
        } catch (Exception e) {
            log.error("메시지 전송 실패: {}", e);
        }
    }

    private HttpHeaders getSlackHeaders(String slackToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + slackToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
