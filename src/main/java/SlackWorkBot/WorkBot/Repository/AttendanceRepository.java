package SlackWorkBot.WorkBot.Repository;

import SlackWorkBot.WorkBot.Entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByChannelId(String channelId);
    List<Attendance> findByChannelIdAndUserName(String channelId, String userName);
}
