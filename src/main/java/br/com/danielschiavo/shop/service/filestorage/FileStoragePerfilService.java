package br.com.danielschiavo.shop.service.filestorage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.springframework.core.io.FileUrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.danielschiavo.shop.FileStorageUtilidadeService;
import br.com.danielschiavo.shop.model.FileStorageException;
import br.com.danielschiavo.shop.model.ValidacaoException;
import br.com.danielschiavo.shop.model.filestorage.ArquivoInfoDTO;

@Service
public class FileStoragePerfilService {
	
	private final Path raizPerfil = Paths.get("imagens/perfil");

	public ArquivoInfoDTO deletarFotoPerfilNoDisco(String nome) throws IOException {
		if (nome.matches("Padrao.jpeg")) {
			throw new ValidacaoException("Você não pode excluir a foto padrão");
		}
		Files.delete(this.raizPerfil.resolve(nome));
		return ArquivoInfoDTO.comNomeEMensagem(nome, "Imagem de perfil deletada com sucesso!");
	}
	
	public ArquivoInfoDTO pegarFotoPerfilPorNome(String nomeArquivo) {
		try {
			byte[] bytes = recuperarBytesFotoPerfilDoDisco(nomeArquivo);
			return new ArquivoInfoDTO(nomeArquivo, bytes);
		} catch (FileStorageException e) {
			return ArquivoInfoDTO.comErro(nomeArquivo, e.getMessage());
		}
	}
	
	public ArquivoInfoDTO persistirFotoPerfil(MultipartFile arquivo, UriComponentsBuilder uriBuilderBase) {
		try {
			UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(uriBuilderBase.toUriString());
			String nome = gerarNovoNomeFotoPerfil(arquivo);
			byte[] bytesArquivo = salvarNoDiscoFotoPerfil(nome, arquivo);
			URI uri = uriBuilder.path("/arquivo-produto/" + nome).build().toUri();
			
			return ArquivoInfoDTO.comUri(nome, uri.toString(), bytesArquivo);
			
		} catch (FileStorageException e) {
			return ArquivoInfoDTO.comErro(arquivo.getOriginalFilename(), e.getMessage());
		}
		
	}
	
	public ArquivoInfoDTO alterarFotoPerfil(MultipartFile novaFoto, String nomeArquivoASerSubstituido) {
		if (novaFoto == null || nomeArquivoASerSubstituido.isEmpty()) {
			throw new ValidacaoException("Você tem que mandar pelo menos um arquivo e um nomeArquivoASerExcluido");
		}
		String[] contentType = novaFoto.getContentType().split("/");
		if (!contentType[0].contains("image")) {
			throw new FileStorageException("Só é aceito imagens e videos");
		}
		if (!contentType[1].contains("jpg") && !contentType[1].contains("jpeg") && !contentType[1].contains("png")) {
			throw new FileStorageException("Os tipos aceitos são jpg, jpeg, png");
		}
		try {
			verificarSeExisteFotoPerfilPorNome(nomeArquivoASerSubstituido);
			byte[] bytes = sobrescreverNoDiscoFotoPerfil(novaFoto, nomeArquivoASerSubstituido);
			
			return new ArquivoInfoDTO(nomeArquivoASerSubstituido, bytes);
			
		} catch (FileStorageException e) {
			return ArquivoInfoDTO.comErro(novaFoto.getOriginalFilename(), e.getMessage());
		}
	}
	
	
//
// METODOS UTILITARIOS DE PERFIL
//	
	
	private byte[] sobrescreverNoDiscoFotoPerfil(MultipartFile novaFoto, String nome) {
		try {
			Files.copy(novaFoto.getInputStream(), this.raizPerfil.resolve(nome), StandardCopyOption.REPLACE_EXISTING);
			return novaFoto.getInputStream().readAllBytes();
		} catch (IOException e) {
			throw new FileStorageException("Não foi possível sobrescrever o arquivo no disco");
		}

	}
	
	private String gerarNovoNomeFotoPerfil(MultipartFile fotoPerfil) {
		String[] contentType = fotoPerfil.getContentType().split("/");
		if (!Arrays.asList(contentType[0]).contains("image")) {
			throw new FileStorageException("Só é aceito imagem na foto de perfil");
		}
		if (!contentType[1].contains("jpg") && !contentType[1].contains("jpeg") && !contentType[1].contains("png")) {
			throw new FileStorageException("Os tipos aceitos são jpg, jpeg, png");
		}
		String stringUnica = FileStorageUtilidadeService.gerarStringUnica();
		return stringUnica + "." + contentType[1];
	}

	private byte[] salvarNoDiscoFotoPerfil(String nomeFotoPerfil, MultipartFile fotoPerfil) {
		try {
			byte[] bytes = fotoPerfil.getInputStream().readAllBytes();
			Files.copy(fotoPerfil.getInputStream(), this.raizPerfil.resolve(nomeFotoPerfil), StandardCopyOption.REPLACE_EXISTING);
			return bytes;
		} catch (Exception e) {
			throw new FileStorageException("Falha ao salvar imagem no disco. ", e);
		}
	}

	public byte[] recuperarBytesFotoPerfilDoDisco(String nomeArquivoProduto) {
		FileUrlResource fileUrlResource;
		try {
			fileUrlResource = new FileUrlResource(raizPerfil + "/" + nomeArquivoProduto);
			return fileUrlResource.getContentAsByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			throw new FileStorageException("Não foi possivel recuperar os bytes da imagem nome " + nomeArquivoProduto + ", motivo: " + e);
		}
	}
	
	public void verificarSeExisteFotoPerfilPorNome(String nome) {
		try {
			FileUrlResource fileUrlResource = new FileUrlResource(raizPerfil + "/" + nome);
			if (!fileUrlResource.exists()) {
				throw new ValidacaoException("Não existe foto de perfil com o nome " + nome);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

}
