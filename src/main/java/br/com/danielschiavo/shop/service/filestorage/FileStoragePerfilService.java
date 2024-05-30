package br.com.danielschiavo.shop.service.filestorage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.danielschiavo.shop.model.FileStorageException;
import br.com.danielschiavo.shop.model.ValidacaoException;
import br.com.danielschiavo.shop.model.filestorage.ArquivoInfoDTO;

@Service
public class FileStoragePerfilService {
	
	@Autowired
	private FileStorageService fileStorageService;
	
	private final Path raizPerfil = Paths.get("imagens/perfil");

	public String deletarFotoPerfilNoDisco(String nome) throws IOException {
		fileStorageService.deletarNoDisco(raizPerfil, nome);
		return "Foto de perfil deletada com sucesso!";
	}
	
	public ArquivoInfoDTO pegarFotoPerfilPorNome(String nomeArquivo) {
		ArquivoInfoDTO arquivoInfoDTO = (ArquivoInfoDTO) fileStorageService.recuperarBytesImagemDoDisco(raizPerfil, nomeArquivo);
		
		if (arquivoInfoDTO.erro() != null) 
			throw new FileStorageException(arquivoInfoDTO.erro());
		else
			return arquivoInfoDTO;
	}
	
	public String persistirFotoPerfil(MultipartFile arquivo) {
		String[] contentType = arquivo.getContentType().split("/");
		if (!contentType[1].contains("jpg") && !contentType[1].contains("jpeg") && !contentType[1].contains("png"))
			throw new FileStorageException("Os tipos aceitos são jpg, jpeg, png");
		
		try {
			String nomeFotoPerfilGerado = gerarNovoNomeFotoPerfil(contentType[1]);
			fileStorageService.salvarNoDisco(raizPerfil, nomeFotoPerfilGerado, arquivo.getBytes());
		} catch (IOException e) {
			throw new FileStorageException("Erro ao pegar bytes da nova foto de perfil");
		}
		
		return "Foto de perfil adicionada com sucesso!";
		
	}
	
	public String alterarFotoPerfil(MultipartFile novaFoto, String nomeArquivoASerSubstituido) {
		if (novaFoto == null || nomeArquivoASerSubstituido.isEmpty()) {
			throw new ValidacaoException("Você tem que mandar pelo menos um arquivo e um nomeArquivoASerExcluido");
		}
		String[] contentType = novaFoto.getContentType().split("/");
		if (!contentType[1].contains("jpg") && !contentType[1].contains("jpeg") && !contentType[1].contains("png")) {
			throw new FileStorageException("Os tipos aceitos são jpg, jpeg, png");
		}
		
		try {
			fileStorageService.deletarNoDisco(raizPerfil, nomeArquivoASerSubstituido);
			String nomeFotoPerfilGerado = gerarNovoNomeFotoPerfil(contentType[1]);
			fileStorageService.salvarNoDisco(raizPerfil, nomeFotoPerfilGerado, novaFoto.getBytes());
		} catch (IOException e) {
			throw new FileStorageException("Erro ao pegar bytes da nova foto de perfil");
		}
		
		return "Foto de perfil alterada com sucesso!";
	}
	
	
//
// METODOS UTILITARIOS DE PERFIL
//	
	
	private String gerarNovoNomeFotoPerfil(String formato) {
		return FileStorageService.gerarStringUnica() + "." + formato;
	}

}
