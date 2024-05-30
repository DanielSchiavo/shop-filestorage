package br.com.danielschiavo.shop.service.filestorage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
	
	public static final Path raizProduto = Paths.get("imagens/produto");
	
	@Autowired
	private FileStorageService fileStorageService;
	
	public Object deletarImagens(List<String> nomesArquivos) {
		Object arquivos = fileStorageService.deletarNoDisco(raizProduto, nomesArquivos.toArray(new String[0]));
		
		return arquivos;
	}
	
	public Object pegarImagens(String... nomesImagens) {
		Object arquivos = fileStorageService.recuperarBytesImagemDoDisco(raizProduto, nomesImagens);
		
		return arquivos;
	}
	
	public Object persistirImagens(MultipartFile[] arquivos, UriComponentsBuilder uriBuilderBase) {
	    List<ArquivoInfoDTO> arquivosInfo = new ArrayList<>();

	    for (MultipartFile arquivo : arquivos) {
	    	try {
	    		String nomeArquivo = gerarNomeArquivoProduto(arquivo);
	    		fileStorageService.salvarNoDisco(raizProduto, nomeArquivo, arquivo.getBytes());
	    		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(uriBuilderBase.toUriString());
	    		URI uri = uriBuilder.path("/arquivo-produto/" + nomeArquivo).build().toUri();
	    		var arquivoInfo = ArquivoInfoDTO.comUriENomeAntigoArquivo(nomeArquivo, arquivo.getOriginalFilename(), uri.toString());
	    		arquivosInfo.add(arquivoInfo);
			} catch (FileStorageException e) {
				arquivosInfo.add(ArquivoInfoDTO.comErro(arquivo.getOriginalFilename(), e.getMessage()));
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    
	    return arquivosInfo;
	}
	
	public RespostaAlterarArquivosDTO alterarImagens(MultipartFile[] arquivos, List<String> excluirArquivo, UriComponentsBuilder uriBuilderBase) {
		if (arquivos.length == 0 || excluirArquivo.isEmpty())
			throw new ValidacaoException("Você tem que mandar pelo menos um arquivo e um nome de um arquivo a ser excluido");
		if (arquivos.length != excluirArquivo.size())
			throw new ValidacaoException("Para cada arquivo enviado você deve enviar um nome de arquivo a ser excluido");
		
		List<ArquivoInfoDTO> sucesso = new ArrayList<>();
		List<ArquivoInfoDTO> falha = new ArrayList<>();
		
		for (int i = 0; i < excluirArquivo.size(); i++) {
			try {
				ArquivoInfoDTO arquivoInfoDTO = (ArquivoInfoDTO) fileStorageService.deletarNoDisco(raizProduto, excluirArquivo.get(i));
				if (arquivoInfoDTO != null)
					falha.add(arquivoInfoDTO);
				
				UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(uriBuilderBase.toUriString());
				String novoNomeGerado = gerarNomeArquivoProduto(arquivos[i]);
				fileStorageService.salvarNoDisco(raizProduto, novoNomeGerado, arquivos[i].getBytes());
				URI uri = uriBuilder.path("/arquivo-produto/" + novoNomeGerado).build().toUri();
				
				sucesso.add(ArquivoInfoDTO.comUri(novoNomeGerado, uri.toString(), arquivos[i].getBytes()));
			} catch (FileStorageException e) {
				falha.add(ArquivoInfoDTO.comErro(arquivos[i].getOriginalFilename(), e.getMessage()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return new RespostaAlterarArquivosDTO(sucesso, falha);
	}

	
//
// METODOS UTILITARIOS DE PRODUTO
//	

	private String gerarNomeArquivoProduto(MultipartFile arquivo) {
		String[] contentType = arquivo.getContentType().split("/");
		if (!contentType[1].contains("jpg") && !contentType[1].contains("jpeg") && !contentType[1].contains("png")
				&& !contentType[1].contains("mp4") && !contentType[1].contains("avi")) {
			throw new FileStorageException("Os tipos aceitos são jpg, jpeg, png, mp4 e avi");
		}
		String stringUnica = FileStorageService.gerarStringUnica();
		return stringUnica + "." + contentType[1];
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
