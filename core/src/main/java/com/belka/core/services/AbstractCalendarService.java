package com.belka.core.services;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Abstract service for generating calendar messages.
 */
public abstract class AbstractCalendarService {
    private final static String CALENDAR = "Calendar";

    /**
     * Sends a calendar message to the specified chat.
     *
     * @param chatId the chat ID
     * @param year   the year to display
     * @param month  the month to display
     * @return the SendMessage object containing the calendar
     */
    public SendMessage sendCalendarMessage(Long chatId, Integer year, Integer month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(addHeadersToCalendar(calendar, year, month));
        rows.add(addDaysOfTheWeekToCalendar(calendar));
        calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        for (int i = 1; i <= 5; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            rows.add(addDaysToCalendar(row, calendar, chatId));
        }
        markup.setKeyboard(rows);
        SendMessage message = new SendMessage(String.valueOf(chatId), CALENDAR);
        message.setReplyMarkup(markup);
        return message;
    }

    /**
     * Adds headers to the calendar.
     *
     * @param calendar the calendar instance
     * @param year     the year to display
     * @param month    the month to display
     * @return a list of InlineKeyboardButton objects representing the headers
     */
    protected abstract List<InlineKeyboardButton> addHeadersToCalendar(Calendar calendar, Integer year, Integer month);

    /**
     * Adds days of the week to the calendar.
     *
     * @param calendar the calendar instance
     * @return a list of InlineKeyboardButton objects representing the days of the week
     */
    private List<InlineKeyboardButton> addDaysOfTheWeekToCalendar(Calendar calendar) {
        List<InlineKeyboardButton> rowDaysOfTheWeek = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US));
            inlineKeyboardButton.setCallbackData("DAY-" + calendar.get(Calendar.DAY_OF_MONTH));
            rowDaysOfTheWeek.add(inlineKeyboardButton);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return rowDaysOfTheWeek;
    }

    /**
     * Adds days to the calendar.
     *
     * @param row      the row to add the days to
     * @param calendar the calendar instance
     * @param userId   the user ID
     * @return a list of InlineKeyboardButton objects representing the days
     */
    protected abstract List<InlineKeyboardButton> addDaysToCalendar(List<InlineKeyboardButton> row, Calendar calendar, Long userId);
}
// todo try to do calendar with first day of week as monday
