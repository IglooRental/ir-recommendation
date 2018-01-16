package si.uni_lj.fri.rso.ir_recommendation.models;

import org.eclipse.persistence.annotations.UuidGenerator;
import si.uni_lj.fri.rso.ir_recommendation.models.dependencies.Property;
import si.uni_lj.fri.rso.ir_recommendation.models.dependencies.Review;

import javax.persistence.*;
import java.util.List;

@Entity(name = "recommendations")
@NamedQueries(value = {
        @NamedQuery(name = "Recommendation.getAll", query = "SELECT r FROM recommendations r")
})
@UuidGenerator(name = "idGenerator")
public class Recommendation {
    @Id
    @GeneratedValue(generator = "idGenerator")
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "recommendation_data")
    private String recommendationData;

    @Transient
    private List<Property> properties;

    @Transient
    private List<Review> reviewsSubmitted;

    public Recommendation() {}

    public Recommendation(String id, String userId, String recommendationData) {
        this.id = id;
        this.userId = userId;
        this.recommendationData = recommendationData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String name) {
        this.userId = userId;
    }

    public String getRecommendationData() {
        return recommendationData;
    }

    public void setRecommendationData(String recommendationData) {
        this.recommendationData = recommendationData;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public List<Review> getReviewsSubmitted() {
        return reviewsSubmitted;
    }

    public void setReviewsSubmitted(List<Review> reviewsSubmitted) {
        this.reviewsSubmitted = reviewsSubmitted;
    }
}
