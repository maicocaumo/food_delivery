package com.delivery.cadastro.resource;

import com.delivery.cadastro.commons.ConstraintViolationResponse;
import com.delivery.cadastro.dto.*;
import com.delivery.cadastro.entity.Prato;
import com.delivery.cadastro.entity.Restaurante;
import com.delivery.cadastro.mapper.PratoMapper;
import com.delivery.cadastro.mapper.RestauranteMapper;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.SimplyTimed;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlow;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlows;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.yaml.snakeyaml.emitter.Emitter;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/restaurantes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "restaurante")
@RolesAllowed("proprietario")
@SecurityScheme(securitySchemeName = "delivery-oauth", type = SecuritySchemeType.OAUTH2, flows = @OAuthFlows(password = @OAuthFlow(tokenUrl = "http://localhost:8180/auth/realms/delivery/protocol/openid-connect/token")))
@SecurityRequirement(name = "delivery-oauth", scopes = {})
public class RestauranteResource {

    @Inject
    RestauranteMapper restauranteMapper;

    @Inject
    PratoMapper pratoMapper;

//    @Inject
//    @Channel("restaurantes")
//    Emitter<Restaurante> emitter;

//    @Inject
//    JsonWebToken jwt;
//
//    @Inject
//    @Claim(standard = Claims.sub)
//    String sub;

    @GET
    @Counted(name = "Quantidade buscas Restaurante")
    @SimplyTimed(name = "Tempo simples de busca")
    @Timed(name = "Tempo completo de busca")
    public List<RestauranteDTO> buscar() {
        Stream<Restaurante> restaurantes = Restaurante.streamAll();
        return restaurantes.map(r -> restauranteMapper.toRestauranteDTO(r)).collect(Collectors.toList());
    }

    @POST
    @Transactional
    @APIResponse(responseCode = "201", description = "Caso restaurante seja cadastrado com sucesso")
    @APIResponse(responseCode = "400", content = @Content(schema = @Schema(allOf = ConstraintViolationResponse.class)))
    public Response adicionar(@Valid AdicionarRestauranteDTO dto) {
        Restaurante restaurante = restauranteMapper.toRestaurante(dto);
//        restaurante.proprietario = sub;
        restaurante.persist();

//        emitter.send(restaurante);
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public void atualizar(@PathParam("id") Long id, AtualizarRestauranteDTO dto) {
        Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(id);
        if (restauranteOp.isEmpty()) {
            throw new NotFoundException();
        }
        Restaurante restaurante = restauranteOp.get();

//        if (!restaurante.proprietario.equals(sub)) {
//            throw new ForbiddenException();
//        }

        //MapStruct: aqui passo a referencia para ser atualizada
        restauranteMapper.toRestaurante(dto, restaurante);

        restaurante.persist();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public void delete(@PathParam("id") Long id) {
        Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(id);
        restauranteOp.ifPresentOrElse(Restaurante::delete, () -> {
            throw new NotFoundException();
        });
    }

    //Pratos

    @GET
    @Path("{idRestaurante}/pratos")
    @Tag(name = "prato")
    public List<PratoDTO> buscarPratos(@PathParam("idRestaurante") Long idRestaurante) {
        Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);
        if (restauranteOp.isEmpty()) {
            throw new NotFoundException("Restaurante n??o existe");
        }
        Stream<Prato> pratos = Prato.stream("restaurante", restauranteOp.get());
        return pratos.map(p -> pratoMapper.toDTO(p)).collect(Collectors.toList());
    }

    @POST
    @Path("{idRestaurante}/pratos")
    @Transactional
    @Tag(name = "prato")
    public Response adicionarPrato(@PathParam("idRestaurante") Long idRestaurante, AdicionarPratoDTO dto) {
        Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);
        if (restauranteOp.isEmpty()) {
            throw new NotFoundException("Restaurante n??o existe");
        }
        //        //Utilizando dto, recebi detached entity passed to persist:
        //        Prato prato = new Prato();
        //        prato.nome = dto.nome;
        //        prato.descricao = dto.descricao;
        //
        //        prato.preco = dto.preco;
        //        prato.restaurante = restauranteOp.get();
        //        prato.persist();

        Prato prato = pratoMapper.toPrato(dto);
        prato.restaurante = restauranteOp.get();
        prato.persist();
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("{idRestaurante}/pratos/{id}")
    @Transactional
    @Tag(name = "prato")
    public void atualizar(@PathParam("idRestaurante") Long idRestaurante, @PathParam("id") Long id, AtualizarPratoDTO dto) {
        Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);
        if (restauranteOp.isEmpty()) {
            throw new NotFoundException("Restaurante n??o existe");
        }

        //No nosso caso, id do prato vai ser ??nico, independente do restaurante...
        Optional<Prato> pratoOp = Prato.findByIdOptional(id);

        if (pratoOp.isEmpty()) {
            throw new NotFoundException("Prato n??o existe");
        }
        Prato prato = pratoOp.get();
        pratoMapper.toPrato(dto, prato);
        prato.persist();
    }

    @DELETE
    @Path("{idRestaurante}/pratos/{id}")
    @Transactional
    @Tag(name = "prato")
    public void delete(@PathParam("idRestaurante") Long idRestaurante, @PathParam("id") Long id) {
        Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);
        if (restauranteOp.isEmpty()) {
            throw new NotFoundException("Restaurante n??o existe");
        }

        Optional<Prato> pratoOp = Prato.findByIdOptional(id);

        pratoOp.ifPresentOrElse(Prato::delete, () -> {
            throw new NotFoundException();
        });
    }
}
