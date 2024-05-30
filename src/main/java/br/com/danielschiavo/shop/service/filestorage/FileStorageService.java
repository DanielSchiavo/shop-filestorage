package br.com.danielschiavo.shop.service.filestorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.danielschiavo.shop.Base64Utils;
import br.com.danielschiavo.shop.model.FileStorageException;
import br.com.danielschiavo.shop.model.filestorage.ArquivoInfoDTO;

@Service
public class FileStorageService {
	
	public static String gerarStringUnica() {
        String string = UUID.randomUUID().toString();
        int divisao = string.length() / 3;
        long timestamp = Instant.now().toEpochMilli();
        String substring = string.substring(0, divisao);
        return substring + timestamp;
    }
	
	public Object deletarNoDisco(Path caminho, String... nomesImagens) {
		verificacaoDiretorioAtual();
		
		List<ArquivoInfoDTO> listaArquivosInfoDTO = new ArrayList<>();
		
		for (String nomeImagem : nomesImagens) {
			if (nomeImagem.equals("Padrao.jpeg"))
				throw new FileStorageException("O arquivo não pode ser excluido porque é a imagem padrão para produtos sem fotos.");
			
			try {
				if (!Files.deleteIfExists(caminho.resolve(nomeImagem)))
					throw new FileStorageException("O arquivo não existe, portanto não foi possivel exclui-lo");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (FileStorageException e) {
				listaArquivosInfoDTO.add(ArquivoInfoDTO.comErro(nomeImagem, e.getMessage()));
			}
		}
		
		return nomesImagens.length > 1 ? 
				listaArquivosInfoDTO.get(0) : 
					listaArquivosInfoDTO;
		
	}
	
	public void salvarNoDisco(Path caminho, String nomesImagens, byte[] bytes) {
		verificacaoDiretorioAtual();
		
		try {
			Files.write(caminho.resolve(nomesImagens), bytes, StandardOpenOption.CREATE_NEW);
		} catch (IOException e) {
			e.printStackTrace();
			throw new FileStorageException("Não foi possivel salvar o arquivo " + nomesImagens + " no disco");
		}
	}
	
    public Object recuperarBytesImagemDoDisco(Path caminho, String... nomesImagens) {
    	verificacaoDiretorioAtual();

    	String nomeAtual = null;
    	List<ArquivoInfoDTO> listaArquivosInfoDTO = new ArrayList<>();

		for (String nome : nomesImagens) {
			try {
				nomeAtual = nome;
				byte[] allBytes = Files.readAllBytes(caminho.resolve(nome));
				byte[] allBytesBase64 = Base64Utils.codificarParaBase64(allBytes);
				listaArquivosInfoDTO.add(new ArquivoInfoDTO(nome, allBytesBase64));
			} catch (IOException e) {
				listaArquivosInfoDTO.add(ArquivoInfoDTO.comErro(nome, "Não foi possivel recuperar os bytes do arquivo nome " + nomeAtual
						+ ", motivo: " + e.getMessage()));
				e.printStackTrace();
			}
		}
		
		return listaArquivosInfoDTO.size() == 1 ? 
				listaArquivosInfoDTO.get(0) : 
					listaArquivosInfoDTO;
	}
    
    public void verificacaoDiretorioAtual() {
    	String diretorioAtual = System.getProperty("user.dir");
    	System.out.println(" O diretorio atual é: " + diretorioAtual);
    }

}
