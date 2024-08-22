package SlackWorkBot.WorkBot.DTO;

import com.slack.api.model.Attachment;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
public abstract class BaseResponse {
    private String channel;
    private String user;
    private MsgType type;
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();
}
