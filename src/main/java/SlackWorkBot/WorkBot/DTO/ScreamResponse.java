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
public class ScreamResponse extends BaseResponse {

    public ScreamResponse(String channelId,  String text){
        super(channelId, null, MsgType.IN_CHANNEL,createAttachments(text));
    }

    private static List<Attachment> createAttachments(String text) {
        List<Attachment> attachments = new ArrayList<>();

        Attachment attachment = new Attachment();
        List<LayoutBlock> blocks = new ArrayList<>();

        LayoutBlock headerBlock = Blocks.header(h->h.text(
                BlockCompositions.plainText(pt->pt.emoji(true).text(
                        "\uD83D\uDD25\uD83D\uDD25\uD83D\uDCA5\uD83D\uDCA5집중\uD83D\uDCA5\uD83D\uDCA5\uD83D\uDD25\uD83D\uDD25"))
        ));

        blocks.add(headerBlock);
        blocks.add(Blocks.divider());
        blocks.add(Blocks.section(s -> s.text(
                BlockCompositions.markdownText("*" + text + "*")
        )));

        attachment.setColor("#FF204E");
        attachment.setBlocks(blocks);
        attachments.add(attachment);

        return attachments;
    }
}
