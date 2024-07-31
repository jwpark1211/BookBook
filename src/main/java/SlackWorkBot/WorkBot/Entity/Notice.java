package SlackWorkBot.WorkBot.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice {

    @Id @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String channelId;
    private String userId;
    private String content;

    @CreationTimestamp
    @DateTimeFormat(pattern = "yyyy-mm-dd")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public Notice(String channelId, String userId, String content ){
        this.channelId = channelId;
        this.userId = userId;
        this.content = content;
    }

}
