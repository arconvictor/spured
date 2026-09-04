package de.victorarcon.spured.resource;

import de.hansemerkur.port.history.core.NumericTransactionIdProvider;
import de.hansemerkur.port.history.core.UserIdProvider;
import de.victorarcon.spured.api.GrosskundeApi;
import de.victorarcon.spured.dto.GrosskundeDTOMapper;
import de.victorarcon.spured.dto.GrosskundeDto;
import de.victorarcon.spured.dto.GrosskundeService;
import de.victorarcon.spured.entity.HGrosskunde;
import de.victorarcon.spured.entity.SGrosskunde;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST endpoints for Grosskunde entities under {@code /v2/clients}.
 * Implements {@link GrosskundeApi}; delegates all business logic to {@link GrosskundeService}.
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/v2/clients")
public class GrosskundeResource implements GrosskundeApi {
    private final Clock clock;
    private final GrosskundeService grosskundeservice;
    private final UriInfo uriInfo;
    private final GrosskundeDTOMapper dtoMapper;
    private final NumericTransactionIdProvider transactionIdProvider;
    private final UserIdProvider userIdProvider;

    /**
     * Constructor injecting all required dependencies for business logic, mapping, and context.
     *
     * @param clock                  system clock for timestamping
     * @param grosskundeservice      service layer for Grosskunde operations
     * @param uriInfo                URI context for building resource links
     * @param dtoMapper              mapper for converting DTOs to input models
     * @param transactionIdProvider  provider for transaction identifiers
     * @param userIdProvider         provider for user identifiers
     */
    @Inject
    public GrosskundeResource(Clock clock, GrosskundeService grosskundeservice, UriInfo uriInfo, GrosskundeDTOMapper dtoMapper, NumericTransactionIdProvider transactionIdProvider, UserIdProvider userIdProvider) {
        this.clock = clock;
        this.grosskundeservice = grosskundeservice;
        this.uriInfo = uriInfo;
        this.dtoMapper = dtoMapper;
        this.transactionIdProvider = transactionIdProvider;
        this.userIdProvider = userIdProvider;
    }

    /**
     * Retrieves all current Grosskunde entities.
     *
     * @return HTTP 200 response with a list of GrosskundeDto
     */
    @Override
    @GET
    public Response readAllGrosskunden() {
        List<SGrosskunde> grosskunden = grosskundeservice.findAll();

        var grosskundeDtos = grosskunden.stream()
                .map(grosskunde -> new GrosskundeDto(grosskunde.getId(), grosskunde.getkurzBezeichnung(), grosskunde.getlangBezeichnung(),
                        grosskunde.getDgbdat(),
                        grosskunde.getDgedat(), grosskunde.getGrosskundeNr(), grosskunde.getDruckText()))
                .toList();

        return Response.ok(grosskundeDtos).build();
    }

    /**
     * Retrieves a specific Grosskunde entity by ID.
     * If the 'history' query parameter is true, returns the full historical timeline.
     *
     * @param id      Grosskunde ID
     * @param history whether to include historical versions
     * @return HTTP 200 with GrosskundeDto or list of historical versions; 404 if not found
     */
    @Override
    @GET
    @Path("{id}")
    public Response readGrosskunde(@PathParam("id") Long id, @QueryParam("history") @DefaultValue("false") boolean history) {
        if (history) {
            List<HGrosskunde> historyById = grosskundeservice.findHistoryById(id);
            var grosskundeDtos = historyById.stream()
                    .map(grosskunde -> new GrosskundeDto(grosskunde.getId(), grosskunde.getkurzBezeichnung(), grosskunde.getlangBezeichnung(), grosskunde.getDgbdat(), grosskunde.getDgedat(), grosskunde.getGrosskundeNr(), grosskunde.getDruckText()))
                    .toList();

            return Response.ok(grosskundeDtos).build();
        } else {
            SGrosskunde grosskunde = grosskundeservice.findById(id);
            if (grosskunde != null) {
                GrosskundeDto grosskundeDto = new GrosskundeDto(grosskunde.getId(),
                        grosskunde.getkurzBezeichnung(),
                        grosskunde.getlangBezeichnung(),
                        grosskunde.getDgbdat(),
                        grosskunde.getDgedat(),
                        grosskunde.getGrosskundeNr(),
                        grosskunde.getDruckText());

                return Response.ok(grosskundeDto).build();
            }
        }

        String errorMessage = """ 
                {
                "error": "Grosskunde not found"
                }
                """;

        return Response.status(Response.Status.NOT_FOUND).entity(errorMessage).build();
    }

    /**
     * Creates a new Grosskunde entity.
     *
     * @param grosskundeDto the data transfer object containing input data
     * @return HTTP 201 with location header and persisted entity
     */
    @POST
    @Transactional //FIXME: Ist Transactional hier noetig?
    public Response createGrosskunde(GrosskundeDto grosskundeDto) {
        var grosskundeInput = dtoMapper.toGrosskundeInput(grosskundeDto);
        SGrosskunde savedGrosskunde = grosskundeservice.save(grosskundeInput, userIdProvider, transactionIdProvider);
        URI createdUri = uriInfo.getRequestUriBuilder().path(String.valueOf(savedGrosskunde.getId())).build();
        return Response.created(createdUri).entity(savedGrosskunde).build();
    }

    /**
     * Saves a Grosskunde entity with a specific ID.
     * Typically used for form-based submissions or external ID control.
     *
     * @param id             the Grosskunde ID
     * @param grosskundeDto  the data transfer object
     * @return HTTP 201 Created
     */
    @POST
    @Path("/{id}")
    public Response saveFormular(@PathParam("id") Long id, GrosskundeDto grosskundeDto) {
        var grosskundeInput = dtoMapper.toGrosskundeInput(id, grosskundeDto);
        grosskundeservice.update(grosskundeInput, userIdProvider, transactionIdProvider);
        return Response.status(Response.Status.CREATED).build();
    }

    /**
     * Updates an existing Grosskunde entity and terminates its current validity.
     * Creates a new time slice with updated data.
     *
     * @param grosskundeId   the Grosskunde ID
     * @param grosskundeDto  the updated data transfer object
     * @return HTTP 200 OK
     */
    @PATCH
    @Transactional
    @Path("{id}")
    public Response updateGrosskunde(@PathParam("id") Long grosskundeId, GrosskundeDto grosskundeDto) {
        var grosskundeInput = dtoMapper.toGrosskundeInput(grosskundeDto.id(), grosskundeDto);
        grosskundeservice.terminateTechnicalValidity(grosskundeInput, LocalDateTime.now(clock), userIdProvider, transactionIdProvider);
        grosskundeservice.update(grosskundeInput, userIdProvider, transactionIdProvider);
        return Response.ok().build();
    }
}

