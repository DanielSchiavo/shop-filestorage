package br.com.danielschiavo.shop.service.filestorage;

import java.util.List;

import br.com.danielschiavo.shop.model.filestorage.ArquivoInfoDTO;

public record RespostaAlterarArquivosDTO(
		List<ArquivoInfoDTO> sucesso,
		List<ArquivoInfoDTO> falha
		) {

}
