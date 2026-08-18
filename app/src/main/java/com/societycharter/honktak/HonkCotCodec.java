package com.societycharter.honktak;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/** Bounded CoT 2.0 codec for the non-military HonkTAK custom type. */
public final class HonkCotCodec {
    public static final String COT_TYPE = "b-m-p-s-p-i-honktak-camera";
    public static final String DETAIL_ELEMENT = "honktak_camera";
    public static final int MAX_XML_BYTES = 8192;

    private HonkCotCodec() { }

    public static String serialize(CameraObservation o) {
        String azimuth = o.azimuth == null ? "" : Integer.toString(o.azimuth);
        return "<event version=\"2.0\" uid=\"" + esc(o.uid) + "\" type=\"" + COT_TYPE
            + "\" time=\"" + time(o.observedAtMs) + "\" start=\"" + time(o.observedAtMs)
            + "\" stale=\"" + time(o.staleAtMs) + "\" how=\"h-g-i-g-o\"><point lat=\""
            + o.latitude + "\" lon=\"" + o.longitude + "\" hae=\"0\" ce=\"9999999\" le=\"9999999\"/><detail><contact callsign=\"Unidentified Waterfowl\"/><"
            + DETAIL_ELEMENT + " schema=\"1\" class=\"" + o.cameraClass.name().toLowerCase()
            + "\" azimuth=\"" + azimuth + "\" confidence=\"" + o.confidence.name().toLowerCase()
            + "\" status=\"" + o.status.name().toLowerCase() + "\" notes=\"" + esc(o.notes)
            + "\" observed_at=\"" + time(o.observedAtMs) + "\"/></detail></event>";
    }

    public static CameraObservation parse(String xml, long nowMs) {
        if (xml == null || xml.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > MAX_XML_BYTES) throw new IllegalArgumentException("oversized CoT");
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Document d = f.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            Element event = d.getDocumentElement();
            if (!"event".equals(event.getTagName()) || !COT_TYPE.equals(event.getAttribute("type"))) throw new IllegalArgumentException("wrong CoT type");
            Element point = (Element) event.getElementsByTagName("point").item(0);
            Element detail = (Element) event.getElementsByTagName(DETAIL_ELEMENT).item(0);
            if (point == null || detail == null || !"1".equals(detail.getAttribute("schema"))) throw new IllegalArgumentException("missing detail");
            long observed = millis(detail.getAttribute("observed_at"));
            long stale = millis(event.getAttribute("stale"));
            Integer azimuth = detail.getAttribute("azimuth").isEmpty() ? null : Integer.valueOf(detail.getAttribute("azimuth"));
            CameraObservation o = new CameraObservation(event.getAttribute("uid"),
                Double.parseDouble(point.getAttribute("lat")), Double.parseDouble(point.getAttribute("lon")),
                CameraObservation.CameraClass.valueOf(detail.getAttribute("class").toUpperCase()), azimuth,
                CameraObservation.Confidence.valueOf(detail.getAttribute("confidence").toUpperCase()),
                CameraObservation.Status.valueOf(detail.getAttribute("status").toUpperCase()),
                detail.getAttribute("notes"), observed, stale);
            if (o.isStale(nowMs)) throw new IllegalArgumentException("stale CoT");
            return o;
        } catch (IllegalArgumentException e) { throw e; }
        catch (Exception e) { throw new IllegalArgumentException("malformed CoT", e); }
    }

    private static SimpleDateFormat formatter() { SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US); f.setTimeZone(TimeZone.getTimeZone("UTC")); f.setLenient(false); return f; }
    private static long millis(String value) { try { return formatter().parse(value).getTime(); } catch (ParseException e) { throw new IllegalArgumentException("invalid time", e); } }
    private static String time(long ms) { return formatter().format(new Date(ms)); }
    private static String esc(String s) { return s.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;"); }
}
