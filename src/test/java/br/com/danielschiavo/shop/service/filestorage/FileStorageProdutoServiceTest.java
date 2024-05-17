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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import br.com.danielschiavo.shop.model.filestorage.ArquivoInfoDTO;

@ExtendWith(MockitoExtension.class)
class FileStorageProdutoServiceTest {

	@Mock
	private UsuarioAutenticadoService usuarioAutenticadoService;
	
	@Spy
	@InjectMocks
	private FileStorageProdutoService fileStorageProdutoService;
	
	@Mock
	private FileUrlResource fileUrlResource;
	
    @Mock
    private MultipartFile arquivo1;
    
    @Mock
    private MultipartFile arquivo2;
	
    @Test
    @DisplayName("Deletar arquivo produto no disco deve executar normalmente quando arquivo existe")
    void deletarArquivoProdutoNoDisco_ArquivoExiste_NaoDeveLancarExcecao() throws IOException {
    	//ARRANGE
        Path pathEsperado = Paths.get("imagens/produto");
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
        	
        	//ACT
        	String arquivo = "teste.jpeg";
        	fileStorageProdutoService.deletarArquivoProdutoNoDisco(new ArrayList<>(List.of(arquivo)));

        	//ASSERT
            mockedFiles.verify(() -> Files.delete(pathEsperado.resolve(arquivo)), Mockito.times(1));
        }
    }
    
    @Test
    @DisplayName("Deletar arquivo produto no disco deve lançar exceção quando arquivo não existe")
    void deletarArquivoProdutoNoDisco_ArquivoNaoExiste_DeveLancarExcecao() throws IOException {
    	//ARRANGE
        Path pathEsperado = Paths.get("imagens/produto/teste.jpeg");
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
        	mockedFiles.when(() -> Files.delete(pathEsperado)).thenThrow(IOException.class);

        	//ASSERT + ACT
        	Assertions.assertThrows(FileStorageException.class, () -> fileStorageProdutoService.deletarArquivoProdutoNoDisco(new ArrayList<>(List.of("teste.jpeg"))));
        }
    }
    
    @Test
    @DisplayName("Mostrar arquivo produto por lista de nomes deve devolver um ArquivoInfoDTO por cada nome contendo os bytes do arquivo")
    void mostrarArquivoProdutoPorListaDeNomes_ConseguiuRecuperarBytesArquivosComSucesso_DeveCriarArquivoInfoDtoNormalmente() throws IOException {
    	//ARRANGE
        List<String> nomesArquivos = Arrays.asList("arquivo1.txt", "arquivo2.txt");
        byte[] bytes = {1, 2, 3};
        Mockito.doReturn(bytes).when(fileStorageProdutoService).recuperarBytesArquivoProdutoDoDisco(any());
        
        //ACT
        List<ArquivoInfoDTO> resultado = fileStorageProdutoService.mostrarArquivoProdutoPorListaDeNomes(nomesArquivos);

        //ASSERT
        Assertions.assertEquals(2, resultado.size());
        Assertions.assertEquals(nomesArquivos.get(0), resultado.get(0).nomeArquivo());
        Assertions.assertEquals(nomesArquivos.get(1), resultado.get(1).nomeArquivo());
        Assertions.assertArrayEquals(bytes, resultado.get(0).bytesArquivo());
        Assertions.assertArrayEquals(bytes, resultado.get(1).bytesArquivo());
    }
    
    @Test
    @DisplayName("Mostrar arquivo produto por lista de nomes quando tiver problema ao pegar algum arquivo deve criar um arquivo info dto com mensagem de erro e bytes arquivo null")
    void mostrarArquivoProdutoPorListaDeNomes_ProblemaAoPegarUmArquivo_DeveCriarUmArquivoInfoDtoComMensagemDeErro() throws IOException {
    	//ARRANGE
        List<String> nomesArquivos = Arrays.asList("arquivo1.jpeg", "arquivo2.jpeg", "arquivo3.jpeg");
        byte[] bytes = {1, 2, 3};
        Mockito.doReturn(bytes).when(fileStorageProdutoService).recuperarBytesArquivoProdutoDoDisco("arquivo1.jpeg");
        Mockito.doThrow(new FileStorageException("Falha ao acessar arquivo")).when(fileStorageProdutoService).recuperarBytesArquivoProdutoDoDisco("arquivo2.jpeg");
        Mockito.doReturn(bytes).when(fileStorageProdutoService).recuperarBytesArquivoProdutoDoDisco("arquivo3.jpeg");
        
        //ACT
        List<ArquivoInfoDTO> resultado = fileStorageProdutoService.mostrarArquivoProdutoPorListaDeNomes(nomesArquivos);

        //ASSERT
        Assertions.assertEquals(3, resultado.size());
        Assertions.assertEquals(nomesArquivos.get(0), resultado.get(0).nomeArquivo());
        Assertions.assertEquals(nomesArquivos.get(1), resultado.get(1).nomeArquivo());
        Assertions.assertEquals(nomesArquivos.get(2), resultado.get(2).nomeArquivo());
        Assertions.assertEquals(null, resultado.get(0).erro());
        Assertions.assertEquals("Falha ao acessar arquivo", resultado.get(1).erro());
        Assertions.assertEquals(null, resultado.get(2).erro());
        Assertions.assertArrayEquals(bytes, resultado.get(0).bytesArquivo());
        Assertions.assertArrayEquals(null, resultado.get(1).bytesArquivo());
        Assertions.assertArrayEquals(bytes, resultado.get(2).bytesArquivo());
    }
    
    @Test
    @DisplayName("Pegar arquivo produto por nome deve executar normalmente quando enviado um nome válido")
    void pegarArquivoProdutoPorNome_ArquivoExiste_NaoDeveLancarExcecao() {
    	//ARRANGE
    	String nomeArquivo = "arquivo1.jpeg";
    	byte[] bytes = {1, 2, 3};
    	Mockito.doReturn(bytes).when(fileStorageProdutoService).recuperarBytesArquivoProdutoDoDisco("arquivo1.jpeg");
    	
    	//ACT
    	ArquivoInfoDTO arquivoInfoDTO = fileStorageProdutoService.pegarArquivoProdutoPorNome(nomeArquivo);
    	
    	//ASSERT
    	Assertions.assertEquals(nomeArquivo, arquivoInfoDTO.nomeArquivo());
    	Assertions.assertEquals(bytes, arquivoInfoDTO.bytesArquivo());
    }
    
    @Test
    @DisplayName("Pegar arquivo produto por nome deve lançar exceção quando nome do arquivo não existir no diretorio de arquivos")
    void pegarArquivoProdutoPorNome_ArquivoNaoExiste_DeveLancarExcecao() {
    	//ARRANGE
    	String nomeArquivo = "arquivo1.jpeg";
        Mockito.doThrow(new FileStorageException("Falha ao acessar arquivo")).when(fileStorageProdutoService).recuperarBytesArquivoProdutoDoDisco("arquivo1.jpeg");
    	
    	//ASSERT + ACT
    	Assertions.assertThrows(FileStorageException.class, () -> fileStorageProdutoService.pegarArquivoProdutoPorNome(nomeArquivo));
    }
    
    @Test
    @DisplayName("Persistir array arquivo produto deve funcionar normalmente quando extensão do arquivo for válida (PNG, JPEG, JPG, AVI, MP4)")
    void persistirArrayArquivoProduto_ExtensaoDoArquivoValida_NaoDeveLancarExcecao() throws IOException {
    	//ARRAGE
    	byte[] bytes1 = "conteúdo do arquivo 1".getBytes();
    	byte[] bytes2 = "conteúdo do arquivo 2".getBytes();
    	ByteArrayInputStream inputStream1 = new ByteArrayInputStream(bytes1);
    	ByteArrayInputStream inputStream2 = new ByteArrayInputStream(bytes2);
    	when(arquivo1.getInputStream()).thenReturn(inputStream1);
    	when(arquivo2.getInputStream()).thenReturn(inputStream2);
    	when(arquivo1.getContentType()).thenReturn("image/jpeg");
    	when(arquivo2.getContentType()).thenReturn("image/jpeg");
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
            .thenAnswer(invocation -> null);
        	
        	//ACT
        	MultipartFile[] arquivos = {arquivo1, arquivo2};
        	UriComponentsBuilder uriBuilderBase = UriComponentsBuilder.fromUriString("http://localhost:8080");
        	List<ArquivoInfoDTO> resultado = fileStorageProdutoService.persistirArrayArquivoProduto(arquivos, uriBuilderBase);
        	
        	//ASSERT
        	Assertions.assertNotNull(resultado);
        	Assertions.assertEquals(2, resultado.size());
        	ArquivoInfoDTO arquivoInfo1 = resultado.get(0);
        	Assertions.assertEquals(true, arquivoInfo1.nomeArquivo().endsWith(".jpeg"));
        	Assertions.assertTrue(arquivoInfo1.uri().contains(".jpeg"));
        	Assertions.assertArrayEquals(bytes1, arquivoInfo1.bytesArquivo());
        	ArquivoInfoDTO arquivoInfo2 = resultado.get(1);
        	Assertions.assertEquals(true, arquivoInfo2.nomeArquivo().endsWith(".jpeg"));
        	Assertions.assertTrue(arquivoInfo2.uri().contains(".jpeg"));
        	Assertions.assertArrayEquals(bytes2, arquivoInfo2.bytesArquivo());
        }
    }
    
    @Test
    @DisplayName("Persistir array arquivo produto deve lançar exceção quando extensão do arquivo for diferente de PNG, JPEG, JPG, AVI, MP4")
    void persistirArrayArquivoProduto_ExtensaoDoArquivoInvalida_DeveLancarExcecao() throws IOException {
    	//ARRAGE
        when(arquivo1.getContentType()).thenReturn("application/pdf");
        when(arquivo2.getContentType()).thenReturn("application/pdf");
        	
        //ACT
        MultipartFile[] arquivos = {arquivo1, arquivo2};
        UriComponentsBuilder uriBuilderBase = UriComponentsBuilder.fromUriString("http://localhost:8080");
        List<ArquivoInfoDTO> listaArquivoInfoDTO = fileStorageProdutoService.persistirArrayArquivoProduto(arquivos, uriBuilderBase);
        	
        //ASSERT
        Assertions.assertNotNull(listaArquivoInfoDTO.get(0).erro());
        Assertions.assertNotNull(listaArquivoInfoDTO.get(1).erro());
    }
    
    @Test
    @DisplayName("Alterar arquivo produto deve executar normalmente quando arquivo e nome valido são enviados")
    void alterarArquivoProduto_ArquivoENomeValidoEnviado_DeveExecutarNormalmente() throws IOException {
    	//ARRANGE
    	byte[] bytes1 = "conteúdo do arquivo 1".getBytes();
    	byte[] bytes2 = "conteúdo do arquivo 2".getBytes();
    	ByteArrayInputStream inputStream1 = new ByteArrayInputStream(bytes1);
    	ByteArrayInputStream inputStream2 = new ByteArrayInputStream(bytes2);
    	when(arquivo1.getInputStream()).thenReturn(inputStream1);
    	when(arquivo2.getInputStream()).thenReturn(inputStream2);
    	when(arquivo1.getContentType()).thenReturn("image/jpeg");
    	when(arquivo2.getContentType()).thenReturn("image/jpeg");
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
            .thenAnswer(invocation -> null);
            mockedFiles.when(() -> Files.delete(any(Path.class))).thenAnswer(invocation -> null);
            
        	//ACT
        	MultipartFile[] arquivos = {arquivo1, arquivo2};
        	String nomesArquivosASeremExcluidos = "arquivoexcluido1.jpg, arquivoexcluido2.jpg";
        	UriComponentsBuilder uriBuilderBase = UriComponentsBuilder.fromUriString("http://localhost:8080");
        	List<ArquivoInfoDTO> listaArquivoInfoDTO = fileStorageProdutoService.alterarArrayArquivoProduto(arquivos, nomesArquivosASeremExcluidos, uriBuilderBase);
        	
        	//ASSERT
        	Assertions.assertEquals(2, listaArquivoInfoDTO.size());
        	Assertions.assertEquals(true, listaArquivoInfoDTO.get(0).nomeArquivo().endsWith(".jpeg"));
        	Assertions.assertEquals(true, listaArquivoInfoDTO.get(1).nomeArquivo().endsWith(".jpeg"));
        	Assertions.assertArrayEquals(bytes1, listaArquivoInfoDTO.get(0).bytesArquivo());
        	Assertions.assertArrayEquals(bytes2, listaArquivoInfoDTO.get(1).bytesArquivo());
        }
    }
    
    @Test
    @DisplayName("Alterar arquivo produto deve retornar ArquivoInfoDTO com erro quando nome invalido é enviado")
    void alterarArquivoProduto_NomeInvalidoEnviado_DeveRetornarArquivoInfoDtoComErro() throws IOException {
    	//ARRANGE
    	byte[] bytes1 = "conteúdo do arquivo 1".getBytes();
    	byte[] bytes2 = "conteúdo do arquivo 2".getBytes();
    	ByteArrayInputStream inputStream1 = new ByteArrayInputStream(bytes1);
    	ByteArrayInputStream inputStream2 = new ByteArrayInputStream(bytes2);
    	when(arquivo1.getInputStream()).thenReturn(inputStream1);
    	when(arquivo2.getInputStream()).thenReturn(inputStream2);
    	when(arquivo1.getContentType()).thenReturn("image/jpeg");
    	when(arquivo2.getContentType()).thenReturn("image/jpeg");
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
            .thenAnswer(invocation -> null);
            mockedFiles.when(() -> Files.delete(any(Path.class))).thenThrow(IOException.class);
            
        	//ACT
        	MultipartFile[] arquivos = {arquivo1, arquivo2};
        	String nomesArquivosASeremExcluidos = "arquivoexcluido1.jpg, arquivoexcluido2.jpg";
        	UriComponentsBuilder uriBuilderBase = UriComponentsBuilder.fromUriString("http://localhost:8080");
        	List<ArquivoInfoDTO> listaArquivoInfoDTO = fileStorageProdutoService.alterarArrayArquivoProduto(arquivos, nomesArquivosASeremExcluidos, uriBuilderBase);
        	
        	//ASSERT
        	Assertions.assertEquals(4, listaArquivoInfoDTO.size());
        	Assertions.assertNotNull(listaArquivoInfoDTO.get(0).erro());
        	Assertions.assertNotNull(listaArquivoInfoDTO.get(1).erro());
        	Assertions.assertEquals(true, listaArquivoInfoDTO.get(2).nomeArquivo().endsWith(".jpeg"));
        	Assertions.assertEquals(true, listaArquivoInfoDTO.get(3).nomeArquivo().endsWith(".jpeg"));
        	Assertions.assertArrayEquals(bytes1, listaArquivoInfoDTO.get(2).bytesArquivo());
        	Assertions.assertArrayEquals(bytes2, listaArquivoInfoDTO.get(3).bytesArquivo());
        }
    }
    
    @Test
    @DisplayName("Alterar arquivo produto deve retornar ArquivoInfoDTO com erro quando arquivo invalido é enviado")
    void alterarArquivoProduto_ArquivoInvalidoEnviado_DeveRetornarArquivoInfoDtoComErro() throws IOException {
    	//ARRANGE
    	when(arquivo1.getContentType()).thenReturn("application/pdf");
    	when(arquivo2.getContentType()).thenReturn("application/pdf");
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
            .thenAnswer(invocation -> null);
            mockedFiles.when(() -> Files.delete(any(Path.class))).thenAnswer(invocation -> null);
            
        	//ACT
        	MultipartFile[] arquivos = {arquivo1, arquivo2};
        	String nomesArquivosASeremExcluidos = "arquivoexcluido1.jpg, arquivoexcluido2.jpg";
        	UriComponentsBuilder uriBuilderBase = UriComponentsBuilder.fromUriString("http://localhost:8080");
        	List<ArquivoInfoDTO> listaArquivoInfoDTO = fileStorageProdutoService.alterarArrayArquivoProduto(arquivos, nomesArquivosASeremExcluidos, uriBuilderBase);
        	
        	//ASSERT
        	Assertions.assertEquals(2, listaArquivoInfoDTO.size());
        	Assertions.assertNotNull(listaArquivoInfoDTO.get(0).erro());
        	Assertions.assertNotNull(listaArquivoInfoDTO.get(1).erro());
        }
    }
}
