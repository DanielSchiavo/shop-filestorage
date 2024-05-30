package br.com.danielschiavo.shop.service.filestorage;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.danielschiavo.shop.model.FileStorageException;
import br.com.danielschiavo.shop.model.filestorage.ArquivoInfoDTO;

@Service
public class FileStoragePedidoService {
	
	@Autowired
	private FileStorageService fileStorageService;
	
	private final Path raizPedido = Paths.get("imagens/pedido");
	
	public ArquivoInfoDTO pegarImagemPedidoPorNome(String nomeArquivo) {
		ArquivoInfoDTO arquivoInfoDTO = (ArquivoInfoDTO) fileStorageService.recuperarBytesImagemDoDisco(FileStorageProdutoService.raizProduto, nomeArquivo);
		return arquivoInfoDTO;
	}
	
	public String persistirOuRecuperarImagemPedido(String nomePrimeiraImagemProduto, Long idProduto) {
		String nomeImagemPedido = verificarSeExisteImagemPedidoNoDisco(nomePrimeiraImagemProduto);
		if (nomeImagemPedido != null) {
			return nomeImagemPedido;
		}
		else {
			String nomeImagemPedidoGerado = gerarNomeImagemPedido(idProduto, nomePrimeiraImagemProduto);
			ArquivoInfoDTO arquivoInfoDTO = (ArquivoInfoDTO) fileStorageService.recuperarBytesImagemDoDisco(FileStorageProdutoService.raizProduto, nomePrimeiraImagemProduto);
			fileStorageService.salvarNoDisco(raizPedido, nomeImagemPedidoGerado, arquivoInfoDTO.bytesArquivo());
			return nomeImagemPedidoGerado;
		}
	}
	
//
// METODOS UTILITARIOS DE PEDIDO
//	

	private String gerarNomeImagemPedido(Long idProduto, String nomePrimeiraImagemProduto) {
		String[] split = nomePrimeiraImagemProduto.split("\\.");
		if (!Arrays.asList(split[1]).contains("png") && !Arrays.asList(split[1]).contains("jpg") && !Arrays.asList(split[1]).contains("jpeg")) {
			throw new FileStorageException("Só é aceito imagem na foto de perfil");
		}
		String nome = "PRODID" + idProduto + "-" + nomePrimeiraImagemProduto;
		return nome;
	}
	
	public String verificarSeExisteImagemPedidoNoDisco(String nomePrimeiraImagemProduto) {
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*" + nomePrimeiraImagemProduto);
	    try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.raizPedido)) {
	        for (Path entry : stream) {
	        	if (matcher.matches(entry.getFileName())) {
		        	return entry.getFileName().toString(); 
	            }
	        }
	        return null;
	    } catch (IOException e) {
	        throw new FileStorageException("Falha ao tentar recuperar imagem do pedido no disco.", e);
	    }
	}
	

}
