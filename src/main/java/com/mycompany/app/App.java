package com.mycompany.app;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Hello world!
 */
public class App {

    final static JsonNodeFactory factory = JsonNodeFactory.instance;

    public static void main(String[] args) {

        ObjectMapper mapper = new ObjectMapper();

        ArrayNode newArray = factory.arrayNode();


        try {
            InputStream is = new FileInputStream("/Users/agrigoryan2/workspace/anna-project/ZIPcode.json");

            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                sb.append(line).append("\n");
                line = buf.readLine();
            }
            String fileAsString = sb.toString();

            JsonNode bigFoot = mapper.readTree(fileAsString);


            for (JsonNode root : bigFoot) {

                JsonNode theGeom = root.get("the_geom");
                JsonNode objectId = root.get("OBJECTID");
                JsonNode zipCode = root.get("ZIPCODE");
                JsonNode shapeArea = root.get("Shape_area");
                JsonNode shapeLen = root.get("Shape_len");

                String geomStr = theGeom.asText();


                char[] old = geomStr.replace("MULTIPOLYGON ", "").toCharArray();
                char[] chars = new char[old.length];

                ObjectNode subRoot = factory.objectNode();

                subRoot.put("zipCode", zipCode.asText());
                subRoot.put("area", shapeArea.doubleValue());
                subRoot.put("len", shapeLen.doubleValue());

                ObjectNode location = subRoot.putObject("location");

                location.put("type", "multipolygon");


                walkAll(old, location);

                newArray.add(subRoot);
            }

            System.out.println(newArray);

            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            writer.writeValue(new File("/Users/agrigoryan2/workspace/anna-project/nice.json"), newArray);


        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    private static void walkAll(char[] chars, ObjectNode location) {
        walk3(chars, null, location);
    }

    private static void walk3(char[] chars, ArrayNode parent, ObjectNode location) {

        if (chars == null || chars.length == 0) {
            return;
        }

        if (chars[0] == '(') {

            if (parent == null) {
                walk3(Arrays.copyOfRange(chars, 1, chars.length), location.putArray("coordinates"), location);

            } else {
                walk3(Arrays.copyOfRange(chars, 1, chars.length), parent.addArray(), location);
            }


            return;

        } else if (chars[0] == ')') {

            return;
        }

        String remainder = new String(chars);

        String[] coords = remainder.substring(0, remainder.indexOf(')')).split(",");

        for (String coord : coords) {

            String[] longLat = coord.split(" ");

            if (longLat != null || longLat.length != 0) {
                ArrayNode tuple = parent.addArray();

                if (longLat[0].equals("")) {
                    tuple.add(Double.valueOf(longLat[1]));
                    tuple.add(Double.valueOf(longLat[2]));

                } else {
                    tuple.add(Double.valueOf(longLat[0]));
                    tuple.add(Double.valueOf(longLat[1]));
                }
            }
        }

        walk3(Arrays.copyOfRange(chars, remainder.indexOf(')'), chars.length), parent, location);

    }

    private static void walk2(char[] chars, List parent) {

        if (chars == null || chars.length == 0) {
            return;
        }

        if (chars[0] == '(') {
            List nested = new ArrayList();
            parent.add(nested);

            walk2(Arrays.copyOfRange(chars, 1, chars.length), nested);
            return;
        } else if (chars[0] == ')') {

            return;
        }

        String remainder = new String(chars);

        String[] coords = remainder.substring(0, remainder.indexOf(')')).split(",");

        for (String coord : coords) {

            String[] longLat = coord.split(" ");

            if (longLat != null || longLat.length != 0) {
                if (longLat[0].equals("")) {
                    parent.add(new Coord(Double.valueOf(longLat[1]), Double.valueOf(longLat[2])));
                } else {
                    try {
                        parent.add(new Coord(Double.valueOf(longLat[0]), Double.valueOf(longLat[1])));
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        walk2(Arrays.copyOfRange(chars, remainder.indexOf(')'), chars.length), parent);

    }

    private static List<?> walk(char[] chars, List parent) {

        if (chars == null || chars.length == 0) {
            return parent;
        }

        if (chars[0] == '(') {
            List nested = new ArrayList();
            parent.add(nested);

            return walk(Arrays.copyOfRange(chars, 1, chars.length), nested);
        } else if (chars[0] == ')') {

            return parent;
        }

        String remainder = new String(chars);

        String[] coords = remainder.substring(0, remainder.indexOf(')')).split(",");

        for (String coord : coords) {

            String[] longLat = coord.split(" ");

            if (longLat != null || longLat.length != 0) {
                if (longLat[0].equals("")) {
                    parent.add(new Coord(Double.valueOf(longLat[1]), Double.valueOf(longLat[2])));
                } else {
                    parent.add(new Coord(Double.valueOf(longLat[0]), Double.valueOf(longLat[1])));
                }
            }
        }

        return walk(Arrays.copyOfRange(chars, remainder.indexOf(')'), chars.length), parent);

    }


    private static class Coord {

        double v1, v2;

        public Coord(double v1, double v2) {
            this.v1 = v1;
            this.v2 = v2;
        }

        @Override
        public String toString() {
            return "[" + v1 + "," + v2 + "]";
        }
    }


}
