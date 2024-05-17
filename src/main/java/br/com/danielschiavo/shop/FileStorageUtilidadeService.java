package br.com.danielschiavo.shop;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class FileStorageUtilidadeService {
	
	public static String gerarStringUnica() {
        String string = UUID.randomUUID().toString();
        int divisao = string.length() / 3;
        long timestamp = Instant.now().toEpochMilli();
        String substring = string.substring(0, divisao);
        return substring + timestamp;
    }

}
