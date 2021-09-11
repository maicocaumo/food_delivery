package com.delivery.cadastro;

import com.delivery.cadastro.dto.AtualizarRestauranteDTO;
import com.delivery.cadastro.entity.Restaurante;
import com.github.database.rider.cdi.api.DBRider;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.configuration.Orthography;
import com.github.database.rider.core.api.dataset.DataSet;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.approvaltests.Approvals;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response.Status;

import static io.restassured.RestAssured.given;

@DBRider
@DBUnit(caseInsensitiveStrategy = Orthography.LOWERCASE)
@QuarkusTest
@QuarkusTestResource(CadastroTestLifecycleManager.class)
public class RestauranteResourceTest {

//    private String token;
//
//    @BeforeEach
//    public void gereToken() throws Exception {
//        token = TokenUtils.generateTokenString("/JWTProprietarioClaims.json", null);
//    }

    @Test
    @DataSet("restaurantes-cenario-1.yml")
    public void testBuscarRestaurantes() {
        String resultado = given()
                .when().get("/restaurantes")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract().asString();
        Approvals.verifyJson(resultado);
    }

//    private RequestSpecification given() {
//        return RestAssured.given()
//                .contentType(ContentType.JSON).header(new Header("Authorization", "Bearer " + token));
//    }

    //Exemplo de um teste de PUT

//    @Test
//    @DataSet("restaurantes-cenario-1.yml")
//    public void testAlterarRestaurante() {
//        AtualizarRestauranteDTO dto = new AtualizarRestauranteDTO();
//        dto.nomeFantasia = "novoNome";
//        Long parameterValue = 123L;
//        given()
//                .with().pathParam("id", parameterValue)
//                .body(dto)
//                .when().put("/restaurantes/{id}")
//                .then()
//                .statusCode(Status.NO_CONTENT.getStatusCode())
//                .extract().asString();
//
//        Restaurante findById = Restaurante.findById(parameterValue);
//
//        Assert.assertEquals(dto.nomeFantasia, findById.nome);
//    }

}
