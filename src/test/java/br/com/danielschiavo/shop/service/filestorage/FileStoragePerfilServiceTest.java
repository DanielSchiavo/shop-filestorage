package br.com.danielschiavo.shop.service.filestorage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileUrlResource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.danielschiavo.infra.security.UsuarioAutenticadoService;
import br.com.danielschiavo.shop.model.FileStorageException;
import br.com.danielschiavo.shop.model.ValidacaoException;
import br.com.danielschiavo.shop.model.filestorage.ArquivoInfoDTO;

@ExtendWith(MockitoExtension.class)
class FileStoragePerfilServiceTest {

	@Mock
	private UsuarioAutenticadoService usuarioAutenticadoService;
	
	@Spy
	@InjectMocks
	private FileStoragePerfilService fileStoragePerfilService;
	
	@Mock
	private FileUrlResource fileUrlResource;
	
    @Mock
    private MultipartFile arquivo1;
    
    @Mock
    private MultipartFile arquivo2;
    
    @Test
    @DisplayName("Deletar foto perfil no disco deve executar normalmente quando foto existe")
    void deletarFotoPerfilNoDisco_FotoPerfilExiste_NaoDeveLancarExcecao() throws IOException {
    	//ARRANGE
        Path pathEsperado = Paths.get("imagens/perfil");
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
        	
        	//ACT
        	String arquivo = "teste.jpeg";
        	fileStoragePerfilService.deletarFotoPerfilNoDisco("teste.jpeg");

        	//ASSERT
            mockedFiles.verify(() -> Files.delete(pathEsperado.resolve(arquivo)));
        }
    }
    
    @Test
    @DisplayName("Deletar foto perfil no disco deve lançar exceção quando foto perfil não existe")
    void deletarFotoPerfilNoDisco_FotoPerfilNaoExiste_DeveLancarExcecao() throws IOException {
    	//ARRANGE
    	Path pathEsperado = Paths.get("imagens/perfil/teste.jpeg");
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
        	mockedFiles.when(() -> Files.delete(pathEsperado)).thenThrow(IOException.class);

        	//ASSERT + ACT
        	Assertions.assertThrows(IOException.class, () -> fileStoragePerfilService.deletarFotoPerfilNoDisco("teste.jpeg"));
        }
    }
    
    @Test
    @DisplayName("Deletar foto perfil no disco deve lançar exceção quando tentar excluir foto de perfil padrão")
    void deletarFotoPerfilNoDisco_TentandoExcluirFotoPerfilPadrao_DeveLancarExcecao() throws IOException {
    	//ARRANGE
    	String fotoPadrao = "Padrao.jpeg";

        //ASSERT + ACT
        Assertions.assertThrows(ValidacaoException.class, () -> fileStoragePerfilService.deletarFotoPerfilNoDisco(fotoPadrao));
    }
    
    @Test
    @DisplayName("Pegar foto perfil por nome deve executar normalmente quando enviado um nome válido")
    void pegarFotoPerfilPorNome_FotoExiste_NaoDeveLancarExcecao() {
    	//ARRANGE
    	String nomeArquivo = "arquivo1.jpeg";
    	byte[] bytes = {1, 2, 3};
    	Mockito.doReturn(bytes).when(fileStoragePerfilService).recuperarBytesFotoPerfilDoDisco("arquivo1.jpeg");
    	
    	//ACT
    	ArquivoInfoDTO arquivoInfoDTO = fileStoragePerfilService.pegarFotoPerfilPorNome(nomeArquivo);
    	
    	//ASSERT
    	Assertions.assertEquals(nomeArquivo, arquivoInfoDTO.nomeArquivo());
    	Assertions.assertEquals(bytes, arquivoInfoDTO.bytesArquivo());
    }
    
    @Test
    @DisplayName("Pegar foto perfil por nome deve lançar exceção quando nome do arquivo não existir no diretorio de arquivos")
    void pegarFotoPerfilPorNome_FotoNaoExiste_DeveLancarExcecao() {
    	//ARRANGE
    	String nomeArquivo = "arquivo1.jpeg";
        Mockito.doThrow(new FileStorageException("Falha ao acessar arquivo")).when(fileStoragePerfilService).recuperarBytesFotoPerfilDoDisco("arquivo1.jpeg");
    	
    	//ASSERT + ACT
    	Assertions.assertThrows(FileStorageException.class, () -> fileStoragePerfilService.pegarFotoPerfilPorNome(nomeArquivo));
    }
    
  @Test
  @DisplayName("Persistir foto perfil deve funcionar normalmente quando extensão do arquivo for válida (PNG, JPEG, JPG)")
  void persistirFotoPerfil_ExtensaoDoArquivoValida_NaoDeveLancarExcecao() throws IOException {
  	//ARRAGE
	  byte[] bytes1 = "conteúdo do arquivo 1".getBytes();
	  ByteArrayInputStream inputStream1 = new ByteArrayInputStream(bytes1);
	  when(arquivo1.getInputStream()).thenReturn(inputStream1);
	  when(arquivo1.getContentType()).thenReturn("image/jpeg");
      try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
          mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
          .thenAnswer(invocation -> null);
      	
      	  //ACT
      	  MultipartFile arquivo = arquivo1;
      	  UriComponentsBuilder uriBuilderBase = UriComponentsBuilder.fromUriString("http://localhost:8080");
      	  ArquivoInfoDTO resultado = fileStoragePerfilService.persistirFotoPerfil(arquivo, uriBuilderBase);
      	
      	  //ASSERT
      	  Assertions.assertNotNull(resultado);
      	  Assertions.assertEquals(true, resultado.nomeArquivo().endsWith(".jpeg"));
      	  Assertions.assertTrue(resultado.uri().contains(".jpeg"));
      	  Assertions.assertArrayEquals(bytes1, resultado.bytesArquivo());
      }
  }
  
  @Test
  @DisplayName("Persistir foto perfil deve lançar exceção quando extensão do arquivo for diferente de PNG, JPEG, JPG")
  void persistirFotoPerfil_ExtensaoDoArquivoInvalida_DeveLancarExcecao() throws IOException {
  	//ARRAGE
      try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
      	when(arquivo1.getContentType()).thenReturn("application/pdf");
      	String nomeArquivo = "arquivo1.jpeg";
      	when(arquivo1.getOriginalFilename()).thenReturn(nomeArquivo);
      	
      	//ACT
      	MultipartFile arquivo = arquivo1;
      	UriComponentsBuilder uriBuilderBase = UriComponentsBuilder.fromUriString("http://localhost:8080");
      	ArquivoInfoDTO resultado = fileStoragePerfilService.persistirFotoPerfil(arquivo, uriBuilderBase);
      	
      	//ASSERT
      	Assertions.assertNotNull(resultado);
      	Assertions.assertEquals(nomeArquivo, resultado.nomeArquivo());
      	Assertions.assertNotNull(resultado.erro());
      }
  }
  
  @Test
  @DisplayName("Alterar arquivo produto deve executar normalmente quando arquivo e nome valido são enviados")
  void alterarFotoPerfil_ArquivoENomeValidoEnviado_DeveExecutarNormalmente() throws IOException {
	  //ARRANGE
	  byte[] bytes1 = "conteúdo do arquivo 1".getBytes();
	  ByteArrayInputStream inputStream1 = new ByteArrayInputStream(bytes1);
	  when(arquivo1.getInputStream()).thenReturn(inputStream1);
	  when(arquivo1.getContentType()).thenReturn("image/jpeg");
	  String nomeArquivoASerSubstituido = "arquivosubstituido.jpeg";
	  Mockito.doNothing().when(fileStoragePerfilService).verificarSeExisteFotoPerfilPorNome(nomeArquivoASerSubstituido);
      try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
          mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
          .thenAnswer(invocation -> null);
          
      	//ACT
      	MultipartFile arquivo = arquivo1;
      	ArquivoInfoDTO arquivoInfoDTO = fileStoragePerfilService.alterarFotoPerfil(arquivo, nomeArquivoASerSubstituido);
      	
      	//ASSERT
      	Assertions.assertNotNull(arquivoInfoDTO);
      	Assertions.assertEquals(true, arquivoInfoDTO.nomeArquivo().endsWith(".jpeg"));
      	Assertions.assertArrayEquals(bytes1, arquivoInfoDTO.bytesArquivo());
      }
  }
  
  @Test
  @DisplayName("Alterar arquivo produto deve lançar exceção quando nome invalido é enviado")
  void alterarFotoPerfil_NomeInvalidoEnviado_DeveLancarExcecao() throws IOException {
	  // ARRANGE
	  when(arquivo1.getContentType()).thenReturn("image/jpeg");
	  String nomeArquivoASerSubstituido = "arquivosubstituido.jpeg";

	  // ASSERT + ACT
	  MultipartFile arquivo = arquivo1;
	  Assertions.assertThrows(ValidacaoException.class, () -> fileStoragePerfilService.alterarFotoPerfil(arquivo, nomeArquivoASerSubstituido));
  }
  
  @Test
  @DisplayName("Alterar arquivo produto deve lançar exceção quando arquivo invalido é enviado")
  void alterarFotoPerfil_ArquivoInvalidoEnviado_DeveLancarExcecao() throws IOException {
	  // ARRANGE
	  when(arquivo1.getContentType()).thenReturn("application/pdf");
	  String nomeArquivoASerSubstituido = "arquivosubstituido.jpeg";
	  MultipartFile arquivo = arquivo1;

	  // ASSERT + ACT
	  Assertions.assertThrows(FileStorageException.class, () -> fileStoragePerfilService.alterarFotoPerfil(arquivo, nomeArquivoASerSubstituido));
  }
}
