package ar.edu.itba.protos.Proxy.Filters;

import ar.edu.itba.protos.Proxy.Metrics.Metrics;

/**
 * Created by sebastian on 10/16/16.
 * Leet conversor
 */
public class Conversor {

    public static boolean applyLeet = true;

    public static String findAndConvert(String s) {
        int message = s.indexOf("<message");

        if (message == -1) {
            return null;
        }

        int bodyStartIndex = s.indexOf("<body>");
        int bodyEndIndex = s.indexOf("</body>");

        if (bodyStartIndex == -1 || bodyEndIndex == -1) {
            return null;
        }

        String body = s.substring(bodyStartIndex + 7, bodyEndIndex);
        StringBuffer newBody = apply(body);

        return s.substring(0,bodyStartIndex+7) +  newBody.toString() + s.substring(bodyEndIndex);
    }


    /**
     *
     * Applies a basic leet conversor the received message
     *
     * @param characters
     * @return the converted message
     */
    private static StringBuffer apply(String characters) {
        StringBuffer stringBuffer = new StringBuffer();
        int length = characters.length();
        for (int i = 0; i < length; i++) {
            switch (characters.charAt(i)) {
                case 'a':
                    stringBuffer.append("4");
                    Metrics.getInstance().addConvertedCharacter();
                    break;
                case 'e':
                    stringBuffer.append("3");
                    Metrics.getInstance().addConvertedCharacter();
                    break;
                case 'i':
                    stringBuffer.append("1");
                    Metrics.getInstance().addConvertedCharacter();
                    break;
                case 'o':
                    stringBuffer.append("0");
                    Metrics.getInstance().addConvertedCharacter();
                    break;
                case 'c':
                    stringBuffer.append("<");
                    Metrics.getInstance().addConvertedCharacter();
                    break;
                default:
                    stringBuffer.append(characters.charAt(i));
                    break;
            }
        }
        return stringBuffer;
    }

}
