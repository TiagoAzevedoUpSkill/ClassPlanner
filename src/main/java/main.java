import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class main {
    public static void main(String[] args) {
        CommandLine commandLine = parseArguments(args);
        String username = commandLine.getOptionValue("username");
        String password = commandLine.getOptionValue("password");
        String id_box = commandLine.getOptionValue("id_box");
        String cookie = login(username, password, id_box);
        getAllClassIds("regybox_user=" + cookie, LocalDate.of(2022,05,29));


    }

//    private static void scheduleClassByDayAndHour(String cookie, LocalDate date, String hour) {
////        getClassId(cookie, date);
////        isClassAvailable(cookie);
////        getURLClassParams(getAllClassesByDay(cookie, date, "marca_aulas.php?"), hour);
//
//    }


//    private static String getSingleClassId(String cookie, LocalDate date) {
//        return "";
//    }

    private static void getAllClassIds(String cookie, LocalDate date) {
        String teste = getAllClassesByDay(cookie, date);

        Matcher m = Pattern.compile("valor3=.{4}").matcher(teste);
        Map<String, String> parameters = new HashMap<>();
        while (m.find())
        {
            parameters.put("valor3=",m.group());
        }
    }

    private static String getDateEpochParam(LocalDate date) {
        return Long.toString(date.atStartOfDay().atZone(ZoneId.of("Europe/Lisbon")).toInstant().toEpochMilli());
    }


//    private static boolean isClassAvailable(String cookie, LocalDate date, String hour, String id_box) {
//
//        // id_box (we got this) and class id (we dont have this yet) needed
//        return false;
//    }

    private static String getURLClassParams(String allClassesByDay, String hour, String separator) {
        String subStringURL = StringUtils.substringAfter(allClassesByDay.split(hour)[1], separator);
        return subStringURL.substring(0, subStringURL.indexOf("'"));
    }

    private static String getAllClassesByDay(String cookie, LocalDate date) {
        String url = "https://www.regybox.pt/app/app_nova/php/aulas/aulas.php";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("valor1", getDateEpochParam(date));

        String form = toURLParameters(parameters);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet getRequest = new HttpGet(url + "?" + form);
        getRequest.addHeader("Cookie", cookie);

        return getResponse(client, getRequest);
    }

    private static String login(String username, String password, String id_box) {
        String url = "https://www.regybox.pt/app/app_nova/php/login/scripts/verifica_acesso.php";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("login", username);
        parameters.put("password", password);
        parameters.put("id_box", id_box);
        String form = toURLParameters(parameters);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        post.setEntity(new StringEntity(form, StandardCharsets.UTF_8));
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");

        final String response = getResponse(client, post);

        return obterCookie(response);
    }

    private static String obterCookie(String response) {
        String array = response.split("\n")[1];
        return array.substring(array.indexOf("=") + 1, array.indexOf("&"));
    }

    private static String getResponse(HttpClient client, HttpRequestBase requestBase) {
        final String response;
        try {
            response = client.execute(requestBase, new BasicResponseHandler());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private static String toURLParameters(Map<String, String> parameters) {
        return parameters.entrySet().stream().map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));
    }


    private static CommandLine parseArguments(final String[] args) {
        final Options options = new Options();
        options.addOption("", "username", true, "");
        options.addOption("", "password", true, "");
        options.addOption("", "id_box", true, "");

        final CommandLine commandLine;
        try {
            commandLine = new DefaultParser().parse(options,
                    Arrays.stream(args).filter(arg -> !arg.startsWith("-D")).toArray(String[]::new));

        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }

        return commandLine;
    }
}
