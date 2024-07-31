package SlackWorkBot.WorkBot.Repository;

import SlackWorkBot.WorkBot.Entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice,Long> {
    List<Notice> findByChannelId(String channelId);
}
