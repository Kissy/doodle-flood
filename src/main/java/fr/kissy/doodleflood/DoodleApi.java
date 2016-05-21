package fr.kissy.doodleflood;

import fr.kissy.doodleflood.model.Doodle;
import fr.kissy.doodleflood.model.Participant;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DoodleApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(DoodleApi.class);

    private static final String DOODLE_BASE_URL = "http://www.doodle.com";
    private static final String PARTICIPANT_URL = "http://www.doodle.com/np/new-polls/%s/participants";
    private static final String DELETE_URL = "http://www.doodle.com/np/new-polls/%s/participants/%s/delete?token=&adminKey=";
    public static final Pattern OPTIONS_HASH_PATTERN = Pattern.compile("\"optionsHash\":\"([^\"]+)\"");
    public static final Pattern OPTIONS_AVAILABLE_PATTERN = Pattern.compile("\"optionsAvailable\":\"([^\"]+)\"");
    public static final Pattern PARTICIPANTS_PATTERN = Pattern.compile("\"participants\":\\[([^\\]]+)\\]");

    public Doodle extractDoodle(String doodleHash) {
        Doodle doodle = new Doodle(doodleHash);
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(DOODLE_BASE_URL + "/poll/" + doodleHash);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            Source source = new Source(response.getEntity().getContent());
            Element titleElement = source.getFirstElement(HTMLElementName.TITLE);
            doodle.setTitle(titleElement.getContent().toString().replace("Doodle: ", "").trim());
            for (Element javascriptElement : source.getAllElements(HTMLElementName.SCRIPT)) {
                String content = javascriptElement.getContent().toString();
                if (content.contains("\"optionsHash\"") && content.contains("optionsAvailable")) {
                    Matcher optionsHashMatcher = OPTIONS_HASH_PATTERN.matcher(content);
                    if (optionsHashMatcher.find()) {
                        doodle.setOptionsHash(optionsHashMatcher.group(1));
                    }
                    Matcher optionsAvailableMatcher = OPTIONS_AVAILABLE_PATTERN.matcher(content);
                    if (optionsAvailableMatcher.find()) {
                        doodle.setOptionsAvailable(optionsAvailableMatcher.group(1));
                    }
                    computeParticipants(doodle, content);
                    break;
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error while fetching the doodle data", e);
        }
        return doodle;
    }

    public void addName(Doodle doodle, String name) {
        String participantUrl = String.format(PARTICIPANT_URL, doodle.getHash());
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(participantUrl);
        ArrayList<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("name", name));
        postParameters.add(new BasicNameValuePair("preferences", computePreferences(doodle)));
        postParameters.add(new BasicNameValuePair("shownCalendars", ""));
        postParameters.add(new BasicNameValuePair("optionsHash", doodle.getOptionsHash()));
        postParameters.add(new BasicNameValuePair("onCalendarView", "false"));
        postParameters.add(new BasicNameValuePair("token", ""));
        postParameters.add(new BasicNameValuePair("locale", "fr_FR"));
        postParameters.add(new BasicNameValuePair("adminKey", ""));
        postParameters.add(new BasicNameValuePair("targetCalendarId", ""));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(postParameters, StandardCharsets.UTF_8.name()));
            HttpResponse response = httpClient.execute(httpPost);
            LOGGER.info("Adding new participant {} with result {}", name, response.getStatusLine().getStatusCode());
        } catch (IOException e) {
            LOGGER.error("Error while adding participant {}", name, e);
        }
    }

    public void deleteParticipants(Doodle doodle, final List<String> includesNames) {
        doodle.getParticipants().stream()
                .filter(participant -> includesNames.contains(participant.getName()))
                .forEach(participant -> {
                    HttpClient httpClient = HttpClientBuilder.create().build();
                    String deleteUrl = String.format(DELETE_URL, doodle.getHash(), participant.getId());
                    HttpPost httpPost = new HttpPost(deleteUrl);
                    try {
                        HttpResponse response = httpClient.execute(httpPost);
                        LOGGER.info("Deleted participant {} with result {}", participant.getName(), response.getStatusLine().getStatusCode());
                    } catch (Exception e) {
                        LOGGER.error("Error while deleting participant {}", participant.getName(), e);
                    }
                });
    }

    private String computePreferences(Doodle doodle) {
        int numberOfPreferences = doodle.getOptionsAvailable().length();
        return new Random().ints(numberOfPreferences, 0, 2)
                .mapToObj(i -> i == 0 ? "y" : "q")
                .collect(Collectors.joining());
    }

    private void computeParticipants(Doodle doodle, String content) {
        Matcher participantsMatcher = PARTICIPANTS_PATTERN.matcher(content);
        if (participantsMatcher.find()) {
            String rawParticipants = participantsMatcher.group(1);
            for (String participant : rawParticipants.split("}")) {
                String id = participant.replaceAll(".*id\":", "").replaceAll(",.*", "").replaceAll("\"", "");
                String name = participant.replaceAll(".*name\":", "").replaceAll(",.*", "").replaceAll("\"", "");
                doodle.getParticipants().add(new Participant(id, name));
            }
        }
    }
}
