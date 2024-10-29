package com.belka.newDiary.service;

import com.belka.core.services.AbstractCalendarService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@Component
@AllArgsConstructor
public class DiaryCalendarService extends AbstractCalendarService {
    public final static String DAY_DIARY_CALENDAR_CODE = "DIARY_CALENDAR";
    public final static String DIARY_CALENDAR_PREV_MONTH = "DIARY_PREV_MONTH";
    public final static String DIARY_CALENDAR_NEXT_MONTH = "DIARY_NEXT_MONTH";
    private final DiaryService diaryService;

    @Override
    protected List<InlineKeyboardButton> addHeadersToCalendar(Calendar calendar, Integer year, Integer month) {
        List<InlineKeyboardButton> rowHeaders = new ArrayList<>();
        rowHeaders.add(createButton("<<",
                DIARY_CALENDAR_PREV_MONTH + "-" + getPreviousYear(year, month) + "-" + getPreviousMonth(month)));
        rowHeaders.add(createButton(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " " + calendar.get(Calendar.YEAR),
                "IGNORE"));
        rowHeaders.add(createButton(">>",
                DIARY_CALENDAR_NEXT_MONTH + "-" + getNextYear(year, month) + "-" + getNextMonth(month)));
        return rowHeaders;
    }

    @Override
    protected List<InlineKeyboardButton> addDaysToCalendar(List<InlineKeyboardButton> row, Calendar calendar, Long userId) {
        for (int j = 1; j <= 7; j++) {
            LocalDate currentDate = toLocalDate(calendar);
            boolean hasDiaryEntry = diaryService.existsByUserIdAndDate(userId, currentDate);

            InlineKeyboardButton dayButton = createDayButton(calendar, hasDiaryEntry);
            row.add(dayButton);

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return row;
    }

    private InlineKeyboardButton createDayButton(Calendar calendar, boolean hasDiaryEntry) {
        String dayText = hasDiaryEntry ? "-" + calendar.get(Calendar.DAY_OF_MONTH) + "-" : String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String callbackData = DAY_DIARY_CALENDAR_CODE + "." + calendar.get(Calendar.YEAR) + "." + calendar.get(Calendar.MONTH) + "." + calendar.get(Calendar.DAY_OF_MONTH);

        return createButton(dayText, callbackData);
    }

    private int getPreviousYear(int year, int month) {
        return month == 0 ? year - 1 : year;
    }

    private int getNextYear(int year, int month) {
        return month == 11 ? year + 1 : year;
    }

    private int getPreviousMonth(int month) {
        return month == 0 ? 11 : month - 1;
    }

    private int getNextMonth(int month) {
        return month == 11 ? 0 : month + 1;
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    private LocalDate toLocalDate(Calendar calendar) {
        return calendar.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
