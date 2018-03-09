package cc.officina.cloudapilib.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created on 09/03/18.
 *
 * @author Umberto Marini
 */
public class Location {

    /**
     * position : {"type":"Point","coordinates":[11.346931,44.5102854]}
     * id : 89714
     * name : Geom. Busi Busi Massimo Massimo
     * description : Via Giacomo Matteotti, 34, Bologna
     * sourceId : ChIJQaF0NWDTf0cRdadJZrg6fPY
     * streetAddress : null
     * postalCode : null
     * city : null
     * stateProvince : null
     * latitude : 44.5102854
     * longitude : 11.346931
     * nation : null
     * userProvided : falseM
     * eventss : []
     */

    @SerializedName("position")
    private Position mPosition;
    @SerializedName("id")
    private long mId;
    @SerializedName("name")
    private String mName;
    @SerializedName("description")
    private String mDescription;
    @SerializedName("sourceId")
    private String mSourceId;
    @SerializedName("streetAddress")
    private String mStreetAddress;
    @SerializedName("postalCode")
    private String mPostalCode;
    @SerializedName("city")
    private String mCity;
    @SerializedName("stateProvince")
    private String mStateProvince;
    @SerializedName("latitude")
    private double mLatitude;
    @SerializedName("longitude")
    private double mLongitude;
    @SerializedName("nation")
    private Nation mNation;
    @SerializedName("userProvided")
    private boolean mUserProvided;
    @SerializedName("eventss")
    private List<?> mEventss;

    public Position getPosition() {
        return mPosition;
    }

    public void setPosition(Position position) {
        mPosition = position;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getSourceId() {
        return mSourceId;
    }

    public void setSourceId(String sourceId) {
        mSourceId = sourceId;
    }

    public String getStreetAddress() {
        return mStreetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        mStreetAddress = streetAddress;
    }

    public String getPostalCode() {
        return mPostalCode;
    }

    public void setPostalCode(String postalCode) {
        mPostalCode = postalCode;
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String city) {
        mCity = city;
    }

    public String getStateProvince() {
        return mStateProvince;
    }

    public void setStateProvince(String stateProvince) {
        mStateProvince = stateProvince;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public Nation getNation() {
        return mNation;
    }

    public void setNation(Nation nation) {
        mNation = nation;
    }

    public boolean isUserProvided() {
        return mUserProvided;
    }

    public void setUserProvided(boolean userProvided) {
        mUserProvided = userProvided;
    }

    public List<?> getEventss() {
        return mEventss;
    }

    public void setEventss(List<?> eventss) {
        mEventss = eventss;
    }

    public static class Position {

        /**
         * type : Point
         * coordinates : [11.346931,44.5102854]
         */

        @SerializedName("type")
        private String mType;
        @SerializedName("coordinates")
        private List<Double> mCoordinates;

        public String getType() {
            return mType;
        }

        public void setType(String type) {
            mType = type;
        }

        public List<Double> getCoordinates() {
            return mCoordinates;
        }

        public void setCoordinates(List<Double> coordinates) {
            mCoordinates = coordinates;
        }
    }

    // FIXME: verify model
    public static class Nation {

        private long id;
        private String description;
        private List<Location> locations;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<Location> getLocations() {
            return locations;
        }

        public void setLocations(List<Location> locations) {
            this.locations = locations;
        }
    }
}
