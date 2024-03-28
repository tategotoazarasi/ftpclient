package cn.jsou.ftpclient.utils;

import org.ocpsoft.prettytime.PrettyTime;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class TimeUtil {
	private static final PrettyTime p = new PrettyTime(new Locale("zh_CN"));

	public static String formatRelativeTime(LocalDateTime dateTime) {
		long months = ChronoUnit.MONTHS.between(dateTime, LocalDateTime.now());
		if ((months > 12)) {
			// 对于超过1年的，直接返回具体日期
			return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		} else {
			return p.format(dateTime);
		}
	}
}
