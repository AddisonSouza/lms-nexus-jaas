package br.edu.lms.module.storage.interfaces;

import br.edu.lms.module.storage.domain.model.StorageContext;
import br.edu.lms.module.storage.domain.port.out.StoragePort;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * O arquivo volta com o tipo e o nome originais: sem eles o navegador salva um
 * `application/octet-stream` batizado com a chave, que é um UUID sem extensão.
 */
@QuarkusTest
class FileResourceIT {

    static final String USER_ID = "58000000-5800-5800-5800-580000000001";
    static final String ORG_ID  = "58000000-5800-5800-5800-580000000002";
    static final byte[] CONTENT = "conteúdo do anexo".getBytes(StandardCharsets.UTF_8);

    @Inject StoragePort storagePort;

    String fileKey;

    @BeforeEach
    void storeAFile() {
        fileKey = storagePort.store(
                new ByteArrayInputStream(CONTENT),
                "prova final.pdf",
                "application/pdf",
                CONTENT.length,
                StorageContext.TASK_ATTACHMENT).getFileKey();
    }

    @Test
    void getFile_withoutToken_returns401() {
        given()
                .when().get("/files/{key}", fileKey)
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = USER_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = USER_ID), @Claim(key = "org", value = ORG_ID) })
    void getFile_servesItWithTheRealTypeAndName() {
        given()
                .when().get("/files/{key}", fileKey)
                .then()
                .statusCode(200)
                .contentType("application/pdf")
                // `filename*` carrega o nome real; o `filename` só-ASCII é o fallback.
                .header("Content-Disposition", allOf(
                        startsWith("attachment;"),
                        containsString("filename=\"prova_final.pdf\""),
                        containsString("filename*=UTF-8''prova%20final.pdf")));
    }

    @Test
    @TestSecurity(user = USER_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = USER_ID), @Claim(key = "org", value = ORG_ID) })
    void getFile_returnsTheStoredBytes() {
        var body = given()
                .when().get("/files/{key}", fileKey)
                .then().statusCode(200)
                .extract().asByteArray();

        org.assertj.core.api.Assertions.assertThat(body).isEqualTo(CONTENT);
    }

    // Antes o `NoSuchKeyException` do SDK subia cru e virava 500: um anexo
    // apagado parecia falha do servidor.
    @Test
    @TestSecurity(user = USER_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = USER_ID), @Claim(key = "org", value = ORG_ID) })
    void getFile_withUnknownKey_returns404() {
        given()
                .when().get("/files/{key}", "task_attachment/2026/01/nao-existe.pdf")
                .then()
                .statusCode(404)
                .body("error", equalTo("FILE_NOT_FOUND"));
    }
}
