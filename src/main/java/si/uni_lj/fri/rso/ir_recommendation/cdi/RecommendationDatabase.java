package si.uni_lj.fri.rso.ir_recommendation.cdi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import si.uni_lj.fri.rso.ir_recommendation.models.Recommendation;
import si.uni_lj.fri.rso.ir_recommendation.models.dependencies.Property;
import si.uni_lj.fri.rso.ir_recommendation.models.dependencies.Review;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RequestScoped
public class RecommendationDatabase {
    private Logger log = LogManager.getLogger(RecommendationDatabase.class.getName());

    @Inject
    private EntityManager em;

    // fault tolerance needs to be run through a CDI bean, so we can't just call this.method(),
    // instead, we have to inject ourselves
    @Inject
    private RecommendationDatabase recommendationDatabase;

    private HttpClient httpClient;
    private ObjectMapper objectMapper;

    // recommendation-catalogue-service is the kumuluzee application name as defined in the yaml file
    // this also MUST be in a bean (see @requestscoped above) for it to work
    @Inject
    @DiscoverService("property-catalogue-service")
    private String propertyCatalogueBasePath;

    @Inject
    @DiscoverService("review-service")
    private String reviewBasePath;

    @PostConstruct
    private void init() {
        httpClient = HttpClientBuilder.create().build();
        objectMapper = new ObjectMapper();
    }

    private List<Property> getRecommendationObjects(String json) throws IOException {
        return json == null ? new ArrayList<>() : objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Property.class));
    }

    private List<Review> getReviewObjects(String json) throws IOException {
        return json == null ? new ArrayList<>() : objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Review.class));
    }

    public List<Recommendation> getRecommendations() {
        TypedQuery<Recommendation> query = em.createNamedQuery("Recommendation.getAll", Recommendation.class);
        return query.getResultList();
    }

    public List<Recommendation> getRecommendationsFilter(UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery()).defaultOffset(0).build();
        return JPAUtils.queryEntities(em, Recommendation.class, queryParameters);
    }

    public Recommendation createRecommendation(Recommendation recommendation) {
        try {
            beginTx();
            em.persist(recommendation);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return recommendation;
    }

    public Recommendation putRecommendation(String recommendationId, Recommendation recommendation) {
        Recommendation p = em.find(Recommendation.class, recommendationId);
        if (p == null) {
            return null;
        }
        try {
            beginTx();
            recommendation.setId(p.getId());
            recommendation = em.merge(recommendation);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }
        return recommendation;
    }

    public boolean deleteRecommendation(String recommendationId) {
        Recommendation p = em.find(Recommendation.class, recommendationId);
        if (p != null) {
            try {
                beginTx();
                em.remove(p);
                commitTx();
            } catch (Exception e) {
                rollbackTx();
            }
        } else {
            return false;
        }
        return true;
    }

    public Recommendation getRecommendation(String recommendationId, boolean includeExtended) {
        Recommendation recommendation = em.find(Recommendation.class, recommendationId);
        if (recommendation == null) {
            throw new NotFoundException();
        }
        if (includeExtended) {
            int recId = Integer.parseInt(recommendation.getUserId());
            Random rand = new Random();
            int recInt = rand.nextInt(2) + 1;
            while (recInt == recId) recInt = rand.nextInt(2) + 1;

            recommendation.setProperties(recommendationDatabase.getProperties(Integer.toString(recInt)));
            recommendation.setReviewsSubmitted(recommendationDatabase.getReviews(Integer.toString(recInt)));
        }
        return recommendation;
    }

    @CircuitBreaker(requestVolumeThreshold = 2)
    @Fallback(fallbackMethod = "getPropertiesFallback")
    @Timeout
    public List<Property> getProperties(String recommendationId) {
        if (propertyCatalogueBasePath != null) {
            try {
                HttpGet request = new HttpGet(propertyCatalogueBasePath + "/v1/properties/filtered?where=ownerId:EQ:" + recommendationId);
                HttpResponse response = httpClient.execute(request);

                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        return getRecommendationObjects(EntityUtils.toString(entity));
                    }
                } else {
                    String msg = "Remote server '" + propertyCatalogueBasePath + "' has responded with status " + status + ".";
                    throw new InternalServerErrorException(msg);
                }

            } catch (IOException e) {
                String msg = e.getClass().getName() + " occurred: " + e.getMessage();
                throw new InternalServerErrorException(msg);
            }
        } else {
            // service not available placeholder
            log.error("base path is null");
        }
        return new ArrayList<>();
    }

    @CircuitBreaker(requestVolumeThreshold = 2)
    @Fallback(fallbackMethod = "getReviewsFallback")
    @Timeout
    public List<Review> getReviews(String userId) {
        if (reviewBasePath != null) {
            try {
                HttpGet request = new HttpGet(reviewBasePath + "/v1/reviews/filtered?where=userId:EQ:" + userId);
                HttpResponse response = httpClient.execute(request);

                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        return getReviewObjects(EntityUtils.toString(entity));
                    }
                } else {
                    String msg = "Remote server '" + reviewBasePath + "' has responded with status " + status + ".";
                    throw new InternalServerErrorException(msg);
                }

            } catch (IOException e) {
                String msg = e.getClass().getName() + " occurred: " + e.getMessage();
                throw new InternalServerErrorException(msg);
            }
        } else {
            // service not available placeholder
            log.error("base path is null");
        }
        return new ArrayList<>();
    }

    public List<Review> getReviewsFallback(String userId) {
        ArrayList<Review> result = new ArrayList<>();
        Review review = new Review();
        review.setHeader("N/A");
        review.setMessage("N/A");
        review.setScore(0);
        result.add(review);
        return result;
    }

    public List<Property> getPropertiesFallback(String recommendationId) {
        ArrayList<Property> result = new ArrayList<>();
        Property property = new Property();
        property.setLocation("N/A");
        result.add(property);
        return result;
    }

    private void beginTx() {
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
    }

    private void commitTx() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().commit();
        }
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}
