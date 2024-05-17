package br.com.danielschiavo.shop.service.filestorage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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

import br.com.danielschiavo.infra.security.UsuarioAutenticadoService;
import br.com.danielschiavo.shop.model.FileStorageException;
import br.com.danielschiavo.shop.model.filestorage.ArquivoInfoDTO;

@ExtendWith(MockitoExtension.class)
class FileStoragePedidoServiceTest {

	@Mock
	private UsuarioAutenticadoService usuarioAutenticadoService;
	
	@Spy
	@InjectMocks
	private FileStoragePedidoService fileStoragePedidoService;
	
	@Spy
	private FileStorageProdutoService fileStorageProdutoService;
	
	@Mock
	private FileUrlResource fileUrlResource;
	
    @Mock
    private MultipartFile arquivo1;
    
    @Mock
    private MultipartFile arquivo2;
    
  @Test
  @DisplayName("Pegar arquivo produto por nome deve executar normalmente quando enviado um nome válido")
  void pegarImagemPedidoPorNome_ArquivoExiste_NaoDeveLancarExcecao() {
  	//ARRANGE
  	String nomeArquivo = "arquivo1.jpeg";
  	byte[] bytes = {1, 2, 3};
  	Mockito.doReturn(bytes).when(fileStoragePedidoService).recuperarBytesImagemPedidoDoDisco("arquivo1.jpeg");
  	
  	//ACT
  	ArquivoInfoDTO arquivoInfoDTO = fileStoragePedidoService.pegarImagemPedidoPorNome(nomeArquivo);
  	
  	//ASSERT
  	Assertions.assertEquals(nomeArquivo, arquivoInfoDTO.nomeArquivo());
  	Assertions.assertEquals(bytes, arquivoInfoDTO.bytesArquivo());
  }
  
  @Test
  @DisplayName("Pegar arquivo produto por nome deve lançar exceção quando nome do arquivo não existir no diretorio de arquivos")
  void pegarImagemPedidoPorNome_ArquivoNaoExiste_DeveLancarExcecao() {
  	//ARRANGE
  	String nomeArquivo = "arquivo1.jpeg";
      Mockito.doThrow(new FileStorageException("Falha ao acessar arquivo")).when(fileStoragePedidoService).recuperarBytesImagemPedidoDoDisco("arquivo1.jpeg");
  	
  	//ASSERT + ACT
  	Assertions.assertThrows(FileStorageException.class, () -> fileStoragePedidoService.pegarImagemPedidoPorNome(nomeArquivo));
  }
  
  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Persistir foto perfil deve cadastrar nova imagem pedido no disco quando imagem pedido não já existir no disco")
  void persistirOuRecuperarImagemPedido_NaoExisteImagemPedidoNoDisco_DeveCadastrarNovaImagemPedido() throws IOException {
  	  //ARRAGE
	  String nomePrimeiraImagemProduto = "Padrao.jpeg";
	  byte[] bytes = "conteúdo do arquivo primeira imagem".getBytes();
	  when(fileStoragePedidoService.verificarSeExisteImagemPedidoNoDisco(nomePrimeiraImagemProduto)).thenReturn(null);
	  when(fileStorageProdutoService.recuperarBytesArquivoProdutoDoDisco(nomePrimeiraImagemProduto)).thenReturn(bytes);
      try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
    	  DirectoryStream<Path> emptyDirectoryStream = Mockito.mock(DirectoryStream.class);
          mockedFiles.when(() -> Files.newDirectoryStream(any())).thenReturn(emptyDirectoryStream);
          mockedFiles.when(() -> Files.write(any(Path.class), any(byte[].class), any(StandardOpenOption.class)))
          .thenAnswer(invocation -> null);
          
      	  //ACT
          Long idProduto = 1L;
      	  String resultado = fileStoragePedidoService.persistirOuRecuperarImagemPedido(nomePrimeiraImagemProduto, idProduto);
      	
      	  //ASSERT
      	  Assertions.assertNotNull(resultado);
      	  Assertions.assertEquals(true, resultado.endsWith(".jpeg"));
      	  mockedFiles.verify(() -> Files.write(any(Path.class), any(byte[].class), any(StandardOpenOption.class)), times(1));
      }
  }
  
  @Test
  @DisplayName("Persistir foto perfil deve retornar imagem pedido do disco quando já existir no disco")
  void persistirOuRecuperarImagemPedido_ExisteImagemPedidoNoDisco_DeveRetornarImagemPedidoDoDisco() throws IOException {
  	  //ARRAGE
	  String nomePrimeiraImagemProduto = "Padrao.jpeg";
	  Long idProduto = 1L;
          
	  //ACT
      String resultado = fileStoragePedidoService.persistirOuRecuperarImagemPedido(nomePrimeiraImagemProduto, idProduto);
      	
      //ASSERT
      Assertions.assertEquals(true, resultado.equals(nomePrimeiraImagemProduto));
  }
}
