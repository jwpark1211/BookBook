package SlackWorkBot.WorkBot.Service;

import SlackWorkBot.WorkBot.DTO.ErrorNotiResponse;
import SlackWorkBot.WorkBot.DTO.ListResponse;
import SlackWorkBot.WorkBot.Entity.Attendance;
import SlackWorkBot.WorkBot.Repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SlackService slackService;
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

    public void saveAttendanceData(String channelId, String userName, String userId, String checkInDate,
                                   String checkInTime, String checkOutDate, String checkOutTime) {
        try {
            LocalDateTime checkInDateTime = convertToLocalDateTime(checkInDate + " " + checkInTime);
            LocalDateTime checkOutDateTime = convertToLocalDateTime(checkOutDate + " " + checkOutTime);

            if (checkInDateTime.isAfter(checkOutDateTime)) {
                throw new IllegalArgumentException("퇴근 시간은 출근 시간 이후이어야 합니다.");
            }

            Attendance attendance = Attendance.builder()
                    .channelId(channelId)
                    .userName("@" + userName)
                    .userId("<@" + userId + ">")
                    .checkInTime(checkInDateTime)
                    .checkOutTime(checkOutDateTime)
                    .createdAt(LocalDateTime.now())
                    .build();

            attendanceRepository.save(attendance);
            ListResponse response = new ListResponse(channelId,
                    "!NEW! 근무시간 저장 완료","[출근] "+checkInDateTime+"\n"+"[퇴근] "+checkOutDateTime,"#77E4C8");
            slackService.sendMessageToChannelUseBaseResponse(response);

        } catch (DataIntegrityViolationException e) {
            log.error("중복 데이터 저장 시도: {}", e);
            ErrorNotiResponse response =
                    new ErrorNotiResponse(channelId, userId, "데이터 저장 중 오류 발생",
                            "이미 저장된 데이터입니다. 중복 저장은 불가합니다.");
            slackService.sendMessageToChannelUseBaseResponse(response);
        } catch (IllegalArgumentException e) {
            log.error("잘못된 시간 형식: {}", e);
            ErrorNotiResponse response =
                    new ErrorNotiResponse(channelId, userId, "시간 형식 오류 발생",
                            "잘못된 시간 형식입니다.\n"+e.getMessage());
            slackService.sendMessageToChannelUseBaseResponse(response);
        } catch (Exception e) {
            log.error("근무 시간 저장 중 오류 발생", e);
            ErrorNotiResponse response =
                    new ErrorNotiResponse(channelId, userId, "근무 시간 저장 오류 발생",
                            "근무 시간 저장 중 오류가 발생했습니다. 관리자에게 문의하세요.");
            slackService.sendMessageToChannelUseBaseResponse(response);
        }
    }

    public void getMonthlyRecord(String channelId, String userId, String[] contents){
        if(contents.length < 1 || contents.length > 2){
            ErrorNotiResponse response =
                    new ErrorNotiResponse(channelId, userId, "입력 내용 오류 발생",
                            "잘못된 입력입니다. \n[ex] /월별기록 2024-05 {option : @userName}");
            slackService.sendMessageToChannelUseBaseResponse(response);
        }else{
            try {
                YearMonth recordMonth = convertToYearMonth(contents[0]);
                StringBuilder sendText = new StringBuilder();
                List<Attendance> attendances = new ArrayList<>();

                if (contents.length == 1) {
                    attendances = attendanceRepository.findByChannelId(channelId)
                            .stream()
                            .sorted(Comparator.comparing(Attendance::getCheckInTime))
                            .collect(Collectors.toList());
                } else if (contents.length == 2) {
                    attendances = attendanceRepository.findByChannelIdAndUserName(channelId, contents[1])
                            .stream()
                            .sorted(Comparator.comparing(Attendance::getCheckInTime))
                            .collect(Collectors.toList());;
                }

                long workMonthlyTotal = 0L;
                for (Attendance att : attendances) {
                    if (recordMonth.equals(YearMonth.from(att.getCheckInTime()))) {
                        LocalDateTime cIn = att.getCheckInTime();
                        LocalDateTime cOut = att.getCheckOutTime();

                        long workDayTotal = ChronoUnit.MINUTES.between(cIn,cOut);
                        workMonthlyTotal+=workDayTotal;
                        int workHour = (int)(workDayTotal/60);
                        int workMinute = (int)(workDayTotal%60);

                        String text = "[" + att.getId() + "] " + att.getUserId() +
                                cIn.getDayOfMonth() + "일 " + cIn.getHour() + "시 " + cIn.getMinute() + "분" + " ~ " +
                                cOut.getDayOfMonth() + "일 " + cOut.getHour() + "시 " + cOut.getMinute() + "분(" +
                                workHour+"시간 "+workMinute+"분)"+"\n";
                        sendText.append(text);
                    }
                }

                if(contents.length == 2)
                    sendText.append("\n"+"이번 달 총 근무시간: "+workMonthlyTotal/60+"시간 "+workMonthlyTotal%60+"분");
                if (sendText.length() == 0) {
                    log.info("attendances.size={}",attendances.size());
                    sendText.append("해당 월에 대한 기록이 없습니다.");
                }

                ListResponse response = new ListResponse(channelId,
                        "\uD83E\uDD73 "+ recordMonth.getYear()+"년 "+recordMonth.getMonthValue()+"월의 근무 기록"
                        ,sendText.toString(),"#77E4C8");
                slackService.sendMessageToChannelUseBaseResponse(response);

            } catch (IllegalArgumentException e) {
                log.error("잘못된 날짜 형식: {}", contents[0], e);
                ErrorNotiResponse response =
                        new ErrorNotiResponse(channelId, userId, "날짜 형식 처리 중 오류 발생", "잘못된 날짜 형식입니다. (ex: 2024-08)");
                slackService.sendMessageToChannelUseBaseResponse(response);
            } catch (Exception e) {
                log.error("근무 시간 출력 중 오류 발생", e);
                ErrorNotiResponse response =
                        new ErrorNotiResponse(channelId, userId, "근무 시간 출력 중 오류 발생",
                                "근무 시간 출력 중 오류가 발생하였습니다. 관리자에게 문의하세요.");
                slackService.sendMessageToChannelUseBaseResponse(response);
            }
        }
    }


    //*** PRIVATE METHOD ***//
    private LocalDateTime convertToLocalDateTime(String time) {
        try {
            return LocalDateTime.parse(time, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            log.error("시간 변환 실패: {}", time, e);
            throw new IllegalArgumentException("잘못된 시간 형식");
        }
    }

    private YearMonth convertToYearMonth(String date) {
        try {
            return YearMonth.parse(date, monthFormatter);
        } catch (DateTimeParseException e) {
            log.error("날짜 변환 실패: {}", date, e);
            throw new IllegalArgumentException("잘못된 날짜 형식");
        }
    }


}
