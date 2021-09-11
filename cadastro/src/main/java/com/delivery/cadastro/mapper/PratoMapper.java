package com.delivery.cadastro.mapper;

import com.delivery.cadastro.dto.AdicionarPratoDTO;
import com.delivery.cadastro.dto.AtualizarPratoDTO;
import com.delivery.cadastro.dto.PratoDTO;
import com.delivery.cadastro.entity.Prato;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi")
public interface PratoMapper {

    PratoDTO toDTO(Prato p);

    Prato toPrato(AdicionarPratoDTO dto);

    void toPrato(AtualizarPratoDTO dto, @MappingTarget Prato prato);
}
