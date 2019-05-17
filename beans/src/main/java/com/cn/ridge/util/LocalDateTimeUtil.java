package com.cn.ridge.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: create by wang.gf
 * Date: create at 2018/11/15
 */
public class LocalDateTimeUtil {
    public static LocalDate week2Date(LocalDate localDate, Integer week) {
        int localDateWeek = localDate.getDayOfWeek().getValue();
        LocalDate afterLocalDate = null;
        switch (week) {
            case 1:
                switch (localDateWeek) {
                    case 1:
                        afterLocalDate = localDate;
                        break;
                    case 2:
                        afterLocalDate = localDate.minusDays(1);
                        break;
                    case 3:
                        afterLocalDate = localDate.minusDays(2);
                        break;
                    case 4:
                        afterLocalDate = localDate.minusDays(3);
                        break;
                    case 5:
                        afterLocalDate = localDate.minusDays(4);
                        break;
                    case 6:
                        afterLocalDate = localDate.minusDays(5);
                        break;
                    case 7:
                        afterLocalDate = localDate.minusDays(6);
                        break;
                    default:
                        break;
                }
                break;
            case 2:
                switch (localDateWeek) {
                    case 1:
                        afterLocalDate = localDate.plusDays(1);
                        break;
                    case 2:
                        afterLocalDate = localDate;
                        break;
                    case 3:
                        afterLocalDate = localDate.minusDays(1);
                        break;
                    case 4:
                        afterLocalDate = localDate.minusDays(2);
                        break;
                    case 5:
                        afterLocalDate = localDate.minusDays(3);
                        break;
                    case 6:
                        afterLocalDate = localDate.minusDays(4);
                        break;
                    case 7:
                        afterLocalDate = localDate.minusDays(5);
                        break;
                    default:
                        break;
                }
                break;
            case 3:
                switch (localDateWeek) {
                    case 1:
                        afterLocalDate = localDate.plusDays(2);
                        break;
                    case 2:
                        afterLocalDate = localDate.plusDays(1);
                        break;
                    case 3:
                        afterLocalDate = localDate;
                        break;
                    case 4:
                        afterLocalDate = localDate.minusDays(1);
                        break;
                    case 5:
                        afterLocalDate = localDate.minusDays(2);
                        break;
                    case 6:
                        afterLocalDate = localDate.minusDays(3);
                        break;
                    case 7:
                        afterLocalDate = localDate.minusDays(4);
                        break;
                    default:
                        break;
                }
                break;
            case 4:
                switch (localDateWeek) {
                    case 1:
                        afterLocalDate = localDate.plusDays(3);
                        break;
                    case 2:
                        afterLocalDate = localDate.plusDays(2);
                        break;
                    case 3:
                        afterLocalDate = localDate.plusDays(1);
                        break;
                    case 4:
                        afterLocalDate = localDate;
                        break;
                    case 5:
                        afterLocalDate = localDate.minusDays(1);
                        break;
                    case 6:
                        afterLocalDate = localDate.minusDays(2);
                        break;
                    case 7:
                        afterLocalDate = localDate.minusDays(3);
                        break;
                    default:
                        break;
                }
                break;
            case 5:
                switch (localDateWeek) {
                    case 1:
                        afterLocalDate = localDate.plusDays(4);
                        break;
                    case 2:
                        afterLocalDate = localDate.plusDays(3);
                        break;
                    case 3:
                        afterLocalDate = localDate.plusDays(2);
                        break;
                    case 4:
                        afterLocalDate = localDate.plusDays(1);
                        break;
                    case 5:
                        afterLocalDate = localDate;
                        break;
                    case 6:
                        afterLocalDate = localDate.minusDays(1);
                        break;
                    case 7:
                        afterLocalDate = localDate.minusDays(2);
                        break;
                    default:
                        break;
                }
                break;
            case 6:
                switch (localDateWeek) {
                    case 1:
                        afterLocalDate = localDate.plusDays(5);
                        break;
                    case 2:
                        afterLocalDate = localDate.plusDays(4);
                        break;
                    case 3:
                        afterLocalDate = localDate.plusDays(3);
                        break;
                    case 4:
                        afterLocalDate = localDate.plusDays(2);
                        break;
                    case 5:
                        afterLocalDate = localDate.plusDays(1);
                        break;
                    case 6:
                        afterLocalDate = localDate;
                        break;
                    case 7:
                        afterLocalDate = localDate.minusDays(1);
                        break;
                    default:
                        break;
                }
                break;
            case 7:
                switch (localDateWeek) {
                    case 1:
                        afterLocalDate = localDate.plusDays(6);
                        break;
                    case 2:
                        afterLocalDate = localDate.plusDays(5);
                        break;
                    case 3:
                        afterLocalDate = localDate.plusDays(4);
                        break;
                    case 4:
                        afterLocalDate = localDate.plusDays(3);
                        break;
                    case 5:
                        afterLocalDate = localDate.plusDays(2);
                        break;
                    case 6:
                        afterLocalDate = localDate.plusDays(1);
                        break;
                    case 7:
                        afterLocalDate = localDate;
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        return afterLocalDate;
    }

    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }

    /**
     * u-MM-dd HH:MM:ss格式的字符串转LocalDateTime
     *
     * @param dateTime u-MM-dd HH:MM:ss格式的字符串
     * @return LocalDateTime
     */
    public static LocalDateTime String2DateTime(String dateTime) {
        List<Integer> dateTimes = Arrays.stream(
                dateTime.replace(" ", "-").replace(":", "-").split("-"))
                .map(Integer::valueOf)
                .collect(Collectors.toList());
        return LocalDateTime.of(dateTimes.get(0), dateTimes.get(1), dateTimes.get(2), dateTimes.get(3), dateTimes.get(4), dateTimes.get(5));
    }
}
