package si.uni_lj.fri.rso.ir_recommendation.models.dependencies;

import java.util.List;

public class Property {
    private String id;
    private String location;
    private String ownerId;
    private List<Review> reviews;

    public Property(String id, String location, String ownerId) {
        this.id = id;
        this.location = location;
        this.ownerId = ownerId;
    }

    public Property() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
}
