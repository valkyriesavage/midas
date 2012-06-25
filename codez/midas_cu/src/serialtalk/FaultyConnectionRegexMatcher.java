package serialtalk;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FaultyConnectionRegexMatcher {
  public static final int REASONABLE_HUMAN_REACTION = 200;
  public static final int REASONABLE_HOLDING_TIME = 10000;
  
  private static final String timestamp = "(\\d{2,20})";
  private static final String UP_CODE = "r";
  private static final String DOWN_CODE = "t";
  private static final String up = "("+UP_CODE+")";
  private static final String down = "("+DOWN_CODE+")";
  private static final String upOrDown = "("+UP_CODE+"|"+DOWN_CODE+")";
  private static final String identifier = "(\\d{1,2}|s\\d{1,3})";
  private static final String terminator = "(;)";

  private static final Pattern flicker = Pattern.compile(timestamp + upOrDown + identifier + terminator +
                                                         timestamp + upOrDown + "(\\3)" + terminator + 
                                                         timestamp + upOrDown + identifier + terminator +
                                                         timestamp + upOrDown + "(\\11)" + terminator);
  private static final Pattern alwaysOn = Pattern.compile(timestamp + down + identifier + terminator +
                                                          timestamp + up + "\\3" + terminator);
  
  public static FaultyConnectionType containsFaultyConnection (String eventStream) {
    Matcher flickerMatcher = flicker.matcher(eventStream);
    Matcher alwaysOnMatcher = alwaysOn.matcher(eventStream);
    
    while (flickerMatcher.find()) {
      long firstFlickerTime = (new BigInteger(flickerMatcher.group(1))).longValue();
      long fourthFlickerTime = (new BigInteger(flickerMatcher.group(13))).longValue();
      if (fourthFlickerTime - firstFlickerTime < REASONABLE_HUMAN_REACTION) {
        return FaultyConnectionType.FLICKER;
      }
    } while (alwaysOnMatcher.find()) {
      long startTime = (new BigInteger(alwaysOnMatcher.group(1))).longValue();
      long endTime = (new BigInteger(alwaysOnMatcher.group(5))).longValue();
      if (endTime - startTime > REASONABLE_HOLDING_TIME) {
        return FaultyConnectionType.ALWAYS_ON;
      }
    }
    return FaultyConnectionType.OK;
  }
  
  public static int[] whichConnectionIsFaulty(String eventStream) {
    Matcher flickerMatcher = flicker.matcher(eventStream);
    Matcher alwaysOnMatcher = alwaysOn.matcher(eventStream);
    
    while (flickerMatcher.find()) {
      long firstFlickerTime = (new BigInteger(flickerMatcher.group(1))).longValue();
      long fourthFlickerTime = (new BigInteger(flickerMatcher.group(13))).longValue();
      if (fourthFlickerTime - firstFlickerTime < REASONABLE_HUMAN_REACTION) {
        return new int[] {Integer.parseInt(flickerMatcher.group(3)), Integer.parseInt(alwaysOnMatcher.group(11))};
      }
    } while (alwaysOnMatcher.find()) {
      long startTime = (new BigInteger(alwaysOnMatcher.group(1))).longValue();
      long endTime = (new BigInteger(alwaysOnMatcher.group(5))).longValue();
      if (endTime - startTime > REASONABLE_HOLDING_TIME) {
        return new int[] {Integer.parseInt(alwaysOnMatcher.group(3))};
      }
    }
    return new int[] {-1};
  }
  
  public static void main(String[] args) {
    List<String> testStrs = new ArrayList<String>();
    testStrs.add("00t0;01r0;02t0;03r0;");
    testStrs.add("00t0;999999r0;");
    testStrs.add("00t0;200r0;650t2;900r2;");
    testStrs.add("1339476066695t0;1339476067569r0;1339476201788t0;1339476226750r0;");
    
    for (String inStr : testStrs) {
      System.out.println("in : " + inStr + "\n\tfaulty? " + FaultyConnectionRegexMatcher.containsFaultyConnection(inStr));
    }
  }
}
