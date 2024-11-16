package SlackWorkBot.WorkBot.Service;

import com.slack.api.model.block.*;
import com.slack.api.model.block.element.*;
import com.slack.api.model.block.composition.BlockCompositions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class SlackMessageBuilder {

    public List<LayoutBlock> createAttendanceLayoutBlocks() {
        List<LayoutBlock> layoutBlocks = new ArrayList<>();

        // 텍스트 섹션
        SectionBlock sectionBlock = SectionBlock.builder()
                .text(BlockCompositions.markdownText("근무시간을 설정해주세요:"))
                .build();
        layoutBlocks.add(sectionBlock);

        // 구분선
        layoutBlocks.add(DividerBlock.builder().build());

        // 출근 날짜 선택 (DatePicker)
        ActionsBlock checkInDatePickerBlock = ActionsBlock.builder()
                .elements(asElements(
                        DatePickerElement.builder()
                                .actionId("check_in_datepicker")
                                .placeholder(BlockCompositions.plainText("출근 날짜 선택"))
                                .build()
                ))
                .blockId("check_in_date")
                .build();
        layoutBlocks.add(checkInDatePickerBlock);

        // 출근 시간 선택 (TimePicker)
        ActionsBlock checkInTimePickerBlock = ActionsBlock.builder()
                .elements(asElements(
                        TimePickerElement.builder()
                                .actionId("check_in_timepicker")
                                .placeholder(BlockCompositions.plainText("출근 시간 선택"))
                                .build()
                ))
                .blockId("check_in_time")
                .build();
        layoutBlocks.add(checkInTimePickerBlock);

        // 퇴근 날짜 선택 (DatePicker)
        ActionsBlock checkOutDatePickerBlock = ActionsBlock.builder()
                .elements(asElements(
                        DatePickerElement.builder()
                                .actionId("check_out_datepicker")
                                .placeholder(BlockCompositions.plainText("퇴근 날짜 선택"))
                                .build()
                ))
                .blockId("check_out_date")
                .build();
        layoutBlocks.add(checkOutDatePickerBlock);

        // 퇴근 시간 선택 (TimePicker)
        ActionsBlock checkOutTimePickerBlock = ActionsBlock.builder()
                .elements(asElements(
                        TimePickerElement.builder()
                                .actionId("check_out_timepicker")
                                .placeholder(BlockCompositions.plainText("퇴근 시간 선택"))
                                .build()
                ))
                .blockId("check_out_time")
                .build();
        layoutBlocks.add(checkOutTimePickerBlock);

        // 출근/퇴근 시간 저장 버튼 추가 (고유한 action_id 부여)
        ActionsBlock actionsBlock = ActionsBlock.builder()
                .elements(asElements(
                        ButtonElement.builder()
                                .text(BlockCompositions.plainText(pt -> pt.emoji(true).text("근무시간 저장")))
                                .style("primary")
                                .actionId("action_save_attendance")  // 근무시간 저장 버튼
                                .build()
                ))
                .build();
        layoutBlocks.add(actionsBlock);

        return layoutBlocks;
    }

    // 공지사항 등록 블록 생성 메서드
    public List<LayoutBlock> createNoticeLayoutBlocks() {
        List<LayoutBlock> layoutBlocks = new ArrayList<>();

        // 공지사항 등록 섹션
        layoutBlocks.add(SectionBlock.builder()
                .text(BlockCompositions.markdownText("*공지사항 등록*"))
                .build());

        // 구분선 추가
        layoutBlocks.add(DividerBlock.builder().build());

        // 공지사항 입력 필드 추가
        layoutBlocks.add(InputBlock.builder()
                .label(BlockCompositions.plainText("공지사항 내용 입력"))
                .element(PlainTextInputElement.builder()
                        .actionId("notice_input")
                        .multiline(true)  // 멀티라인 입력 가능
                        .build())
                .blockId("notice_input_block")
                .build());

        // 공지사항 저장 버튼 추가
        layoutBlocks.add(ActionsBlock.builder()
                .elements(asElements(
                        ButtonElement.builder()
                                .text(BlockCompositions.plainText(pt -> pt.emoji(true).text("공지사항 저장")))
                                .style("primary")
                                .actionId("action_save_notice")
                                .build()
                ))
                .build());

        return layoutBlocks;
    }

    public List<LayoutBlock> createScreamLayoutBlocks() {
        List<LayoutBlock> layoutBlocks = new ArrayList<>();

        // 공지사항 등록 섹션
        layoutBlocks.add(SectionBlock.builder()
                .text(BlockCompositions.markdownText("*외치기 등록*"))
                .build());

        // 구분선 추가
        layoutBlocks.add(DividerBlock.builder().build());

        // 공지사항 입력 필드 추가
        layoutBlocks.add(InputBlock.builder()
                .label(BlockCompositions.plainText("내용 입력"))
                .element(PlainTextInputElement.builder()
                        .actionId("scream_input")
                        .multiline(true)  // 멀티라인 입력 가능
                        .build())
                .blockId("scream_input_block")
                .build());

        // 공지사항 저장 버튼 추가
        layoutBlocks.add(ActionsBlock.builder()
                .elements(asElements(
                        ButtonElement.builder()
                                .text(BlockCompositions.plainText(pt -> pt.emoji(true).text("외치기")))
                                .style("primary")
                                .actionId("scream_action")
                                .build()
                ))
                .build());

        return layoutBlocks;
    }

    private List<BlockElement> asElements(BlockElement... elements) {
        List<BlockElement> blockElements = new ArrayList<>();
        for (BlockElement element : elements) {
            blockElements.add(element);
        }
        return blockElements;
    }
}
