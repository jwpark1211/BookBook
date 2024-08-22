package SlackWorkBot.WorkBot.DTO;

import com.slack.api.model.Attachment;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.BlockCompositions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@SuperBuilder
public class ErrorNotiResponse extends BaseResponse {

    public ErrorNotiResponse(String channelId, String userId,String title, String text){
        super(channelId,userId,MsgType.EPHEMERAL,createAttachments(title,text));
    }

    private static List<Attachment> createAttachments(String title, String text) {
        List<Attachment> attachments = new ArrayList<>();

        Attachment attachment = new Attachment();
        List<LayoutBlock> blocks = new ArrayList<>();

        LayoutBlock headerBlock = Blocks.header(h -> h.text(
                BlockCompositions.plainText(pt -> pt.emoji(true).text("\uD83E\uDD2F 오류 발생"))
        ));
        blocks.add(headerBlock);
        blocks.add(Blocks.section(s->s.text(
                BlockCompositions.markdownText(title)
        )));
        blocks.add(Blocks.divider());
        blocks.add(Blocks.section(s -> s.text(
                BlockCompositions.markdownText(text)
        )));

        attachment.setColor("#C80036");
        attachment.setBlocks(blocks);
        attachments.add(attachment);

        return attachments;
    }
}
