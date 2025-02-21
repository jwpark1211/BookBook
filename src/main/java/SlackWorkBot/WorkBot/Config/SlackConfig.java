package SlackWorkBot.WorkBot.Config;

import SlackWorkBot.WorkBot.DTO.AladinBestSellerListResponse;
import SlackWorkBot.WorkBot.DTO.AladinBookSearchListResponse;
import SlackWorkBot.WorkBot.Service.*;
import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.model.block.LayoutBlock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SlackConfig {
    @Value("${slack.token}")
    private String token;
    @Value("${slack.signingSecret}")
    private String signingSecret;

    private final BookService bookService;
    private final BookMessageBuilder bookMessageBuilder;

    @Bean
    public AppConfig loadSingleWorkspaceAppConfig() {
        return AppConfig.builder()
                .singleTeamBotToken(token)
                .signingSecret(signingSecret)
                .build();
    }

    @Bean
    public App initSlackApp(AppConfig config) {
        App app = new App(config);

        /** ===========================
         *  도서 관련 Slash Commands
         *  =========================== */

        // 1) /isbn 예: /isbn 9781234567890
        app.command("/isbn검색", (req, ctx) -> {
            String isbn = req.getPayload().getText(); // 사용자가 입력한 ISBN
            if (isbn == null || isbn.isEmpty()) {
                ctx.respond("검색할 ISBN을 입력해주세요. 예) `/isbn 9781234567890`");
                return ctx.ack();
            }

            // BookService로 검색 (Mono -> block())
            AladinBookSearchListResponse result = bookService.searchByBookISBN(isbn).block();
            if (result == null) {
                ctx.respond("알라딘 API 응답이 없거나 에러가 발생했습니다.");
                return ctx.ack();
            }

            // BookMessageBuilder로 Block Kit 구성 (5권까지)
            List<LayoutBlock> blocks = bookMessageBuilder.createBookSearchResultBlocks(result, isbn);

            // Block Kit으로 응답
            ctx.respond(r -> r
                    .responseType("ephemeral") // 또는 "in_channel"
                    .blocks(blocks)
            );
            return ctx.ack();
        });

        // 2) /keyword 예: /keyword 자바
        app.command("/키워드검색", (req, ctx) -> {
            String keyword = req.getPayload().getText();
            if (keyword == null || keyword.isEmpty()) {
                ctx.respond("검색할 키워드를 입력해주세요. 예) `/keyword 자바`");
                return ctx.ack();
            }

            AladinBookSearchListResponse result = bookService.searchByKeyword(keyword).block();
            if (result == null) {
                ctx.respond("알라딘 API 응답이 없거나 에러가 발생했습니다.");
                return ctx.ack();
            }

            List<LayoutBlock> blocks = bookMessageBuilder.createBookSearchResultBlocks(result, keyword);
            ctx.respond(r -> r
                    .responseType("ephemeral")
                    .blocks(blocks)
            );
            return ctx.ack();
        });

        // 3) /bestseller
        app.command("/베스트셀러", (req, ctx) -> {
            // 베스트셀러 10권
            AladinBestSellerListResponse responseData = bookService.getBestSeller().block();
            if (responseData == null) {
                ctx.respond("베스트셀러 목록을 가져오지 못했습니다.");
                return ctx.ack();
            }

            List<LayoutBlock> blocks = bookMessageBuilder.createBestSellerBlocks(responseData, "베스트셀러 TOP 10");
            ctx.respond(r -> r
                    .responseType("ephemeral")
                    .blocks(blocks)
            );
            return ctx.ack();
        });

        // 4) /newBook
        app.command("/신간", (req, ctx) -> {
            // 베스트셀러 10권
            AladinBestSellerListResponse responseData = bookService.getBestSellerNewBook().block();
            if (responseData == null) {
                ctx.respond("신간 목록을 가져오지 못했습니다.");
                return ctx.ack();
            }

            List<LayoutBlock> blocks = bookMessageBuilder.createBestSellerBlocks(responseData, "신간 TOP 10");
            ctx.respond(r -> r
                    .responseType("ephemeral")
                    .blocks(blocks)
            );
            return ctx.ack();
        });

        //5) /문학베스트셀러
        app.command("/문학베스트셀러", (req, ctx) -> {
            // 베스트셀러 10권
            AladinBestSellerListResponse responseData = bookService.getBestSellerByGenre(1).block();
            if (responseData == null) {
                ctx.respond("문학 베스트셀러 목록을 가져오지 못했습니다.");
                return ctx.ack();
            }

            List<LayoutBlock> blocks = bookMessageBuilder.createBestSellerBlocks(responseData, "문학 베스트셀러 TOP 10");
            ctx.respond(r -> r
                    .responseType("ephemeral")
                    .blocks(blocks)
            );
            return ctx.ack();
        });

        //5) /과학베스트셀러
        app.command("/과학베스트셀러", (req, ctx) -> {
            // 베스트셀러 10권
            AladinBestSellerListResponse responseData = bookService.getBestSellerByGenre(987).block();
            if (responseData == null) {
                ctx.respond("과학 베스트셀러 목록을 가져오지 못했습니다.");
                return ctx.ack();
            }

            List<LayoutBlock> blocks = bookMessageBuilder.createBestSellerBlocks(responseData, "과학 베스트셀러 TOP 10");
            ctx.respond(r -> r
                    .responseType("ephemeral")
                    .blocks(blocks)
            );
            return ctx.ack();
        });

        //5) /경제베스트셀러
        app.command("/경제베스트셀러", (req, ctx) -> {
            // 베스트셀러 10권
            AladinBestSellerListResponse responseData = bookService.getBestSellerByGenre(170).block();
            if (responseData == null) {
                ctx.respond("경제 베스트셀러 목록을 가져오지 못했습니다.");
                return ctx.ack();
            }

            List<LayoutBlock> blocks = bookMessageBuilder.createBestSellerBlocks(responseData, "경제 베스트셀러 TOP 10");
            ctx.respond(r -> r
                    .responseType("ephemeral")
                    .blocks(blocks)
            );
            return ctx.ack();
        });


        return app;
    }

}
