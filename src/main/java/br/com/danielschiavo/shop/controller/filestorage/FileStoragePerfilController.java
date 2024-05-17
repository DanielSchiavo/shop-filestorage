package br.com.danielschiavo.shop.controller.filestorage;

import java.io.IOException;
import java.net.URI;
import java.nio.file.NoSuchFileException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.danielschiavo.shop.model.ValidacaoException;
import br.com.danielschiavo.shop.model.filestorage.ArquivoInfoDTO;
import br.com.danielschiavo.shop.service.filestorage.FileStoragePerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Cliente - Serviço de Armazenamento de Arquivos", description = "Para fazer upload da foto de perfil do cliente. Uso exclusivo do backend.")
public class FileStoragePerfilController {

	@Autowired
	private FileStoragePerfilService fileStoragePerfilService;
	
	@DeleteMapping("/cliente/filestorage/perfil/{nomeFotoPerfil}")
	@Operation(summary = "Deleta a foto de perfil com o nome enviado no parametro da requisição")
	public ResponseEntity<?> deletarFotoPerfil(@PathVariable String nomeFotoPerfil) {
		try {
			ArquivoInfoDTO respostaDeletarFotoPerfil = fileStoragePerfilService.deletarFotoPerfilNoDisco(nomeFotoPerfil);
			return ResponseEntity.ok().body(respostaDeletarFotoPerfil);
		} catch (ValidacaoException e) {
			HttpStatus status = HttpStatus.BAD_REQUEST;
			return ResponseEntity.status(status).body(ArquivoInfoDTO.comErro(nomeFotoPerfil, e.getMessage()));
		} catch (NoSuchFileException e) {
			HttpStatus status = HttpStatus.NOT_FOUND;
			return ResponseEntity.status(status).body(ArquivoInfoDTO.comErro(nomeFotoPerfil, "O arquivo " + nomeFotoPerfil + " não existe"));
		} catch (IOException e) {
			System.out.println(" ENTROU AQUI2 ");
			HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
			return ResponseEntity.status(status).body(ArquivoInfoDTO.comErro(nomeFotoPerfil, "Falha interna no servidor ao tentar excluir o arquivo."));
		}
	}
	
	@GetMapping("/cliente/filestorage/perfil/{nomeFotoPerfil}")
	@Operation(summary = "Pega uma foto de perfil dado o nome da foto no parametro da requisição")
	public ResponseEntity<ArquivoInfoDTO> pegarFotoPerfilPorNome(@PathVariable String nomeFotoPerfil) {
		ArquivoInfoDTO arquivo = fileStoragePerfilService.pegarFotoPerfilPorNome(nomeFotoPerfil);
		
		return ResponseEntity.ok(arquivo);
	}
	
	@PostMapping("/cliente/filestorage/perfil/")
	@Operation(summary = "Cadastra uma foto de perfil enviada através de um formulario html e gera um nome")
	public ResponseEntity<?> cadastrarFotoPerfil(
			@RequestPart(name = "foto", required = true) MultipartFile foto,
			UriComponentsBuilder uriBuilder
			) {
		ArquivoInfoDTO arquivoInfoDTO = fileStoragePerfilService.persistirFotoPerfil(foto, uriBuilder);
		
		if (arquivoInfoDTO.erro() == null) {
			URI uri = uriBuilder.path("/shop/admin/filestorage/foto-perfil/" + arquivoInfoDTO.nomeArquivo()).build().toUri();
			return ResponseEntity.created(uri).body(arquivoInfoDTO);
		}
		else {
			return ResponseEntity.badRequest().body(arquivoInfoDTO);
		}
		

	}
	
	@PutMapping("/cliente/filestorage/perfil/{nomeFotoPerfilAntiga}")
	@Operation(summary = "Deleta o nomeAntigoDoArquivo e salva o arquivo enviado e gera um novo nome")
	public ResponseEntity<?> alterarFotoPerfil(
			@RequestPart(name = "foto", required = true) MultipartFile novaFoto,
			@RequestParam String nomeFotoPerfilAntiga,
			UriComponentsBuilder uriBuilder
			) {
		ArquivoInfoDTO arquivoInfoDTO = fileStoragePerfilService.alterarFotoPerfil(novaFoto, nomeFotoPerfilAntiga);
		
		if (arquivoInfoDTO.erro() == null) {
			return ResponseEntity.ok(arquivoInfoDTO);
		}
		else {
			return ResponseEntity.badRequest().body(arquivoInfoDTO);
		}
	}
}
