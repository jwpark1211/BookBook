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

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ListResponse extends BaseResponse {

    public ListResponse(String channelId, String title, String text, String color) {
        super(channelId, null, MsgType.IN_CHANNEL ,createAttachments(title, text, color));
    }

    private static List<Attachment> createAttachments(String title, String text, String color) {
        List<Attachment> attachments = new ArrayList<>();

        Attachment attachment = new Attachment();
        List<LayoutBlock> blocks = new ArrayList<>();

        LayoutBlock headerBlock = Blocks.header(h -> h.text(
                BlockCompositions.plainText(pt -> pt.emoji(true).text(title))
        ));

        blocks.add(headerBlock);
        blocks.add(Blocks.divider());
        blocks.add(Blocks.section(s -> s.text(
                BlockCompositions.markdownText(text)
        )));

        attachment.setColor(color);
        attachment.setBlocks(blocks);
        attachments.add(attachment);

        return attachments;
    }
}
