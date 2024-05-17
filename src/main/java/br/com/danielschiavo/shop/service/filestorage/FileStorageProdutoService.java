package br.com.danielschiavo.shop.service.filestorage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.FileUrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.danielschiavo.shop.Base64Utils;
import br.com.danielschiavo.shop.model.FileStorageException;
import br.com.danielschiavo.shop.model.ValidacaoException;
import br.com.danielschiavo.shop.model.filestorage.ArquivoInfoDTO;

@Service
public class FileStorageProdutoService {
	
	private final Path raizProduto = Paths.get("imagens/produto");
	
	public List<ArquivoInfoDTO> deletarArquivoProdutoNoDisco(List<String> nomesArquivos) {
        String diretorioAtual = System.getProperty("user.dir");

        // Imprimir o diretório atual
        System.out.println(" O diretorio atual é: " + diretorioAtual);
		List<ArquivoInfoDTO> listaArquivoInfoDTO = new ArrayList<>();
		nomesArquivos.forEach(arquivo -> {
			String mensagem = null;
			if (arquivo.equals("Padrao.jpeg")) {
				mensagem = "O arquivo não pode ser excluido porque é a imagem padrão para produtos sem fotos.";
			} else {
				try {
					Files.delete(this.raizProduto.resolve(arquivo));
					mensagem = "Arquivo excluido com sucesso!";
				} catch (IOException e) {
					mensagem = "Falha ao excluir arquivo no disco. ";
				}
			}
			listaArquivoInfoDTO.add(ArquivoInfoDTO.comNomeEMensagem(arquivo, mensagem));
		});
		return listaArquivoInfoDTO;
	}
	
	public List<ArquivoInfoDTO> mostrarArquivoProdutoPorListaDeNomes(List<String> listNomes) {
		List<ArquivoInfoDTO> listaArquivosInfoDTO = new ArrayList<>();
		listNomes.forEach(nome -> {
			try {
				ArquivoInfoDTO arquivoInfoDTO = this.pegarArquivoProdutoPorNome(nome);
				listaArquivosInfoDTO.add(arquivoInfoDTO);
			} catch (FileStorageException e) {
				listaArquivosInfoDTO.add(ArquivoInfoDTO.comErro(nome, e.getMessage()));
			}
		});
		return listaArquivosInfoDTO;
	}
	
	public ArquivoInfoDTO pegarArquivoProdutoPorNome(String nomeArquivo) {
		byte[] bytes = recuperarBytesArquivoProdutoDoDisco(nomeArquivo);
		return new ArquivoInfoDTO(nomeArquivo, bytes);
	}
	
	public List<ArquivoInfoDTO> persistirArrayArquivoProduto(MultipartFile[] arquivos, UriComponentsBuilder uriBuilderBase) {
	    List<ArquivoInfoDTO> arquivosInfo = new ArrayList<>();

	    for (MultipartFile arquivo : arquivos) {
	    	try {
	    		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(uriBuilderBase.toUriString());
	    		String nomeArquivo = gerarNomeArquivoProduto(arquivo);
	    		byte[] bytesArquivo = salvarNoDiscoArquivoProduto(nomeArquivo, arquivo);
	    		URI uri = uriBuilder.path("/arquivo-produto/" + nomeArquivo).build().toUri();
	    		var arquivoInfo = ArquivoInfoDTO.comUriENomeAntigoArquivo(nomeArquivo, arquivo.getOriginalFilename(), uri.toString(), bytesArquivo);
	    		arquivosInfo.add(arquivoInfo);
			} catch (FileStorageException e) {
				arquivosInfo.add(ArquivoInfoDTO.comErro(arquivo.getOriginalFilename(), e.getMessage()));
			}
	    }
	    
	    return arquivosInfo;
	}
	
	public List<ArquivoInfoDTO> alterarArrayArquivoProduto(MultipartFile[] arquivos, String nomesArquivosASeremExcluidos, UriComponentsBuilder uriBuilderBase) {
		if (arquivos.length == 0 || nomesArquivosASeremExcluidos.isEmpty()) {
			throw new ValidacaoException("Você tem que mandar pelo menos um arquivo e um nomeArquivoASerExcluido");
		}
		List<ArquivoInfoDTO> arquivosInfo = new ArrayList<>();
		String[] split = nomesArquivosASeremExcluidos.trim().split(",");
		
		List<String> listaArquivos = Arrays.asList(split);
		List<ArquivoInfoDTO> respostaDeletarArquivoProduto = deletarArquivoProdutoNoDisco(listaArquivos);
		arquivosInfo.addAll(respostaDeletarArquivoProduto);
		
		for (MultipartFile arquivo : arquivos) {
			try {
				UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(uriBuilderBase.toUriString());
				String novoNomeGerado = gerarNomeArquivoProduto(arquivo);
				byte[] bytes = salvarNoDiscoArquivoProduto(novoNomeGerado, arquivo);
				URI uri = uriBuilder.path("/arquivo-produto/" + novoNomeGerado).build().toUri();
				
				arquivosInfo.add(ArquivoInfoDTO.comUri(novoNomeGerado, uri.toString(), bytes));
			} catch (FileStorageException e) {
				arquivosInfo.add(ArquivoInfoDTO.comErro(arquivo.getOriginalFilename(), e.getMessage()));
			}
		}
		return arquivosInfo;
	}

	
//
// METODOS UTILITARIOS DE PRODUTO
//	

	private byte[] salvarNoDiscoArquivoProduto(String nomeArquivo, MultipartFile arquivo) {
		try {
			byte[] bytes = arquivo.getInputStream().readAllBytes();
			Files.copy(arquivo.getInputStream(), this.raizProduto.resolve(nomeArquivo), StandardCopyOption.REPLACE_EXISTING);
			return bytes;
		} catch (Exception e) {
			throw new FileStorageException("Falha ao salvar arquivo de nome "+ nomeArquivo + " no disco. ", e);
		}
	}
	
	private String gerarNomeArquivoProduto(MultipartFile arquivo) {
		String[] contentType = arquivo.getContentType().split("/");
		if (!contentType[0].contains("image") && !contentType[0].contains("video")) {
			throw new FileStorageException("Só é aceito imagens e videos");
		}
		if (!contentType[1].contains("jpg") && !contentType[1].contains("jpeg") && !contentType[1].contains("png")
				&& !contentType[1].contains("mp4") && !contentType[1].contains("avi")) {
			throw new FileStorageException("Os tipos aceitos são jpg, jpeg, png, mp4 e avi");
		}
		String stringUnica = gerarStringUnica();
		return stringUnica + "." + contentType[1];
	}
	
	private static String gerarStringUnica() {
        String string = UUID.randomUUID().toString();
        int divisao = string.length() / 3;
        long timestamp = Instant.now().toEpochMilli();
        String substring = string.substring(0, divisao);
        return substring + timestamp;
    }
    
    public byte[] recuperarBytesArquivoProdutoDoDisco(String nomeArquivoProduto) {
		try {
	        String diretorioAtual = System.getProperty("user.dir");

	        // Imprimir o diretório atual
	        System.out.println(" O diretorio atual é: " + diretorioAtual);
			
			FileUrlResource fileUrlResource = new FileUrlResource(raizProduto + "/" + nomeArquivoProduto);
			byte[] bytes = fileUrlResource.getContentAsByteArray();
			byte[] bytesBase64 = Base64Utils.codificarParaBase64(bytes);
			return bytesBase64;
		} catch (IOException e) {
			e.printStackTrace();
			throw new FileStorageException("Não foi possivel recuperar os bytes do arquivo nome " + nomeArquivoProduto + ", motivo: " + e);
		}
	}

	public void verificarSeExisteArquivoProdutoPorNome(String nome) {
		try {
			FileUrlResource fileUrlResource = new FileUrlResource(raizProduto + "/" + nome);
			if (!fileUrlResource.exists()) {
				throw new ValidacaoException("Não existe arquivo-produto com o nome " + nome);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

}
