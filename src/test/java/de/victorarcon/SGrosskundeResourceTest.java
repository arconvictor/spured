package de.victorarcon;


import de.victorarcon.spured.RandomIdGenerator;
import de.victorarcon.spured.resource.GrosskundeResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestHTTPEndpoint(GrosskundeResource.class)
class SGrosskundeResourceTest {

    @Inject
    EntityManager entityManager;

    @BeforeAll
    public static void setup() {
        RestAssured.defaultParser = Parser.JSON;
    }

    /**
    * Cleans up the database before each test by deleting all entries from SGrosskunde and H_GKD.
    */
    @BeforeEach
    @Transactional
    public void cleanup() {
        entityManager.createNativeQuery("DELETE FROM H_GKD").executeUpdate();
        entityManager.createQuery("DELETE FROM SGrosskunde").executeUpdate();
    }

    /**
    * Verifies that the GET endpoint returns an empty list when no Grosskunden exist.
    */
    @Test
    void testGrosskundeEndPoint() {
        given()
                .when().get()
                .then()
                .statusCode(200)
                .body(is("[]"));
    }

    /**
    * Tests the creation of a new Grosskunde via POST.
    * Asserts that the response status is 201 (Created).
    */
    @Test
    void testAddGrosskunde() {

        // Initiales Anlegen
        var grosskunde = createGrosskunde(createRandomId().toString(), "DBK", "Deutsche Bank AG", "2023-01-01T00:00:00", "4444-04-04T00:00:00", "2208", "Testing");

        given()
                .contentType(ContentType.JSON)
                .body(grosskunde)
                .when().post()
                .then()
                .statusCode(201);
    }

    /**
    * Tests reading a specific Grosskunde by ID.
    * Asserts that the returned data matches the inserted values.
    */
    @Test
    void testReadGrosskunde() {
        var grosskundeId = Long.toString(createRandomId());

        // Initial insertion
        var grosskunde = createGrosskunde(grosskundeId, "LHA", "Lufthansa AG", "2023-01-01T00:00:00", "4444-04-04T00:00:00", "4787", "Testing");


        given()
                .contentType(ContentType.JSON)
                .body(grosskunde)
                .when().post()
                .then()
                .statusCode(201);

        given()
                .when()
                .get(grosskundeId)
                .then()
                .statusCode(200)
                .body("kurzBezeichnung", is("LHA"))
                .body("langBezeichnung", is("Lufthansa AG"))
                .body("gueltigAb", is("2023-01-01T00:00:00"))
                .body("ungueltigAb", is("4444-04-04T00:00:00"));
    }

    /**
    * Tests reading all Grosskunden as a list.
    * Asserts that the list contains the expected entries in order.
    */
    @Test
    void testReadGrosskunden() {

        var grosskunde1 = createGrosskunde(createRandomId().toString(), "FNTN", "Freenet AG", "2023-01-01T00:00:00", "4444-04-04T00:00:00", "9747", "Testing");

        var grosskunde2 = createGrosskunde(createRandomId().toString(), "SIE", "Siemens AG", "2023-01-01T00:00:00", "4444-04-04T00:00:00", "7016", "Testing");

        given()
                .contentType(ContentType.JSON)
                .body(grosskunde1)
                .when().post()
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(grosskunde2)
                .when().post()
                .then()
                .statusCode(201);

        given()
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("langBezeichnung[0]", equalTo("Freenet AG"))
                .body("langBezeichnung[1]", equalTo("Siemens AG"))
                .body("kurzBezeichnung[0]", equalTo("FNTN"))
                .body("kurzBezeichnung[1]", equalTo("SIE"));

    }

    /**
    * Tests replacing existing time slices using PATCH.
    * Verifies that the updated values are reflected in the response and history.
    */
    @Test
    void testPatchGrosskunde() {
        var grosskundeId = Long.toString(createRandomId());

        // Initial insertion
        var grosskunde = createGrosskunde(grosskundeId, "LHA", "Lufthansa AG", "2023-01-01T00:00:00", "4444-04-04T00:00:00", "4787", "Testing");

        given()
                .contentType(ContentType.JSON)
                .body(grosskunde)
                .when()
                .post()
                .then()
                .statusCode(201);

        given()
                .when()
                .get(grosskundeId + "?history=true")
                .then()
                .statusCode(200)
                .body("kurzBezeichnung", hasItem("LHA"))
                .body("langBezeichnung", hasItem("Lufthansa AG"));


        // PATCH
        var grosskundeForPatch = createGrosskunde(grosskundeId, "LHA", "Lufthansa AG", "2028-01-02T00:00:00", "4444-04-04T00:00:00", "4787", "Testing after update");

        given()
                .contentType(ContentType.JSON)
                .body(grosskundeForPatch)
                .when()
                .patch(grosskundeId)
                .then()
                .statusCode(200);


        given()
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("gueltigAb", hasItem("2028-01-02T00:00:00"))
                .body("druckText", hasItem("Testing after update"));

        given()
                .when()
                .get(grosskundeId + "?history=true")
                .then()
                .statusCode(200)
                .body("gueltigAb", hasItem("2028-01-02T00:00:00"))
                .body("druckText", hasItem("Testing after update"));

    }

    /**
    * Tests inserting a new time slice using POST on an existing Grosskunde.
    * Verifies that historical time slices are preserved and correctly ordered.
    */
    @Test
    void testPostGrosskunde() {

        var grosskundeId = Long.toString(createRandomId());
        System.out.println(grosskundeId);

        // Initial insertion
        var grosskunde = createGrosskunde(grosskundeId, "LHA", "Lufthansa AG", "2023-01-01T00:00:00", "4444-04-04T00:00:00", "4787", "Testing");

        given()
                .contentType(ContentType.JSON)
                .body(grosskunde)
                .when()
                .post()
                .then()
                .statusCode(201);

        ////////////////////////////////////////////////////////
        // POST: New validity period: 2030 to 4444

        var grosskundeForPost = createGrosskunde(grosskundeId, "LHA", "Lufthansa AG", "2030-01-01T00:00:00", "4444-04-04T00:00:00", "4787", "Testing after update");

        given()
                .contentType(ContentType.JSON)
                .body(grosskundeForPost)
                .when()
                .post(grosskundeId)
                .then()
                .statusCode(201);

        // Expected result: two time slices:
        // 2023-2030
        // 2030-4444
        given()
                .when()
                .get(grosskundeId + "?history=true")
                .then()
                .statusCode(200)
                .body("gueltigAb[0]", equalTo("2023-01-01T00:00:00"))
                .body("gueltigAb[1]", equalTo("2030-01-01T00:00:00"))
                .body("ungueltigAb[0]", equalTo("2030-01-01T00:00:00"))
                .body("ungueltigAb[1]", equalTo("4444-04-04T00:00:00"));
    }

    /**
    * Generates a random user ID using the {@link RandomIdGenerator}.
    * This ID is used to simulate unique Grosskunde identifiers in test scenarios.
    *
    * @return a randomly generated Long value representing a user ID
    */
    private Long createRandomId() {
        return RandomIdGenerator.generateUserId();
    }

    /**
    * Creates a JSON string representing a Grosskunde entity with the provided attributes.
    * This method is used to construct request bodies for REST API tests.
    *
    * @param id             the unique identifier of the Grosskunde
    * @param kurzBezeichnung short name or abbreviation
    * @param langBezeichnung full name or description
    * @param gueltigAb       start date of validity (ISO 8601 format)
    * @param ungueltigAb     end date of validity (ISO 8601 format)
    * @param grosskundeNr    customer number
    * @param druckText        descriptive text for printing or display
    * @return a formatted JSON string representing the Grosskunde
    */
    private String createGrosskunde(String id,
                                    String kurzBezeichnung,
                                    String langBezeichnung,
                                    String gueltigAb,
                                    String ungueltigAb,
                                    String grosskundeNr,
                                    String druckText) {
        return """
                {
                    "id": "%s",
                    "kurzBezeichnung": "%s",
                    "langBezeichnung": "%s",
                    "gueltigAb": "%s",
                    "ungueltigAb": "%s",
                    "grosskundeNr": "%s",
                    "druckText": "%s"
                }
                """.formatted(id, kurzBezeichnung, langBezeichnung, gueltigAb, ungueltigAb, grosskundeNr, druckText);
    }
}
