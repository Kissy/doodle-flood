package fr.kissy.doodleflood;

import fr.kissy.doodleflood.model.Doodle;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        Options options = new Options();
        options.addOption(Option.builder().longOpt("hash").desc("doodle hash").hasArg().argName("hash").required().build());
        options.addOption(Option.builder().longOpt("flood").desc("flood the doodle").hasArg().argName("number").build());
        options.addOption(Option.builder().longOpt("clear").desc("remove the flood").build());

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String hash = cmd.getOptionValue("hash");
            DoodleApi doodleService = new DoodleApi();
            Doodle doodle = doodleService.extractDoodle(hash);
            LOGGER.info("Found doodle " + doodle);

            if (cmd.hasOption("flood")) {
                LOGGER.info("Flooding doodle");
                floodDoodle(doodle, doodleService, cmd.getOptionValue("flood"));
            } else if (cmd.hasOption("clear")) {
                LOGGER.info("Clearing doodle");
                doodleService.deleteParticipants(doodle, getSurnames());
            }
        } catch (ParseException ignored) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("doodle-flood", options);
        }
    }

    private static void floodDoodle(Doodle doodle, DoodleApi doodleService, String number) throws InterruptedException {
        List<String> surnames = getSurnames();
        Collections.shuffle(surnames);
        for (int i = 0; i < Integer.parseInt(number); i++) {
            doodleService.addName(doodle, surnames.get(i));
        }
    }

    private static List<String> getSurnames() {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(
                Main.class.getResourceAsStream("/fr/kissy/doodleflood/surname.all.txt"), StandardCharsets.UTF_8
        ))) {
            return buffer.lines().collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Fail to load surnames", e);
            return Collections.emptyList();
        }
    }

}
