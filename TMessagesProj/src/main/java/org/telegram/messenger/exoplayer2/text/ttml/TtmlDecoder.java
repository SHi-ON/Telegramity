/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.telegram.messenger.exoplayer2.text.ttml;

import android.text.Layout;
import android.util.Log;

import org.telegram.messenger.exoplayer2.C;
import org.telegram.messenger.exoplayer2.text.Cue;
import org.telegram.messenger.exoplayer2.text.SimpleSubtitleDecoder;
import org.telegram.messenger.exoplayer2.text.SubtitleDecoderException;
import org.telegram.messenger.exoplayer2.util.ColorParser;
import org.telegram.messenger.exoplayer2.util.Util;
import org.telegram.messenger.exoplayer2.util.XmlPullParserUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link SimpleSubtitleDecoder} for TTML supporting the DFXP presentation profile. Features
 * supported by this decoder are:
 * <ul>
 *   <li>content
 *   <li>core
 *   <li>presentation
 *   <li>profile
 *   <li>structure
 *   <li>time-offset
 *   <li>timing
 *   <li>tickRate
 *   <li>time-clock-with-frames
 *   <li>time-clock
 *   <li>time-offset-with-frames
 *   <li>time-offset-with-ticks
 * </ul>
 * @see <a href="http://www.w3.org/TR/ttaf1-dfxp/">TTML specification</a>
 */
public final class TtmlDecoder extends SimpleSubtitleDecoder {

  private static final String TAG = "TtmlDecoder";

  private static final String TTP = "http://www.w3.org/ns/ttml#parameter";

  private static final String ATTR_BEGIN = "begin";
  private static final String ATTR_DURATION = "dur";
  private static final String ATTR_END = "end";
  private static final String ATTR_STYLE = "style";
  private static final String ATTR_REGION = "region";

  private static final Pattern CLOCK_TIME =
      Pattern.compile("^([0-9][0-9]+):([0-9][0-9]):([0-9][0-9])"
          + "(?:(\\.[0-9]+)|:([0-9][0-9])(?:\\.([0-9]+))?)?$");
  private static final Pattern OFFSET_TIME =
      Pattern.compile("^([0-9]+(?:\\.[0-9]+)?)(h|m|s|ms|f|t)$");
  private static final Pattern FONT_SIZE = Pattern.compile("^(([0-9]*.)?[0-9]+)(px|em|%)$");
  private static final Pattern PERCENTAGE_COORDINATES =
      Pattern.compile("^(\\d+\\.?\\d*?)% (\\d+\\.?\\d*?)%$");

  private static final int DEFAULT_FRAME_RATE = 30;

  private static final FrameAndTickRate DEFAULT_FRAME_AND_TICK_RATE =
      new FrameAndTickRate(DEFAULT_FRAME_RATE, 1, 1);

  private final XmlPullParserFactory xmlParserFactory;

  public TtmlDecoder() {
    super("TtmlDecoder");
    try {
      xmlParserFactory = XmlPullParserFactory.newInstance();
      xmlParserFactory.setNamespaceAware(true);
    } catch (XmlPullParserException e) {
      throw new RuntimeException("Couldn't create XmlPullParserFactory instance", e);
    }
  }

  @Override
  protected TtmlSubtitle decode(byte[] bytes, int length, boolean reset)
      throws SubtitleDecoderException {
    try {
      XmlPullParser xmlParser = xmlParserFactory.newPullParser();
      Map<String, TtmlStyle> globalStyles = new HashMap<>();
      Map<String, TtmlRegion> regionMap = new HashMap<>();
      regionMap.put(TtmlNode.ANONYMOUS_REGION_ID, new TtmlRegion(null));
      ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes, 0, length);
      xmlParser.setInput(inputStream, null);
      TtmlSubtitle ttmlSubtitle = null;
      LinkedList<TtmlNode> nodeStack = new LinkedList<>();
      int unsupportedNodeDepth = 0;
      int eventType = xmlParser.getEventType();
      FrameAndTickRate frameAndTickRate = DEFAULT_FRAME_AND_TICK_RATE;
      while (eventType != XmlPullParser.END_DOCUMENT) {
        TtmlNode parent = nodeStack.peekLast();
        if (unsupportedNodeDepth == 0) {
          String name = xmlParser.getName();
          if (eventType == XmlPullParser.START_TAG) {
            if (TtmlNode.TAG_TT.equals(name)) {
              frameAndTickRate = parseFrameAndTickRates(xmlParser);
            }
            if (!isSupportedTag(name)) {
              Log.i(TAG, "Ignoring unsupported tag: " + xmlParser.getName());
              unsupportedNodeDepth++;
            } else if (TtmlNode.TAG_HEAD.equals(name)) {
              parseHeader(xmlParser, globalStyles, regionMap);
            } else {
              try {
                TtmlNode node = parseNode(xmlParser, parent, regionMap, frameAndTickRate);
                nodeStack.addLast(node);
                if (parent != null) {
                  parent.addChild(node);
                }
              } catch (SubtitleDecoderException e) {
                Log.w(TAG, "Suppressing parser error", e);
                // Treat the node (and by extension, all of its children) as unsupported.
                unsupportedNodeDepth++;
              }
            }
          } else if (eventType == XmlPullParser.TEXT) {
            parent.addChild(TtmlNode.buildTextNode(xmlParser.getText()));
          } else if (eventType == XmlPullParser.END_TAG) {
            if (xmlParser.getName().equals(TtmlNode.TAG_TT)) {
              ttmlSubtitle = new TtmlSubtitle(nodeStack.getLast(), globalStyles, regionMap);
            }
            nodeStack.removeLast();
          }
        } else {
          if (eventType == XmlPullParser.START_TAG) {
            unsupportedNodeDepth++;
          } else if (eventType == XmlPullParser.END_TAG) {
            unsupportedNodeDepth--;
          }
        }
        xmlParser.next();
        eventType = xmlParser.getEventType();
      }
      return ttmlSubtitle;
    } catch (XmlPullParserException xppe) {
      throw new SubtitleDecoderException("Unable to decode source", xppe);
    } catch (IOException e) {
      throw new IllegalStateException("Unexpected error when reading input.", e);
    }
  }

  private FrameAndTickRate parseFrameAndTickRates(XmlPullParser xmlParser)
      throws SubtitleDecoderException {
    int frameRate = DEFAULT_FRAME_RATE;
    String frameRateString = xmlParser.getAttributeValue(TTP, "frameRate");
    if (frameRateString != null) {
      frameRate = Integer.parseInt(frameRateString);
    }

    float frameRateMultiplier = 1;
    String frameRateMultiplierString = xmlParser.getAttributeValue(TTP, "frameRateMultiplier");
    if (frameRateMultiplierString != null) {
      String[] parts = frameRateMultiplierString.split(" ");
      if (parts.length != 2) {
        throw new SubtitleDecoderException("frameRateMultiplier doesn't have 2 parts");
      }
      float numerator = Integer.parseInt(parts[0]);
      float denominator = Integer.parseInt(parts[1]);
      frameRateMultiplier = numerator / denominator;
    }

    int subFrameRate = DEFAULT_FRAME_AND_TICK_RATE.subFrameRate;
    String subFrameRateString = xmlParser.getAttributeValue(TTP, "subFrameRate");
    if (subFrameRateString != null) {
      subFrameRate = Integer.parseInt(subFrameRateString);
    }

    int tickRate = DEFAULT_FRAME_AND_TICK_RATE.tickRate;
    String tickRateString = xmlParser.getAttributeValue(TTP, "tickRate");
    if (tickRateString != null) {
      tickRate = Integer.parseInt(tickRateString);
    }
    return new FrameAndTickRate(frameRate * frameRateMultiplier, subFrameRate, tickRate);
  }

  private Map<String, TtmlStyle> parseHeader(XmlPullParser xmlParser,
      Map<String, TtmlStyle> globalStyles, Map<String, TtmlRegion> globalRegions)
      throws IOException, XmlPullParserException {
    do {
      xmlParser.next();
      if (XmlPullParserUtil.isStartTag(xmlParser, TtmlNode.TAG_STYLE)) {
        String parentStyleId = XmlPullParserUtil.getAttributeValue(xmlParser, ATTR_STYLE);
        TtmlStyle style = parseStyleAttributes(xmlParser, new TtmlStyle());
        if (parentStyleId != null) {
          for (String id : parseStyleIds(parentStyleId)) {
            style.chain(globalStyles.get(id));
          }
        }
        if (style.getId() != null) {
          globalStyles.put(style.getId(), style);
        }
      } else if (XmlPullParserUtil.isStartTag(xmlParser, TtmlNode.TAG_REGION)) {
        TtmlRegion ttmlRegion = parseRegionAttributes(xmlParser);
        if (ttmlRegion != null) {
          globalRegions.put(ttmlRegion.id, ttmlRegion);
        }
      }
    } while (!XmlPullParserUtil.isEndTag(xmlParser, TtmlNode.TAG_HEAD));
    return globalStyles;
  }

  /**
   * Parses a region declaration.
   * <p>
   * If the region defines an origin and extent, it is required that they're defined as percentages
   * of the viewport. Region declarations that define origin and extent in other formats are
   * unsupported, and null is returned.
   */
  private TtmlRegion parseRegionAttributes(XmlPullParser xmlParser) {
    String regionId = XmlPullParserUtil.getAttributeValue(xmlParser, TtmlNode.ATTR_ID);
    if (regionId == null) {
      return null;
    }

    float position;
    float line;
    String regionOrigin = XmlPullParserUtil.getAttributeValue(xmlParser, TtmlNode.ATTR_TTS_ORIGIN);
    if (regionOrigin != null) {
      Matcher originMatcher = PERCENTAGE_COORDINATES.matcher(regionOrigin);
      if (originMatcher.matches()) {
        try {
          position = Float.parseFloat(originMatcher.group(1)) / 100f;
          line = Float.parseFloat(originMatcher.group(2)) / 100f;
        } catch (NumberFormatException e) {
          Log.w(TAG, "Ignoring region with malformed origin: " + regionOrigin);
          return null;
        }
      } else {
        Log.w(TAG, "Ignoring region with unsupported origin: " + regionOrigin);
        return null;
      }
    } else {
      Log.w(TAG, "Ignoring region without an origin");
      return null;
      // TODO: Should default to top left as below in this case, but need to fix
      // https://github.com/google/ExoPlayer/issues/2953 first.
      // Origin is omitted. Default to top left.
      // position = 0;
      // line = 0;
    }

    float width;
    float height;
    String regionExtent = XmlPullParserUtil.getAttributeValue(xmlParser, TtmlNode.ATTR_TTS_EXTENT);
    if (regionExtent != null) {
      Matcher extentMatcher = PERCENTAGE_COORDINATES.matcher(regionExtent);
      if (extentMatcher.matches()) {
        try {
          width = Float.parseFloat(extentMatcher.group(1)) / 100f;
          height = Float.parseFloat(extentMatcher.group(2)) / 100f;
        } catch (NumberFormatException e) {
          Log.w(TAG, "Ignoring region with malformed extent: " + regionOrigin);
          return null;
        }
      } else {
        Log.w(TAG, "Ignoring region with unsupported extent: " + regionOrigin);
        return null;
      }
    } else {
      Log.w(TAG, "Ignoring region without an extent");
      return null;
      // TODO: Should default to extent of parent as below in this case, but need to fix
      // https://github.com/google/ExoPlayer/issues/2953 first.
      // Extent is omitted. Default to extent of parent.
      // width = 1;
      // height = 1;
    }

    @Cue.AnchorType int lineAnchor = Cue.ANCHOR_TYPE_START;
    String displayAlign = XmlPullParserUtil.getAttributeValue(xmlParser,
        TtmlNode.ATTR_TTS_DISPLAY_ALIGN);
    if (displayAlign != null) {
      switch (Util.toLowerInvariant(displayAlign)) {
        case "center":
          lineAnchor = Cue.ANCHOR_TYPE_MIDDLE;
          line += height / 2;
          break;
        case "after":
          lineAnchor = Cue.ANCHOR_TYPE_END;
          line += height;
          break;
        default:
          // Default "before" case. Do nothing.
          break;
      }
    }

    return new TtmlRegion(regionId, position, line, Cue.LINE_TYPE_FRACTION, lineAnchor, width);
  }

  private String[] parseStyleIds(String parentStyleIds) {
    return parentStyleIds.split("\\s+");
  }

  private TtmlStyle parseStyleAttributes(XmlPullParser parser, TtmlStyle style) {
    int attributeCount = parser.getAttributeCount();
    for (int i = 0; i < attributeCount; i++) {
      String attributeValue = parser.getAttributeValue(i);
      switch (parser.getAttributeName(i)) {
        case TtmlNode.ATTR_ID:
          if (TtmlNode.TAG_STYLE.equals(parser.getName())) {
            style = createIfNull(style).setId(attributeValue);
          }
          break;
        case TtmlNode.ATTR_TTS_BACKGROUND_COLOR:
          style = createIfNull(style);
          try {
            style.setBackgroundColor(ColorParser.parseTtmlColor(attributeValue));
          } catch (IllegalArgumentException e) {
            Log.w(TAG, "Failed parsing background value: " + attributeValue);
          }
          break;
        case TtmlNode.ATTR_TTS_COLOR:
          style = createIfNull(style);
          try {
            style.setFontColor(ColorParser.parseTtmlColor(attributeValue));
          } catch (IllegalArgumentException e) {
            Log.w(TAG, "Failed parsing color value: " + attributeValue);
          }
          break;
        case TtmlNode.ATTR_TTS_FONT_FAMILY:
          style = createIfNull(style).setFontFamily(attributeValue);
          break;
        case TtmlNode.ATTR_TTS_FONT_SIZE:
          try {
            style = createIfNull(style);
            parseFontSize(attributeValue, style);
          } catch (SubtitleDecoderException e) {
            Log.w(TAG, "Failed parsing fontSize value: " + attributeValue);
          }
          break;
        case TtmlNode.ATTR_TTS_FONT_WEIGHT:
          style = createIfNull(style).setBold(
              TtmlNode.BOLD.equalsIgnoreCase(attributeValue));
          break;
        case TtmlNode.ATTR_TTS_FONT_STYLE:
          style = createIfNull(style).setItalic(
              TtmlNode.ITALIC.equalsIgnoreCase(attributeValue));
          break;
        case TtmlNode.ATTR_TTS_TEXT_ALIGN:
          switch (Util.toLowerInvariant(attributeValue)) {
            case TtmlNode.LEFT:
              style = createIfNull(style).setTextAlign(Layout.Alignment.ALIGN_NORMAL);
              break;
            case TtmlNode.START:
              style = createIfNull(style).setTextAlign(Layout.Alignment.ALIGN_NORMAL);
              break;
            case TtmlNode.RIGHT:
              style = createIfNull(style).setTextAlign(Layout.Alignment.ALIGN_OPPOSITE);
              break;
            case TtmlNode.END:
              style = createIfNull(style).setTextAlign(Layout.Alignment.ALIGN_OPPOSITE);
              break;
            case TtmlNode.CENTER:
              style = createIfNull(style).setTextAlign(Layout.Alignment.ALIGN_CENTER);
              break;
          }
          break;
        case TtmlNode.ATTR_TTS_TEXT_DECORATION:
          switch (Util.toLowerInvariant(attributeValue)) {
            case TtmlNode.LINETHROUGH:
              style = createIfNull(style).setLinethrough(true);
              break;
            case TtmlNode.NO_LINETHROUGH:
              style = createIfNull(style).setLinethrough(false);
              break;
            case TtmlNode.UNDERLINE:
              style = createIfNull(style).setUnderline(true);
              break;
            case TtmlNode.NO_UNDERLINE:
              style = createIfNull(style).setUnderline(false);
              break;
          }
          break;
        default:
          // ignore
          break;
      }
    }
    return style;
  }

  private TtmlStyle createIfNull(TtmlStyle style) {
    return style == null ? new TtmlStyle() : style;
  }

  private TtmlNode parseNode(XmlPullParser parser, TtmlNode parent,
      Map<String, TtmlRegion> regionMap, FrameAndTickRate frameAndTickRate)
      throws SubtitleDecoderException {
    long duration = C.TIME_UNSET;
    long startTime = C.TIME_UNSET;
    long endTime = C.TIME_UNSET;
    String regionId = TtmlNode.ANONYMOUS_REGION_ID;
    String[] styleIds = null;
    int attributeCount = parser.getAttributeCount();
    TtmlStyle style = parseStyleAttributes(parser, null);
    for (int i = 0; i < attributeCount; i++) {
      String attr = parser.getAttributeName(i);
      String value = parser.getAttributeValue(i);
      switch (attr) {
        case ATTR_BEGIN:
          startTime = parseTimeExpression(value, frameAndTickRate);
          break;
        case ATTR_END:
          endTime = parseTimeExpression(value, frameAndTickRate);
          break;
        case ATTR_DURATION:
          duration = parseTimeExpression(value, frameAndTickRate);
          break;
        case ATTR_STYLE:
          // IDREFS: potentially multiple space delimited ids
          String[] ids = parseStyleIds(value);
          if (ids.length > 0) {
            styleIds = ids;
          }
          break;
        case ATTR_REGION:
          if (regionMap.containsKey(value)) {
            // If the region has not been correctly declared or does not define a position, we use
            // the anonymous region.
            regionId = value;
          }
          break;
        default:
          // Do nothing.
          break;
      }
    }
    if (parent != null && parent.startTimeUs != C.TIME_UNSET) {
      if (startTime != C.TIME_UNSET) {
        startTime += parent.startTimeUs;
      }
      if (endTime != C.TIME_UNSET) {
        endTime += parent.startTimeUs;
      }
    }
    if (endTime == C.TIME_UNSET) {
      if (duration != C.TIME_UNSET) {
        // Infer the end time from the duration.
        endTime = startTime + duration;
      } else if (parent != null && parent.endTimeUs != C.TIME_UNSET) {
        // If the end time remains unspecified, then it should be inherited from the parent.
        endTime = parent.endTimeUs;
      }
    }
    return TtmlNode.buildNode(parser.getName(), startTime, endTime, style, styleIds, regionId);
  }

  private static boolean isSupportedTag(String tag) {
    return tag.equals(TtmlNode.TAG_TT)
        || tag.equals(TtmlNode.TAG_HEAD)
        || tag.equals(TtmlNode.TAG_BODY)
        || tag.equals(TtmlNode.TAG_DIV)
        || tag.equals(TtmlNode.TAG_P)
        || tag.equals(TtmlNode.TAG_SPAN)
        || tag.equals(TtmlNode.TAG_BR)
        || tag.equals(TtmlNode.TAG_STYLE)
        || tag.equals(TtmlNode.TAG_STYLING)
        || tag.equals(TtmlNode.TAG_LAYOUT)
        || tag.equals(TtmlNode.TAG_REGION)
        || tag.equals(TtmlNode.TAG_METADATA)
        || tag.equals(TtmlNode.TAG_SMPTE_IMAGE)
        || tag.equals(TtmlNode.TAG_SMPTE_DATA)
        || tag.equals(TtmlNode.TAG_SMPTE_INFORMATION);
  }

  private static void parseFontSize(String expression, TtmlStyle out) throws
      SubtitleDecoderException {
    String[] expressions = expression.split("\\s+");
    Matcher matcher;
    if (expressions.length == 1) {
      matcher = FONT_SIZE.matcher(expression);
    } else if (expressions.length == 2){
      matcher = FONT_SIZE.matcher(expressions[1]);
      Log.w(TAG, "Multiple values in fontSize attribute. Picking the second value for vertical font"
          + " size and ignoring the first.");
    } else {
      throw new SubtitleDecoderException("Invalid number of entries for fontSize: "
          + expressions.length + ".");
    }

    if (matcher.matches()) {
      String unit = matcher.group(3);
      switch (unit) {
        case "px":
          out.setFontSizeUnit(TtmlStyle.FONT_SIZE_UNIT_PIXEL);
          break;
        case "em":
          out.setFontSizeUnit(TtmlStyle.FONT_SIZE_UNIT_EM);
          break;
        case "%":
          out.setFontSizeUnit(TtmlStyle.FONT_SIZE_UNIT_PERCENT);
          break;
        default:
          throw new SubtitleDecoderException("Invalid unit for fontSize: '" + unit + "'.");
      }
      out.setFontSize(Float.valueOf(matcher.group(1)));
    } else {
      throw new SubtitleDecoderException("Invalid expression for fontSize: '" + expression + "'.");
    }
  }

  /**
   * Parses a time expression, returning the parsed timestamp.
   * <p>
   * For the format of a time expression, see:
   * <a href="http://www.w3.org/TR/ttaf1-dfxp/#timing-value-timeExpression">timeExpression</a>
   *
   * @param time A string that includes the time expression.
   * @param frameAndTickRate The effective frame and tick rates of the stream.
   * @return The parsed timestamp in microseconds.
   * @throws SubtitleDecoderException If the given string does not contain a valid time expression.
   */
  private static long parseTimeExpression(String time, FrameAndTickRate frameAndTickRate)
      throws SubtitleDecoderException {
    Matcher matcher = CLOCK_TIME.matcher(time);
    if (matcher.matches()) {
      String hours = matcher.group(1);
      double durationSeconds = Long.parseLong(hours) * 3600;
      String minutes = matcher.group(2);
      durationSeconds += Long.parseLong(minutes) * 60;
      String seconds = matcher.group(3);
      durationSeconds += Long.parseLong(seconds);
      String fraction = matcher.group(4);
      durationSeconds += (fraction != null) ? Double.parseDouble(fraction) : 0;
      String frames = matcher.group(5);
      durationSeconds += (frames != null)
          ? Long.parseLong(frames) / frameAndTickRate.effectiveFrameRate : 0;
      String subframes = matcher.group(6);
      durationSeconds += (subframes != null)
          ? ((double) Long.parseLong(subframes)) / frameAndTickRate.subFrameRate
              / frameAndTickRate.effectiveFrameRate
          : 0;
      return (long) (durationSeconds * C.MICROS_PER_SECOND);
    }
    matcher = OFFSET_TIME.matcher(time);
    if (matcher.matches()) {
      String timeValue = matcher.group(1);
      double offsetSeconds = Double.parseDouble(timeValue);
      String unit = matcher.group(2);
      switch (unit) {
        case "h":
          offsetSeconds *= 3600;
          break;
        case "m":
          offsetSeconds *= 60;
          break;
        case "s":
          // Do nothing.
          break;
        case "ms":
          offsetSeconds /= 1000;
          break;
        case "f":
          offsetSeconds /= frameAndTickRate.effectiveFrameRate;
          break;
        case "t":
          offsetSeconds /= frameAndTickRate.tickRate;
          break;
      }
      return (long) (offsetSeconds * C.MICROS_PER_SECOND);
    }
    throw new SubtitleDecoderException("Malformed time expression: " + time);
  }

  private static final class FrameAndTickRate {
    final float effectiveFrameRate;
    final int subFrameRate;
    final int tickRate;

    FrameAndTickRate(float effectiveFrameRate, int subFrameRate, int tickRate) {
      this.effectiveFrameRate = effectiveFrameRate;
      this.subFrameRate = subFrameRate;
      this.tickRate = tickRate;
    }
  }
}
