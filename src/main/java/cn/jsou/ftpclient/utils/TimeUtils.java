package cn.jsou.ftpclient.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TimeUtils {
	public static String formatRelativeTime(LocalDateTime dateTime) {
		LocalDateTime now     = LocalDateTime.now();
		long          seconds = ChronoUnit.SECONDS.between(dateTime, now);
		long          minutes = ChronoUnit.MINUTES.between(dateTime, now);
		long          hours   = ChronoUnit.HOURS.between(dateTime, now);
		long          days    = ChronoUnit.DAYS.between(dateTime, now);
		long          weeks   = ChronoUnit.WEEKS.between(dateTime, now);
		long          months  = ChronoUnit.MONTHS.between(dateTime, now);

		if (seconds < 60) {
			return "刚刚";
		} else if (minutes < 60) {
			return minutes + "分钟前";
		} else if (hours < 24) {
			return hours + "小时前";
		} else if (days < 7) {
			return days + "天前";
		} else if (weeks < 4) {
			return weeks + "星期前";
		} else if (months < 12) {
			return months + "个月前";
		} else {
			// 对于超过30天的，直接返回具体日期
			return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}
	}
}
