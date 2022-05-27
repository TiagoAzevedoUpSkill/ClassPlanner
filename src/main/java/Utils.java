import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

public class Utils {
    public static void printMapValues(Map<String, String> map) {
        map.forEach((key, value) -> System.out.println(key + " " + value));
    }

    public static String getDateEpochParam(LocalDate date) {
        return Long.toString(date.atStartOfDay().atZone(ZoneId.of("Europe/Lisbon")).toInstant().toEpochMilli());
    }
}
