package SlackWorkBot.WorkBot.Service;

import SlackWorkBot.WorkBot.Config.AladinOpenApiClient;
import SlackWorkBot.WorkBot.DTO.AladinBestSellerListResponse;
import SlackWorkBot.WorkBot.DTO.AladinBookSearchListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookService {

    private final AladinOpenApiClient aladinOpenApi;

    public Mono<AladinBookSearchListResponse> searchByBookISBN(String isbn) {
        return aladinOpenApi.searchByBookISBN(isbn);
    }

    public Mono<AladinBookSearchListResponse> searchByKeyword(String keyword) {
        return aladinOpenApi.searchByKeyword(keyword);
    }

    @Cacheable(value = "bestsellers", cacheManager = "cacheManager")
    public Mono<AladinBestSellerListResponse> getBestSeller() {
        return aladinOpenApi.getAllBestSeller();
    }

    @Cacheable(value = "bestsellersByGenre", key = "#a0", unless = "#result == null")
    public Mono<AladinBestSellerListResponse> getBestSellerByGenre(int cid) {
        return aladinOpenApi.getBestSellerByGenre(cid);
    }

    @Cacheable(value = "newBooks", cacheManager = "cacheManager")
    public Mono<AladinBestSellerListResponse> getBestSellerNewBook() {
        return aladinOpenApi.getNewBook();
    }

    @Cacheable(value = "blogChoices", cacheManager = "cacheManager")
    public Mono<AladinBestSellerListResponse> getBlogChoice() {
        return aladinOpenApi.getBlogChoice();
    }


}
