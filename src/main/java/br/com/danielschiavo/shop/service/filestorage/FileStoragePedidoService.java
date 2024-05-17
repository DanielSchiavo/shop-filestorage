package br.com.danielschiavo.shop.service.filestorage;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileUrlResource;
import org.springframework.stereotype.Service;

import br.com.danielschiavo.shop.model.FileStorageException;
import br.com.danielschiavo.shop.model.filestorage.ArquivoInfoDTO;

@Service
public class FileStoragePedidoService {
	
	private final Path raizPedido = Paths.get("imagens/pedido");
	
	@Autowired
	private FileStorageProdutoService fileProdutoService;
	
	public ArquivoInfoDTO pegarImagemPedidoPorNome(String nomeArquivo) {
		byte[] bytes = recuperarBytesImagemPedidoDoDisco(nomeArquivo);
		return new ArquivoInfoDTO(nomeArquivo, bytes);
	}
	
	public String persistirOuRecuperarImagemPedido(String nomePrimeiraImagemProduto, Long idProduto) {
		String arquivoInfoDTO = verificarSeExisteImagemPedidoNoDisco(nomePrimeiraImagemProduto);
		if (arquivoInfoDTO != null) {
			return arquivoInfoDTO;
		}
		else {
			String novoNomeImagemPedidoGerado = gerarNomeImagemPedido(idProduto, nomePrimeiraImagemProduto);
			salvarNoDiscoImagemPedido(novoNomeImagemPedidoGerado, nomePrimeiraImagemProduto);
			return novoNomeImagemPedidoGerado;
		}
	}
	
//
// METODOS UTILITARIOS DE PEDIDO
//	
	
	public byte[] recuperarBytesImagemPedidoDoDisco(String nomeArquivo) {
		FileUrlResource fileUrlResource;
		try {
			fileUrlResource = new FileUrlResource(raizPedido + "/" + nomeArquivo);
			return fileUrlResource.getContentAsByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			throw new FileStorageException("Não foi possivel recuperar os bytes da imagem nome " + nomeArquivo + ", motivo: " + e);
		}
	}

	private byte[] salvarNoDiscoImagemPedido(String novoNomeImagemPedidoGerado, String nomePrimeiraImagemProduto) {
		try {
			ArquivoInfoDTO arquivoInfoDTO = fileProdutoService.pegarArquivoProdutoPorNome(nomePrimeiraImagemProduto);
			byte[] bytes = arquivoInfoDTO.bytesArquivo();
			Files.write(this.raizPedido.resolve(novoNomeImagemPedidoGerado), bytes, StandardOpenOption.CREATE_NEW);
			return bytes;
		} catch (IOException e) {
			e.printStackTrace();
			throw new FileStorageException("Não foi possivel salvar o arquivo " + novoNomeImagemPedidoGerado + " no disco");
		}
	}

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
