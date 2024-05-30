package br.com.danielschiavo.shop.controller.filestorage;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.danielschiavo.shop.service.filestorage.FileStorageProdutoService;
import br.com.danielschiavo.shop.service.filestorage.RespostaAlterarArquivosDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Produto - Serviço de Armazenamento de Arquivos", description = "Para fazer upload de imagens e videos do produto. Uso exclusivo do backend.")
public class FileStorageProdutoController {

	@Autowired
	private FileStorageProdutoService fileStorageService;

	@DeleteMapping("/admin/produto/{nomesArquivos}")
	@Operation(summary = "Deleta o arquivo com o nome enviado no parametro da requisição")
	public ResponseEntity<?> deletarImagensProduto(@PathVariable(name = "nomesArquivos") List<String> nomesArquivos,
			HttpServletRequest request) {
		Object respostaDeletarImagens = fileStorageService.deletarImagens(nomesArquivos);
		return ResponseEntity.ok().body(respostaDeletarImagens);
	}

	@GetMapping("/publico/produto/{nomesArquivos}")
	@Operation(summary = "Recupera os bytes do nome de todas imagens enviadas no parâmetro da requisição")
	public ResponseEntity<?> pegarImagensProduto(
			@PathVariable(name = "nomesArquivos") String... nomesImagens) {
		return ResponseEntity.ok(fileStorageService.pegarImagens(nomesImagens));
	}

	@PostMapping(path = "/admin/produto", consumes = "multipart/form-data")
	@ResponseBody
	@Operation(summary = "Salva um array de arquivos enviados através de um formulário html e gera os seus respectivos nomes")
	public ResponseEntity<?> persistirImagensProduto(
			@RequestPart(name = "arquivos", required = true) MultipartFile[] arquivosMultipart,
			UriComponentsBuilder uriBuilder) {
		Object arquivos = fileStorageService.persistirImagens(arquivosMultipart, uriBuilder);
		return ResponseEntity.created(uriBuilder.build().toUri()).body(arquivos);
	}

	@PutMapping("/admin/produto")
	@Operation(summary = "Deleta todos os arquivos enviados no campo nomesArquivosASeremExcluidos e salva todos os arquivos enviados e gera um nome a cada um deles")
	public ResponseEntity<?> alterarImagensProduto(
			@RequestPart(name = "arquivo", required = true) MultipartFile[] arquivos,
			@RequestPart(name = "excluirArquivo", required = true) List<String> excluirArquivo,
			UriComponentsBuilder uriBuilder) {
		RespostaAlterarArquivosDTO respostaAlterarImagemProduto = 
				fileStorageService.alterarImagens(arquivos, excluirArquivo, uriBuilder);

		return ResponseEntity.ok(respostaAlterarImagemProduto);
	}

}
