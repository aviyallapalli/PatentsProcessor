package lodi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;

/**
 * This class loads the entire raw_google database into memory for efficient lookups.
 */
public class RawGoogle {

    public static class Record extends City{
        public final String inputAddress;
        public final double confidence;

        public Record(String inputAddress, String city, String region, String country,
                      double latitude, double longitude, double confidence)
        {
            super(city, region, country, latitude, longitude);
            this.inputAddress = inputAddress;
            this.confidence = confidence;
        }
    }

    public RawGoogle(Connection conn, double confidenceThreshold)
        throws SQLException
    {
        map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        PreparedStatement pstmt = conn.prepareStatement(
                "select input_address, city, region, country, latitude, longitude, confidence " +
                "from raw_google " +
                "where confidence > ? " +
                "and (city <> '' or region <> '')");
        pstmt.setDouble(1, confidenceThreshold);
        ResultSet rs = pstmt.executeQuery();
                 
        while (rs.next()) {
            String inputAddress = rs.getString(1);
            String city = rs.getString(2);
            String region = rs.getString(3);
            String country = rs.getString(4);
            double latitude = rs.getDouble(5);
            double longitude = rs.getDouble(6);
            double confidence = rs.getDouble(7);

            Record rec = new Record(inputAddress, city, region, country, latitude, longitude, confidence);
            map.put(rec.inputAddress, rec);
        }
    }

    public boolean containsKey(String cleanedLocation) {
        return map.containsKey(cleanedLocation);
    }

    public Record get(String cleanedLocation) {
        return map.get(cleanedLocation);
    }

    public int size() {
        return map.size();
    }

    private final TreeMap<String, Record> map;
}