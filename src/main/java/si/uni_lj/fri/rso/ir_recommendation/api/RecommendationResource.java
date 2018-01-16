package si.uni_lj.fri.rso.ir_recommendation.api;

import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import com.kumuluz.ee.logs.cdi.Log;
import org.eclipse.microprofile.metrics.annotation.Metered;
import si.uni_lj.fri.rso.ir_recommendation.cdi.RecommendationDatabase;
import si.uni_lj.fri.rso.ir_recommendation.models.Recommendation;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.logging.Logger;

@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("recommendations")
@Log
public class RecommendationResource {
    @Inject
    private RecommendationDatabase recommendationDatabase;

    @Context
    protected UriInfo uriInfo;

    private Logger log = Logger.getLogger(RecommendationResource.class.getName());


    @GET
    @Metered
    public Response getAllRecommendations() {
        if (ConfigurationUtil.getInstance().getBoolean("rest-config.endpoint-enabled").orElse(false)) {
            List<Recommendation> recommendations = recommendationDatabase.getRecommendations();
            return Response.ok(recommendations).build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("{\"reason\": \"Endpoint disabled.\"}").build();
        }
    }

    @GET
    @Path("/filtered")
    public Response getRecommendationsFiltered() {
        if (ConfigurationUtil.getInstance().getBoolean("rest-config.endpoint-enabled").orElse(false)) {
            List<Recommendation> customers = recommendationDatabase.getRecommendationsFilter(uriInfo);
            return Response.status(Response.Status.OK).entity(customers).build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("{\"reason\": \"Endpoint disabled.\"}").build();
        }
    }

    @GET
    @Metered
    @Path("/{recommendationId}")
    public Response getRecommendation(@PathParam("recommendationId") String recommendationId, @DefaultValue("true") @QueryParam("includeExtended") boolean includeExtended) {
        if (ConfigurationUtil.getInstance().getBoolean("rest-config.endpoint-enabled").orElse(false)) {
            Recommendation recommendation = recommendationDatabase.getRecommendation(recommendationId, includeExtended);
            return recommendation != null
                    ? Response.ok(recommendation).build()
                    : Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("{\"reason\": \"Endpoint disabled.\"}").build();
        }
    }

    @POST
    @Metered
    public Response addNewRecommendation(Recommendation recommendation) {
        if (ConfigurationUtil.getInstance().getBoolean("rest-config.endpoint-enabled").orElse(false)) {
            recommendationDatabase.createRecommendation(recommendation);
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("{\"reason\": \"Endpoint disabled.\"}").build();
        }
    }

    @DELETE
    @Metered
    @Path("/{recommendationId}")
    public Response deleteRecommendation(@PathParam("recommendationId") String recommendationId) {
        if (ConfigurationUtil.getInstance().getBoolean("rest-config.endpoint-enabled").orElse(false)) {
            recommendationDatabase.deleteRecommendation(recommendationId);
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("{\"reason\": \"Endpoint disabled.\"}").build();
        }
    }
}
