package cn.jsou.ftpclient.utils;

import org.ocpsoft.prettytime.PrettyTime;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * 时间工具类
 */
public class TimeUtil {
	/**
	 * PrettyTime实例
	 */
	private static final PrettyTime p = new PrettyTime(new Locale("zh_CN"));

	/**
	 * 格式化相对时间
	 *
	 * @param dateTime 时间
	 *
	 * @return 格式化后的相对时间
	 */
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
