package SlackWorkBot.WorkBot.Service;

import SlackWorkBot.WorkBot.DTO.AladinBestSellerListResponse;
import SlackWorkBot.WorkBot.DTO.AladinBookSearchListResponse;
import com.slack.api.model.block.DividerBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.model.block.element.ImageElement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookMessageBuilder {

    /**
     * (1) 도서 검색 결과 (최대 5권) - 이모지와 이미지 추가
     */
    public List<LayoutBlock> createBookSearchResultBlocks(AladinBookSearchListResponse response, String keywordOrIsbn) {
        List<LayoutBlock> layoutBlocks = new ArrayList<>();

        // 헤더 (이모지 추가)
        layoutBlocks.add(SectionBlock.builder()
                .text(BlockCompositions.markdownText(
                        String.format(":mag: *도서 검색 결과* (검색어/ISBN: `%s`)", keywordOrIsbn)))
                .build());

        layoutBlocks.add(DividerBlock.builder().build());

        // 검색 결과가 없는 경우
        if (response.item().isEmpty()) {
            layoutBlocks.add(SectionBlock.builder()
                    .text(BlockCompositions.markdownText(
                            String.format(":x: `%s`에 대한 검색 결과가 없습니다.", keywordOrIsbn)))
                    .build());
            return layoutBlocks;
        }

        // 최대 5권 표시
        response.item().stream().limit(5).forEach(book -> {
            // 책 표지 이미지(cover) 사용 가능할 시, accessory로 추가
            // (만약 cover가 null 혹은 유효하지 않은 URL이면 예외처리 필요)
            ImageElement coverImage = ImageElement.builder()
                    .imageUrl(book.cover())
                    .altText("Book cover")
                    .build();

            layoutBlocks.add(SectionBlock.builder()
                    .text(BlockCompositions.markdownText(
                            String.format(
                                    ":bookmark_tabs: *제목:* %s\n" +
                                            ":man_technologist: *저자:* %s\n" +
                                            ":house: *출판사:* %s\n" +
                                            ":moneybag: *가격:* %d원\n" +
                                            ":link: *URL:* %s",
                                    book.title(),
                                    book.author(),
                                    book.publisher(),
                                    book.priceStandard(),
                                    book.link()
                            )
                    ))
                    // accessory 로 책 표지 이미지를 오른쪽에 배치
                    .accessory(coverImage)
                    .build());

            layoutBlocks.add(DividerBlock.builder().build());
        });

        return layoutBlocks;
    }

    /**
     * (2) 베스트셀러 목록 (최대 10권) - 이모지와 이미지 추가
     */
    public List<LayoutBlock> createBestSellerBlocks(AladinBestSellerListResponse response, String titleText) {
        List<LayoutBlock> layoutBlocks = new ArrayList<>();

        // 헤더 (이모지 강조)
        layoutBlocks.add(SectionBlock.builder()
                .text(BlockCompositions.markdownText(
                        String.format(":sparkles: *%s* :sparkles:", titleText)))
                .build());

        layoutBlocks.add(DividerBlock.builder().build());

        // 목록이 비어있는 경우
        if (response.item().isEmpty()) {
            layoutBlocks.add(SectionBlock.builder()
                    .text(BlockCompositions.markdownText(":warning: 베스트셀러 목록이 비어있습니다."))
                    .build());
            return layoutBlocks;
        }

        // 최대 10권
        response.item().stream().limit(10).forEach(book -> {
            // 책 표지 이미지
            ImageElement coverImage = ImageElement.builder()
                    .imageUrl(book.cover())
                    .altText("Book cover")
                    .build();

            layoutBlocks.add(SectionBlock.builder()
                    .text(BlockCompositions.markdownText(
                            String.format(
                                    ":star: *제목:* %s\n" +
                                            ":man_technologist: *저자:* %s\n" +
                                            ":house: *출판사:* %s\n" +
                                            ":trophy: *랭킹:* %d위\n" +
                                            ":link: *URL:* %s",
                                    book.title(),
                                    book.author(),
                                    book.publisher(),
                                    book.bestRank(),
                                    book.link()
                            )
                    ))
                    .accessory(coverImage)
                    .build());

            layoutBlocks.add(DividerBlock.builder().build());
        });

        return layoutBlocks;
    }
}
