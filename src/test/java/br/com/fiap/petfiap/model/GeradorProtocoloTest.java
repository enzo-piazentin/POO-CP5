package br.com.fiap.petfiap.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

// Testes unitarios do Singleton (Aula 14 + Aula 15): sem banco, sem Spring.
public class GeradorProtocoloTest {

    @Test
    public void deveManterUmaUnicaInstancia() {
        // Arrange + Act
        GeradorProtocolo primeira = GeradorProtocolo.getInstancia();
        GeradorProtocolo segunda = GeradorProtocolo.getInstancia();

        // Assert: e sempre o MESMO objeto (padrao Singleton)
        assertSame(primeira, segunda);
    }

    @Test
    public void deveGerarProtocolosSequenciais() {
        // Act: a numeracao e global - cada atendimento novo pega o proximo numero,
        // independente de quantas vezes o sistema pede o gerador
        int primeiro = GeradorProtocolo.getInstancia().proximo();
        int segundo = GeradorProtocolo.getInstancia().proximo();
        int terceiro = GeradorProtocolo.getInstancia().proximo();

        // Assert: 1, 2, 3... (numeracao sequencial dos atendimentos)
        assertEquals(1, primeiro);
        assertEquals(2, segundo);
        assertEquals(3, terceiro);
    }

    @Test
    public void deveGerarProtocolosUnicosEmAmbienteConcorrente() throws Exception {
        // Arrange: reseta o contador para o teste
        GeradorProtocolo gerador = GeradorProtocolo.getInstancia();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Callable<Integer>> tasks = new ArrayList<>();

        // Cria 10 tarefas que chamam proximo() simultaneamente
        for (int i = 0; i < 10; i++) {
            tasks.add(gerador::proximo);
        }

        // Act: executa todas as tarefas simultaneamente
        List<java.util.concurrent.Future<Integer>> futures = executor.invokeAll(tasks);
        List<Integer> resultados = new ArrayList<>();
        for (java.util.concurrent.Future<Integer> future : futures) {
            resultados.add(future.get());
        }
        executor.shutdown();

        // Assert: todos os protocolos devem ser unicos (sem duplicatas)
        long unicos = resultados.stream().distinct().count();
        assertEquals(10, unicos, "Protocolos devem ser unicos em ambiente concorrente");
    }
}
